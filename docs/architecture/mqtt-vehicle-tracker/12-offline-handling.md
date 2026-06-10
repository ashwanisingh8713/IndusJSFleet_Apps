# 12. Offline Handling & Retry

## 12.1 Offline Scenarios

| Scenario | Duration | Impact | Solution |
|----------|----------|--------|----------|
| Network glitch | < 30s | Minimal | MQTT keep-alive + auto-reconnect |
| Cell tower switch | 30s - 2min | Low | Offline buffer |
| No coverage zone | 2min - 1hr | Medium | Large offline buffer |
| Extended no coverage | > 1hr | High | Flash storage + prioritization |
| Device power loss | Variable | Critical | Flash persistence on shutdown |

---

## 12.2 Device-Side Offline Buffer

```go
// buffer/ring_buffer.go
package buffer

const MaxBufferSize = 1000 // Store up to 1000 messages

type RingBuffer struct {
    data  [][]byte
    head  int
    tail  int
    count int
}

func NewRingBuffer() *RingBuffer {
    return &RingBuffer{
        data: make([][]byte, MaxBufferSize),
    }
}

func (rb *RingBuffer) Push(msg []byte) bool {
    if rb.count >= MaxBufferSize {
        // Buffer full, drop oldest
        rb.head = (rb.head + 1) % MaxBufferSize
        rb.count--
    }
    
    // Copy message
    msgCopy := make([]byte, len(msg))
    copy(msgCopy, msg)
    
    rb.data[rb.tail] = msgCopy
    rb.tail = (rb.tail + 1) % MaxBufferSize
    rb.count++
    
    return true
}

func (rb *RingBuffer) Pop() ([]byte, bool) {
    if rb.count == 0 {
        return nil, false
    }
    
    msg := rb.data[rb.head]
    rb.data[rb.head] = nil // Allow GC
    rb.head = (rb.head + 1) % MaxBufferSize
    rb.count--
    
    return msg, true
}

func (rb *RingBuffer) Count() int {
    return rb.count
}

func (rb *RingBuffer) IsFull() bool {
    return rb.count >= MaxBufferSize
}
```

---

## 12.3 MQTT Reconnection Strategy

```go
// mqtt/reconnect.go
package mqtt

import (
    "time"
    "math"
)

type ReconnectStrategy struct {
    BaseDelay    time.Duration
    MaxDelay     time.Duration
    MaxRetries   int  // 0 = infinite
    attempts     int
    offlineBuffer *buffer.RingBuffer
}

func NewReconnectStrategy() *ReconnectStrategy {
    return &ReconnectStrategy{
        BaseDelay:    1 * time.Second,
        MaxDelay:     5 * time.Minute,
        MaxRetries:   0, // Infinite retries
        offlineBuffer: buffer.NewRingBuffer(),
    }
}

// NextDelay calculates exponential backoff delay
func (rs *ReconnectStrategy) NextDelay() time.Duration {
    delay := time.Duration(math.Pow(2, float64(rs.attempts))) * rs.BaseDelay
    if delay > rs.MaxDelay {
        delay = rs.MaxDelay
    }
    rs.attempts++
    return delay
}

// Reset resets the retry counter (call on successful connection)
func (rs *ReconnectStrategy) Reset() {
    rs.attempts = 0
}

// OnDisconnect handles disconnection
func (rs *ReconnectStrategy) OnDisconnect(client *Client) {
    go func() {
        for {
            delay := rs.NextDelay()
            time.Sleep(delay)
            
            if err := client.Connect(); err == nil {
                rs.Reset()
                rs.flushBuffer(client)
                return
            }
            
            // Check max retries
            if rs.MaxRetries > 0 && rs.attempts >= rs.MaxRetries {
                // Max retries reached, persist to flash
                persistToFlash(rs.offlineBuffer)
                return
            }
        }
    }()
}

// flushBuffer sends all buffered messages
func (rs *ReconnectStrategy) flushBuffer(client *Client) {
    for {
        msg, ok := rs.offlineBuffer.Pop()
        if !ok {
            break
        }
        
        // Re-publish buffered message
        client.PublishRaw(msg)
        
        // Small delay to avoid flooding
        time.Sleep(10 * time.Millisecond)
    }
}
```

---

## 12.4 Reconnection Backoff Visualization

```
┌─────────────────────────────────────────────────────────────────┐
│                    EXPONENTIAL BACKOFF                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Attempt 1:  1 second   ━━━                                     │
│  Attempt 2:  2 seconds  ━━━━━━                                  │
│  Attempt 3:  4 seconds  ━━━━━━━━━━━━                            │
│  Attempt 4:  8 seconds  ━━━━━━━━━━━━━━━━━━━━━━━━                │
│  Attempt 5:  16 seconds ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│
│  Attempt 6:  32 seconds ━━━━━━━━━━━━━━━━━━━━━━━━━━...            │
│  ...                                                             │
│  Attempt N:  5 minutes (MAX)                                    │
│                                                                  │
│  Total time before max delay: ~2 minutes                        │
│  After max: retry every 5 minutes indefinitely                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 12.5 Server-Side Offline Detection

```kotlin
// OfflineDetectionService.kt
@Service
class OfflineDetectionService(
    private val deviceRepository: DeviceRepository,
    private val notificationService: NotificationService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    private val offlineThreshold = Duration.ofMinutes(5)
    
    @Scheduled(fixedRate = 60_000) // Run every minute
    fun checkOfflineDevices() {
        val threshold = Instant.now().minus(offlineThreshold)
        
        val offlineDevices = deviceRepository.findByStatusAndLastSeenBefore(
            DeviceStatus.ACTIVE,
            threshold
        )
        
        offlineDevices.forEach { device ->
            val key = "device:offline:notified:${device.id}"
            
            // Only notify once per offline period
            if (redisTemplate.opsForValue().get(key) == null) {
                notificationService.sendDeviceOfflineAlert(device)
                redisTemplate.opsForValue().set(key, "1", Duration.ofHours(1))
            }
        }
    }
    
    fun markDeviceOnline(deviceId: Int) {
        val key = "device:offline:notified:$deviceId"
        redisTemplate.delete(key)
        
        deviceRepository.updateLastSeen(deviceId, Instant.now())
    }
}
```

---

## 12.6 Flash Persistence for Critical Data

```go
// storage/flash.go
package storage

import (
    "encoding/binary"
    "machine"
)

const (
    FlashBaseAddr = 0x100000  // Flash storage start address
    MaxStoredMsgs = 100       // Max messages in flash
    MsgSize       = 24        // Bytes per message
)

type FlashStorage struct {
    writePtr uint32
    readPtr  uint32
    count    uint8
}

// Save critical telemetry to flash before power loss
func (fs *FlashStorage) SaveCritical(msgs [][]byte) error {
    for _, msg := range msgs {
        if fs.count >= MaxStoredMsgs {
            break // Flash full
        }
        
        addr := FlashBaseAddr + uint32(fs.count)*MsgSize
        
        // Write to flash (platform-specific)
        machine.Flash.Write(addr, msg[:MsgSize])
        fs.count++
    }
    
    // Save metadata
    fs.saveMeta()
    return nil
}

// LoadStored reads messages from flash after boot
func (fs *FlashStorage) LoadStored() [][]byte {
    fs.loadMeta()
    
    msgs := make([][]byte, 0, fs.count)
    
    for i := uint8(0); i < fs.count; i++ {
        addr := FlashBaseAddr + uint32(i)*MsgSize
        buf := make([]byte, MsgSize)
        machine.Flash.Read(addr, buf)
        msgs = append(msgs, buf)
    }
    
    // Clear flash after reading
    fs.Clear()
    
    return msgs
}

// Clear erases flash storage
func (fs *FlashStorage) Clear() {
    machine.Flash.Erase(FlashBaseAddr, MaxStoredMsgs*MsgSize)
    fs.count = 0
    fs.saveMeta()
}
```

---

## 12.7 Message Priority During Offline

| Priority | Message Type | Buffer Strategy |
|----------|--------------|-----------------|
| **Critical** | SOS, Tamper, Power cut | Always store, persist to flash |
| **High** | Ignition events, Geofence | Store, drop oldest if full |
| **Medium** | Telemetry | Store, drop oldest if full |
| **Low** | Status updates | Skip during offline |

```go
// Priority-based buffering
func (c *Client) PublishWithPriority(topic string, msg []byte, priority int) {
    if !c.IsConnected() {
        switch priority {
        case PriorityCritical:
            c.criticalBuffer.Push(msg)
            c.persistCriticalToFlash()
        case PriorityHigh:
            c.highBuffer.Push(msg)
        case PriorityMedium:
            c.normalBuffer.Push(msg)
        case PriorityLow:
            // Skip low priority during offline
        }
        return
    }
    
    c.publish(topic, msg)
}
```

---

## 12.8 Zero Data Loss Checklist

- [x] QoS 2 for all critical messages
- [x] CleanSession = false (persistent session)
- [x] Device-side ring buffer (1000+ messages)
- [x] Flash storage for power loss scenarios
- [x] Exponential backoff reconnection
- [x] Server-side message queue (10,000/client)
- [x] Broker disk persistence enabled
- [x] Backend ACK only after database write
- [x] Monitoring alerts for dropped messages

---

[← Previous: Scalability & HA](./11-scalability-ha.md) | [Next: Implementation Roadmap →](./13-implementation-roadmap.md)


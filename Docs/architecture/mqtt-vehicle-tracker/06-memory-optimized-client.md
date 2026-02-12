# 6. Memory-Optimized Client

For devices with limited RAM (256KB or less), use this memory-optimized approach.

---

## 6.1 Memory Savings Overview

| Component | Standard | Optimized | Savings |
|-----------|----------|-----------|---------|
| Telemetry struct | ~80 bytes | 20 bytes | **75%** |
| JSON encoding | ~150 bytes | 0 bytes | **100%** |
| Message buffer (50 msgs) | ~12KB | ~1.2KB | **90%** |
| String allocations | Variable | 0 bytes | **100%** |
| **Total per message** | ~250 bytes | ~24 bytes | **90%** |

---

## 6.2 Compact Telemetry Format (Binary)

```go
// telemetry/compact.go
// Memory-optimized telemetry - uses 20 bytes vs 80+ bytes JSON

package telemetry

import "time"

// TelemetryCompact uses fixed-point integers to save memory
// Total size: 20 bytes per message
type TelemetryCompact struct {
    Timestamp  uint32  // Unix timestamp (4 bytes)
    LatInt     int32   // Latitude * 1000000 (4 bytes)
    LngInt     int32   // Longitude * 1000000 (4 bytes)
    SpeedX10   uint16  // Speed * 10 in km/h (2 bytes)
    HeadingX10 uint16  // Heading * 10 in degrees (2 bytes)
    BatteryMv  uint16  // Battery in millivolts (2 bytes)
    Flags      uint8   // Bit flags: ignition, GPS fix, etc. (1 byte)
    Satellites uint8   // Satellite count (1 byte)
}

// Flag bit positions
const (
    FlagIgnition = 1 << 0  // Bit 0: Ignition on/off
    FlagGPSFix   = 1 << 1  // Bit 1: GPS has fix
    FlagMoving   = 1 << 2  // Bit 2: Vehicle moving
    FlagCharging = 1 << 3  // Bit 3: Battery charging
    FlagTamper   = 1 << 4  // Bit 4: Tamper detected
    FlagSOS      = 1 << 5  // Bit 5: SOS button pressed
)

// Pack serializes to 20-byte binary format (no heap allocation)
func (t *TelemetryCompact) Pack(buf []byte) int {
    // Little-endian binary packing
    buf[0] = byte(t.Timestamp)
    buf[1] = byte(t.Timestamp >> 8)
    buf[2] = byte(t.Timestamp >> 16)
    buf[3] = byte(t.Timestamp >> 24)
    buf[4] = byte(t.LatInt)
    buf[5] = byte(t.LatInt >> 8)
    buf[6] = byte(t.LatInt >> 16)
    buf[7] = byte(t.LatInt >> 24)
    buf[8] = byte(t.LngInt)
    buf[9] = byte(t.LngInt >> 8)
    buf[10] = byte(t.LngInt >> 16)
    buf[11] = byte(t.LngInt >> 24)
    buf[12] = byte(t.SpeedX10)
    buf[13] = byte(t.SpeedX10 >> 8)
    buf[14] = byte(t.HeadingX10)
    buf[15] = byte(t.HeadingX10 >> 8)
    buf[16] = byte(t.BatteryMv)
    buf[17] = byte(t.BatteryMv >> 8)
    buf[18] = t.Flags
    buf[19] = t.Satellites
    return 20
}

// Unpack deserializes from binary format
func (t *TelemetryCompact) Unpack(buf []byte) {
    t.Timestamp = uint32(buf[0]) | uint32(buf[1])<<8 | uint32(buf[2])<<16 | uint32(buf[3])<<24
    t.LatInt = int32(buf[4]) | int32(buf[5])<<8 | int32(buf[6])<<16 | int32(buf[7])<<24
    t.LngInt = int32(buf[8]) | int32(buf[9])<<8 | int32(buf[10])<<16 | int32(buf[11])<<24
    t.SpeedX10 = uint16(buf[12]) | uint16(buf[13])<<8
    t.HeadingX10 = uint16(buf[14]) | uint16(buf[15])<<8
    t.BatteryMv = uint16(buf[16]) | uint16(buf[17])<<8
    t.Flags = buf[18]
    t.Satellites = buf[19]
}

// NewCompact creates compact telemetry from GPS data
func NewCompact(lat, lng float64, speed, heading float32, 
                batteryMv uint16, ignition bool, sats uint8) TelemetryCompact {
    var flags uint8 = 0
    if ignition {
        flags |= FlagIgnition
    }
    if sats >= 4 {
        flags |= FlagGPSFix
    }
    if speed > 5.0 {
        flags |= FlagMoving
    }
    
    return TelemetryCompact{
        Timestamp:  uint32(time.Now().Unix()),
        LatInt:     int32(lat * 1000000),
        LngInt:     int32(lng * 1000000),
        SpeedX10:   uint16(speed * 10),
        HeadingX10: uint16(heading * 10),
        BatteryMv:  batteryMv,
        Flags:      flags,
        Satellites: sats,
    }
}

// Latitude returns the latitude as float64
func (t *TelemetryCompact) Latitude() float64 {
    return float64(t.LatInt) / 1000000.0
}

// Longitude returns the longitude as float64
func (t *TelemetryCompact) Longitude() float64 {
    return float64(t.LngInt) / 1000000.0
}

// Speed returns the speed in km/h
func (t *TelemetryCompact) Speed() float32 {
    return float32(t.SpeedX10) / 10.0
}

// Ignition returns ignition state
func (t *TelemetryCompact) Ignition() bool {
    return t.Flags&FlagIgnition != 0
}
```

---

## 6.3 Fixed-Size Ring Buffer (No Dynamic Allocation)

```go
// buffer/ring.go
// Zero-allocation ring buffer for offline message storage

package buffer

const (
    MessageSize = 24    // 20 bytes data + 4 bytes header
    BufferCount = 50    // Store 50 messages when offline
)

// RingBuffer uses fixed arrays - no malloc/free
type RingBuffer struct {
    data  [BufferCount][MessageSize]byte
    head  uint8
    tail  uint8
    count uint8
}

// Push adds message to buffer, returns false if dropped
func (rb *RingBuffer) Push(msg []byte) bool {
    if len(msg) > MessageSize {
        return false
    }
    
    if rb.count >= BufferCount {
        // Buffer full - drop oldest
        rb.head = (rb.head + 1) % BufferCount
        rb.count--
    }
    
    // Copy message to fixed buffer
    copy(rb.data[rb.tail][:], msg)
    rb.tail = (rb.tail + 1) % BufferCount
    rb.count++
    return true
}

// Pop retrieves oldest message
func (rb *RingBuffer) Pop(buf []byte) bool {
    if rb.count == 0 {
        return false
    }
    
    copy(buf, rb.data[rb.head][:])
    rb.head = (rb.head + 1) % BufferCount
    rb.count--
    return true
}

// Count returns number of buffered messages
func (rb *RingBuffer) Count() uint8 {
    return rb.count
}

// IsFull returns true if buffer is at capacity
func (rb *RingBuffer) IsFull() bool {
    return rb.count >= BufferCount
}

// IsEmpty returns true if buffer is empty
func (rb *RingBuffer) IsEmpty() bool {
    return rb.count == 0
}

// Clear empties the buffer
func (rb *RingBuffer) Clear() {
    rb.head = 0
    rb.tail = 0
    rb.count = 0
}
```

---

## 6.4 Memory-Optimized MQTT Client Usage

```go
// main.go - Memory optimized main loop
package main

import (
    "tracker/buffer"
    "tracker/telemetry"
)

var (
    offlineBuffer buffer.RingBuffer
    sendBuf       [24]byte  // Pre-allocated send buffer
)

func sendTelemetryOptimized() {
    loc := gpsDriver.GetLocation()
    if loc == nil {
        return
    }
    
    // Create compact telemetry (no heap allocation)
    compact := telemetry.NewCompact(
        loc.Latitude,
        loc.Longitude,
        loc.Speed,
        loc.Heading,
        readBatteryMillivolts(),
        readIgnitionState(),
        uint8(loc.Satellites),
    )
    
    // Pack to pre-allocated buffer
    n := compact.Pack(sendBuf[:])
    
    // Try to send
    if mqttClient.IsConnected() {
        // Flush any buffered messages first
        flushOfflineBuffer()
        
        // Send current message
        mqttClient.PublishRaw(cfg.TelemetryTopic, sendBuf[:n])
    } else {
        // Store in offline buffer
        offlineBuffer.Push(sendBuf[:n])
    }
}

func flushOfflineBuffer() {
    var msg [24]byte
    
    for offlineBuffer.Pop(msg[:]) {
        mqttClient.PublishRaw(cfg.TelemetryTopic, msg[:20])
    }
}
```

---

## 6.5 Backend Decoder (Kotlin)

```kotlin
// TelemetryDecoder.kt
// Decodes compact binary telemetry from devices

class TelemetryDecoder {
    
    fun decode(bytes: ByteArray): TelemetryData {
        require(bytes.size >= 20) { "Invalid telemetry size" }
        
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        
        val timestamp = buffer.int.toLong() and 0xFFFFFFFFL
        val latInt = buffer.int
        val lngInt = buffer.int
        val speedX10 = buffer.short.toInt() and 0xFFFF
        val headingX10 = buffer.short.toInt() and 0xFFFF
        val batteryMv = buffer.short.toInt() and 0xFFFF
        val flags = buffer.get().toInt() and 0xFF
        val satellites = buffer.get().toInt() and 0xFF
        
        return TelemetryData(
            timestamp = Instant.ofEpochSecond(timestamp),
            latitude = latInt / 1_000_000.0,
            longitude = lngInt / 1_000_000.0,
            speed = speedX10 / 10.0,
            heading = headingX10 / 10.0,
            batteryVoltage = batteryMv / 1000.0,
            ignition = (flags and 0x01) != 0,
            gpsFixed = (flags and 0x02) != 0,
            moving = (flags and 0x04) != 0,
            satellites = satellites
        )
    }
}

data class TelemetryData(
    val timestamp: Instant,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val heading: Double,
    val batteryVoltage: Double,
    val ignition: Boolean,
    val gpsFixed: Boolean,
    val moving: Boolean,
    val satellites: Int
)
```

---

## 6.6 Memory Budget Summary

| Component | RAM Usage |
|-----------|-----------|
| TinyGo Runtime | ~30KB |
| MQTT Client | ~15KB |
| GPS Driver | ~5KB |
| GSM Driver | ~10KB |
| Ring Buffer (50 msgs × 24 bytes) | ~1.2KB |
| Send/Receive Buffers | ~0.5KB |
| Application Logic | ~20KB |
| Stack | ~30KB |
| **TOTAL** | **~112KB** |
| **Available (256KB device)** | **~144KB free** ✅ |

---

[← Previous: TinyGo Firmware](./05-tinygo-firmware.md) | [Next: Security Architecture →](./07-security-architecture.md)


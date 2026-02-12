# 5. TinyGo Device Firmware

## 5.1 Why TinyGo?

| Advantage | Description |
|-----------|-------------|
| **Small Binary Size** | ~50KB vs ~2MB for standard Go |
| **Low Memory** | Runs on devices with 256KB RAM |
| **Go Ecosystem** | Use familiar Go syntax and patterns |
| **Cross-Platform** | Supports ESP32, STM32, nRF52, etc. |
| **Concurrency** | Goroutines for concurrent operations |
| **Type Safety** | Compile-time error detection |

---

## 5.2 Hardware Requirements

| Component | Specification |
|-----------|---------------|
| MCU | ESP32-S3 or STM32L4 |
| RAM | Minimum 256KB |
| Flash | Minimum 2MB |
| GPS | u-blox NEO-M8N or similar |
| GSM/LTE | SIM7600 or Quectel EC25 |
| Power | 12V-24V automotive input |

---

## 5.3 Firmware Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    TINYGO FIRMWARE ARCHITECTURE                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │                      MAIN LOOP                               ││
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         ││
│  │  │  GPS    │  │ Sensor  │  │  MQTT   │  │ Command │         ││
│  │  │ Task    │  │ Task    │  │ Task    │  │ Handler │         ││
│  │  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘         ││
│  │       │            │            │            │               ││
│  └───────┼────────────┼────────────┼────────────┼───────────────┘│
│          │            │            │            │                │
│  ┌───────▼────────────▼────────────▼────────────▼───────────────┐│
│  │                    MESSAGE QUEUE (Ring Buffer)               ││
│  └──────────────────────────────────────────────────────────────┘│
│                              │                                   │
│  ┌───────────────────────────▼──────────────────────────────────┐│
│  │                    DRIVERS LAYER                             ││
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         ││
│  │  │  GPS    │  │  GSM    │  │ Storage │  │  ADC    │         ││
│  │  │ Driver  │  │ Driver  │  │ Driver  │  │ Driver  │         ││
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘         ││
│  └──────────────────────────────────────────────────────────────┘│
│                              │                                   │
│  ┌───────────────────────────▼──────────────────────────────────┐│
│  │                    HARDWARE ABSTRACTION                      ││
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         ││
│  │  │  UART   │  │  SPI    │  │  I2C    │  │  GPIO   │         ││
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘         ││
│  └──────────────────────────────────────────────────────────────┘│
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5.4 Project Structure

```
tracker-firmware/
├── main.go
├── go.mod
├── config/
│   └── config.go
├── drivers/
│   ├── gps/
│   │   └── neo_m8n.go
│   ├── gsm/
│   │   └── sim7600.go
│   └── storage/
│       └── flash.go
├── mqtt/
│   ├── client.go
│   ├── messages.go
│   └── topics.go
├── telemetry/
│   ├── collector.go
│   └── types.go
├── commands/
│   └── handler.go
└── utils/
    ├── buffer.go
    └── crc.go
```

---

## 5.5 Core Firmware Code

```go
// main.go
package main

import (
    "time"
    "machine"
    "tracker/config"
    "tracker/drivers/gps"
    "tracker/drivers/gsm"
    "tracker/mqtt"
    "tracker/telemetry"
    "tracker/commands"
)

var (
    cfg          *config.Config
    gpsDriver    *gps.Driver
    gsmDriver    *gsm.Driver
    mqttClient   *mqtt.Client
    cmdHandler   *commands.Handler
)

func main() {
    // Initialize configuration
    cfg = config.Load()
    
    // Initialize hardware drivers
    initDrivers()
    
    // Initialize MQTT client
    initMQTT()
    
    // Start main loop
    mainLoop()
}

func initDrivers() {
    // GPS initialization
    gpsDriver = gps.New(machine.UART1, 9600)
    gpsDriver.Start()
    
    // GSM/LTE initialization
    gsmDriver = gsm.New(machine.UART2, 115200)
    gsmDriver.Init(cfg.APN, cfg.APNUser, cfg.APNPass)
    
    // Wait for network registration
    for !gsmDriver.IsRegistered() {
        time.Sleep(time.Second)
    }
}

func initMQTT() {
    mqttClient = mqtt.NewClient(mqtt.Config{
        Broker:       cfg.MQTTBroker,
        Port:         cfg.MQTTPort,
        ClientID:     cfg.DeviceID,
        Username:     cfg.MQTTUsername,
        Password:     cfg.MQTTPassword,
        UseTLS:       true,
        CleanSession: false,  // CRITICAL: Persistent session
    })
    
    // Connect with retry
    for {
        if err := mqttClient.Connect(); err == nil {
            break
        }
        time.Sleep(5 * time.Second)
    }
    
    // Subscribe to command topics
    mqttClient.Subscribe(cfg.CommandTopic, mqtt.QoS2, handleCommand)
    mqttClient.Subscribe(cfg.ConfigTopic, mqtt.QoS2, handleConfig)
    mqttClient.Subscribe(cfg.OTATopic, mqtt.QoS2, handleOTA)
    
    // Initialize command handler
    cmdHandler = commands.NewHandler(mqttClient, cfg)
}

func mainLoop() {
    telemetryTicker := time.NewTicker(time.Duration(cfg.ReportingInterval) * time.Second)
    statusTicker := time.NewTicker(5 * time.Minute)
    
    for {
        select {
        case <-telemetryTicker.C:
            sendTelemetry()
            
        case <-statusTicker.C:
            sendStatus()
        }
    }
}

func sendTelemetry() {
    loc := gpsDriver.GetLocation()
    if loc == nil {
        return // No valid GPS fix
    }
    
    msg := telemetry.Message{
        Timestamp:  time.Now().Unix(),
        Latitude:   loc.Latitude,
        Longitude:  loc.Longitude,
        Altitude:   loc.Altitude,
        Speed:      loc.Speed,
        Heading:    loc.Heading,
        Accuracy:   loc.HDOP,
        Satellites: loc.Satellites,
        Ignition:   readIgnitionState(),
        Battery:    readBatteryVoltage(),
    }
    
    data, _ := msg.Marshal()
    mqttClient.Publish(cfg.TelemetryTopic, mqtt.QoS1, false, data)
}

func sendStatus() {
    status := telemetry.DeviceStatus{
        Timestamp:       time.Now().Unix(),
        FirmwareVersion: cfg.FirmwareVersion,
        Uptime:          getUptime(),
        FreeMemory:      getFreeMemory(),
        GSMSignal:       gsmDriver.GetSignalStrength(),
        GPSStatus:       gpsDriver.GetStatus(),
        BatteryVoltage:  readBatteryVoltage(),
        Temperature:     readTemperature(),
    }
    
    data, _ := status.Marshal()
    mqttClient.Publish(cfg.StatusTopic, mqtt.QoS1, true, data)
}

func handleCommand(topic string, payload []byte) {
    cmd, err := commands.Parse(payload)
    if err != nil {
        return
    }
    
    result := cmdHandler.Execute(cmd)
    
    // Send acknowledgment
    ack := commands.Ack{
        CommandID: cmd.ID,
        Status:    result.Status,
        Message:   result.Message,
        Timestamp: time.Now().Unix(),
    }
    
    data, _ := ack.Marshal()
    mqttClient.Publish(cfg.AckTopic, mqtt.QoS1, false, data)
}

func handleConfig(topic string, payload []byte) {
    newCfg, err := config.ParseUpdate(payload)
    if err != nil {
        return
    }
    
    cfg.Apply(newCfg)
    cfg.Save()
}

func handleOTA(topic string, payload []byte) {
    ota, err := commands.ParseOTA(payload)
    if err != nil {
        return
    }
    
    cmdHandler.ExecuteOTA(ota)
}

// Hardware reading functions
func readIgnitionState() bool {
    return machine.D5.Get()
}

func readBatteryVoltage() float32 {
    adc := machine.ADC{Pin: machine.ADC0}
    adc.Configure(machine.ADCConfig{})
    raw := adc.Get()
    return float32(raw) * 3.3 / 65535 * 11
}
```

---

## 5.6 Telemetry Message Types

```go
// telemetry/types.go
package telemetry

import "encoding/json"

// Message represents GPS telemetry data
type Message struct {
    Timestamp  int64   `json:"ts"`
    Latitude   float64 `json:"lat"`
    Longitude  float64 `json:"lng"`
    Altitude   float64 `json:"alt,omitempty"`
    Speed      float64 `json:"spd"`
    Heading    float64 `json:"hdg"`
    Accuracy   float64 `json:"acc,omitempty"`
    Satellites int     `json:"sat,omitempty"`
    Ignition   bool    `json:"ign"`
    Battery    float32 `json:"bat"`
}

// Marshal serializes message to compact JSON
func (m *Message) Marshal() ([]byte, error) {
    return json.Marshal(m)
}

// DeviceStatus represents device health status
type DeviceStatus struct {
    Timestamp       int64   `json:"ts"`
    FirmwareVersion string  `json:"fw"`
    Uptime          int64   `json:"up"`
    FreeMemory      uint32  `json:"mem"`
    GSMSignal       int     `json:"gsm"`
    GPSStatus       string  `json:"gps"`
    BatteryVoltage  float32 `json:"bat"`
    Temperature     float32 `json:"tmp,omitempty"`
}
```

---

## 5.7 Build & Flash

```bash
# Install TinyGo
brew install tinygo  # macOS
# or
sudo apt install tinygo  # Linux

# Build for ESP32
tinygo build -target=esp32 -o firmware.bin ./main.go

# Flash to device
esptool.py --port /dev/ttyUSB0 write_flash 0x0 firmware.bin

# Build for STM32
tinygo build -target=stm32l4-disco -o firmware.elf ./main.go

# Flash STM32
openocd -f board/stm32l4discovery.cfg -c "program firmware.elf verify reset exit"
```

---

[← Previous: MQTT Topic Design](./04-mqtt-topic-design.md) | [Next: Memory-Optimized Client →](./06-memory-optimized-client.md)


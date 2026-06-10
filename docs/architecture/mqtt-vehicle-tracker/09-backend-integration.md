# 9. Backend Integration

## 9.1 Tracking Service Architecture

```kotlin
// TrackingService.kt
@Service
class TrackingService(
    private val mqttClient: MqttClient,
    private val telemetryRepository: TelemetryRepository,
    private val vehicleRepository: VehicleRepository,
    private val deviceRepository: DeviceRepository,
    private val kafkaProducer: KafkaProducer<String, TelemetryEvent>,
    private val redisTemplate: RedisTemplate<String, String>,
    private val alertService: AlertService
) {
    
    private val logger = LoggerFactory.getLogger(TrackingService::class.java)
    
    @PostConstruct
    fun init() {
        // Subscribe to all device telemetry
        mqttClient.subscribe("indusjs/fleet/v1/devices/+/telemetry") { topic, message ->
            handleTelemetry(topic, message)
        }
        
        mqttClient.subscribe("indusjs/fleet/v1/devices/+/status") { topic, message ->
            handleDeviceStatus(topic, message)
        }
        
        mqttClient.subscribe("indusjs/fleet/v1/devices/+/events") { topic, message ->
            handleDeviceEvent(topic, message)
        }
    }
    
    private fun handleTelemetry(topic: String, message: MqttMessage) {
        val deviceId = extractDeviceId(topic)
        val telemetry = parseTelemetry(message.payload)
        
        // Get vehicle for this device
        val device = deviceRepository.findByMqttClientId(deviceId)
            ?: run {
                logger.warn("Unknown device: $deviceId")
                return
            }
        
        val vehicleId = device.vehicleId
            ?: run {
                logger.warn("Device not assigned to vehicle: $deviceId")
                return
            }
        
        // Store in TimescaleDB
        telemetryRepository.save(
            TelemetryEntity(
                time = Instant.ofEpochSecond(telemetry.timestamp),
                vehicleId = vehicleId,
                deviceId = device.id,
                latitude = telemetry.latitude,
                longitude = telemetry.longitude,
                altitude = telemetry.altitude,
                speed = telemetry.speed,
                heading = telemetry.heading,
                accuracy = telemetry.accuracy,
                satellites = telemetry.satellites,
                ignition = telemetry.ignition,
                batteryVoltage = telemetry.battery
            )
        )
        
        // Update Redis cache (latest location)
        val locationJson = objectMapper.writeValueAsString(
            VehicleLocation(
                vehicleId = vehicleId,
                latitude = telemetry.latitude,
                longitude = telemetry.longitude,
                speed = telemetry.speed,
                heading = telemetry.heading,
                ignition = telemetry.ignition,
                timestamp = telemetry.timestamp
            )
        )
        redisTemplate.opsForValue().set(
            "vehicle:location:$vehicleId",
            locationJson,
            Duration.ofMinutes(5)
        )
        
        // Publish to Kafka for analytics
        kafkaProducer.send(
            "telemetry-events",
            vehicleId.toString(),
            TelemetryEvent(vehicleId, device.id, telemetry)
        )
        
        // Check geofences and alerts
        alertService.checkGeofences(vehicleId, telemetry.latitude, telemetry.longitude)
        alertService.checkSpeedLimit(vehicleId, telemetry.speed)
        
        // Update device last_seen
        deviceRepository.updateLastSeen(device.id, Instant.now())
    }
    
    fun sendCommand(deviceId: String, command: DeviceCommand): CommandResult {
        val commandId = UUID.randomUUID().toString()
        val commandMessage = CommandMessage(
            id = commandId,
            type = command.type,
            params = command.params,
            timestamp = Instant.now().epochSecond,
            ttl = command.ttl ?: 300
        )
        
        // Store pending command
        redisTemplate.opsForValue().set(
            "command:pending:$commandId",
            objectMapper.writeValueAsString(commandMessage),
            Duration.ofSeconds(commandMessage.ttl.toLong())
        )
        
        // Publish command
        val topic = "indusjs/fleet/v1/devices/$deviceId/commands"
        mqttClient.publish(topic, commandMessage.toJson(), MqttQos.EXACTLY_ONCE)
        
        // Wait for acknowledgment (with timeout)
        return waitForAck(commandId, command.ttl ?: 300)
    }
}
```

---

## 9.2 Real-Time Location API

```kotlin
// LocationController.kt
@RestController
@RequestMapping("/api/v2/vehicles")
class LocationController(
    private val redisTemplate: RedisTemplate<String, String>,
    private val telemetryRepository: TelemetryRepository
) {
    
    @GetMapping("/{vehicleId}/location")
    fun getCurrentLocation(@PathVariable vehicleId: Int): ResponseEntity<VehicleLocation> {
        // Try Redis cache first
        val cached = redisTemplate.opsForValue().get("vehicle:location:$vehicleId")
        if (cached != null) {
            return ResponseEntity.ok(objectMapper.readValue(cached, VehicleLocation::class.java))
        }
        
        // Fallback to database
        val latest = telemetryRepository.findLatestByVehicleId(vehicleId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(
            VehicleLocation(
                vehicleId = vehicleId,
                latitude = latest.latitude,
                longitude = latest.longitude,
                speed = latest.speed,
                heading = latest.heading,
                ignition = latest.ignition,
                timestamp = latest.time.epochSecond
            )
        )
    }
    
    @GetMapping("/{vehicleId}/location/history")
    fun getLocationHistory(
        @PathVariable vehicleId: Int,
        @RequestParam startDate: String,
        @RequestParam endDate: String
    ): ResponseEntity<List<TelemetryPoint>> {
        val start = LocalDate.parse(startDate).atStartOfDay(ZoneOffset.UTC).toInstant()
        val end = LocalDate.parse(endDate).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        
        val history = telemetryRepository.findByVehicleIdAndTimeBetween(vehicleId, start, end)
        
        return ResponseEntity.ok(history.map { 
            TelemetryPoint(
                timestamp = it.time.epochSecond,
                latitude = it.latitude,
                longitude = it.longitude,
                speed = it.speed,
                ignition = it.ignition
            )
        })
    }
}
```

---

## 9.3 Command API

```kotlin
// CommandController.kt
@RestController
@RequestMapping("/api/v2/devices")
class CommandController(
    private val trackingService: TrackingService,
    private val deviceRepository: DeviceRepository
) {
    
    @PostMapping("/{deviceId}/commands")
    fun sendCommand(
        @PathVariable deviceId: String,
        @RequestBody request: CommandRequest
    ): ResponseEntity<CommandResponse> {
        // Validate device exists
        val device = deviceRepository.findByDeviceSerial(deviceId)
            ?: return ResponseEntity.notFound().build()
        
        // Send command
        val result = trackingService.sendCommand(
            device.mqttClientId!!,
            DeviceCommand(
                type = request.type,
                params = request.params,
                ttl = request.ttl ?: 300
            )
        )
        
        return ResponseEntity.ok(
            CommandResponse(
                commandId = result.commandId,
                status = result.status,
                message = result.message
            )
        )
    }
    
    @GetMapping("/{deviceId}/commands/{commandId}")
    fun getCommandStatus(
        @PathVariable deviceId: String,
        @PathVariable commandId: String
    ): ResponseEntity<CommandStatus> {
        // Get command status from Redis
        val status = redisTemplate.opsForValue().get("command:result:$commandId")
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(objectMapper.readValue(status, CommandStatus::class.java))
    }
}
```

---

## 9.4 Binary Telemetry Decoder

```kotlin
// TelemetryDecoder.kt
@Component
class TelemetryDecoder {
    
    fun decode(bytes: ByteArray): TelemetryData {
        require(bytes.size >= 20) { "Invalid telemetry size: ${bytes.size}" }
        
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
```

---

## 9.5 Data Models

```kotlin
// Models.kt

data class VehicleLocation(
    val vehicleId: Int,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val heading: Double,
    val ignition: Boolean,
    val timestamp: Long
)

data class TelemetryPoint(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val ignition: Boolean
)

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

data class CommandRequest(
    val type: String,
    val params: Map<String, Any> = emptyMap(),
    val ttl: Int? = 300
)

data class CommandResponse(
    val commandId: String,
    val status: String,
    val message: String?
)
```

---

[← Previous: Message Protocols](./08-message-protocols.md) | [Next: Device Lifecycle →](./10-device-lifecycle.md)


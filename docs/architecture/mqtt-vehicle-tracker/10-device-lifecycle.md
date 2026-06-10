# 10. Device Lifecycle Management

## 10.1 Device States

```
┌─────────────────────────────────────────────────────────────────┐
│                    DEVICE STATE MACHINE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│     ┌──────────────┐                                             │
│     │  MANUFACTURED │ Initial state after factory                │
│     └───────┬──────┘                                             │
│             │ provision()                                        │
│             ▼                                                    │
│     ┌──────────────┐                                             │
│     │  PROVISIONED │ Credentials loaded, ready for deployment    │
│     └───────┬──────┘                                             │
│             │ activate()                                         │
│             ▼                                                    │
│     ┌──────────────┐         deactivate()      ┌──────────────┐ │
│     │    ACTIVE    │◄─────────────────────────►│   INACTIVE   │ │
│     └───────┬──────┘         activate()        └──────────────┘ │
│             │                                                    │
│             │ reportFault() / replace()                          │
│             ▼                                                    │
│     ┌──────────────┐                                             │
│     │    FAULTY    │ Needs replacement                           │
│     └───────┬──────┘                                             │
│             │ dispose()                                          │
│             ▼                                                    │
│     ┌──────────────┐                                             │
│     │   DISPOSED   │ End of life, credentials revoked            │
│     └──────────────┘                                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 10.2 State Descriptions

| State | Description | Allowed Transitions |
|-------|-------------|---------------------|
| **MANUFACTURED** | Fresh from factory, no credentials | → PROVISIONED |
| **PROVISIONED** | Credentials loaded, not yet deployed | → ACTIVE |
| **ACTIVE** | Assigned to vehicle, operational | → INACTIVE, FAULTY |
| **INACTIVE** | Temporarily disabled | → ACTIVE, FAULTY |
| **FAULTY** | Hardware failure, needs replacement | → DISPOSED |
| **DISPOSED** | End of life, credentials revoked | (terminal state) |

---

## 10.3 Device Assignment API

```kotlin
// DeviceManagementService.kt
@Service
class DeviceManagementService(
    private val deviceRepository: DeviceRepository,
    private val vehicleRepository: VehicleRepository,
    private val deviceHistoryRepository: DeviceHistoryRepository,
    private val mqttCredentialService: MqttCredentialService
) {
    
    @Transactional
    fun assignDeviceToVehicle(
        deviceSerial: String,
        vehicleId: Int,
        assignedBy: Int
    ): DeviceAssignmentResult {
        // 1. Validate device exists and is available
        val device = deviceRepository.findByDeviceSerial(deviceSerial)
            ?: throw NotFoundException("Device not found: $deviceSerial")
        
        if (device.status != DeviceStatus.PROVISIONED && device.status != DeviceStatus.INACTIVE) {
            throw BadRequestException("Device not available. Status: ${device.status}")
        }
        
        // 2. If vehicle has existing device, unassign it first
        vehicleRepository.findById(vehicleId)?.currentDeviceId?.let { existingDeviceId ->
            unassignDeviceFromVehicle(existingDeviceId, "Replaced with new device")
        }
        
        // 3. Generate MQTT credentials
        val credentials = mqttCredentialService.generateCredentials(device.id)
        
        // 4. Assign device to vehicle
        device.apply {
            this.vehicleId = vehicleId
            this.status = DeviceStatus.ACTIVE
            this.mqttClientId = "device-${device.deviceSerial}"
            this.mqttUsername = credentials.username
            this.assignedAt = Instant.now()
        }
        deviceRepository.save(device)
        
        // 5. Update vehicle
        val vehicle = vehicleRepository.findById(vehicleId)!!
        vehicle.currentDeviceId = device.id
        vehicleRepository.save(vehicle)
        
        // 6. Create history entry
        deviceHistoryRepository.save(
            DeviceHistory(
                vehicleId = vehicleId,
                deviceId = device.id,
                assignedAt = Instant.now(),
                assignedBy = assignedBy
            )
        )
        
        return DeviceAssignmentResult(
            deviceId = device.id,
            vehicleId = vehicleId,
            mqttClientId = device.mqttClientId!!,
            status = "assigned"
        )
    }
    
    @Transactional
    fun unassignDeviceFromVehicle(deviceId: Int, reason: String) {
        val device = deviceRepository.findById(deviceId)
            ?: throw NotFoundException("Device not found: $deviceId")
        
        val vehicleId = device.vehicleId ?: return
        
        // Update history
        val history = deviceHistoryRepository.findActiveByDeviceId(deviceId)
        history?.apply {
            this.removedAt = Instant.now()
            this.removalReason = reason
        }
        history?.let { deviceHistoryRepository.save(it) }
        
        // Update vehicle
        val vehicle = vehicleRepository.findById(vehicleId)
        vehicle?.let {
            it.currentDeviceId = null
            vehicleRepository.save(it)
        }
        
        // Update device
        device.apply {
            this.vehicleId = null
            this.status = DeviceStatus.INACTIVE
        }
        deviceRepository.save(device)
        
        // Revoke MQTT credentials
        mqttCredentialService.revokeCredentials(device.id)
    }
    
    @Transactional
    fun replaceDevice(
        vehicleId: Int,
        oldDeviceSerial: String,
        newDeviceSerial: String,
        reason: String
    ): DeviceReplacementResult {
        // 1. Unassign old device
        val oldDevice = deviceRepository.findByDeviceSerial(oldDeviceSerial)
            ?: throw NotFoundException("Old device not found: $oldDeviceSerial")
        
        unassignDeviceFromVehicle(oldDevice.id, reason)
        
        // Mark old device as faulty if hardware issue
        if (reason.contains("hardware", ignoreCase = true) || 
            reason.contains("faulty", ignoreCase = true)) {
            oldDevice.status = DeviceStatus.FAULTY
            deviceRepository.save(oldDevice)
        }
        
        // 2. Assign new device
        val result = assignDeviceToVehicle(newDeviceSerial, vehicleId, getCurrentUserId())
        
        return DeviceReplacementResult(
            vehicleId = vehicleId,
            oldDeviceId = oldDevice.id,
            newDeviceId = result.deviceId,
            status = "replaced"
        )
    }
}
```

---

## 10.4 Device API Endpoints

```kotlin
// DeviceController.kt
@RestController
@RequestMapping("/api/v2/devices")
class DeviceController(
    private val deviceManagementService: DeviceManagementService,
    private val deviceRepository: DeviceRepository
) {
    
    @PostMapping
    fun registerDevice(@RequestBody request: RegisterDeviceRequest): ResponseEntity<Device> {
        val device = Device(
            deviceSerial = request.deviceSerial,
            imei = request.imei,
            hardwareVersion = request.hardwareVersion,
            status = DeviceStatus.MANUFACTURED
        )
        return ResponseEntity.ok(deviceRepository.save(device))
    }
    
    @PostMapping("/{deviceSerial}/provision")
    fun provisionDevice(@PathVariable deviceSerial: String): ResponseEntity<ProvisionResult> {
        // Generate and store credentials
        val result = deviceManagementService.provisionDevice(deviceSerial)
        return ResponseEntity.ok(result)
    }
    
    @PostMapping("/{deviceSerial}/assign")
    fun assignDevice(
        @PathVariable deviceSerial: String,
        @RequestBody request: AssignDeviceRequest
    ): ResponseEntity<DeviceAssignmentResult> {
        val result = deviceManagementService.assignDeviceToVehicle(
            deviceSerial,
            request.vehicleId,
            getCurrentUserId()
        )
        return ResponseEntity.ok(result)
    }
    
    @PostMapping("/{deviceSerial}/unassign")
    fun unassignDevice(
        @PathVariable deviceSerial: String,
        @RequestBody request: UnassignRequest
    ): ResponseEntity<Unit> {
        val device = deviceRepository.findByDeviceSerial(deviceSerial)
            ?: return ResponseEntity.notFound().build()
        deviceManagementService.unassignDeviceFromVehicle(device.id, request.reason)
        return ResponseEntity.ok().build()
    }
    
    @PostMapping("/replace")
    fun replaceDevice(@RequestBody request: ReplaceDeviceRequest): ResponseEntity<DeviceReplacementResult> {
        val result = deviceManagementService.replaceDevice(
            request.vehicleId,
            request.oldDeviceSerial,
            request.newDeviceSerial,
            request.reason
        )
        return ResponseEntity.ok(result)
    }
    
    @GetMapping("/{deviceSerial}")
    fun getDevice(@PathVariable deviceSerial: String): ResponseEntity<Device> {
        val device = deviceRepository.findByDeviceSerial(deviceSerial)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(device)
    }
    
    @GetMapping("/{deviceSerial}/history")
    fun getDeviceHistory(@PathVariable deviceSerial: String): ResponseEntity<List<DeviceHistory>> {
        val device = deviceRepository.findByDeviceSerial(deviceSerial)
            ?: return ResponseEntity.notFound().build()
        val history = deviceHistoryRepository.findByDeviceId(device.id)
        return ResponseEntity.ok(history)
    }
}
```

---

[← Previous: Backend Integration](./09-backend-integration.md) | [Next: Scalability & HA →](./11-scalability-ha.md)


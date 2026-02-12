# Appendix D: Error Codes

## D.1 Error Code Format

```
EXXXX
│││││
│└┴┴┴── 4-digit error number
└────── E = Error prefix
```

---

## D.2 Device Errors (E1xxx)

| Code | Name | Description | Resolution |
|------|------|-------------|------------|
| E1001 | GPS_NO_FIX | GPS fix not available | Wait for satellite signal, check antenna |
| E1002 | GPS_TIMEOUT | GPS timeout waiting for fix | Check GPS module connection |
| E1003 | GPS_INVALID_DATA | Invalid NMEA data received | Check GPS module, restart |
| E1010 | GSM_NOT_REGISTERED | GSM network registration failed | Check SIM card, signal strength |
| E1011 | GSM_NO_SIGNAL | No GSM signal | Move to area with coverage |
| E1012 | GSM_SIM_ERROR | SIM card error | Check SIM insertion, PIN |
| E1013 | GSM_APN_FAILED | APN connection failed | Verify APN settings |
| E1020 | MQTT_CONNECT_FAILED | MQTT connection failed | Check broker address, credentials |
| E1021 | MQTT_DISCONNECT | MQTT disconnected unexpectedly | Auto-reconnect will handle |
| E1022 | MQTT_PUBLISH_FAILED | Failed to publish message | Check connection, retry |
| E1023 | MQTT_SUBSCRIBE_FAILED | Failed to subscribe | Check ACL permissions |
| E1030 | LOW_BATTERY | Battery below threshold | Charge/replace battery |
| E1031 | CRITICAL_BATTERY | Battery critically low | Immediate attention needed |
| E1040 | TAMPER_DETECTED | Device tampering detected | Investigate immediately |
| E1041 | POWER_CUT | Main power disconnected | Check power connection |
| E1050 | STORAGE_FULL | Flash storage full | Clear old data |
| E1051 | STORAGE_ERROR | Flash storage read/write error | Hardware issue |
| E1060 | OTA_DOWNLOAD_FAILED | Firmware download failed | Retry, check network |
| E1061 | OTA_VERIFY_FAILED | Firmware verification failed | Invalid firmware file |
| E1062 | OTA_APPLY_FAILED | Firmware apply failed | Rollback to previous |

---

## D.3 Backend Errors (E2xxx)

| Code | Name | Description | Resolution |
|------|------|-------------|------------|
| E2001 | DEVICE_NOT_FOUND | Device not found in database | Verify device serial |
| E2002 | DEVICE_NOT_ASSIGNED | Device not assigned to vehicle | Assign device first |
| E2003 | DEVICE_ALREADY_ASSIGNED | Device already assigned | Unassign first |
| E2004 | DEVICE_INACTIVE | Device is inactive | Activate device |
| E2005 | DEVICE_FAULTY | Device marked as faulty | Replace device |
| E2010 | VEHICLE_NOT_FOUND | Vehicle not found | Verify vehicle ID |
| E2011 | VEHICLE_NO_DEVICE | Vehicle has no tracker | Assign a device |
| E2020 | COMMAND_INVALID | Invalid command type | Check supported commands |
| E2021 | COMMAND_TIMEOUT | Command timed out | Retry, check device online |
| E2022 | COMMAND_REJECTED | Command rejected by device | Check command parameters |
| E2023 | COMMAND_NOT_SUPPORTED | Command not supported | Check device firmware |
| E2030 | TELEMETRY_INVALID | Invalid telemetry data | Check data format |
| E2031 | TELEMETRY_DUPLICATE | Duplicate telemetry | Already processed |
| E2040 | GEOFENCE_NOT_FOUND | Geofence not found | Verify geofence ID |
| E2041 | GEOFENCE_LIMIT | Max geofences reached | Delete unused geofences |
| E2050 | DATABASE_ERROR | Database operation failed | Check database connection |
| E2051 | DATABASE_TIMEOUT | Database query timeout | Optimize query |
| E2060 | KAFKA_ERROR | Kafka publish failed | Check Kafka connection |
| E2061 | REDIS_ERROR | Redis operation failed | Check Redis connection |

---

## D.4 Security Errors (E3xxx)

| Code | Name | Description | Resolution |
|------|------|-------------|------------|
| E3001 | AUTH_FAILED | Authentication failed | Check credentials |
| E3002 | AUTH_EXPIRED | Credentials expired | Refresh credentials |
| E3003 | CERT_EXPIRED | Certificate expired | Renew certificate |
| E3004 | CERT_INVALID | Invalid certificate | Check CA chain |
| E3005 | CERT_REVOKED | Certificate revoked | Issue new certificate |
| E3010 | ACL_DENIED | Topic access denied | Check ACL rules |
| E3011 | ACL_NOT_FOUND | ACL rule not found | Add ACL entry |
| E3020 | TOKEN_INVALID | Invalid JWT token | Re-authenticate |
| E3021 | TOKEN_EXPIRED | JWT token expired | Refresh token |
| E3030 | RATE_LIMITED | Too many requests | Wait and retry |
| E3031 | IP_BLOCKED | IP address blocked | Contact admin |

---

## D.5 Error Response Format

### API Error Response

```json
{
  "success": false,
  "error": {
    "code": "E2001",
    "message": "Device not found",
    "details": {
      "device_serial": "TRK-123456"
    }
  },
  "timestamp": "2026-02-10T12:00:00Z"
}
```

### MQTT Error Event

```json
{
  "type": "error",
  "code": "E1001",
  "message": "GPS fix not available",
  "ts": 1707580800,
  "data": {
    "satellites_visible": 2,
    "last_fix_age": 300
  }
}
```

---

## D.6 Error Handling Best Practices

| Practice | Implementation |
|----------|----------------|
| **Log all errors** | Include error code, context, stack trace |
| **Alert on critical** | E1030, E1040, E3xxx → immediate alert |
| **Retry transient** | E1020, E2050, E2060 → exponential backoff |
| **User-friendly messages** | Map codes to readable messages |
| **Include resolution** | Provide actionable guidance |

---

[← Monitoring](./appendix-c-monitoring.md) | [Next: Glossary →](./appendix-e-glossary.md)


# 7. Security Architecture

## 7.1 Security Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                      SECURITY ARCHITECTURE                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  LAYER 1: TRANSPORT SECURITY                                     │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • TLS 1.3 encryption for all MQTT connections            │  │
│  │  • Certificate pinning on device                          │  │
│  │  • Mutual TLS (mTLS) for device authentication            │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  LAYER 2: AUTHENTICATION                                         │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • Unique client certificate per device                    │  │
│  │  • JWT tokens for backend services                         │  │
│  │  • Device provisioning with secure enrollment              │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  LAYER 3: AUTHORIZATION (ACL)                                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • Topic-level access control                              │  │
│  │  • Device can only access own topics                       │  │
│  │  • Backend has controlled wildcard access                  │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  LAYER 4: MESSAGE SECURITY                                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • Message signing (HMAC-SHA256)                           │  │
│  │  • Replay attack prevention (timestamp + nonce)            │  │
│  │  • Payload encryption for sensitive commands               │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  LAYER 5: DEVICE SECURITY                                        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • Secure boot                                             │  │
│  │  • Encrypted credential storage                            │  │
│  │  • Firmware signature verification                         │  │
│  │  • Watchdog timer                                          │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7.2 Device Provisioning Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                   DEVICE PROVISIONING FLOW                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  FACTORY PROVISIONING                                            │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 1. Generate unique device key pair on secure element        │ │
│  │ 2. Create CSR (Certificate Signing Request)                 │ │
│  │ 3. Sign CSR with IndusJS Fleet CA                           │ │
│  │ 4. Flash certificate to device secure storage               │ │
│  │ 5. Register device in provisioning database                 │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                   │
│                              ▼                                   │
│  FIELD ACTIVATION                                                │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 1. Installer scans device QR code (contains device_serial)  │ │
│  │ 2. App sends activation request to backend                  │ │
│  │ 3. Backend validates device exists and is unassigned        │ │
│  │ 4. Generate MQTT credentials (username/password)            │ │
│  │ 5. Push credentials to device via bootstrap topic           │ │
│  │ 6. Device connects with new credentials                     │ │
│  │ 7. Backend assigns device to vehicle                        │ │
│  └────────────────────────────────────────────────────────────┘ │
│                              │                                   │
│                              ▼                                   │
│  OPERATIONAL                                                     │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ • Device uses mTLS + username/password for MQTT             │ │
│  │ • Credentials rotated every 30 days                         │ │
│  │ • Certificate renewed annually                               │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7.3 Credential Management

```go
// Device credentials structure
type DeviceCredentials struct {
    DeviceID      string    `json:"device_id"`
    MQTTUsername  string    `json:"mqtt_username"`
    MQTTPassword  string    `json:"mqtt_password"` // Hashed with bcrypt
    Certificate   []byte    `json:"-"`             // X.509 certificate
    PrivateKey    []byte    `json:"-"`             // Stored in secure element
    ExpiresAt     time.Time `json:"expires_at"`
    CreatedAt     time.Time `json:"created_at"`
}

// Credential rotation (backend)
func RotateDeviceCredentials(deviceID string) error {
    // Generate new credentials
    newPassword := generateSecurePassword(32)
    hashedPassword := bcrypt.HashPassword(newPassword, 10)
    
    // Store in database
    db.UpdateDeviceCredentials(deviceID, hashedPassword)
    
    // Push to device via secure channel
    pushCredentialUpdate(deviceID, newPassword)
    
    return nil
}
```

---

## 7.4 TLS Configuration

### 7.4.1 Mosquitto TLS Setup

```conf
# /etc/mosquitto/mosquitto.conf

# TLS Listener
listener 8883
certfile /etc/mosquitto/certs/server.crt
keyfile /etc/mosquitto/certs/server.key
cafile /etc/mosquitto/certs/ca.crt

# Require client certificate (mTLS)
require_certificate true

# TLS version
tls_version tlsv1.3
```

### 7.4.2 Certificate Generation

```bash
# Generate CA
openssl genrsa -out ca.key 4096
openssl req -new -x509 -days 3650 -key ca.key -out ca.crt \
    -subj "/CN=IndusJS Fleet CA"

# Generate Server Certificate
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr \
    -subj "/CN=mqtt.indusjs.com"
openssl x509 -req -days 365 -in server.csr \
    -CA ca.crt -CAkey ca.key -set_serial 01 -out server.crt

# Generate Device Certificate
openssl genrsa -out device-001.key 2048
openssl req -new -key device-001.key -out device-001.csr \
    -subj "/CN=device-001"
openssl x509 -req -days 365 -in device-001.csr \
    -CA ca.crt -CAkey ca.key -set_serial 02 -out device-001.crt
```

---

## 7.5 Security Best Practices

| Practice | Implementation |
|----------|----------------|
| **Unique credentials per device** | Generate during factory provisioning |
| **Credential rotation** | Every 30 days automatic rotation |
| **Certificate pinning** | Embed CA certificate in device firmware |
| **Secure storage** | Use hardware secure element if available |
| **Audit logging** | Log all authentication attempts |
| **Rate limiting** | Limit connection attempts per device |
| **Anomaly detection** | Alert on unusual message patterns |

---

[← Previous: Memory-Optimized Client](./06-memory-optimized-client.md) | [Next: Message Protocols →](./08-message-protocols.md)


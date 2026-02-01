# 06. Documents

Document management for vehicles.

---

## Document Types

| Type | Description |
|------|-------------|
| `rc` | Registration Certificate |
| `insurance` | Insurance Policy |
| `puc` | Pollution Under Control |
| `fitness` | Fitness Certificate |
| `permit` | Transport Permit |
| `tax` | Road Tax |
| `other` | Other Documents |

---

## Endpoints

### Get All Document Types
```http
GET {{base_url}}/documents/types
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": [
        {"type": "rc", "label": "Registration Certificate"},
        {"type": "insurance", "label": "Insurance Policy"},
        {"type": "puc", "label": "Pollution Under Control"},
        {"type": "fitness", "label": "Fitness Certificate"},
        {"type": "permit", "label": "Transport Permit"},
        {"type": "tax", "label": "Road Tax"},
        {"type": "other", "label": "Other Documents"}
    ]
}
```

---

### Upload Document
```http
POST {{base_url}}/vehicles/:id/documents
Authorization: Bearer {{token}}
Content-Type: multipart/form-data
```

**Form Data:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| document | file | Yes | Document file |
| type | text | Yes | Document type |
| expiry_date | text | No | Expiry date (DD-MM-YYYY) |
| document_number | text | No | Document number |

---

### Bulk Upload Documents
```http
POST {{base_url}}/vehicles/:id/documents/bulk
Authorization: Bearer {{token}}
Content-Type: multipart/form-data
```

**Form Data:**
| Field | Type | Description |
|-------|------|-------------|
| rc_document | file | RC file |
| rc_expiry | text | RC expiry date |
| insurance_document | file | Insurance file |
| insurance_expiry | text | Insurance expiry |
| puc_document | file | PUC file |
| puc_expiry | text | PUC expiry |

---

### Get Vehicle Documents
```http
GET {{base_url}}/vehicles/:id/documents
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "vehicle_id": 1,
            "type": "insurance",
            "document_number": "INS123456",
            "file_url": "/uploads/documents/ins_123.pdf",
            "expiry_date": "2025-12-31T00:00:00Z",
            "status": "valid",
            "days_until_expiry": 365
        }
    ]
}
```

---

### Get Expiring Documents
```http
GET {{base_url}}/documents/expiring
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Default |
|-----------|-------------|---------|
| days | Days until expiry | 30 |

**Response:**
```json
{
    "success": true,
    "data": {
        "expiring_within_days": 30,
        "documents": [
            {
                "id": 1,
                "vehicle_id": 1,
                "vehicle_registration": "MH12AB1234",
                "type": "insurance",
                "expiry_date": "2025-02-15T00:00:00Z",
                "days_until_expiry": 15
            }
        ],
        "count": 1
    }
}
```

---

### Get Expired Documents
```http
GET {{base_url}}/documents/expired
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "documents": [
            {
                "id": 2,
                "vehicle_id": 2,
                "vehicle_registration": "MH12CD5678",
                "type": "puc",
                "expiry_date": "2024-12-31T00:00:00Z",
                "days_expired": 10
            }
        ],
        "count": 1
    }
}
```

---

### Get Documents by Status
```http
GET {{base_url}}/documents/status/:status
Authorization: Bearer {{token}}
```

**Status Options:** `valid`, `expiring`, `expired`

---

### Get Document
```http
GET {{base_url}}/documents/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "id": 1,
        "vehicle_id": 1,
        "type": "insurance",
        "document_number": "INS123456",
        "file_url": "/uploads/documents/ins_123.pdf",
        "expiry_date": "2025-12-31T00:00:00Z",
        "status": "valid",
        "verified_at": null,
        "verified_by_id": null
    }
}
```

---

### Download Document
```http
GET {{base_url}}/documents/:id/download
Authorization: Bearer {{token}}
```

Returns the document file for download.

---

### Update Document
```http
PUT {{base_url}}/documents/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "document_number": "INS123457",
    "expiry_date": "31-12-2026"
}
```

---

### Verify Document
```http
POST {{base_url}}/documents/:id/verify
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "status": "verified",
    "notes": "Document verified successfully"
}
```

**Status Options:** `verified`, `rejected`

---

### Delete Document
```http
DELETE {{base_url}}/documents/:id
Authorization: Bearer {{token}}
```

---

## Error Responses

### Document Not Found
```json
{
    "success": false,
    "message": "Document not found",
    "error": "record not found"
}
```

### Invalid Document Type
```json
{
    "success": false,
    "message": "Invalid document type",
    "error": "type must be one of: rc, insurance, puc, fitness, permit, tax, other"
}
```


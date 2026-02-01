# Documents Table

## Table: `documents`

Stores vehicle documents including Registration Certificate (RC), Insurance, PUC, Fitness Certificate, Road Tax, and Permits.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `vehicle_id` | `bigint` | NOT NULL, FK, INDEX | Associated vehicle |
| **Document Details** |
| `document_type` | `varchar(50)` | NOT NULL | Document type code |
| `tag` | `varchar(20)` | | Short tag (RC, INS, etc.) |
| `document_name` | `varchar(255)` | | Document name/title |
| `document_number` | `varchar(100)` | | Unique document number |
| `file_path` | `varchar(500)` | | File storage path |
| `file_size` | `bigint` | | File size in bytes |
| `mime_type` | `varchar(100)` | | File MIME type |
| **Dates** |
| `issue_date` | `timestamp` | | Document issue date |
| `expiry_date` | `timestamp` | INDEX | Document expiry date |
| **Authority** |
| `issuing_authority` | `varchar(255)` | | Issuing authority name |
| **Status** |
| `status` | `varchar(20)` | DEFAULT 'active' | Document status |
| `remarks` | `varchar(500)` | | Additional remarks |
| **Verification** |
| `verified_by` | `bigint` | FK → users.id | Verified by user |
| `verified_at` | `timestamp` | | Verification timestamp |
| `uploaded_by` | `bigint` | NOT NULL, FK | Uploaded by user |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Document Types

| Type Code | Tag | Description |
|-----------|-----|-------------|
| `registration_certificate` | RC | Vehicle Registration Certificate |
| `insurance` | INS | Vehicle Insurance Policy |
| `puc_certificate` | PUC | Pollution Under Control |
| `fitness_certificate` | FC | Fitness Certificate |
| `road_tax` | RT | Road Tax Receipt |
| `permit` | PERMIT | Transport Permit |
| `other` | OTHER | Other Documents |

---

## Document Status

| Status | Description |
|--------|-------------|
| `active` | Document is valid and active |
| `expired` | Document has expired |
| `pending` | Pending verification |
| `rejected` | Rejected after verification |

---

## Indexes

```sql
CREATE INDEX idx_documents_vehicle_id ON documents(vehicle_id);
CREATE INDEX idx_documents_document_type ON documents(document_type);
CREATE INDEX idx_documents_expiry_date ON documents(expiry_date);
CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_deleted_at ON documents(deleted_at);
```

---

## Sample Data

### Registration Certificate
```json
{
  "id": 1,
  "vehicle_id": 1,
  "document_type": "registration_certificate",
  "tag": "RC",
  "document_name": "Registration Certificate",
  "document_number": "MH12AB1234",
  "file_path": "/uploads/documents/1/rc_mh12ab1234.pdf",
  "file_size": 245678,
  "mime_type": "application/pdf",
  "issue_date": "2023-01-15T00:00:00Z",
  "expiry_date": "2038-01-14T00:00:00Z",
  "issuing_authority": "RTO Pune",
  "status": "active",
  "uploaded_by": 1
}
```

### Insurance
```json
{
  "id": 2,
  "vehicle_id": 1,
  "document_type": "insurance",
  "tag": "INS",
  "document_name": "Comprehensive Insurance",
  "document_number": "POL-2026-00123456",
  "file_path": "/uploads/documents/1/insurance_2026.pdf",
  "file_size": 512000,
  "mime_type": "application/pdf",
  "issue_date": "2026-01-01T00:00:00Z",
  "expiry_date": "2027-01-01T00:00:00Z",
  "issuing_authority": "ICICI Lombard",
  "status": "active",
  "uploaded_by": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/documents` | Upload document | Owner, GM, Manager |
| GET | `/api/v2/documents` | List all documents | All |
| GET | `/api/v2/documents/:id` | Get document details | All |
| PUT | `/api/v2/documents/:id` | Update document | Owner, GM, Manager |
| DELETE | `/api/v2/documents/:id` | Delete document | Owner, GM |
| **Vehicle-specific** |
| GET | `/api/v2/vehicles/:id/documents` | Get vehicle's documents | All |
| POST | `/api/v2/vehicles/:id/documents` | Upload for vehicle | Owner, GM, Manager |
| **Expiry** |
| GET | `/api/v2/documents/expiring` | Get expiring documents | All |
| GET | `/api/v2/documents/expired` | Get expired documents | All |

---

## Expiry Alerts

System generates alerts for document expiry:

| Alert Level | Days Before Expiry |
|-------------|-------------------|
| Info | 30 days |
| Warning | 15 days |
| Critical | 7 days |
| Expired | 0 or past |

### Expiring Documents Query

```sql
SELECT 
    d.*,
    v.registration_number,
    EXTRACT(DAY FROM d.expiry_date - NOW()) as days_remaining
FROM documents d
JOIN vehicles v ON d.vehicle_id = v.id
WHERE d.expiry_date <= NOW() + INTERVAL '30 days'
  AND d.expiry_date > NOW()
  AND d.status = 'active'
  AND d.deleted_at IS NULL
  AND v.deleted_at IS NULL
ORDER BY d.expiry_date ASC;
```

### Expired Documents Query

```sql
SELECT 
    d.*,
    v.registration_number,
    EXTRACT(DAY FROM NOW() - d.expiry_date) as days_expired
FROM documents d
JOIN vehicles v ON d.vehicle_id = v.id
WHERE d.expiry_date < NOW()
  AND d.status = 'active'
  AND d.deleted_at IS NULL
  AND v.deleted_at IS NULL
ORDER BY d.expiry_date DESC;
```

---

## Document Verification Flow

```
┌──────────┐     ┌─────────┐     ┌─────────┐
│ Upload   │────►│ Pending │────►│ Active  │
└──────────┘     └────┬────┘     └─────────┘
                      │
                      │ Reject
                      ▼
                 ┌──────────┐
                 │ Rejected │
                 └──────────┘
```

---

## File Storage

### Path Structure
```
/uploads/documents/{vehicle_id}/{document_type}_{unique_id}.{ext}
```

### Supported Formats
| Format | MIME Type | Max Size |
|--------|-----------|----------|
| PDF | application/pdf | 10 MB |
| JPEG | image/jpeg | 5 MB |
| PNG | image/png | 5 MB |

---

## Document Summary Response

```json
{
  "vehicle_id": 1,
  "documents": [
    {
      "type": "registration_certificate",
      "tag": "RC",
      "status": "active",
      "expiry_date": "2038-01-14T00:00:00Z",
      "days_remaining": 4382
    },
    {
      "type": "insurance",
      "tag": "INS",
      "status": "active",
      "expiry_date": "2027-01-01T00:00:00Z",
      "days_remaining": 351
    },
    {
      "type": "puc_certificate",
      "tag": "PUC",
      "status": "expired",
      "expiry_date": "2025-12-01T00:00:00Z",
      "days_remaining": -45
    }
  ],
  "expiring_count": 1,
  "expired_count": 1,
  "alert_level": "critical"
}
```

---

## Dashboard Integration

Documents appear in dashboard alerts:

```json
{
  "alerts": {
    "documents_expiring": 5,
    "documents_expired": 2,
    "licenses_expiring": 3
  },
  "expiring_documents": [
    {
      "document_type": "insurance",
      "vehicle_registration": "MH12AB1234",
      "expiry_date": "2026-02-01",
      "days_remaining": 17
    }
  ]
}
```

---

## Notes

1. **File Storage**: Currently uses local storage, should migrate to GCS for production
2. **Expiry Tracking**: System automatically updates status when documents expire
3. **Verification**: Optional verification step for critical documents
4. **Alert Preferences**: Users can configure alert timing via notification preferences
5. **Compliance**: Essential for fleet compliance and regulatory requirements

---

*Last Updated: January 2026*


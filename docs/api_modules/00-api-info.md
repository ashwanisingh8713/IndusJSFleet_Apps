# 00. API Info & Versioning

API information and versioning endpoints (no authentication required).

## Base URLs
| Environment | URL |
|-------------|-----|
| Production | `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2` |
| Local | `http://localhost:8080/api/v2` |

## API Versioning (Hybrid)
- **URL Path**: `/api/v2/...` (primary)
- **Header**: `X-API-Version: v2`
- **Query**: `?api_version=v2`

---

## Endpoints

### Get API Info
```http
GET {{api_base}}
```
Returns general API information.

### Get All API Versions
```http
GET {{api_base}}/versions
```
Returns list of all supported API versions.

### Get Current Version Info
```http
GET {{api_base}}/version
```
**Headers:**
```
X-API-Version: v1
```

### Get Version Details
```http
GET {{api_base}}/versions/v1
GET {{api_base}}/versions/v2
```

### Get Version Changelog
```http
GET {{api_base}}/versions/v1/changelog
```

### Get Migration Guide
```http
GET {{api_base}}/migration
```

### API Health Check
```http
GET {{api_base}}/health
```

---

## Response Example
```json
{
    "success": true,
    "message": "API info retrieved",
    "data": {
        "name": "Fleet Management API",
        "version": "v2",
        "supported_versions": ["v1", "v2"]
    }
}
```


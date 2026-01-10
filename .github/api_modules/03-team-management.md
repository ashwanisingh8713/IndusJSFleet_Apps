# 03. Team Management

Team member management endpoints (role-based access).

## Role Hierarchy
```
Owner (Full Access)
  └── General Manager (Financial & Operational)
        └── Manager (Operational)
              └── Supervisor (Limited)
```

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Create General Manager | ✅ | ❌ | ❌ | ❌ |
| Create Manager | ✅ | ✅ | ❌ | ❌ |
| Create Supervisor | ✅ | ✅ | ❌ | ❌ |
| View Team Members | ✅ | ✅ | ✅ | ✅ |
| Edit Team Members | ✅ | ✅ (M/S) | ❌ | ❌ |
| Disable/Enable | ✅ | ✅ (M/S) | ❌ | ❌ |
| Change Role | ✅ | ✅ (M↔S) | ❌ | ❌ |
| Delete | ✅ | ❌ | ❌ | ❌ |

---

## Endpoints

### Create Team Member
```http
POST {{base_url}}/team/members
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "email": "manager@example.com",
    "mobile": "+919876543212",
    "password": "password123",
    "first_name": "Jane",
    "last_name": "Manager",
    "role": "manager"
}
```

**Role Options:** `general_manager`, `manager`, `supervisor`

**Response:**
```json
{
    "success": true,
    "message": "Team member created successfully",
    "data": {
        "id": 2,
        "email": "manager@example.com",
        "first_name": "Jane",
        "last_name": "Manager",
        "role": "manager",
        "owner_id": 1,
        "is_active": true
    }
}
```

---

### List Team Members
```http
GET {{base_url}}/team/members
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| role | Filter by role | `?role=manager` |

**Response:**
```json
{
    "success": true,
    "message": "Team members retrieved successfully",
    "data": {
        "team": [
            {
                "id": 2,
                "email": "manager@example.com",
                "first_name": "Jane",
                "last_name": "Manager",
                "role": "manager",
                "is_active": true
            }
        ],
        "count": 1
    }
}
```

---

### Get Team Member
```http
GET {{base_url}}/team/members/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Team member retrieved successfully",
    "data": {
        "id": 2,
        "email": "manager@example.com",
        "first_name": "Jane",
        "last_name": "Manager",
        "role": "manager",
        "is_active": true
    }
}
```

---

### Update Team Member
```http
PUT {{base_url}}/team/members/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "first_name": "Jane",
    "last_name": "Updated",
    "email": "jane.updated@example.com",
    "mobile": "+919876543299",
    "role": "supervisor",
    "is_active": true,
    "password": "newpassword123"
}
```

**Notes:**
- Owner can update all fields for GM, Manager, Supervisor
- General Manager can update Manager and Supervisor only
- Cannot edit Owner accounts or your own role

---

### Toggle Team Member Active
```http
PATCH {{base_url}}/team/members/:id/toggle-active
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Team member disabled successfully",
    "data": {
        "id": 2,
        "is_active": false
    }
}
```

**Notes:**
- Owner can toggle any role except Owner
- General Manager can toggle Manager and Supervisor only
- Cannot disable yourself

---

### Change Team Member Role
```http
PATCH {{base_url}}/team/members/:id/change-role
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "new_role": "supervisor"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Team member role changed successfully",
    "data": {
        "id": 2,
        "role": "supervisor"
    }
}
```

**Notes:**
- Owner can change: GM ↔ Manager ↔ Supervisor (except to/from Owner)
- General Manager can change: Manager ↔ Supervisor only
- Cannot change your own role

---

### Reset Team Member Password
```http
POST {{base_url}}/team/members/:id/reset-password
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "new_password": "newpassword123"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Password reset successfully"
}
```

---

### Delete Team Member
```http
DELETE {{base_url}}/team/members/:id
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Team member deleted successfully"
}
```

**Notes:**
- Owner only
- Consider using Toggle Active to disable instead (preserves audit trail)

---

## Error Responses

### Access Denied
```json
{
    "success": false,
    "message": "Access denied",
    "error": "only owners and general managers can update team members"
}
```

### Cannot Edit Self
```json
{
    "success": false,
    "message": "Access denied",
    "error": "You do not have permission to edit this team member"
}
```


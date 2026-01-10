# 02. Profile

User profile management endpoints (authentication required).

---

## Endpoints

### Get Profile
```http
GET {{base_url}}/profile
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "message": "Profile retrieved successfully",
    "data": {
        "id": 1,
        "email": "owner@example.com",
        "mobile": "+919876543210",
        "first_name": "John",
        "last_name": "Doe",
        "role": "owner",
        "is_active": true,
        "created_at": "2025-01-01T10:00:00Z",
        "updated_at": "2025-01-01T10:00:00Z"
    }
}
```

---

### Update Profile
```http
PUT {{base_url}}/profile
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "first_name": "John",
    "last_name": "Updated",
    "email": "john.updated@example.com",
    "mobile": "+919876543211"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Profile updated successfully",
    "data": {
        "id": 1,
        "email": "john.updated@example.com",
        "first_name": "John",
        "last_name": "Updated",
        "role": "owner"
    }
}
```

---

### Change Password
```http
POST {{base_url}}/profile/change-password
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
    "current_password": "password123",
    "new_password": "new_password123"
}
```

**Response:**
```json
{
    "success": true,
    "message": "Password changed successfully"
}
```

---

## Error Responses

### Invalid Current Password
```json
{
    "success": false,
    "message": "Invalid password",
    "error": "current password is incorrect"
}
```

### Email Already Exists
```json
{
    "success": false,
    "message": "Email already exists",
    "error": "email is already registered to another user"
}
```


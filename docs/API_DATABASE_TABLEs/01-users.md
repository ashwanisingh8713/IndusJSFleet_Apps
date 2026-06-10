# Users Table

## Table: `users`

Stores all user accounts in the system including Owners, General Managers, Managers, and Supervisors.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `email` | `varchar(255)` | UNIQUE, INDEX | User email address |
| `mobile` | `varchar(20)` | UNIQUE, INDEX | Mobile phone number |
| `password` | `varchar(255)` | NOT NULL | Hashed password (bcrypt) |
| `first_name` | `varchar(100)` | | First name |
| `last_name` | `varchar(100)` | | Last name |
| `role` | `varchar(50)` | DEFAULT 'owner' | User role |
| `owner_id` | `bigint` | INDEX, FK → users.id | Parent owner (NULL for owners) |
| `created_by_id` | `bigint` | INDEX, FK → users.id | User who created this account |
| `is_active` | `boolean` | DEFAULT true | Account active status |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Role Constants

```go
const (
    UserRoleOwner          UserRole = "owner"           // Full access
    UserRoleGeneralManager UserRole = "general_manager" // Financial & Operational
    UserRoleManager        UserRole = "manager"         // Operational
    UserRoleSupervisor     UserRole = "supervisor"      // View & Track only
)
```

---

## Role Hierarchy

| Role | Level | Can Create | Can View Financials | Can Delete |
|------|-------|------------|---------------------|------------|
| Owner | 100 | GM, Manager, Supervisor | ✅ | ✅ |
| General Manager | 75 | Manager, Supervisor | ✅ | ✅ |
| Manager | 50 | None | ❌ | ✅ (limited) |
| Supervisor | 25 | None | ❌ | ❌ |

---

## Indexes

```sql
CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE UNIQUE INDEX idx_users_mobile ON users(mobile);
CREATE INDEX idx_users_owner_id ON users(owner_id);
CREATE INDEX idx_users_created_by_id ON users(created_by_id);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
```

---

## Relationships

| Relation | Type | Target Table | Foreign Key |
|----------|------|--------------|-------------|
| Owner | belongs_to | users | owner_id |
| CreatedBy | belongs_to | users | created_by_id |
| Vehicles | has_many | vehicles | owner_id |
| Drivers | has_many | drivers | owner_id |
| Trips | has_many | trips | owner_id |

---

## Sample Data

```json
{
  "id": 1,
  "email": "owner@company.com",
  "mobile": "9876543210",
  "first_name": "Rajesh",
  "last_name": "Kumar",
  "role": "owner",
  "owner_id": null,
  "is_active": true,
  "created_at": "2026-01-01T00:00:00Z"
}
```

---

## Permission Methods

```go
// CanViewFinancials - Owner & GM only
func (u *User) CanViewFinancials() bool

// CanCreateUser - Owner can create GM/Manager/Supervisor, GM can create Manager/Supervisor
func (u *User) CanCreateUser(targetRole UserRole) bool

// CanEditUser - Based on role hierarchy
func (u *User) CanEditUser(targetUser *User) bool

// CanToggleUserActive - Enable/disable users
func (u *User) CanToggleUserActive(targetUser *User) bool

// CanChangeRole - Owner only, between Manager/Supervisor
func (u *User) CanChangeRole(targetUser *User, newRole UserRole) bool
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/auth/register` | Register new owner | Public |
| POST | `/api/v2/auth/login` | User login | Public |
| GET | `/api/v2/profile` | Get own profile | All |
| PUT | `/api/v2/profile` | Update own profile | All |
| GET | `/api/v2/team-members` | List team members | All |
| POST | `/api/v2/team-members` | Create team member | Owner, GM |
| PUT | `/api/v2/team-members/:id` | Update team member | Owner, GM, Manager |
| PATCH | `/api/v2/team-members/:id/role` | Change role | Owner only |
| PATCH | `/api/v2/team-members/:id/toggle` | Enable/disable | Owner, GM, Manager |
| DELETE | `/api/v2/team-members/:id` | Delete member | Owner only |

---

## Notes

1. **Password Storage**: Passwords are hashed using bcrypt before storage
2. **Email/Mobile**: Both must be unique across all users
3. **Owner Hierarchy**: Owners have `owner_id = NULL`, team members reference their owner
4. **Soft Delete**: Users are never physically deleted, only marked with `deleted_at`
5. **Self-Edit**: Users cannot change their own role or toggle their own active status

---

*Last Updated: January 2026*


# Backend specification: Driver IAM role setup

## Problem

`POST /api/v1/drivers` reaches the Fleet backend successfully, but driver creation fails with:

```json
{
  "errorMessage": "driver_create_failed",
  "developerMessage": "failed to create driver: could not create IAM login account: role not found in tenant"
}
```

## Root Cause

The mobile app sends the required driver data, including `password`, so Fleet enters the IAM-login creation path.

In `IndusJSFleet_GoLang_Backend`, `CreateDriver` creates an IAM user with:

```go
Roles: []string{"driver"}
```

Fleet then calls IAM `POST /api/v1/users`. In `IndusJS-IAM`, `UserUseCase.CreateUser` validates every requested role with `roleRepo.GetByName(ctx, roleName, &creator.TenantID, req.AppID)`.

The IAM lookup fails because the role named `driver` is not available for the current tenant/app context.

## Required Backend/IAM Fix

Ensure IAM has a `driver` role available for Fleet user creation:

1. Seed or migrate a `driver` role for the Fleet app (`fleet-management`), preferably as an app role template with `tenant_id = NULL`, or provision it into each tenant when the tenant is created.
2. Ensure Fleet resolves and sends the Fleet app id when calling IAM `CreateUser`.
3. Ensure the role lookup can find `driver` by name for `(tenant_id = current tenant OR NULL)` and `(app_id = fleet-management OR NULL)`.
4. Return a proper `4xx` error if the role is missing, instead of `500`, so clients can distinguish setup/configuration issues from server crashes.

## App-Side Handling

The app now maps this backend error to:

`Driver role is not configured for this organization. Please contact support or backend team.`


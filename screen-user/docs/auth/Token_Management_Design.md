# Token Management Design Document

> Fully autonomous device token management with internet-aware retry logic.

**Module:** screen-user  
**Author:** IndusJS Team  
**Version:** 1.0.0  
**Last Updated:** June 11, 2026

---

## Overview

`TokenManager` is a fully autonomous, self-contained `@Singleton` that handles all token
operations internally. No external calls needed — just inject via Hilt.

### What TokenManager Does Automatically

1. **On construction** — checks token validity, fetches if needed
3. **Schedules** periodic refresh via WorkManager (every 1/10th total expiry time)
4. **Retries** automatically when internet becomes available

### Key Principles

| Principle | Description |
|-----------|-------------|
| **Autonomous** | No external calls needed — fully self-contained |
| **WorkManager** | Periodic refresh survives process death |
| **Graceful Degradation** | Works offline with cached token until expiry |

---

## Architecture

```
```
// write full class flow
---

## Token States

| State | Condition | Action |
|-------|-----------|--------|
| `NO_TOKEN` | `!hasToken()` | Fetch immediately if internet available |
| `VALID` | `hasToken() && !isExpiringSoon() && !isExpired()` | No action needed |
| `EXPIRING` | `hasToken() && isExpiringSoon() && !isExpired()` | Proactive refresh |
| `EXPIRED` | `hasToken() && isExpired()` | Fetch immediately |

---

## Execution Scenarios

### Scenario 1: App Launch - No Token, Internet Available
- `TokenManager` constructor calls `checkAndFetchIfNeeded()`
- `hasToken()` returns false
- `hasInternet()` returns true → fetch token immediately

### Scenario 2: App Launch - No Token, No Internet
- `TokenManager` constructor calls `checkAndFetchIfNeeded()`
- `hasToken()` returns false
- `hasInternet()` returns false → sets `pendingTokenFetch = true`
- `NetworkMonitor` fires `onNetworkAvailable()` when internet restores
- Fetch token automatically

### Scenario 3: Token Expired
- `isExpired()` returns true → clear and re-fetch
- Same flow as Scenario 1 or 2

### Scenario 4: Proactive Refresh (WorkManager)
- `TokenRefreshWorker.doWork()` runs every 1 hour
- Calls `tokenManager.checkAndFetchIfNeeded()`
- If `isExpiringSoon()` (within 4 hours of expiry), refresh token

### Scenario 5: Missed Refresh (Network Restore)
- `onNetworkAvailable()` always checks `isExpired()`
- If expired, clears and fetches immediately

---

## WorkManager Configuration

- **Interval:** 1 hour
- **Constraints:** Requires network connection
- **Policy:** KEEP (don't replace existing)

---

## Components

| Component | Path | Purpose |
|-----------|------|---------|
| `TokenManager` | `auth/TokenManager.java` | Fully autonomous token management |
| `TokenRefreshWorker` | `auth/TokenRefreshWorker.java` | WorkManager periodic job |

---

## App Integration



---

## Token Refresh Notification

When `TokenManager` successfully fetches or refreshes a token, it notifies registered listeners
via `TokenRefreshListener`. This is critical for MQTT: EMQX validates JWT on every connection
and disconnects the device on JWT expiry.



### Wiring


### Flow

```
// Write flow

```



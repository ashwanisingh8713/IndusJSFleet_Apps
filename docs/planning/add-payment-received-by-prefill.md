# Plan — "Received by" pre-filled with "owner" in Add Trip Payment

## Problem
Add Trip Payment → Additional Info → "Received by" is pre-filled with `owner` (a role), which is wrong — it should be the receiving person's name, or empty.

## Root-cause analysis
- `AddTripPaymentViewModel` (new-payment branch) sets:
  `defaultReceiver = getUserName() ?: <formatted getUserRole()> ?: "Staff"`.
- When the locally-saved user **name is blank** (same name-loading gap seen in the drawer bug), it falls back to the **role** → "owner".
- A role ("owner") or placeholder ("Staff") is not a valid "Received by" value; only a person's name is meaningful.

## Approach (chosen): name-only prefill, else blank
- Pre-fill "Received by" with the saved user **name** only; if unavailable, leave it **blank** so the user enters the actual receiver. Remove the role and "Staff" fallbacks.
- `receivedBy` is optional on submit (`ifBlank { null }`), so an empty default is safe.
- Alternative: keep prefilling but format the role nicely — rejected; a role is semantically wrong for "who received the payment".

## Affected
- `screen-trip-payment`: `AddTripPaymentViewModel` (new-payment init). No contract/API change.
- Related: the saved name itself loads correctly for normally-registered users (login/profile `saveUserName`); if it is ever blank, the field is simply empty now (no bogus "owner"). The deeper "name not saved" concern is tracked with the drawer-name fix; if names are empty from backend that is a backend data gap (Session A).

## Best practices
- Sensible defaults: prefill a real, editable value (name) or nothing — never a role/placeholder masquerading as data. MVI preserved.

## Edge cases
- Name unknown → blank field (user types). Edit-existing-payment path unchanged (uses the stored `payment.receivedBy`).

## Verification
- `:androidApp:assembleDebug`; open Add Trip Payment → "Received by" shows the user's name (or empty), never "owner". D to confirm on-device.

## Future enhancement
- Optionally a picker of team members for "Received by" instead of free text.

# 13. Location Services API

> **Package:** `com.indusjs.fleet.data.datasource.location`  
> **Auth Required:** No (uses Google API key)  
> **External API:** Google Maps Platform  
> **Last Updated:** 16-Mar-2026

---

## Overview

`GooglePlacesService` provides location-related functionality using Google Maps APIs:

1. **Places Autocomplete** — Search suggestions as user types
2. **Place Details** — Get coordinates for a selected place
3. **Distance Matrix** — Calculate road distance between two locations

---

## Service Class

```kotlin
class GooglePlacesService(
    private val httpClient: HttpClient,
    private val apiKey: String   // from ApiConfig.GOOGLE_PLACES_API_KEY
)
```

> **Not injected via Metro** — manually instantiated with `ApiConfig.GOOGLE_PLACES_API_KEY`.

---

## Methods

### `searchPlaces`

Search for place predictions based on input text.

```kotlin
suspend fun searchPlaces(
    query: String,
    sessionToken: String? = null
): Result<List<PlacePrediction>>
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `query` | `String` | Search text (min 3 chars) |
| `sessionToken` | `String?` | Session token for billing optimization |

**Google API:** `GET https://maps.googleapis.com/maps/api/place/autocomplete/json`

| API Parameter | Value |
|---------------|-------|
| `input` | User query |
| `components` | `country:in` (restricted to India) |
| `types` | `geocode\|establishment` |

**Returns:** `List<PlacePrediction>` or empty list if `< 3` chars.

---

### `getPlaceDetails`

Get coordinates for a place ID.

```kotlin
suspend fun getPlaceDetails(
    placeId: String,
    sessionToken: String? = null
): Result<PlaceDetails>
```

**Google API:** `GET https://maps.googleapis.com/maps/api/place/details/json`

| API Parameter | Value |
|---------------|-------|
| `place_id` | Place ID from autocomplete |
| `fields` | `formatted_address,geometry,name` |

---

### `getRoadDistance`

Calculate road distance between two GPS coordinates.

```kotlin
suspend fun getRoadDistance(
    originLat: Double,
    originLng: Double,
    destLat: Double,
    destLng: Double
): Result<DistanceResult>
```

**Google API:** `GET https://maps.googleapis.com/maps/api/distancematrix/json`

| API Parameter | Value |
|---------------|-------|
| `origins` | `{lat},{lng}` |
| `destinations` | `{lat},{lng}` |
| `mode` | `driving` |
| `units` | `metric` |

---

## Response DTOs

### `PlacePrediction`

| Field | JSON Key | Type | Description |
|-------|----------|------|-------------|
| `placeId` | `place_id` | `String` | Unique place identifier |
| `description` | `description` | `String` | Full place description |
| `structuredFormatting` | `structured_formatting` | `StructuredFormatting?` | Main/secondary text |

### `StructuredFormatting`

| Field | JSON Key | Type |
|-------|----------|------|
| `mainText` | `main_text` | `String` |
| `secondaryText` | `secondary_text` | `String?` |

### `PlaceDetails`

| Field | JSON Key | Type |
|-------|----------|------|
| `name` | `name` | `String?` |
| `formattedAddress` | `formatted_address` | `String?` |
| `geometry` | `geometry` | `Geometry?` |

### `Geometry` → `LatLng`

| Field | Type |
|-------|------|
| `lat` | `Double` |
| `lng` | `Double` |

### `DistanceResult`

| Field | Type | Description |
|-------|------|-------------|
| `distanceKm` | `Double` | Distance in kilometers |
| `distanceText` | `String` | Human-readable distance (e.g., "245 km") |
| `durationMinutes` | `Long` | Duration in minutes |
| `durationText` | `String` | Human-readable duration (e.g., "3 hours 45 mins") |

---

## Usage in the App

### Trip Creation — Location Autocomplete

```kotlin
// User types → search places
val predictions = googlePlacesService.searchPlaces("Mumbai Airport")

// User selects → get coordinates
val details = googlePlacesService.getPlaceDetails(prediction.placeId)

// Both locations set → calculate distance
val distance = googlePlacesService.getRoadDistance(
    startLat, startLng, endLat, endLng
)
```

### API Key Configuration

Stored in `ApiConfig.GOOGLE_PLACES_API_KEY`.

Required Google Cloud APIs:
- **Places API**
- **Geocoding API**
- **Distance Matrix API**

---

## Source Files

| File | Path |
|------|------|
| GooglePlacesService | `data/datasource/location/GooglePlacesService.kt` |


# Simple Cockpit API Contract

The backend exposes JSON over HTTP. During local development it runs at `http://localhost:8000`.

## Health check

### `GET /health`

Returns `200 OK` while the service is available.

```json
{
  "status": "ok"
}
```

## Dashboard synchronization

### `POST /api/v1/dashboard/sync`

The Android client sends its timestamp and an optional playback action. The backend logs the validated request, advances the in-memory simulation, and returns the complete dashboard state.

### Request

```json
{
  "clientTimestamp": "2026-07-30T17:30:00Z",
  "action": "NONE"
}
```

| Field | Type | Constraints |
| --- | --- | --- |
| `clientTimestamp` | ISO-8601 timestamp | Required |
| `action` | String enum | `NONE` or `TOGGLE_PLAYBACK` |

Unknown request fields are rejected.

### Successful response

Returns `200 OK` with the current simulated state.

```json
{
  "serverTimestamp": "2026-07-30T17:30:00Z",
  "speedKmh": 82.0,
  "batteryPercent": 74,
  "outsideTemperatureC": 21.5,
  "drivingStatus": "DRIVING",
  "media": {
    "isPlaying": true,
    "trackName": "Night Drive",
    "progressPercent": 46
  },
  "navigation": {
    "destination": "Central Station",
    "remainingMinutes": 18,
    "distanceKm": 12.4
  }
}
```

| Field | Constraints |
| --- | --- |
| `speedKmh` | `0` to `250` |
| `batteryPercent` | `0` to `100` |
| `outsideTemperatureC` | `-20` to `50` |
| `drivingStatus` | `PARKED`, `DRIVING`, or `CHARGING` |
| `media.progressPercent` | `0` to `100` |
| `navigation.remainingMinutes` | Non-negative |
| `navigation.distanceKm` | Non-negative |

### Validation errors

FastAPI returns `422 Unprocessable Entity` when the timestamp, action, field type, or field constraints are invalid.

## Logging

Every validated synchronization request is appended to `sync_requests.log`. Each line contains a UTC timestamp and the validated request JSON. Requests rejected during validation are not written by this logger.
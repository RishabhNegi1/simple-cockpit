# Simple Cockpit Backend

Simple Cockpit Backend is a lightweight FastAPI service that validates Android synchronization requests, advances an in-memory vehicle simulation, and returns the complete dashboard state.

## Architecture

### System Architecture

![System Architecture](docs/system-architecture.jpg)

### Data Flow

![Data Flow](docs/data_flow.jpg)

FastAPI defines the routes, and Pydantic validates request and response models. A process-local `VehicleSimulation` owns dashboard state. Uvicorn serves the application.

## Technology Choices

- **Python and FastAPI** keep the REST service concise while providing typed route definitions and automatic API schema generation.
- **Pydantic** validates timestamps, enum values, numeric ranges, and unexpected fields at the API boundary so invalid data cannot enter the simulation.
- **Uvicorn** is a lightweight ASGI server suited to running the FastAPI application locally with quick reloads during development.
- **A process-local simulation** avoids database and infrastructure overhead while making dashboard changes deterministic and easy to demonstrate.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Returns `{"status":"ok"}`. |
| `POST` | `/api/v1/dashboard/sync` | Applies an action, advances the simulation, and returns the full dashboard state. |

The sync request accepts an ISO-8601 timestamp and one of two actions:

```json
{
  "clientTimestamp": "2026-07-30T17:30:00Z",
  "action": "NONE"
}
```

Allowed actions are `NONE` and `TOGGLE_PLAYBACK`. The response schema and field constraints are documented in `API_CONTRACT.md`.

## Simulation

Each sync request advances one tick. Driving status rotates through `DRIVING`, `CHARGING`, and `PARKED` every 15 ticks. Speed, battery, temperature, media progress, and navigation progress update from the current state. Playback toggles only when requested.

## Logging

Validated sync requests are appended to `sync_requests.log`. Each entry has a UTC ISO-8601 timestamp and the validated request JSON. Requests rejected by Pydantic are not logged by this logger.

## Setup

From `cockpit-backend/`, run:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Check the service:

```powershell
Invoke-RestMethod -Uri "http://localhost:8000/health"
```

## Tests

Run the standard-library test suite from `cockpit-backend/`:

```powershell
python -m unittest discover -s tests -v
```

The tests cover request validation, contract ranges, status transitions, playback actions, endpoint output, and timestamped request logging.

## Limitations

- State is process-local and resets after a restart.
- Polling frequency controls simulation speed because each request creates a tick.
- Concurrent workers would not share simulation state.
- Dependencies in `requirements.txt` are not version-pinned.
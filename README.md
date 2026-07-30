# Simple Cockpit

## Overview

Simple Cockpit contains a Kotlin Android tablet dashboard and a Python FastAPI backend. It exchanges simulated vehicle, media, and navigation data over REST, with the backend controlling the current state.

## Features

- Speed, battery, temperature, and driving status
- Media playback, track, and progress
- Navigation destination, time, and distance
- Dynamic simulated data
- Connection-loss handling and automatic recovery

## Architecture and Data Flow

### System Architecture

![System Architecture](docs/system-architecture.jpg)

### Data Flow

![Data Flow](docs/data_flow.jpg)

The main data path is:

```text
Android UI
→ ViewModel
→ Repository
→ Retrofit
→ FastAPI
→ Simulation
→ Dashboard response
```

Validated requests are written to `sync_requests.log`.

## Technology Choices

- **Kotlin, Jetpack Compose, and Material 3** provide a type-safe, declarative Android UI that can render the full dashboard directly from one state object.
- **ViewModel and StateFlow** keep UI state outside composables, survive configuration changes, and make connection recovery visible through a single observable stream.
- **Retrofit, Gson, and OkHttp** provide typed REST calls, straightforward JSON mapping, and explicit short timeouts so connection failures appear quickly in the cockpit.
- **Python, FastAPI, and Pydantic** keep the backend small while providing typed request validation, response serialization, and an explicit API contract.
- **An in-memory simulation** keeps the take-home focused on client/server behavior without adding database or external-service setup.

## Project Structure

- `cockpit-android/` — Android client
- `cockpit-backend/` — FastAPI service
- `docs/` — system architecture and data-flow diagrams
- `evidence/` — runtime evidence

## Approach and Scope

Work started with the API contract and backend. The Android UI was then built with temporary local data, connected through Retrofit, and tested for polling, playback, disconnection, and recovery. The temporary Android data source was removed after integration.

The take-home was scoped to a five-hour window. Core behavior and readability were prioritized. Maps, databases, authentication, real vehicle signals, and advanced animations were left out.

## AI Usage

ChatGPT supported task breakdown, architecture discussion, debugging, test planning, and documentation. Codex assisted with scaffolding, targeted code changes, and documentation drafts.

Architecture, scope, API decisions, generated changes, builds, and runtime behavior were reviewed manually. AI output was not accepted without inspection and testing.

## Running the Project

Start the backend:

```powershell
cd cockpit-backend
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

In another terminal, build the Android client:

```powershell
cd cockpit-android
.\gradlew.bat :app:checkDebugAarMetadata --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

Run the debug build on a landscape tablet emulator while the backend is listening on port `8000`.

## Shared Limitations

- Dashboard data and external services are simulated.
- State is kept in memory and resets with the backend process.
- Authentication, persistence, HTTPS, and deployment configuration are not included.
- The project is a local take-home implementation, not a deployed system.
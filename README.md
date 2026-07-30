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

- `cockpit-android/` - Android client
- `cockpit-backend/` - FastAPI service
- `docs/` - system architecture and data-flow diagrams
- `evidence/` - runtime evidence

## Approach and Scope

Work started with the API contract and backend. The Android UI was then built with temporary local data, connected through Retrofit, and tested for polling, playback, disconnection, and recovery. The temporary Android data source was removed after integration.

The take-home was scoped to a five-hour window. Core behavior and readability were prioritized. Maps, databases, authentication, real vehicle signals, and advanced animations were left out.

## AI Usage

The assignment permits AI, so I used it but with a clear split: the design of this project is mine, and AI handled work that was cheap to verify.

What I did myself: understanding the task and breaking it into phases (contract → backend → UI → integration → failure testing), and the system architecture. The concrete decisions were mine: the API contract (`POST /api/v1/dashboard/sync` plus `/health`, with the backend owning all state), the MVVM structure with a `DashboardRepository` interface so the UI could be built on temporary data and integrated later, the concurrency handling (a Mutex serializing polling and user actions), the connection-loss behaviour (keep the last data visible, show a disconnected state, recover automatically), the strict backend validation (`extra="forbid"`, bounded fields), and the scope cuts needed to fit the five-hour window. All builds, manual testing and the recovery verification in `evidence/` are my own work as well.

Where I used AI: ChatGPT as a discussion partner sanity-checking my plan, helping debug integration issues like emulator networking and Gradle versions, and drafting documentation. Codex for boilerplate and scaffolding: the Retrofit/OkHttp wiring, the FastAPI skeleton, the Compose theme files, and small, precisely described edits.

Process: I asked for one specific file or change at a time, reviewed the output like a code-review diff, ran it, and then accepted, fixed or rejected it  I never generated the whole app in one shot. My working rule was simple: AI writes what is cheap to verify, I decide and write what is expensive to get wrong. Everything in this repo was read, tested, and is mine to defend.

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

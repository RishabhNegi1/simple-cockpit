"""Simple Cockpit backend.

Run with:
    uvicorn main:app --host 0.0.0.0 --port 8000 --reload
"""

import json
import logging
from datetime import datetime, timezone
from enum import Enum

from fastapi import FastAPI
from pydantic import BaseModel, ConfigDict, Field

from simulation import VehicleSimulation


class Action(str, Enum):
    NONE = "NONE"
    TOGGLE_PLAYBACK = "TOGGLE_PLAYBACK"


class DrivingStatus(str, Enum):
    PARKED = "PARKED"
    DRIVING = "DRIVING"
    CHARGING = "CHARGING"


class SyncRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    clientTimestamp: datetime
    action: Action


class MediaState(BaseModel):
    model_config = ConfigDict(extra="forbid")

    isPlaying: bool
    trackName: str
    progressPercent: int = Field(ge=0, le=100)


class NavigationState(BaseModel):
    model_config = ConfigDict(extra="forbid")

    destination: str
    remainingMinutes: int = Field(ge=0)
    distanceKm: float = Field(ge=0)


class DashboardState(BaseModel):
    model_config = ConfigDict(extra="forbid")

    serverTimestamp: datetime
    speedKmh: float = Field(ge=0, le=250)
    batteryPercent: int = Field(ge=0, le=100)
    outsideTemperatureC: float = Field(ge=-20, le=50)
    drivingStatus: DrivingStatus
    media: MediaState
    navigation: NavigationState


class Iso8601Formatter(logging.Formatter):
    def formatTime(self, record: logging.LogRecord, datefmt: str | None = None) -> str:
        return datetime.fromtimestamp(record.created, timezone.utc).isoformat(
            timespec="seconds"
        )


def _create_sync_logger() -> logging.Logger:
    logger = logging.getLogger("sync_requests")
    logger.setLevel(logging.INFO)
    logger.propagate = False

    if not logger.handlers:
        handler = logging.FileHandler("sync_requests.log", encoding="utf-8")
        handler.setFormatter(Iso8601Formatter("%(asctime)s %(levelname)s %(message)s"))
        logger.addHandler(handler)

    return logger


app = FastAPI()
simulation = VehicleSimulation()
sync_logger = _create_sync_logger()


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/api/v1/dashboard/sync", response_model=DashboardState)
async def sync_dashboard(request: SyncRequest) -> DashboardState:
    request_data = request.model_dump(mode="json")
    sync_logger.info(
        "validated_request=%s",
        json.dumps(request_data, separators=(",", ":")),
    )
    return DashboardState.model_validate(simulation.tick(request.action))

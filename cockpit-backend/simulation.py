"""In-memory vehicle state simulation for the Simple Cockpit dashboard."""

import random
from datetime import datetime, timezone
from typing import Any


class VehicleSimulation:
    def __init__(self) -> None:
        self._tick_count = 0
        self._status_index = 0
        self._statuses = ("DRIVING", "CHARGING", "PARKED")

        self._speed = 72.0
        self._battery = 74.0
        self._outside_temperature = 21.5

        self._media_playing = True
        self._media_progress = 20
        self._playlist = (
            "Night Drive",
            "City Lights",
            "Open Road",
            "Morning Cruise",
        )
        self._track_index = 0

        self._destination = "Central Station"
        self._remaining_minutes = 18.0
        self._distance_km = 12.4

    def tick(self, action: Any) -> dict[str, Any]:
        self._tick_count += 1
        self._apply_action(action)
        self._update_driving_status()
        self._update_speed()
        self._update_battery()
        self._update_temperature()
        self._update_media()
        self._update_navigation()
        return self._build_state()

    def _apply_action(self, action: Any) -> None:
        action_value = getattr(action, "value", action)
        if action_value == "TOGGLE_PLAYBACK":
            self._media_playing = not self._media_playing

    def _update_driving_status(self) -> None:
        if self._tick_count % 15 == 0:
            self._status_index = (self._status_index + 1) % len(self._statuses)

    def _update_speed(self) -> None:
        if self._statuses[self._status_index] == "DRIVING":
            self._speed = min(250.0, max(0.0, self._speed + random.uniform(-2.0, 2.0)))
        else:
            self._speed = 0.0

    def _update_battery(self) -> None:
        status = self._statuses[self._status_index]
        if status == "DRIVING":
            self._battery -= 0.2
        elif status == "CHARGING":
            self._battery += 0.5
        self._battery = min(100.0, max(0.0, self._battery))

    def _update_temperature(self) -> None:
        self._outside_temperature += random.uniform(-0.1, 0.1)
        self._outside_temperature = min(
            50.0, max(-20.0, self._outside_temperature)
        )

    def _update_media(self) -> None:
        if not self._media_playing:
            return

        self._media_progress += 2
        if self._media_progress >= 100:
            self._media_progress = 0
            self._track_index = (self._track_index + 1) % len(self._playlist)
        self._media_progress = min(100, max(0, self._media_progress))

    def _update_navigation(self) -> None:
        if self._statuses[self._status_index] != "DRIVING":
            return

        self._distance_km = max(
            0.0, self._distance_km - random.uniform(0.015, 0.025)
        )
        self._remaining_minutes = max(
            0.0, self._remaining_minutes - (1.0 / 60.0)
        )

    def _build_state(self) -> dict[str, Any]:
        server_timestamp = datetime.now(timezone.utc).isoformat(timespec="seconds")
        return {
            "serverTimestamp": server_timestamp,
            "speedKmh": round(self._speed, 1),
            "batteryPercent": int(round(self._battery)),
            "outsideTemperatureC": round(self._outside_temperature, 1),
            "drivingStatus": self._statuses[self._status_index],
            "media": {
                "isPlaying": self._media_playing,
                "trackName": self._playlist[self._track_index],
                "progressPercent": self._media_progress,
            },
            "navigation": {
                "destination": self._destination,
                "remainingMinutes": max(0, int(round(self._remaining_minutes))),
                "distanceKm": round(self._distance_km, 1),
            },
        }

import asyncio
import io
import logging
import unittest

from pydantic import ValidationError

from main import (
    Action,
    DashboardState,
    Iso8601Formatter,
    NavigationState,
    SyncRequest,
    sync_dashboard,
    sync_logger,
)
from simulation import VehicleSimulation


class RequestValidationTests(unittest.TestCase):
    def test_rejects_unknown_action(self) -> None:
        with self.assertRaises(ValidationError):
            SyncRequest.model_validate(
                {
                    "clientTimestamp": "2026-07-31T10:00:00Z",
                    "action": "SKIP_TRACK",
                }
            )

    def test_rejects_unexpected_request_field(self) -> None:
        with self.assertRaises(ValidationError):
            SyncRequest.model_validate(
                {
                    "clientTimestamp": "2026-07-31T10:00:00Z",
                    "action": "NONE",
                    "unexpected": True,
                }
            )

    def test_rejects_dashboard_values_outside_contract(self) -> None:
        with self.assertRaises(ValidationError):
            DashboardState.model_validate(
                {
                    "serverTimestamp": "2026-07-31T10:00:00Z",
                    "speedKmh": 251,
                    "batteryPercent": 101,
                    "outsideTemperatureC": 51,
                    "drivingStatus": "FLYING",
                    "media": {
                        "isPlaying": True,
                        "trackName": "Night Drive",
                        "progressPercent": 101,
                    },
                    "navigation": {
                        "destination": "Central Station",
                        "remainingMinutes": 18,
                        "distanceKm": 12.4,
                    },
                }
            )

    def test_rejects_negative_navigation_values(self) -> None:
        for remaining_minutes, distance_km in ((-1, 1.0), (1, -0.1)):
            with self.subTest(
                remaining_minutes=remaining_minutes,
                distance_km=distance_km,
            ):
                with self.assertRaises(ValidationError):
                    NavigationState(
                        destination="Central Station",
                        remainingMinutes=remaining_minutes,
                        distanceKm=distance_km,
                    )


class VehicleSimulationTests(unittest.TestCase):
    def test_dynamic_state_stays_within_contract_ranges(self) -> None:
        simulation = VehicleSimulation()
        states = [simulation.tick(Action.NONE) for _ in range(45)]

        self.assertEqual(
            {"DRIVING", "CHARGING", "PARKED"},
            {state["drivingStatus"] for state in states},
        )
        for state in states:
            self.assertGreaterEqual(state["speedKmh"], 0)
            self.assertLessEqual(state["speedKmh"], 250)
            self.assertGreaterEqual(state["batteryPercent"], 0)
            self.assertLessEqual(state["batteryPercent"], 100)
            self.assertGreaterEqual(state["outsideTemperatureC"], -20)
            self.assertLessEqual(state["outsideTemperatureC"], 50)
            self.assertGreaterEqual(state["media"]["progressPercent"], 0)
            self.assertLessEqual(state["media"]["progressPercent"], 100)
            self.assertGreaterEqual(state["navigation"]["remainingMinutes"], 0)
            self.assertGreaterEqual(state["navigation"]["distanceKm"], 0)

    def test_toggle_playback_changes_media_state(self) -> None:
        simulation = VehicleSimulation()
        before = simulation.tick(Action.NONE)["media"]["isPlaying"]

        after = simulation.tick(Action.TOGGLE_PLAYBACK)["media"]["isPlaying"]

        self.assertNotEqual(before, after)


class EndpointTests(unittest.TestCase):
    def test_sync_returns_dashboard_and_logs_validated_request(self) -> None:
        request = SyncRequest.model_validate(
            {
                "clientTimestamp": "2026-07-31T10:00:00Z",
                "action": "NONE",
            }
        )
        stream = io.StringIO()
        handler = logging.StreamHandler(stream)
        handler.setFormatter(Iso8601Formatter("%(asctime)s %(levelname)s %(message)s"))
        original_handlers = list(sync_logger.handlers)
        sync_logger.handlers = [handler]

        try:
            response = asyncio.run(sync_dashboard(request))
        finally:
            sync_logger.handlers = original_handlers
            handler.close()

        self.assertIsInstance(response, DashboardState)
        self.assertGreaterEqual(response.speedKmh, 0)
        self.assertLessEqual(response.speedKmh, 250)
        log_line = stream.getvalue()
        self.assertRegex(
            log_line,
            r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\+00:00 INFO ",
        )
        self.assertIn('"clientTimestamp":"2026-07-31T10:00:00Z"', log_line)
        self.assertIn('"action":"NONE"', log_line)


if __name__ == "__main__":
    unittest.main()
package com.example.simplecockpit.data

import kotlinx.coroutines.delay
import java.time.Instant

class FakeDashboardRepository : DashboardRepository {
    private var syncCount = 0

    private var dashboard = DashboardState(
        serverTimestamp = Instant.now().toString(),
        speedKmh = 72.0,
        batteryPercent = 74,
        outsideTemperatureC = 21.5,
        drivingStatus = "DRIVING",
        media = MediaState(
            isPlaying = true,
            trackName = "Night Drive",
            progressPercent = 20
        ),
        navigation = NavigationState(
            destination = "Central Station",
            remainingMinutes = 18,
            distanceKm = 12.4
        )
    )

    override suspend fun sync(action: SyncAction): DashboardState {
        delay(100)
        syncCount += 1

        val drivingStatus = when (syncCount % STATUS_CYCLE_LENGTH) {
            in PARKED_RANGE -> "PARKED"
            in CHARGING_RANGE -> "CHARGING"
            else -> "DRIVING"
        }
        val isPlaying = if (action == SyncAction.TOGGLE_PLAYBACK) {
            !dashboard.media.isPlaying
        } else {
            dashboard.media.isPlaying
        }
        val speedKmh = when (drivingStatus) {
            "DRIVING" -> dashboard.speedKmh +
                if (syncCount % 2 == 0) SPEED_INCREASE else SPEED_DECREASE
            else -> dashboard.speedKmh - NON_DRIVING_SLOWDOWN
        }.coerceIn(MIN_SPEED, MAX_SPEED)
        val batteryPercent = when {
            drivingStatus == "CHARGING" -> dashboard.batteryPercent + 1
            syncCount % BATTERY_DRAIN_INTERVAL == 0 -> dashboard.batteryPercent - 1
            else -> dashboard.batteryPercent
        }.coerceIn(MIN_BATTERY, MAX_BATTERY)
        val temperatureC = (
            dashboard.outsideTemperatureC +
                if (syncCount % 2 == 0) TEMPERATURE_STEP else -TEMPERATURE_STEP
            ).coerceIn(MIN_TEMPERATURE, MAX_TEMPERATURE)
        val mediaProgress = (
            dashboard.media.progressPercent + if (isPlaying) 1 else 0
            ).coerceIn(MIN_PROGRESS, MAX_PROGRESS)
        val distanceKm = (
            dashboard.navigation.distanceKm -
                if (drivingStatus == "DRIVING") DISTANCE_STEP else 0.0
            ).coerceAtLeast(0.0)
        val remainingMinutes = (
            dashboard.navigation.remainingMinutes -
                if (drivingStatus == "DRIVING" && syncCount % MINUTE_INTERVAL == 0) 1 else 0
            ).coerceAtLeast(0)

        dashboard = dashboard.copy(
            serverTimestamp = Instant.now().toString(),
            speedKmh = speedKmh,
            batteryPercent = batteryPercent,
            outsideTemperatureC = temperatureC,
            drivingStatus = drivingStatus,
            media = dashboard.media.copy(
                isPlaying = isPlaying,
                progressPercent = mediaProgress
            ),
            navigation = dashboard.navigation.copy(
                remainingMinutes = remainingMinutes,
                distanceKm = distanceKm
            )
        )
        return dashboard
    }

    private companion object {
        const val STATUS_CYCLE_LENGTH = 50
        val PARKED_RANGE = 35..39
        val CHARGING_RANGE = 40..44
        const val SPEED_INCREASE = 0.8
        const val SPEED_DECREASE = -0.3
        const val NON_DRIVING_SLOWDOWN = 12.0
        const val MIN_SPEED = 0.0
        const val MAX_SPEED = 250.0
        const val BATTERY_DRAIN_INTERVAL = 10
        const val MIN_BATTERY = 0
        const val MAX_BATTERY = 100
        const val TEMPERATURE_STEP = 0.1
        const val MIN_TEMPERATURE = -20.0
        const val MAX_TEMPERATURE = 50.0
        const val MIN_PROGRESS = 0
        const val MAX_PROGRESS = 100
        const val DISTANCE_STEP = 0.1
        const val MINUTE_INTERVAL = 5
    }
}

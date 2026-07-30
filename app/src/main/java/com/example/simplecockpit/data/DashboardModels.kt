package com.example.simplecockpit.data

data class MediaState(
    val isPlaying: Boolean,
    val trackName: String,
    val progressPercent: Int
)

data class NavigationState(
    val destination: String,
    val remainingMinutes: Int,
    val distanceKm: Double
)

data class DashboardState(
    val serverTimestamp: String,
    val speedKmh: Double,
    val batteryPercent: Int,
    val outsideTemperatureC: Double,
    val drivingStatus: String,
    val media: MediaState,
    val navigation: NavigationState
)

enum class SyncAction {
    NONE,
    TOGGLE_PLAYBACK
}

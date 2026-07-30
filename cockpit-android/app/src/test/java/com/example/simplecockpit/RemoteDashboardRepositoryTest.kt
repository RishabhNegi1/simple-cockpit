package com.example.simplecockpit

import com.example.simplecockpit.data.DashboardState
import com.example.simplecockpit.data.MediaState
import com.example.simplecockpit.data.NavigationState
import com.example.simplecockpit.data.SyncAction
import com.example.simplecockpit.data.remote.DashboardApi
import com.example.simplecockpit.data.remote.DashboardSyncRequest
import com.example.simplecockpit.data.remote.RemoteDashboardRepository
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteDashboardRepositoryTest {
    @Test
    fun `sync maps none action and returns API response`() = runBlocking {
        val expected = dashboardState()
        val api = RecordingDashboardApi(expected)
        val repository = RemoteDashboardRepository(api)

        val actual = repository.sync(SyncAction.NONE)

        assertEquals(expected, actual)
        val request = requireNotNull(api.lastRequest)
        assertEquals("NONE", request.action)
        assertEquals(request.clientTimestamp, Instant.parse(request.clientTimestamp).toString())
    }

    @Test
    fun `sync maps playback action to API value`() = runBlocking {
        val api = RecordingDashboardApi(dashboardState())
        val repository = RemoteDashboardRepository(api)

        repository.sync(SyncAction.TOGGLE_PLAYBACK)

        assertEquals("TOGGLE_PLAYBACK", api.lastRequest?.action)
    }

    private fun dashboardState() = DashboardState(
        serverTimestamp = "2026-07-31T10:00:00Z",
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

    private class RecordingDashboardApi(
        private val response: DashboardState
    ) : DashboardApi {
        var lastRequest: DashboardSyncRequest? = null
            private set

        override suspend fun syncDashboard(request: DashboardSyncRequest): DashboardState {
            lastRequest = request
            return response
        }
    }
}
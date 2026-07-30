package com.example.simplecockpit.data.remote

import com.example.simplecockpit.data.DashboardRepository
import com.example.simplecockpit.data.DashboardState
import com.example.simplecockpit.data.SyncAction
import java.time.Instant

class RemoteDashboardRepository(
    private val dashboardApi: DashboardApi
) : DashboardRepository {
    override suspend fun sync(action: SyncAction): DashboardState {
        val request = DashboardSyncRequest(
            clientTimestamp = Instant.now().toString(),
            action = action.toApiValue()
        )
        return dashboardApi.syncDashboard(request)
    }

    private fun SyncAction.toApiValue(): String = when (this) {
        SyncAction.NONE -> "NONE"
        SyncAction.TOGGLE_PLAYBACK -> "TOGGLE_PLAYBACK"
    }
}

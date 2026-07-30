package com.example.simplecockpit.data.remote

import com.example.simplecockpit.data.DashboardState
import retrofit2.http.Body
import retrofit2.http.POST

interface DashboardApi {
    @POST("api/v1/dashboard/sync")
    suspend fun syncDashboard(
        @Body request: DashboardSyncRequest
    ): DashboardState
}

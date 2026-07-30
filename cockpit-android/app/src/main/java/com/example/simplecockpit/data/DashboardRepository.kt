package com.example.simplecockpit.data

interface DashboardRepository {
    suspend fun sync(action: SyncAction): DashboardState
}

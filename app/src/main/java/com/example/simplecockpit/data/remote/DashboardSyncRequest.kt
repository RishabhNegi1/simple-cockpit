package com.example.simplecockpit.data.remote

data class DashboardSyncRequest(
    val clientTimestamp: String,
    val action: String
)

package com.example.simplecockpit.ui.dashboard

import com.example.simplecockpit.data.DashboardState

data class DashboardUiState(
    val dashboard: DashboardState? = null,
    val isInitialLoading: Boolean = true,
    val isConnected: Boolean = false,
    val errorMessage: String? = null
)

package com.example.simplecockpit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simplecockpit.data.remote.NetworkProvider
import com.example.simplecockpit.data.remote.RemoteDashboardRepository
import com.example.simplecockpit.ui.dashboard.DashboardScreen
import com.example.simplecockpit.ui.dashboard.DashboardViewModel
import com.example.simplecockpit.ui.theme.SimpleCockpitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(
            this,
            DashboardViewModel.Factory(
                RemoteDashboardRepository(NetworkProvider.dashboardApi)
            )
        )[DashboardViewModel::class.java]

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            SimpleCockpitTheme {
                DashboardScreen(
                    uiState = uiState,
                    onTogglePlayback = viewModel::togglePlayback
                )
            }
        }
    }
}

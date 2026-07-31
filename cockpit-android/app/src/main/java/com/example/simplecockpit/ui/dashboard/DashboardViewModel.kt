package com.example.simplecockpit.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simplecockpit.data.DashboardRepository
import com.example.simplecockpit.data.SyncAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Don't let polling and button taps send requests at the same time.
    private val synchronizationMutex = Mutex()

    init {
        viewModelScope.launch {
            while (isActive) {
                synchronize(SyncAction.NONE)
                delay(POLL_INTERVAL_MILLIS)
            }
        }
    }

    fun togglePlayback() {
        viewModelScope.launch {
            synchronize(SyncAction.TOGGLE_PLAYBACK)
        }
    }

    private suspend fun synchronize(action: SyncAction) {
        synchronizationMutex.withLock {
            try {
                val dashboard = repository.sync(action)
                _uiState.value = DashboardUiState(
                    dashboard = dashboard,
                    isInitialLoading = false,
                    isConnected = true,
                    errorMessage = null
                )
            } catch (exception: Exception) {
                if (exception is CancellationException) throw exception

                // Leave the old values on screen if a refresh fails.
                _uiState.update { currentState ->
                    currentState.copy(
                        isInitialLoading = false,
                        isConnected = false,
                        errorMessage = exception.message
                            ?.take(MAX_ERROR_LENGTH)
                            ?: "Unable to refresh dashboard"
                    )
                }
            }
        }
    }

    class Factory(
        private val repository: DashboardRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }

            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
    }

    private companion object {
        const val POLL_INTERVAL_MILLIS = 1_000L
        const val MAX_ERROR_LENGTH = 80
    }
}

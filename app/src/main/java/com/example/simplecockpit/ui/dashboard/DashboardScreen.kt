package com.example.simplecockpit.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplecockpit.R
import com.example.simplecockpit.data.DashboardState
import com.example.simplecockpit.data.MediaState
import com.example.simplecockpit.data.NavigationState
import com.example.simplecockpit.ui.theme.SimpleCockpitTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            uiState.dashboard == null && uiState.isInitialLoading -> InitialLoading()
            uiState.dashboard == null -> InitialConnectionError(uiState.errorMessage)
            else -> {
                val dashboard = uiState.dashboard
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .padding(24.dp)
                ) {
                    if (!uiState.isConnected) {
                        ConnectionBanner()
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        DrivingDataPanel(
                            dashboard = dashboard,
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(3f)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(2f),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            MediaPanel(
                                media = dashboard.media,
                                onTogglePlayback = onTogglePlayback,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                            NavigationPanel(
                                navigation = dashboard.navigation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrivingDataPanel(
    dashboard: DashboardState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        ) {
            StatusLabel(status = dashboard.drivingStatus)

            // Speed gets the strongest visual priority because it must be readable at a glance.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dashboard.speedKmh.roundToInt().toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 132.sp,
                    lineHeight = 132.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1
                )
                Text(
                    text = "km/h",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                BatteryIndicator(
                    batteryPercent = dashboard.batteryPercent,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Outside",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%.1f \u00B0C",
                            dashboard.outsideTemperatureC
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryIndicator(
    batteryPercent: Int,
    modifier: Modifier = Modifier
) {
    val safeBatteryPercent = batteryPercent.coerceIn(0, 100)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Battery",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "$safeBatteryPercent%",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { safeBatteryPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun MediaPanel(
    media: MediaState,
    onTogglePlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Media",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = media.trackName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                IconButton(
                    onClick = onTogglePlayback,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(
                            if (media.isPlaying) R.drawable.ic_pause
                            else R.drawable.ic_play
                        ),
                        contentDescription = if (media.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(34.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { media.progressPercent.coerceIn(0, 100) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${media.progressPercent.coerceIn(0, 100)}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationPanel(
    navigation: NavigationState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Navigation",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = navigation.destination,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                NavigationMetric(
                    value = "${navigation.remainingMinutes.coerceAtLeast(0)} min",
                    label = "Remaining"
                )
                NavigationMetric(
                    value = String.format(
                        Locale.getDefault(),
                        "%.1f km",
                        navigation.distanceKm.coerceAtLeast(0.0)
                    ),
                    label = "Distance"
                )
            }
        }
    }
}

@Composable
fun ConnectionBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = "Connection lost \u2013 showing last data",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InitialLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(52.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Connecting\u2026",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun InitialConnectionError(errorMessage: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage ?: "Unable to connect",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Retrying automatically",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatusLabel(status: String) {
    val statusColor = when (status.uppercase(Locale.ROOT)) {
        "DRIVING" -> Color(0xFF4FD18B)
        "CHARGING" -> Color(0xFF59A7FF)
        else -> Color(0xFF9BA3AE)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = statusColor.copy(alpha = 0.14f)
    ) {
        Text(
            text = status.uppercase(Locale.ROOT),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = statusColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NavigationMetric(
    value: String,
    label: String
) {
    Column {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(
    widthDp = 1280,
    heightDp = 800,
    showBackground = true
)
@Composable
private fun DashboardPreview() {
    SimpleCockpitTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                dashboard = DashboardState(
                    serverTimestamp = "2026-07-30T10:00:00Z",
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
                ),
                isInitialLoading = false,
                isConnected = true
            ),
            onTogglePlayback = {}
        )
    }
}

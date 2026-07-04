package com.indusjs.fleet.locationtracker.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indusjs.fleet.locationtracker.data.TrackerConfig
import com.indusjs.fleet.locationtracker.data.TrackingStatus
import com.indusjs.fleet.locationtracker.ui.theme.LocationTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationTrackerTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsState()
    val trackingStatus by viewModel.trackingStatus.collectAsState()
    val showConfigDialog by viewModel.showConfigDialog.collectAsState()

    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasBackgroundLocationPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }

    // Permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        // Background location will be requested separately after foreground permission is granted
    }

    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasBackgroundLocationPermission = granted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    // Check permissions on launch
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        hasBackgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        // Request permissions if not granted
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fleet Location Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { viewModel.showConfig() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!config.isConfigured) {
                        Toast.makeText(context, "Please configure settings first", Toast.LENGTH_SHORT).show()
                        viewModel.showConfig()
                    } else if (!hasLocationPermission) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else if (trackingStatus.isTracking) {
                        viewModel.stopTracking()
                    } else {
                        viewModel.startTracking()
                    }
                },
                containerColor = if (trackingStatus.isTracking)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    if (trackingStatus.isTracking) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = if (trackingStatus.isTracking) "Stop" else "Start"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Card
            val connectionState by viewModel.connectionState.collectAsState()
            StatusCard(config, trackingStatus, hasLocationPermission, connectionState)

            Spacer(modifier = Modifier.height(16.dp))

            // Configuration Card
            ConfigurationCard(config)

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Card
            PermissionsCard(
                hasLocationPermission = hasLocationPermission,
                hasBackgroundLocationPermission = hasBackgroundLocationPermission,
                hasNotificationPermission = hasNotificationPermission,
                onRequestLocationPermission = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onRequestBackgroundPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundLocationPermissionLauncher.launch(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    }
                },
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    }
                }
            )

            // Error display
            trackingStatus.lastError?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }

    // Configuration Dialog
    if (showConfigDialog) {
        ConfigurationDialog(
            config = config,
            onDismiss = { viewModel.hideConfig() },
            onSave = { viewModel.saveConfig(it) }
        )
    }
}

@Composable
fun StatusCard(
    config: TrackerConfig,
    trackingStatus: TrackingStatus,
    hasLocationPermission: Boolean,
    connectionState: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                trackingStatus.isTracking && trackingStatus.isMqttConnected ->
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                trackingStatus.isTracking ->
                    Color(0xFFFF9800).copy(alpha = 0.1f)
                else ->
                    MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = when {
                    trackingStatus.isTracking && trackingStatus.isMqttConnected -> Color(0xFF4CAF50)
                    trackingStatus.isTracking -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    !config.isConfigured -> "Not Configured"
                    !hasLocationPermission -> "Location Permission Required"
                    trackingStatus.isTracking && trackingStatus.isMqttConnected -> "Tracking Active"
                    trackingStatus.isTracking -> "Tracking ($connectionState)"
                    else -> "Tracking Stopped"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (trackingStatus.isTracking) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MQTT: $connectionState",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (connectionState) {
                        "Connected" -> Color(0xFF4CAF50)
                        "Disconnected" -> Color.Gray
                        else -> Color(0xFFFF9800)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Messages Published: ${trackingStatus.messagesPublished}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ConfigurationCard(config: TrackerConfig) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            ConfigRow("Registration No.", config.registrationNumber.ifBlank { "Not set" })
            ConfigRow("Driver ID", config.driverId?.toString() ?: "Not set")
            ConfigRow("Trip ID", config.tripId?.toString() ?: "No active trip")
            ConfigRow("MQTT Broker", config.mqttBrokerUrl.ifBlank { "Not set" })
            ConfigRow("Update Interval", "${config.updateIntervalMs / 1000}s")

            if (config.isConfigured) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Topic: ${config.getMqttTopic()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PermissionsCard(
    hasLocationPermission: Boolean,
    hasBackgroundLocationPermission: Boolean,
    hasNotificationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onRequestBackgroundPermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            PermissionRow(
                label = "Location",
                granted = hasLocationPermission,
                onRequest = onRequestLocationPermission
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionRow(
                    label = "Background Location",
                    granted = hasBackgroundLocationPermission,
                    onRequest = onRequestBackgroundPermission
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionRow(
                    label = "Notifications",
                    granted = hasNotificationPermission,
                    onRequest = onRequestNotificationPermission
                )
            }
        }
    }
}

@Composable
fun PermissionRow(
    label: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (granted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (granted) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }

        if (!granted) {
            TextButton(onClick = onRequest) {
                Text("Grant")
            }
        }
    }
}

@Composable
fun ConfigurationDialog(
    config: TrackerConfig,
    onDismiss: () -> Unit,
    onSave: (TrackerConfig) -> Unit
) {
    var registrationNumber by remember { mutableStateOf(config.registrationNumber) }
    var driverId by remember { mutableStateOf(config.driverId?.toString() ?: "") }
    var tripId by remember { mutableStateOf(config.tripId?.toString() ?: "") }
    var mqttBrokerUrl by remember { mutableStateOf(config.mqttBrokerUrl) }
    var mqttClientId by remember { mutableStateOf(config.mqttClientId) }
    var mqttUsername by remember { mutableStateOf(config.mqttUsername) }
    var mqttPassword by remember { mutableStateOf(config.mqttPassword) }
    var updateInterval by remember { mutableStateOf((config.updateIntervalMs / 1000).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configuration") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = { registrationNumber = it.uppercase() },
                    label = { Text("Registration Number *") },
                    placeholder = { Text("MH12AB1234") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = driverId,
                    onValueChange = { driverId = it },
                    label = { Text("Driver ID (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tripId,
                    onValueChange = { tripId = it },
                    label = { Text("Trip ID (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MQTT Settings",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mqttBrokerUrl,
                    onValueChange = { mqttBrokerUrl = it },
                    label = { Text("MQTT Broker URL *") },
                    placeholder = { Text("tcp://192.168.1.4:1883") },
                    supportingText = { Text("Use port 1883 for MQTT (not 18083 which is the dashboard)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mqttClientId,
                    onValueChange = { mqttClientId = it },
                    label = { Text("Client ID (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mqttUsername,
                    onValueChange = { mqttUsername = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mqttPassword,
                    onValueChange = { mqttPassword = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = updateInterval,
                    onValueChange = { updateInterval = it },
                    label = { Text("Update Interval (seconds)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newConfig = TrackerConfig(
                        registrationNumber = registrationNumber.uppercase().trim(),
                        driverId = driverId.toLongOrNull(),
                        tripId = tripId.toLongOrNull(),
                        mqttBrokerUrl = mqttBrokerUrl,
                        mqttClientId = mqttClientId,
                        mqttUsername = mqttUsername,
                        mqttPassword = mqttPassword,
                        updateIntervalMs = (updateInterval.toLongOrNull() ?: 2) * 1000,
                        isTrackingEnabled = config.isTrackingEnabled
                    )
                    onSave(newConfig)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


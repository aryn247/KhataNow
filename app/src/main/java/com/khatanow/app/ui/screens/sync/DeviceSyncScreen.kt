package com.khatanow.app.ui.screens.sync

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khatanow.app.domain.sync.P2PTransportManager
import com.khatanow.app.domain.sync.SyncState
import com.khatanow.app.ui.MainViewModel
import com.khatanow.app.ui.theme.GreenPrimary

@Composable
fun DeviceSyncScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val p2pManager = remember { P2PTransportManager(context) }
    val syncState by p2pManager.syncState.collectAsState()

    DisposableEffect(Unit) {
        onDispose { p2pManager.stopAll() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Device Sync (Boss / Employee)",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Offline device-to-device shop data synchronization",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Sync Action Card with High Contrast & Unclipped Buttons
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Synchronize Shop Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Merge credit entries recorded on Boss and Employee phones directly via Wi-Fi/Bluetooth.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { p2pManager.startAdvertising() },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Text("Boss Mode (Advertise)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { p2pManager.startDiscovery() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                    ) {
                        Text("Employee Mode (Search)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Current Status Card
        Text(
            text = "Sync Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (val state = syncState) {
                    is SyncState.Idle -> {
                        Text("Status: Ready to Sync", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Select Boss Mode or Employee Mode above.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    is SyncState.Advertising -> {
                        Text("Status: Boss Device Advertising...", fontWeight = FontWeight.Bold, color = GreenPrimary)
                        Text("Waiting for employee device to search and connect.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    is SyncState.Discovering -> {
                        Text("Status: Searching for Boss Device...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("Bring phone close to Boss phone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    is SyncState.DeviceDiscovered -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Found Device: ${state.deviceName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Tap Sync to connect", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Button(
                                onClick = {
                                    p2pManager.connectToDevice(state.endpointId)
                                    p2pManager.initiateSyncRequest(state.endpointId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Text("Sync Now")
                            }
                        }
                    }
                    is SyncState.Transferring -> {
                        Text("Status: Exchanging & Merging Data...", fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                    is SyncState.Completed -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(state.message, fontWeight = FontWeight.Bold, color = GreenPrimary)
                        }
                    }
                    is SyncState.Error -> {
                        Text("⚠️ ${state.message}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                    else -> {}
                }
            }
        }
    }

    // 2-Sided Confirmation Modal Dialog
    if (syncState is SyncState.SyncRequested) {
        val req = syncState as SyncState.SyncRequested
        AlertDialog(
            onDismissRequest = { p2pManager.declineSyncRequest(req.endpointId) },
            title = { Text("Two-Sided Sync Request") },
            text = {
                Column {
                    Text(
                        text = "📱 ${req.requesterName} wants to synchronize shop credit data.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This will exchange and merge new customer, product, and credit entries between both phones.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { p2pManager.acceptSyncRequest(req.endpointId) },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("✓ ACCEPT & MERGE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { p2pManager.declineSyncRequest(req.endpointId) }) {
                    Text("Decline")
                }
            }
        )
    }
}

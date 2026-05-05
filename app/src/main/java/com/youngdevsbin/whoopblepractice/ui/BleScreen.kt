package com.youngdevsbin.whoopblepractice.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.youngdevsbin.whoopblepractice.domain.BleConnectionState

/**
 * Stateless Composable — UI 만 담당.
 * State 와 event handler 만 인자로 받고 ViewModel 직접 참조 X.
 *
 * [면접 단골 — Compose]
 * Q: "왜 ViewModel 을 Composable 안에서 viewModel() 로 받지 않고 인자로 주입?"
 * A: 1) Preview 가능 (mock state 주입)
 *    2) 단방향 dataflow 명확
 *    3) Composable 단위 unit test 쉬움
 */
@Composable
fun BleScreen(
    state: BleViewModel.UiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("WHOOP BLE Practice", style = MaterialTheme.typography.headlineSmall)

        ConnectionStatus(state.connection)

        state.lastNotification?.let {
            Text("Last notification: $it", style = MaterialTheme.typography.bodyMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onStartScan,
                enabled = !state.isScanning,
            ) { Text("Scan") }
            OutlinedButton(
                onClick = onStopScan,
                enabled = state.isScanning,
            ) { Text("Stop") }
            OutlinedButton(onClick = onDisconnect) { Text("Disconnect") }
        }

        HorizontalDivider()

        Text("Found devices (${state.devices.size})", style = MaterialTheme.typography.titleSmall)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items = state.devices, key = { it.address }) { device ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(device.name ?: "Unknown", style = MaterialTheme.typography.titleMedium)
                        Text(device.address, style = MaterialTheme.typography.bodySmall)
                        Text("RSSI ${device.rssi} dBm", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Button(onClick = { onConnect(device.address) }) {
                            Text("Connect")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatus(connection: BleConnectionState) {
    val text = when (connection) {
        BleConnectionState.Idle -> "Idle"
        BleConnectionState.Scanning -> "Scanning…"
        is BleConnectionState.Connecting -> "Connecting to ${connection.deviceAddress}"
        is BleConnectionState.Connected -> "✅ Connected: ${connection.deviceAddress}"
        is BleConnectionState.Disconnected -> "Disconnected (${connection.reason})"
        is BleConnectionState.Failed -> "❌ Failed: ${connection.message} [${connection.statusCode}]"
    }
    Text(text, style = MaterialTheme.typography.bodyLarge)
}

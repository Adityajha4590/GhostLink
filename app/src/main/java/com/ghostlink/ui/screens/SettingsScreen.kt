package com.ghostlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ghostlink.ui.theme.AlertRed
import com.ghostlink.ui.theme.ElectricGreen
import com.ghostlink.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(context) }

    var profileNameInput by remember { mutableStateOf(viewModel.profileName.value) }
    var showPanicConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    val autoDeleteOptions = listOf("Off", "24h", "7 days", "30 days")
    val beaconOptions = listOf("Fast", "Normal", "Stealth")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Profile Section ──
            item {
                SettingsSection("👤 Profile") {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Local Display Name", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = profileNameInput,
                            onValueChange = { profileNameInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Your name (only stored locally)") }
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.saveProfileName(profileNameInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricGreen)
                        ) {
                            Text("Save Name", color = MaterialTheme.colorScheme.background)
                        }
                    }
                }
            }

            // ── Security Section ──
            item {
                SettingsSection("🔐 Security") {
                    SettingsDropdown(
                        "Auto-delete messages",
                        viewModel.autoDeleteSetting.value,
                        autoDeleteOptions
                    ) { viewModel.setAutoDelete(it) }

                    SettingsToggle("Failed unlock wipe", viewModel.failedUnlockWipe.value) {
                        viewModel.toggleFailedUnlockWipe(it)
                    }

                    Divider(color = MaterialTheme.colorScheme.background)

                    TextButton(
                        onClick = { showPanicConfirm = true },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("🔴 Panic Wipe — Delete ALL keys & messages", color = AlertRed)
                    }
                }
            }

            // ── Network Section ──
            item {
                SettingsSection("📡 Network") {
                    SettingsItem("Transport mode", "Auto (Wi-Fi Direct → BLE)")
                    SettingsDropdown(
                        "Discovery beacon interval",
                        viewModel.beaconInterval.value,
                        beaconOptions
                    ) { viewModel.setBeaconInterval(it) }
                }
            }

            // ── Privacy Section ──
            item {
                SettingsSection("🕵️ Privacy") {
                    SettingsToggle("Cover traffic (dummy packets)", viewModel.coverTrafficEnabled.value) {
                        viewModel.toggleCoverTraffic(it)
                    }
                    SettingsToggle("Metadata wipe on close", viewModel.metadataWipeOnClose.value) {
                        viewModel.toggleMetadataWipe(it)
                    }
                }
            }

            // ── Emergency Section ──
            item {
                SettingsSection("🆘 Emergency") {
                    TextButton(onClick = { showResetConfirm = true }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text("Reset Identity (loses all contacts)", color = MaterialTheme.colorScheme.error)
                    }
                    TextButton(onClick = { showBackupDialog = true }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text("Export Encrypted Backup", color = ElectricGreen)
                    }
                }
            }

            item {
                Spacer(Modifier.height(32.dp))
                Text(
                    "GhostLink v1.0 — Fully Offline. Decentralized.\nAll data stored locally. Zero servers.",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // ── Panic Wipe Confirmation Dialog ──
    if (showPanicConfirm) {
        AlertDialog(
            onDismissRequest = { showPanicConfirm = false },
            title = { Text("⚠ Panic Wipe") },
            text = { Text("This will permanently delete ALL keys, messages, and contact data. This cannot be undone. Are you sure?") },
            confirmButton = {
                TextButton(onClick = { viewModel.panicWipe(); showPanicConfirm = false; navController.navigate("splash") { popUpTo(0) } }) {
                    Text("DELETE EVERYTHING", color = AlertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPanicConfirm = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // ── Reset Identity Confirmation Dialog ──
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Identity?") },
            text = { Text("Your anonymous identity and all contacts will be permanently deleted. Messages will be erased.") },
            confirmButton = {
                TextButton(onClick = { viewModel.resetIdentity(); showResetConfirm = false; navController.navigate("splash") { popUpTo(0) } }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, color = ElectricGreen, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsItem(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = MaterialTheme.colorScheme.onBackground)
        Text(value, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedButton(onClick = { expanded = true }) { Text(selected, color = ElectricGreen) }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onBackground) },
                        onClick = { onSelect(option); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsToggle(title: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = MaterialTheme.colorScheme.onBackground)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = ElectricGreen, checkedTrackColor = ElectricGreen.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun SettingsButton(
    title: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(title, color = color)
    }
}

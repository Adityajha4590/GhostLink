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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ghostlink.ui.theme.AlertRed
import com.ghostlink.ui.theme.ElectricGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                SettingsSection("🔐 Security") {
                    SettingsButton("Panic Wipe", AlertRed) { /* Wipe DB & Keys */ }
                    SettingsItem("Auto-delete messages", "Off")
                    SettingsToggle("Failed unlock wipe", false)
                }
                
                SettingsSection("📡 Network") {
                    SettingsItem("Transport mode", "Auto (Wi-Fi Direct → BLE)")
                    SettingsItem("Discovery beacon interval", "Normal")
                }
                
                SettingsSection("🕵️ Privacy") {
                    SettingsToggle("Cover traffic (dummy packets)", true)
                    SettingsItem("Routing ID rotation", "Every session")
                    SettingsToggle("Metadata wipe on close", false)
                }

                SettingsSection("🆘 Emergency") {
                    SettingsButton("Reset Identity", MaterialTheme.colorScheme.error) { }
                    SettingsButton("Export encrypted backup", MaterialTheme.colorScheme.primary) { }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "GhostLink v1.0\nFully Offline. Decentralized.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(title, color = ElectricGreen, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Text(value, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
    }
}

@Composable
fun SettingsToggle(title: String, checked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Switch(
            checked = checked,
            onCheckedChange = { },
            colors = SwitchDefaults.colors(checkedThumbColor = ElectricGreen)
        )
    }
}

@Composable
fun SettingsButton(title: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(title, color = color)
    }
}

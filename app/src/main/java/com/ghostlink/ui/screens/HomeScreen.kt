package com.ghostlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ghostlink.ui.theme.ElectricGreen
import com.ghostlink.ui.theme.SteelBlue
import com.ghostlink.ui.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { HomeViewModel(context) }

    LaunchedEffect(Unit) { viewModel.loadContacts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GhostLink 🛡️", style = MaterialTheme.typography.bodyLarge) },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, "Settings", tint = SteelBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_contact") },
                containerColor = ElectricGreen,
                contentColor = MaterialTheme.colorScheme.background
            ) {
                Icon(Icons.Filled.Add, "New Chat")
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(icon = { Text("💬") }, label = { Text("Chats") }, selected = true, onClick = {})
                NavigationBarItem(icon = { Text("👥") }, label = { Text("Contacts") }, selected = false, onClick = {})
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
                Text(
                    "🟢 Offline Mode — No Internet Required",
                    modifier = Modifier.padding(8.dp),
                    color = ElectricGreen,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            if (viewModel.contacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("🔒", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No contacts yet.\nTap ＋ to add a contact by scanning their QR code.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.contacts, key = { it.id }) { contact ->
                        ContactRow(
                            name = contact.displayName,
                            onClick = { navController.navigate("chat/${contact.id}") }
                        )
                        Divider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactRow(name: String, onClick: () -> Unit) {
    // Generate a consistent avatar color from the name hash
    val avatarColor = Color(0xFF000000 or (name.hashCode().toLong() and 0xFFFFFF))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(name, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔒 Encrypted", color = ElectricGreen, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

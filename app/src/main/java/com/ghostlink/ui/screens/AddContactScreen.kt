package com.ghostlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ghostlink.ui.theme.ElectricGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("SCAN QR", "MY QR CODE")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Contact") },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = ElectricGreen
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (selectedTab == 0) {
                    Text(
                        "Point at your contact's QR code.\nOne scan is all you need.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .background(androidx.compose.ui.graphics.Color.White)
                        ) {
                            // QR Code Placeholder
                            Text("QR CODE HERE", modifier = Modifier.align(Alignment.Center), color = androidx.compose.ui.graphics.Color.Black)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Share this code with your contact in person",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Fingerprint: 8F2A...9B1C",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricGreen
                        )
                    }
                }
            }
        }
    }
}

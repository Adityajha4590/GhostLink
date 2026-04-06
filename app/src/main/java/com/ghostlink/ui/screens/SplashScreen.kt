package com.ghostlink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ghostlink.identity.IdentityManager
import com.ghostlink.ui.theme.ElectricGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val identityManager = remember { IdentityManager(context) }

    LaunchedEffect(key1 = true) {
        withContext(Dispatchers.IO) {
            identityManager.initializeIdentity() // Generate keys if not yet done
        }
        delay(1800)
        navController.navigate("home") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Placeholder for Animated Shield Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(ElectricGreen)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Your identity is created locally.\nNever shared. Never stored online.",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Generating secure identity...",
                color = ElectricGreen,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

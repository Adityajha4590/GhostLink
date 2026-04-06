package com.ghostlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ghostlink.ui.theme.GhostLinkTheme
import com.ghostlink.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GhostLinkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") { SplashScreen(navController) }
                        composable("home") { HomeScreen(navController) }
                        composable("add_contact") { AddContactScreen(navController) }
                        composable("chat/{contactId}") { backStackEntry ->
                            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                            ChatScreen(navController, contactId)
                        }
                        composable("settings") { SettingsScreen(navController) }
                    }
                }
            }
        }
    }
}

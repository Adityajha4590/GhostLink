package com.ghostlink.ui.screens

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ghostlink.identity.IdentityManager
import com.ghostlink.identity.QRCodeGenerator
import com.ghostlink.storage.EncryptedDB
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(navController: NavController) {
    val context = LocalContext.current
    val identityManager = remember { IdentityManager(context) }
    val db = remember { EncryptedDB(context, identityManager) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("SCAN QR", "MY QR CODE")

    val publicKey = remember { identityManager.getPublicKey() ?: ByteArray(0) }
    val qrBitmap: Bitmap? = remember(publicKey) {
        if (publicKey.isNotEmpty()) QRCodeGenerator.generateQRCode(publicKey) else null
    }
    val fingerprint = remember(publicKey) {
        if (publicKey.isNotEmpty()) {
            val hash = MessageDigest.getInstance("SHA-256").digest(publicKey)
            hash.take(4).joinToString("") { "%02X".format(it) }
        } else "------"
    }

    // State for after a successful contact QR scan
    var scannedPublicKey by remember { mutableStateOf<ByteArray?>(null) }
    var newContactName by remember { mutableStateOf("") }
    var scanError by remember { mutableStateOf<String?>(null) }

    // ZXing scanner launcher
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val parsed = QRCodeGenerator.parseQRPayload(result.contents)
            if (parsed != null) {
                scannedPublicKey = parsed
                scanError = null
            } else {
                scanError = "Invalid GhostLink QR code. Please scan again."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Contact") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            if (selectedTab == 0) {
                // ── SCAN TAB ──
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (scannedPublicKey == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text(
                                "Point at your contact's QR code.\nOne scan is all you need.",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    val opts = ScanOptions().apply {
                                        setPrompt("Scan GhostLink contact QR code")
                                        setBeepEnabled(false)
                                        setOrientationLocked(false)
                                    }
                                    scanLauncher.launch(opts)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("📷 Open Camera Scanner", color = MaterialTheme.colorScheme.background)
                            }
                            if (scanError != null) {
                                Spacer(Modifier.height(16.dp))
                                Text(scanError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else {
                        // Contact Confirmation Card
                        Surface(
                            modifier = Modifier.padding(24.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✅ Contact Scanned!", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(8.dp))
                                val scannedFp = scannedPublicKey!!.take(4).joinToString("") { "%02X".format(it) }
                                Text("Fingerprint: $scannedFp", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = newContactName,
                                    onValueChange = { newContactName = it },
                                    label = { Text("Give this contact a name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        if (newContactName.isNotBlank()) {
                                            val contactIdHash = MessageDigest.getInstance("SHA-256").digest(scannedPublicKey!!)
                                            val contactId = Base64.encodeToString(contactIdHash, Base64.NO_WRAP)
                                            val pubKeyB64 = Base64.encodeToString(scannedPublicKey, Base64.NO_WRAP)
                                            db.insertContact(contactId, newContactName, pubKeyB64)
                                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                                        }
                                    },
                                    enabled = newContactName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("✔ Verify & Save Contact", color = MaterialTheme.colorScheme.background)
                                }
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = { scannedPublicKey = null; newContactName = "" }) {
                                    Text("Scan Again", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            } else {
                // ── MY QR CODE TAB ──
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Your QR Code",
                                modifier = Modifier.size(240.dp)
                            )
                        } else {
                            Box(modifier = Modifier.size(240.dp).background(MaterialTheme.colorScheme.surface)) {
                                Text("Generating...", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Share this code with your contact in person",
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Fingerprint: $fingerprint",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

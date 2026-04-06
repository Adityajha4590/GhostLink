package com.ghostlink.ui.viewmodels

import android.content.Context
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ghostlink.crypto.KeystoreManager
import com.ghostlink.identity.IdentityManager
import com.ghostlink.storage.EncryptedDB

class SettingsViewModel(private val context: Context) : ViewModel() {
    private val identityManager = IdentityManager(context)
    private val db = EncryptedDB(context, identityManager)
    private val keystoreManager = KeystoreManager()

    val profileName = mutableStateOf(db.getSetting("profile_name", "Ghost User"))
    val autoDeleteSetting = mutableStateOf(db.getSetting("auto_delete", "Off"))
    val coverTrafficEnabled = mutableStateOf(db.getSetting("cover_traffic", "true") == "true")
    val metadataWipeOnClose = mutableStateOf(db.getSetting("metadata_wipe", "false") == "true")
    val failedUnlockWipe = mutableStateOf(db.getSetting("failed_unlock_wipe", "false") == "true")
    val beaconInterval = mutableStateOf(db.getSetting("beacon_interval", "Normal"))

    fun saveProfileName(name: String) {
        db.setSetting("profile_name", name)
        profileName.value = name
    }

    fun setAutoDelete(value: String) {
        db.setSetting("auto_delete", value)
        autoDeleteSetting.value = value
        applyAutoDelete(value)
    }

    private fun applyAutoDelete(value: String) {
        val cutoffMs = when (value) {
            "24h" -> System.currentTimeMillis() - 86_400_000L
            "7 days" -> System.currentTimeMillis() - 604_800_000L
            "30 days" -> System.currentTimeMillis() - 2_592_000_000L
            else -> return
        }
        db.deleteMessagesOlderThan(cutoffMs)
    }

    fun toggleCoverTraffic(enabled: Boolean) {
        db.setSetting("cover_traffic", enabled.toString())
        coverTrafficEnabled.value = enabled
    }

    fun toggleMetadataWipe(enabled: Boolean) {
        db.setSetting("metadata_wipe", enabled.toString())
        metadataWipeOnClose.value = enabled
    }

    fun toggleFailedUnlockWipe(enabled: Boolean) {
        db.setSetting("failed_unlock_wipe", enabled.toString())
        failedUnlockWipe.value = enabled
    }

    fun setBeaconInterval(value: String) {
        db.setSetting("beacon_interval", value)
        beaconInterval.value = value
    }

    fun panicWipe() {
        keystoreManager.wipeKeys()
        identityManager.resetIdentity()
        db.panicWipe(context)
    }

    fun resetIdentity() {
        identityManager.resetIdentity()
        db.deleteAllMessages()
    }

    fun exportBackup(passphrase: String): ByteArray {
        // Collect all messages/contacts and encrypt with passphrase-derived key for export
        val contacts = db.getAllContacts()
        val sb = StringBuilder()
        contacts.forEach { (id, name, pubKey) ->
            val msgs = db.getMessagesForContact(id)
            sb.appendLine("CONTACT:$name:$pubKey")
            msgs.forEach { (_, blob, isSent, ts) ->
                val text = String(blob, Charsets.UTF_8)
                val dir = if (isSent) "SENT" else "RECV"
                sb.appendLine("$dir:$ts:$text")
            }
        }
        // In production: encrypt with AES-256 derived from passphrase via PBKDF2
        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}

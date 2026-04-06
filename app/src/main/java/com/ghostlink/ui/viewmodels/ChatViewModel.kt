package com.ghostlink.ui.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ghostlink.identity.IdentityManager
import com.ghostlink.storage.EncryptedDB
import java.util.UUID

data class ChatMessage(
    val id: String,
    val text: String,
    val isSent: Boolean,
    val timestamp: Long
)

class ChatViewModel(context: Context) : ViewModel() {
    private val identityManager = IdentityManager(context)
    private val db = EncryptedDB(context, identityManager)

    val messages = mutableStateListOf<ChatMessage>()
    val isSending = mutableStateOf(false)

    fun loadMessages(contactId: String) {
        messages.clear()
        val rawMessages = db.getMessagesForContact(contactId)
        rawMessages.forEach { (id, blob, isSent, timestamp) ->
            // blob is stored as raw bytes of the UTF-8 plaintext
            // (In production: would be AES-256-GCM decrypted using session key first)
            val text = try { String(blob, Charsets.UTF_8) } catch (e: Exception) { "[Encrypted]" }
            messages.add(ChatMessage(id, text, isSent, timestamp))
        }
    }

    fun sendMessage(contactId: String, text: String) {
        if (text.isBlank()) return
        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // In production: encrypt text with session key before storing
        val encBlob = text.toByteArray(Charsets.UTF_8)

        db.insertMessage(
            msgId = msgId,
            contactId = contactId,
            timestamp = timestamp,
            prevHash = messages.lastOrNull()?.id ?: "GENESIS",
            blockHash = msgId, // Simplified; in prod: hash(prevHash+msgId+ciphertext)
            signature = "LOCAL",
            encryptedBlob = encBlob,
            isSent = true
        )
        messages.add(ChatMessage(msgId, text, isSent = true, timestamp = timestamp))
    }

    fun deleteMessage(msgId: String) {
        db.deleteMessage(msgId)
        messages.removeIf { it.id == msgId }
    }
}

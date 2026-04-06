package com.ghostlink.ui.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.ghostlink.identity.IdentityManager
import com.ghostlink.storage.EncryptedDB

data class Contact(val id: String, val displayName: String, val publicKey: String)

class HomeViewModel(context: Context) : ViewModel() {
    private val identityManager = IdentityManager(context)
    private val db = EncryptedDB(context, identityManager)

    val contacts = mutableStateListOf<Contact>()

    fun loadContacts() {
        contacts.clear()
        db.getAllContacts().forEach { (id, name, pubKey) ->
            contacts.add(Contact(id, name, pubKey))
        }
    }

    fun deleteContact(id: String) {
        db.deleteContact(id)
        contacts.removeIf { it.id == id }
    }
}

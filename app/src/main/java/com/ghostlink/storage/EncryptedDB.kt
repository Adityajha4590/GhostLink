package com.ghostlink.storage

import android.content.Context
import android.util.Base64
import com.ghostlink.identity.IdentityManager
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import java.security.MessageDigest

class EncryptedDB(context: Context, identityManager: IdentityManager) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dbPassword: ByteArray

    init {
        SQLiteDatabase.loadLibs(context)
        val rootKey = identityManager.getSecretKey() ?: ByteArray(32) { 0 }
        val digest = MessageDigest.getInstance("SHA-256")
        dbPassword = digest.digest(rootKey)
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "ghostlink_secure.db"

        // Contacts
        const val TABLE_CONTACTS = "contacts"
        const val COL_CONTACT_ID = "contact_id"
        const val COL_DISPLAY_NAME = "display_name"
        const val COL_PUB_KEY = "public_key"
        const val COL_TRUST_STATUS = "trust_status"
        const val COL_FIRST_SEEN = "first_seen"

        // Messages (Blockchain Ledger)
        const val TABLE_MESSAGES = "messages"
        const val COL_MSG_ID = "msg_id"
        const val COL_MSG_CONTACT_ID = "contact_id"
        const val COL_MSG_TIMESTAMP = "timestamp"
        const val COL_MSG_PREV_HASH = "previous_hash"
        const val COL_MSG_BLOCK_HASH = "block_hash"
        const val COL_MSG_SIGNATURE = "signature"
        const val COL_MSG_ENC_BLOB = "encrypted_blob"
        const val COL_MSG_IS_SENT = "is_sent"
        const val COL_MSG_IS_DELETED = "is_deleted"

        // Settings
        const val TABLE_SETTINGS = "settings"
        const val COL_SETTING_KEY = "key"
        const val COL_SETTING_VALUE = "value"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_CONTACTS (
                $COL_CONTACT_ID TEXT PRIMARY KEY,
                $COL_DISPLAY_NAME TEXT,
                $COL_PUB_KEY TEXT,
                $COL_TRUST_STATUS INTEGER DEFAULT 1,
                $COL_FIRST_SEEN INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_MESSAGES (
                $COL_MSG_ID TEXT PRIMARY KEY,
                $COL_MSG_CONTACT_ID TEXT,
                $COL_MSG_TIMESTAMP INTEGER,
                $COL_MSG_PREV_HASH TEXT,
                $COL_MSG_BLOCK_HASH TEXT,
                $COL_MSG_SIGNATURE TEXT,
                $COL_MSG_ENC_BLOB BLOB,
                $COL_MSG_IS_SENT INTEGER DEFAULT 1,
                $COL_MSG_IS_DELETED INTEGER DEFAULT 0,
                FOREIGN KEY($COL_MSG_CONTACT_ID) REFERENCES $TABLE_CONTACTS($COL_CONTACT_ID)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_SETTINGS (
                $COL_SETTING_KEY TEXT PRIMARY KEY,
                $COL_SETTING_VALUE TEXT
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETTINGS")
        onCreate(db)
    }

    private fun getDbKey() = Base64.encodeToString(dbPassword, Base64.NO_WRAP)

    fun getWritable(): SQLiteDatabase = getWritableDatabase(getDbKey())
    fun getReadable(): SQLiteDatabase = getReadableDatabase(getDbKey())

    // ────── Contact Operations ──────
    fun insertContact(id: String, name: String, publicKeyB64: String) {
        val db = getWritable()
        val values = android.content.ContentValues().apply {
            put(COL_CONTACT_ID, id)
            put(COL_DISPLAY_NAME, name)
            put(COL_PUB_KEY, publicKeyB64)
            put(COL_TRUST_STATUS, 1)
            put(COL_FIRST_SEEN, System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_CONTACTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAllContacts(): List<Triple<String, String, String>> {
        val results = mutableListOf<Triple<String, String, String>>()
        val db = getReadable()
        val cursor = db.rawQuery("SELECT $COL_CONTACT_ID, $COL_DISPLAY_NAME, $COL_PUB_KEY FROM $TABLE_CONTACTS", null)
        while (cursor.moveToNext()) {
            results.add(Triple(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
        }
        cursor.close()
        return results
    }

    fun deleteContact(id: String) {
        val db = getWritable()
        db.delete(TABLE_CONTACTS, "$COL_CONTACT_ID = ?", arrayOf(id))
        db.delete(TABLE_MESSAGES, "$COL_MSG_CONTACT_ID = ?", arrayOf(id))
    }

    // ────── Message Operations ──────
    fun insertMessage(
        msgId: String, contactId: String, timestamp: Long,
        prevHash: String, blockHash: String, signature: String,
        encryptedBlob: ByteArray, isSent: Boolean
    ) {
        val db = getWritable()
        val values = android.content.ContentValues().apply {
            put(COL_MSG_ID, msgId)
            put(COL_MSG_CONTACT_ID, contactId)
            put(COL_MSG_TIMESTAMP, timestamp)
            put(COL_MSG_PREV_HASH, prevHash)
            put(COL_MSG_BLOCK_HASH, blockHash)
            put(COL_MSG_SIGNATURE, signature)
            put(COL_MSG_ENC_BLOB, encryptedBlob)
            put(COL_MSG_IS_SENT, if (isSent) 1 else 0)
            put(COL_MSG_IS_DELETED, 0)
        }
        db.insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    /** Returns list of (msgId, encryptedBlob, isSent, timestamp) */
    fun getMessagesForContact(contactId: String): List<Quadruple<String, ByteArray, Boolean, Long>> {
        val results = mutableListOf<Quadruple<String, ByteArray, Boolean, Long>>()
        val db = getReadable()
        val cursor = db.rawQuery(
            "SELECT $COL_MSG_ID, $COL_MSG_ENC_BLOB, $COL_MSG_IS_SENT, $COL_MSG_TIMESTAMP " +
            "FROM $TABLE_MESSAGES WHERE $COL_MSG_CONTACT_ID = ? AND $COL_MSG_IS_DELETED = 0 " +
            "ORDER BY $COL_MSG_TIMESTAMP ASC",
            arrayOf(contactId)
        )
        while (cursor.moveToNext()) {
            results.add(Quadruple(cursor.getString(0), cursor.getBlob(1), cursor.getInt(2) == 1, cursor.getLong(3)))
        }
        cursor.close()
        return results
    }

    fun deleteMessage(msgId: String) {
        val db = getWritable()
        val values = android.content.ContentValues().apply { put(COL_MSG_IS_DELETED, 1) }
        db.update(TABLE_MESSAGES, values, "$COL_MSG_ID = ?", arrayOf(msgId))
    }

    fun deleteAllMessages() {
        val db = getWritable()
        db.delete(TABLE_MESSAGES, null, null)
    }

    fun deleteMessagesOlderThan(cutoffMs: Long) {
        val db = getWritable()
        db.delete(TABLE_MESSAGES, "$COL_MSG_TIMESTAMP < ?", arrayOf(cutoffMs.toString()))
    }

    // ────── Settings Operations ──────
    fun getSetting(key: String, default: String = ""): String {
        val db = getReadable()
        val cursor = db.rawQuery("SELECT $COL_SETTING_VALUE FROM $TABLE_SETTINGS WHERE $COL_SETTING_KEY = ?", arrayOf(key))
        val result = if (cursor.moveToFirst()) cursor.getString(0) else default
        cursor.close()
        return result
    }

    fun setSetting(key: String, value: String) {
        val db = getWritable()
        val values = android.content.ContentValues().apply {
            put(COL_SETTING_KEY, key)
            put(COL_SETTING_VALUE, value)
        }
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ────── Panic Wipe ──────
    fun panicWipe(context: Context) {
        close()
        context.deleteDatabase(DATABASE_NAME)
    }
}

// Simple Quadruple data class since Kotlin doesn't have one built-in
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

package com.ghostlink.storage

import android.content.Context
import android.util.Base64
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import com.ghostlink.identity.IdentityManager
import java.security.MessageDigest

class EncryptedDB(context: Context, identityManager: IdentityManager) : 
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val dbPassword: ByteArray

    init {
        SQLiteDatabase.loadLibs(context)
        // Derive DB password from root secret key so it never exists independently
        val rootKey = identityManager.getSecretKey() ?: ByteArray(32) { 0 }
        val digest = MessageDigest.getInstance("SHA-256")
        dbPassword = digest.digest(rootKey)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ghostlink_secure.db"

        // Contacts Table
        // We only store the hash of the contact's public key (contact_id)
        // and the encrypted public key/fingerprint.
        const val TABLE_CONTACTS = "contacts"
        const val COL_CONTACT_ID = "contact_id"
        const val COL_DISPLAY_NAME = "display_name"
        const val COL_PUB_KEY = "public_key"
        const val COL_TRUST_STATUS = "trust_status"

        // Messages Table (Blockchain Ledger)
        // Messages are stored as cryptographically linked blocks
        const val TABLE_MESSAGES = "messages"
        const val COL_MSG_ID = "msg_id" // Maps to Block Index or Message ID
        const val COL_MSG_CONTACT_ID = "contact_id"
        const val COL_MSG_TIMESTAMP = "timestamp"
        const val COL_MSG_PREV_HASH = "previous_hash"
        const val COL_MSG_BLOCK_HASH = "block_hash"
        const val COL_MSG_SIGNATURE = "signature"
        const val COL_MSG_ENC_BLOB = "encrypted_blob" // The AES encrypted message payload
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createContacts = """
            CREATE TABLE $TABLE_CONTACTS (
                $COL_CONTACT_ID TEXT PRIMARY KEY,
                $COL_DISPLAY_NAME TEXT,
                $COL_PUB_KEY TEXT,
                $COL_TRUST_STATUS INTEGER
            )
        """.trimIndent()
        
        val createMessages = """
            CREATE TABLE $TABLE_MESSAGES (
                $COL_MSG_ID TEXT PRIMARY KEY,
                $COL_MSG_CONTACT_ID TEXT,
                $COL_MSG_TIMESTAMP INTEGER,
                $COL_MSG_PREV_HASH TEXT,
                $COL_MSG_BLOCK_HASH TEXT,
                $COL_MSG_SIGNATURE TEXT,
                $COL_MSG_ENC_BLOB BLOB
            )
        """.trimIndent()

        db.execSQL(createContacts)
        db.execSQL(createMessages)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    fun getWritable(): SQLiteDatabase {
        return getWritableDatabase(Base64.encodeToString(dbPassword, Base64.NO_WRAP))
    }
    
    fun getReadable(): SQLiteDatabase {
        return getReadableDatabase(Base64.encodeToString(dbPassword, Base64.NO_WRAP))
    }
    
    // Panic wipe permanently destroys the DB file from disk
    fun panicWipe(context: Context) {
        close()
        context.deleteDatabase(DATABASE_NAME)
    }
}

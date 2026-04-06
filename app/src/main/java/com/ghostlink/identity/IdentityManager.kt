package com.ghostlink.identity

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.ghostlink.crypto.KeystoreManager
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Sign
import java.security.MessageDigest

class IdentityManager(private val context: Context) {
    private val lazySodium = LazySodiumAndroid(SodiumAndroid())
    private val keystoreManager = KeystoreManager()
    private val prefs: SharedPreferences = context.getSharedPreferences("ghostlink_identity", Context.MODE_PRIVATE)

    fun initializeIdentity() {
        if (!prefs.contains("public_key")) {
            val keyPair = lazySodium.cryptoSignKeypair()
            val publicKey = keyPair.publicKey.asBytes
            val secretKey = keyPair.secretKey.asBytes

            // Secure private key via Keystore AES wrapper
            val encryptedSecret = keystoreManager.encryptKey(secretKey)

            // UserID = SHA-256(PublicKey)
            val digest = MessageDigest.getInstance("SHA-256")
            val userIdHash = digest.digest(publicKey)

            prefs.edit().apply {
                putString("public_key", Base64.encodeToString(publicKey, Base64.NO_WRAP))
                putString("secret_key_enc", Base64.encodeToString(encryptedSecret.first, Base64.NO_WRAP))
                putString("secret_key_iv", Base64.encodeToString(encryptedSecret.second, Base64.NO_WRAP))
                putString("user_id", Base64.encodeToString(userIdHash, Base64.NO_WRAP))
                apply()
            }
        }
    }

    fun getPublicKey(): ByteArray? {
        return prefs.getString("public_key", null)?.let { Base64.decode(it, Base64.NO_WRAP) }
    }

    fun getSecretKey(): ByteArray? {
        val enc = prefs.getString("secret_key_enc", null)
        val iv = prefs.getString("secret_key_iv", null)
        if (enc != null && iv != null) {
            return try {
                val encBytes = Base64.decode(enc, Base64.NO_WRAP)
                val ivBytes = Base64.decode(iv, Base64.NO_WRAP)
                keystoreManager.decryptKey(encBytes, ivBytes)
            } catch (e: Exception) { null }
        }
        return null
    }

    fun getUserId(): String? = prefs.getString("user_id", null)

    fun signMessage(message: ByteArray): ByteArray? {
        val secretKey = getSecretKey() ?: return null
        val signature = ByteArray(Sign.BYTES)
        val success = lazySodium.cryptoSignDetached(signature, message, message.size.toLong(), secretKey)
        return if (success) signature else null
    }

    fun resetIdentity() {
        keystoreManager.wipeKeys()
        prefs.edit().clear().apply()
    }
}

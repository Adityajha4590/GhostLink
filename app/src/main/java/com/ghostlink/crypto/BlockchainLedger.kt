package com.ghostlink.crypto

import android.util.Base64
import java.security.MessageDigest

data class MessageBlock(
    val index: Long,
    val timestamp: Long,
    val previousHash: String,
    val senderHash: String,
    val cipherText: String, // Base64 encoded AES-GCM local ciphertext
    val signature: String,  // Base64 encoded Ed25519 signature
    val blockHash: String
)

class BlockchainLedger {

    companion object {
        const val GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000"
    }

    /**
     * Calculates the SHA-256 hash for a given block's contents.
     */
    fun calculateBlockHash(
        index: Long,
        timestamp: Long,
        previousHash: String,
        senderHash: String,
        cipherText: String,
        signature: String
    ): String {
        val blockData = "$index:$timestamp:$previousHash:$senderHash:$cipherText:$signature"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(blockData.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Creates a new block linked to the previous blocks.
     */
    fun createBlock(
        previousBlock: MessageBlock?,
        senderHash: String,
        cipherText: String,
        signature: String
    ): MessageBlock {
        val index = if (previousBlock != null) previousBlock.index + 1 else 0L
        val previousHash = previousBlock?.blockHash ?: GENESIS_HASH
        val timestamp = System.currentTimeMillis()

        val blockHash = calculateBlockHash(
            index = index,
            timestamp = timestamp,
            previousHash = previousHash,
            senderHash = senderHash,
            cipherText = cipherText,
            signature = signature
        )

        return MessageBlock(
            index = index,
            timestamp = timestamp,
            previousHash = previousHash,
            senderHash = senderHash,
            cipherText = cipherText,
            signature = signature,
            blockHash = blockHash
        )
    }

    /**
     * Validates an entire conversation chain to detect database tampering.
     */
    fun isChainValid(chain: List<MessageBlock>): Boolean {
        if (chain.isEmpty()) return true

        for (i in 1 until chain.size) {
            val currentBlock = chain[i]
            val previousBlock = chain[i - 1]

            // 1. Check if hash matches calculated hash
            val recalculatedHash = calculateBlockHash(
                currentBlock.index,
                currentBlock.timestamp,
                currentBlock.previousHash,
                currentBlock.senderHash,
                currentBlock.cipherText,
                currentBlock.signature
            )
            
            if (currentBlock.blockHash != recalculatedHash) {
                return false // Block data was tampered with
            }

            // 2. Check if chain links perfectly
            if (currentBlock.previousHash != previousBlock.blockHash) {
                return false // Chain link broken
            }
        }
        return true
    }
}

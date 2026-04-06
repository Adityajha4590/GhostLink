package com.ghostlink.protocol

import java.nio.ByteBuffer
import java.security.MessageDigest

data class Fragment(
    val messageId: String,
    val fragmentId: Int,
    val totalFragments: Int,
    val chunkData: ByteArray,
    val checksum: ByteArray
)

class Fragmenter {
    companion object {
        const val FRAGMENT_SIZE = 1024 // 1KB fragments for BLE compatibility and mesh reliability
    }

    fun split(messageId: String, payload: ByteArray): List<Fragment> {
        val totalFragments = Math.ceil(payload.size.toDouble() / FRAGMENT_SIZE).toInt()
        val fragments = mutableListOf<Fragment>()
        
        val digest = MessageDigest.getInstance("SHA-256")

        for (i in 0 until totalFragments) {
            val start = i * FRAGMENT_SIZE
            val length = Math.min(FRAGMENT_SIZE, payload.size - start)
            val chunk = ByteArray(length)
            System.arraycopy(payload, start, chunk, 0, length)
            
            val hash = digest.digest(chunk)
            
            fragments.add(
                Fragment(
                    messageId = messageId,
                    fragmentId = i,
                    totalFragments = totalFragments,
                    chunkData = chunk,
                    checksum = hash
                )
            )
        }
        return fragments
    }

    // Reassembly logic
    private val assemblyBuffer = mutableMapOf<String, MutableMap<Int, Fragment>>()

    fun handleIncomingFragment(fragment: Fragment): ByteArray? {
        // Integrity check
        val digest = MessageDigest.getInstance("SHA-256")
        val computedHash = digest.digest(fragment.chunkData)
        if (!computedHash.contentEquals(fragment.checksum)) {
            return null // Corrupted
        }

        val messageBuffer = assemblyBuffer.getOrPut(fragment.messageId) { mutableMapOf() }
        messageBuffer[fragment.fragmentId] = fragment

        if (messageBuffer.size == fragment.totalFragments) {
            // Reassemble
            val totalSize = messageBuffer.values.sumOf { it.chunkData.size }
            val fullPayload = ByteBuffer.allocate(totalSize)
            
            for (i in 0 until fragment.totalFragments) {
                fullPayload.put(messageBuffer[i]!!.chunkData)
            }
            
            assemblyBuffer.remove(fragment.messageId)
            return fullPayload.array()
        }
        
        return null // Still waiting for fragments
    }
}

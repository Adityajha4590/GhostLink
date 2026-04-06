package com.ghostlink.protocol

import java.nio.ByteBuffer
import java.util.UUID

class PacketBuilder {
    fun buildPayload(
        blockIndex: Long,
        timestamp: Long,
        previousHash: String,
        senderHash: String,
        blockHash: String,
        signature: String,
        cipherText: ByteArray
    ): ByteArray {
        val prevHashBytes = previousHash.toByteArray()
        val senderBytes = senderHash.toByteArray()
        val blockHashBytes = blockHash.toByteArray()
        val sigBytes = signature.toByteArray()
        
        val buffer = ByteBuffer.allocate(
            8 + 8 + 1 + prevHashBytes.size + 1 + senderBytes.size + 1 + blockHashBytes.size + 1 + sigBytes.size + 4 + cipherText.size
        )
        
        buffer.putLong(blockIndex)
        buffer.putLong(timestamp)
        
        buffer.put(prevHashBytes.size.toByte())
        buffer.put(prevHashBytes)
        
        buffer.put(senderBytes.size.toByte())
        buffer.put(senderBytes)

        buffer.put(blockHashBytes.size.toByte())
        buffer.put(blockHashBytes)

        buffer.put(sigBytes.size.toByte())
        buffer.put(sigBytes)
        
        buffer.putInt(cipherText.size)
        buffer.put(cipherText)
        
        return buffer.array()
    }
}

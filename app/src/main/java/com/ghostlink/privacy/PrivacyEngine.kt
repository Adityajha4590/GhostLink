package com.ghostlink.privacy

import java.security.SecureRandom
import kotlinx.coroutines.*

class PrivacyEngine {
    private val random = SecureRandom()
    private var coverTrafficJob: Job? = null
    
    companion object {
        const val STANDARD_PACKET_SIZE = 4096 // 4KB padded blocks
    }

    // Pads a packet to a fixed size to hide the length of the original message
    fun padPacket(originalPacket: ByteArray): ByteArray {
        if (originalPacket.size >= STANDARD_PACKET_SIZE) return originalPacket

        val padded = ByteArray(STANDARD_PACKET_SIZE)
        System.arraycopy(originalPacket, 0, padded, 0, originalPacket.size)
        
        // Fill the rest with random data to prevent compression/analysis attacks
        val padding = ByteArray(STANDARD_PACKET_SIZE - originalPacket.size)
        random.nextBytes(padding)
        System.arraycopy(padding, 0, padded, originalPacket.size, padding.size)
        
        return padded
    }

    // Generates indistinguishable dummy packets
    fun generateDummyPacket(): ByteArray {
        val dummy = ByteArray(STANDARD_PACKET_SIZE)
        random.nextBytes(dummy)
        return dummy
    }

    fun startCoverTraffic(coroutineScope: CoroutineScope, onEmit: (ByteArray) -> Unit) {
        coverTrafficJob?.cancel()
        coverTrafficJob = coroutineScope.launch {
            while (isActive) {
                // Random intervals to prevent timing correlation
                delay(1000L + random.nextInt(4000))
                onEmit(generateDummyPacket())
            }
        }
    }

    fun stopCoverTraffic() {
        coverTrafficJob?.cancel()
    }
}

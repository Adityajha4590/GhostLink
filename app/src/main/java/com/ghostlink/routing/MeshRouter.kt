package com.ghostlink.routing

import java.util.concurrent.ConcurrentHashMap

data class MeshPacket(
    val messageId: String,
    val ttl: Int,
    val encryptedPayload: ByteArray // The content
)

class MeshRouter {
    // MessageID -> Timestamp mapping to prevent duplicates and circular routing
    private val seenMessages = ConcurrentHashMap<String, Long>()
    
    // Store-and-forward buffer
    private val storeAndForwardBuffer = mutableListOf<MeshPacket>()

    fun onPacketReceived(packet: MeshPacket) {
        val now = System.currentTimeMillis()
        
        // Anti-replay and circular routing prevention
        if (seenMessages.containsKey(packet.messageId)) {
            val seenTime = seenMessages[packet.messageId]!!
            if (now - seenTime < 3600000) { // 1 hour memory
                return // Drop duplicate
            }
        }
        
        seenMessages[packet.messageId] = now
        
        // TTL Check
        if (packet.ttl <= 1) {
            return // TTL expired, drop packet
        }
        
        val decrementedPacket = packet.copy(ttl = packet.ttl - 1)
        
        // Check if intended for us (IdentityManager check) -> hand to Protocol layer
        // Otherwise, add to relay buffer
        storeAndForwardBuffer.add(decrementedPacket)
    }

    fun getPacketsForRelay(): List<MeshPacket> {
        return storeAndForwardBuffer.toList()
    }
    
    fun removeRelayedPacket(messageId: String) {
        storeAndForwardBuffer.removeAll { it.messageId == messageId }
    }
}

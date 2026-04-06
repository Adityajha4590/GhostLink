package com.ghostlink.network

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class TransportManager(private val context: Context) {
    private val wifiBuilder = WiFiDirectManager(context)
    private val bleManager = BluetoothManager(context)
    
    private val _incomingPackets = MutableSharedFlow<ByteArray>(replay = 10)
    val incomingPackets: SharedFlow<ByteArray> = _incomingPackets

    fun startDiscovery() {
        // Priority: Wi-Fi Direct -> BLE
        wifiBuilder.startDiscovery()
        bleManager.startAdvertising()
    }

    suspend fun sendPacket(destinationHash: ByteArray, packet: ByteArray): Boolean {
        // Assume packet is already AES-256-GCM encrypted by protocol layer
        // Attempt Wi-Fi direct
        var success = wifiBuilder.sendFast(destinationHash, packet)
        if (!success) {
            // BLE Fallback
            success = bleManager.sendBLE(destinationHash, packet)
        }
        return success
    }
}

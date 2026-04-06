package com.ghostlink.identity

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import android.util.Base64

object QRCodeGenerator {
    fun generateQRCode(publicKey: ByteArray, size: Int = 512): Bitmap {
        val publicKeyBase64 = Base64.encodeToString(publicKey, Base64.NO_WRAP)
        val payload = "ghostlink://$publicKeyBase64"

        val hints = hashMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 1

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size, hints)

        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    fun parseQRPayload(raw: String): ByteArray? {
        return if (raw.startsWith("ghostlink://")) {
            try {
                val b64 = raw.removePrefix("ghostlink://")
                Base64.decode(b64, Base64.NO_WRAP)
            } catch (e: Exception) {
                null
            }
        } else null
    }
}

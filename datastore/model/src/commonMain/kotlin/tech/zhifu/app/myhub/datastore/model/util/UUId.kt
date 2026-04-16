package tech.zhifu.app.myhub.datastore.model.util

import kotlin.random.Random

fun generateUUId(): String {
    val bytes = ByteArray(16)
    Random.nextBytes(bytes)
    // RFC 4122 version 4
    bytes[6] = ((bytes[6].toInt() and 0x0F) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3F) or 0x80).toByte()
    return bytes.toUuidString()
}

private fun ByteArray.toUuidString(): String {
    val hexChars = "0123456789abcdef"
    val out = StringBuilder(36)
    forEachIndexed { index, byte ->
        val value = byte.toInt() and 0xFF
        out.append(hexChars[value ushr 4])
        out.append(hexChars[value and 0x0F])
        when (index) {
            3, 5, 7, 9 -> out.append('-')
        }
    }
    return out.toString()
}

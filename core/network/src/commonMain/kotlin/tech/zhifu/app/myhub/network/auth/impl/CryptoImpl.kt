package tech.zhifu.app.myhub.network.auth.impl

import kotlin.random.Random

/**
 * 加密实现
 * 使用简单的 XOR 加密（适合 MVP 阶段）
 * 注意：生产环境应该使用更安全的加密算法（如 AES）
 */
internal class CryptoImpl : Crypto {
    companion object {
        private const val KEY_SIZE = 32 // 256 bits
    }

    override fun generateKey(): ByteArray {
        return Random.Default.nextBytes(KEY_SIZE)
    }

    override fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        // 简单的 XOR 加密
        // 注意：这只是 MVP 阶段的简单实现，生产环境应该使用 AES 等更安全的算法
        val result = ByteArray(data.size)
        for (i in data.indices) {
            result[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return result
    }

    override fun decrypt(encryptedData: ByteArray, key: ByteArray): ByteArray {
        // XOR 加密的解密就是再次 XOR
        return encrypt(encryptedData, key)
    }
}

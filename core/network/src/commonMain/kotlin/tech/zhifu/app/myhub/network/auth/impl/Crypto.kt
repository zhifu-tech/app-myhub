package tech.zhifu.app.myhub.network.auth.impl

/**
 * 加密接口
 * 用于加密和解密敏感数据（如 Token）
 */
interface Crypto {
    /**
     * 生成加密密钥
     */
    fun generateKey(): ByteArray

    /**
     * 加密数据
     */
    fun encrypt(data: ByteArray, key: ByteArray): ByteArray

    /**
     * 解密数据
     */
    fun decrypt(encryptedData: ByteArray, key: ByteArray): ByteArray
}

package tech.zhifu.app.myhub.network.auth.impl

import com.soywiz.krypto.encoding.Base64
import io.ktor.client.plugins.auth.providers.BearerTokens
import tech.zhifu.app.myhub.network.auth.TokenStorage
import tech.zhifu.app.myhub.settings.LocalSettingStore

/**
 * 加密 Token 存储实现
 * 使用加密存储 Access Token 和 Refresh Token
 */
internal class SecureTokenStorage(
    private val localStore: LocalSettingStore,
    private val crypto: Crypto
) : TokenStorage {
    companion object {
        private const val KEY_ACCESS_TOKEN = "auth.access_token"
        private const val KEY_REFRESH_TOKEN = "auth.refresh_token"
        private const val KEY_ENCRYPTION_KEY = "auth.encryption_key"
    }

    private suspend fun getOrCreateEncryptionKey(): ByteArray {
        val storedKey = localStore.get(KEY_ENCRYPTION_KEY)
        return if (storedKey != null) {
            Base64.decode(storedKey)
        } else {
            val key = crypto.generateKey()
            localStore.set(KEY_ENCRYPTION_KEY, Base64.encode(key))
            key
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        val key = getOrCreateEncryptionKey()
        val encryptedAccess = crypto.encrypt(accessToken.encodeToByteArray(), key)
        val encryptedRefresh = crypto.encrypt(refreshToken.encodeToByteArray(), key)
        localStore.set(KEY_ACCESS_TOKEN, Base64.encode(encryptedAccess))
        localStore.set(KEY_REFRESH_TOKEN, Base64.encode(encryptedRefresh))
    }

    override suspend fun getAccessToken(): String? {
        val encryptedAccess = localStore.get(KEY_ACCESS_TOKEN) ?: return null
        return try {
            val key = localStore.get(KEY_ENCRYPTION_KEY)?.let { Base64.decode(it) }
                ?: return null
            val decrypted = crypto.decrypt(Base64.decode(encryptedAccess), key)
            decrypted.decodeToString()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getRefreshToken(): String? {
        val encryptedRefresh = localStore.get(KEY_REFRESH_TOKEN) ?: return null
        return try {
            val key = localStore.get(KEY_ENCRYPTION_KEY)?.let { Base64.decode(it) }
                ?: return null
            val decrypted = crypto.decrypt(Base64.decode(encryptedRefresh), key)
            decrypted.decodeToString()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun clearTokens() {
        localStore.remove(KEY_ACCESS_TOKEN)
        localStore.remove(KEY_REFRESH_TOKEN)
        // 注意：不清除加密密钥
    }

    // 同步方法，用于 Ktor Auth 插件的 loadTokens 回调
    override fun getBearerTokens(): BearerTokens? {
        val encryptedAccess = localStore.getSync(KEY_ACCESS_TOKEN)
        val encryptedRefresh = localStore.getSync(KEY_REFRESH_TOKEN)
        return if (encryptedAccess != null && encryptedRefresh != null) {
            try {
                val key = localStore.getSync(KEY_ENCRYPTION_KEY)?.let { Base64.decode(it) }
                    ?: return null
                val accessToken = crypto.decrypt(Base64.decode(encryptedAccess), key).decodeToString()
                val refreshToken = crypto.decrypt(Base64.decode(encryptedRefresh), key).decodeToString()
                BearerTokens(accessToken, refreshToken)
            } catch (e: Exception) {
                // 解密失败，清除 token
                // 注意：这里不能调用 suspend 函数，所以不能直接调用 clearTokens()
                null
            }
        } else {
            null
        }
    }
}

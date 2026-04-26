package tech.zhifu.app.myhub.datastore.file.storage

import io.github.vinceglb.filekit.absolutePath
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

private const val NATIVE_HANDLE_PREFIX = "nh1:"

private val nativeStorageJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal fun normalizeKey(
    key: String,
): String = key.trim().trim('/')

internal fun encodeNativeStorageHandle(
    handle: NativeStorageHandle,
): String = NATIVE_HANDLE_PREFIX + Base64.UrlSafe.encode(
    source = nativeStorageJson
        .encodeToString(
            serializer = NativeStorageHandle.serializer(),
            value = handle
        )
        .encodeToByteArray()
)

internal fun decodeNativeStorageHandle(
    raw: String,
): NativeStorageHandle? {
    if (!raw.startsWith(NATIVE_HANDLE_PREFIX)) return null
    return runCatching {
        val payload = raw.removePrefix(NATIVE_HANDLE_PREFIX)
        val jsonText = Base64.UrlSafe.decode(payload).decodeToString()
        nativeStorageJson.decodeFromString(
            deserializer = NativeStorageHandle.serializer(),
            string = jsonText
        )
    }.getOrNull()
}

internal fun nativePrivateKeyFromAccessUrl(
    accessUrl: String,
): String? {
    val normalized = accessUrl.removePrefix("file://").trim()
    if (normalized.isBlank()) return null
    val rootPath = platformManagedAppDataDir().absolutePath().trimEnd('/')
    val withSlash = "$rootPath/"
    return when {
        normalized == rootPath -> ""
        normalized.startsWith(withSlash) -> normalized.removePrefix(withSlash)
        else -> null
    }?.trim('/')?.takeIf { it.isNotBlank() }
}

package tech.zhifu.app.myhub.datastore.file.storage

import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

private const val WEB_HANDLE_PREFIX = "wh2:"

internal val webStorageJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal fun normalizeWebStorageKey(
    key: String,
): String = key.trim().trim('/')

internal fun encodeWebStorageHandle(
    handle: WebStorageHandle,
): String = WEB_HANDLE_PREFIX + Base64.UrlSafe.encode(
    source = webStorageJson
        .encodeToString(
            serializer = WebStorageHandle.serializer(),
            value = handle
        )
        .encodeToByteArray()
)

internal fun decodeWebStorageHandle(
    raw: String,
): WebStorageHandle? {
    if (!raw.startsWith(WEB_HANDLE_PREFIX)) return null
    return runCatching {
        val payload = raw.removePrefix(WEB_HANDLE_PREFIX)
        val jsonText = Base64.UrlSafe.decode(payload).decodeToString()
        webStorageJson.decodeFromString(
            deserializer = WebStorageHandle.serializer(),
            string = jsonText
        )
    }.getOrNull()
}

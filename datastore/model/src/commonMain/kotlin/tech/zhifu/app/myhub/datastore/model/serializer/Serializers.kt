package tech.zhifu.app.myhub.datastore.model.serializer

import kotlinx.serialization.json.Json

val json by lazy {
    Json {
        encodeDefaults = true
        explicitNulls = false
        ignoreUnknownKeys = true
        isLenient = true
    }
}

inline fun <reified T> String?.deserialize(): Result<T?> {
    this ?: return Result.success(null)
    return runCatching {
        json.decodeFromString(this)
    }
}

inline fun <reified T> T?.serialize(): String? {
    this ?: return null
    return runCatching {
        json.encodeToString(this)
    }.getOrNull()
}

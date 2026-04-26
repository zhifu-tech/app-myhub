package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlin.time.Instant

@Serializable
data class Card(
    val id: String,
    val type: CardType,
    val status: CardStatus,
    val title: String,
    val summary: String,

    val version: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,

    internal val locationRaw: String?,
    internal val tagsRaw: String?,
    internal val sourceRaw: String?,
) {
    @Transient
    private var map: Map<String, Any> = emptyMap()

    internal fun <T> getFromMap(
        key: String,
        raw: String?,
        deserializer: DeserializationStrategy<T>,
    ): T? = getFromMap(key) {
        raw ?: return@getFromMap null
        Json.decodeFromString(deserializer = deserializer, string = raw)
    }

    @Suppress("UNCHECKED_CAST")
    internal inline fun <T> getFromMap(
        key: String,
        crossinline factory: Card.() -> T?
    ): T? {
        val value = map.getOrElse(key) {
            val newValue = runCatching(block = factory).getOrNull() ?: NOE
            if (map.isEmpty()) {
                map = mutableMapOf()
            }
            (map as MutableMap<String, Any>)[key] = newValue
            newValue
        }
        if (value === NOE) return null
        return value as? T
    }

    companion object {
        internal object NOE // NULL OR EMPTY
    }
}

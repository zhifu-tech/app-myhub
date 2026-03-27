package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CardContent(
    val type: CardContentType,
    val value: String? = null,
    val ref: String? = null,
    val language: String? = null,
    val items: List<String> = emptyList(),
    val meta: JsonObject? = null,
)

@Serializable
data class CardContentType(
    val value: String,
) {
    companion object {
        val TEXT = CardContentType("text")
        val QUOTE = CardContentType("quote")
        val LINK = CardContentType("link")
        val CODE = CardContentType("code")
        val LIST = CardContentType("list")
        val IMAGE = CardContentType("image")
    }
}

val Card.content: CardContent?
    get() = getFromMap(
        key = "card.content",
        raw = contentRaw,
        deserializer = CardContent.serializer()
    )

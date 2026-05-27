package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CardSource(
    val kind: CardSourceKind,
    val ref: String? = null,
    val meta: JsonObject? = null,
)

@Serializable
data class CardSourceKind(
    val value: String,
) {
    companion object {
        val MANUAL = CardSourceKind("manual")
        val LINK = CardSourceKind("link")
        val SHARE = CardSourceKind("share")
        val IMPORT = CardSourceKind("import")
        val EXTRACT = CardSourceKind("extract")
    }
}

val Card.source: CardSource?
    get() = getFromMap(
        key = "card.source",
        raw = sourceRaw,
        deserializer = CardSource.serializer()
    )

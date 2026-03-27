package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CardRelation(
    val type: CardRelationType,
    val cardId: String,
    val meta: JsonObject? = null,
)

@Serializable
data class CardRelationType(
    val value: String,
) {
    companion object {
        val RELATED = CardRelationType("related")
        val REFERENCE = CardRelationType("reference")
        val MENTION = CardRelationType("mention")
        val PARENT = CardRelationType("parent")
    }
}

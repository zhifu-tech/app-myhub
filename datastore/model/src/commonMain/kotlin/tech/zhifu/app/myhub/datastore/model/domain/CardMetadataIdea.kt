package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

const val CARD_TYPE_IDEA = "idea"

val Card.isIdea: Boolean
    get() = type == CARD_TYPE_IDEA

val String.isIdea: Boolean
    get() = this == CARD_TYPE_IDEA

val Card.ideaMetadata: CardMetadataIdea?
    get() = metadata as? CardMetadataIdea

@Serializable
data class CardMetadataIdea(
    val cardId: String,
    val priority: String? = null,
    val status: String? = null
) : CardMetadata

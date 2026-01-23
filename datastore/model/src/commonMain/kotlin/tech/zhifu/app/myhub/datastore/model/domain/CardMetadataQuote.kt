package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

const val CARD_TYPE_QUOTE = "quote"

val Card.isQuote: Boolean
    get() = type == CARD_TYPE_QUOTE

val String.isQuote: Boolean
    get() = this == CARD_TYPE_QUOTE

val Card.quoteMetadata: CardMetadataQuote?
    get() = metadata as? CardMetadataQuote

@Serializable
data class CardMetadataQuote(
    val cardId: String,
    val author: String? = null,
    val category: String? = null,
    val source: String? = null
) : CardMetadata

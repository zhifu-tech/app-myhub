package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

const val CARD_TYPE_ARTICLE = "article"

val Card.isArticle: Boolean
    get() = type == CARD_TYPE_ARTICLE

val String.isArticle: Boolean
    get() = this == CARD_TYPE_ARTICLE

val Card.articleMetadata: CardMetadataArticle?
    get() = metadata as? CardMetadataArticle

@Serializable
data class CardMetadataArticle(
    val cardId: String,
    val url: String,
    val summary: String,
    val coverImageUrl: String,
    val author: String,
) : CardMetadata

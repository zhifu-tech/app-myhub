package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

const val CARD_TYPE_WORD = "word"

val Card.isWord: Boolean
    get() = type == CARD_TYPE_WORD

val String.isWord: Boolean
    get() = this == CARD_TYPE_WORD

val Card.wordMetadata: CardMetadataWord?
    get() = metadata as? CardMetadataWord

@Serializable
data class CardMetadataWord(
    val cardId: String,
    val pronunciation: String? = null,
    val definition: String,
    val example: String? = null
) : CardMetadata

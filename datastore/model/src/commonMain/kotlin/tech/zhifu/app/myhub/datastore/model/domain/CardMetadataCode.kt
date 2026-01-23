package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

const val CARD_TYPE_CODE = "code"

val Card.isCode: Boolean
    get() = type == CARD_TYPE_CODE

val String.isCode: Boolean
    get() = this == CARD_TYPE_CODE

val Card.codeMetadata: CardMetadataCode?
    get() = metadata as? CardMetadataCode

@Serializable
data class CardMetadataCode(
    val cardId: String,
    val language: String,
    val snippet: String,
    val description: String? = null
) : CardMetadata

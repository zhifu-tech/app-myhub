package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

const val CARD_TYPE_TODO = "todo"

val Card.isTodo: Boolean
    get() = type == CARD_TYPE_TODO

val String.isTodo: Boolean
    get() = this == CARD_TYPE_TODO

val Card.todoMetadata: CardMetadataTodo?
    get() = metadata as? CardMetadataTodo

@Serializable
data class CardMetadataTodo(
    val cardId: String,
    val status: String = "pending",
    val priority: String? = null,
    val dueAt: Instant? = null,
    val completedAt: Instant? = null
) : CardMetadata

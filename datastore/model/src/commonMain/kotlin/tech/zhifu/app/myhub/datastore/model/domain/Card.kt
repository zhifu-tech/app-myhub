package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

interface CardMetadata

typealias CardType = String

@Serializable
data class Card(
    val id: String,
    val type: CardType,
    val title: String? = null,
    val content: String,
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant,

    internal val metadata: CardMetadata? = null,
    val tags: List<Tag> = emptyList(),
)


val Card.isFavorite: Boolean
    get() = false

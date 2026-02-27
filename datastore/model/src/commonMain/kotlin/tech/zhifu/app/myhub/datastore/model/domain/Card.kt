package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Card(
    val id: String,
    val type: CardType,
    val source: CardSource,
    val carriers: String,
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val metadata: List<CardMetadata> = emptyList(),
    val tags: List<Tag> = emptyList(),
)

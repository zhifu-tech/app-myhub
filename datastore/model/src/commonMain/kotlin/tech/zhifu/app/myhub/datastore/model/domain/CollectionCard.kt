package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CollectionCard(
    val collectionId: String,
    val cardId: String,
    val createdAt: Instant
)

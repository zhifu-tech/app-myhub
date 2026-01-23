package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserCard(
    val userId: String,
    val cardId: String,
    val isFavorite: Boolean = false,
    val lastReviewedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

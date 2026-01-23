package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserStatistics(
    val userId: String,
    val totalCards: Int = 0,
    val favoriteCards: Int = 0,
    val recentEdits: Int = 0,
    val lastSyncAt: Instant? = null,
    val updatedAt: Instant
)

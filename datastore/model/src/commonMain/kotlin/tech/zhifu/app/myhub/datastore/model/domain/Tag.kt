package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val color: String? = null,
    val description: String? = null,
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val cardCount: Int = 0
)

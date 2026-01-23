package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Collection(
    val id: String,
    val name: String,
    val topic: String? = null,
    val description: String? = null,
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

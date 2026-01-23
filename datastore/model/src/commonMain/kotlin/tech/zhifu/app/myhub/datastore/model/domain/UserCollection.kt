package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserCollection(
    val userId: String,
    val collectionId: String,
    val role: String = "owner",
    val createdAt: Instant
)

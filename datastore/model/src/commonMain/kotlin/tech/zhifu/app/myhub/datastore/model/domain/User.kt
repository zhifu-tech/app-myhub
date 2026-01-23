package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val avatarText: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: String,
    val lastLoginAt: Instant,
)

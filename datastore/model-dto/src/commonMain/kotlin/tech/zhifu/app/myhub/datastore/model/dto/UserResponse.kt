package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 用户响应 DTO
 */
@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val avatarText: String,
    val status: String,
    val createdAt: String, // ISO 8601 format
    val updatedAt: String,
    val lastLoginAt: String // ISO 8601 format
)

package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 卡片响应 DTO
 */
@Serializable
data class CardResponse(
    val id: String,
    val type: String,
    val title: String? = null,
    val content: String,
    val userId: String,
    val tags: List<TagResponse> = emptyList(),
    val createdAt: String, // ISO 8601 format
    val updatedAt: String
)

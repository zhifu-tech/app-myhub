package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 卡集响应 DTO
 */
@Serializable
data class CollectionResponse(
    val id: String,
    val name: String,
    val topic: String? = null,
    val description: String? = null,
    val userId: String,
    val createdAt: String, // ISO 8601 format
    val updatedAt: String
)

package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 卡片模板响应 DTO
 */
@Serializable
data class CardTemplateResponse(
    val id: String,
    val type: String,
    val title: String? = null,
    val content: String? = null,
    val description: String? = null,
    val createdAt: String // ISO 8601 format
)

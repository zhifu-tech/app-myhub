package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 标签响应 DTO
 */
@Serializable
data class TagResponse(
    val id: String,
    val name: String,
    val color: String? = null
)

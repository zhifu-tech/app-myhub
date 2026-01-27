package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 部分更新卡片请求 DTO
 */
@Serializable
data class PartialUpdateCardRequest(
    val type: String? = null,
    val title: String? = null,
    val content: String? = null,
    val tagIds: List<String>? = null
) {
    fun validate() {
        content?.let {
            require(it.isNotBlank()) { "Content cannot be blank" }
            require(it.length <= 10000) { "Content cannot exceed 10000 characters" }
        }
        title?.let {
            require(it.length <= 200) { "Title cannot exceed 200 characters" }
        }
    }
}

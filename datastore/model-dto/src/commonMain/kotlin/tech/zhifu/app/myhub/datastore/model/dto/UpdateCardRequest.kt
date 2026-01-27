package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 更新卡片请求 DTO（完整更新）
 */
@Serializable
data class UpdateCardRequest(
    val type: String,
    val title: String? = null,
    val content: String,
    val tagIds: List<String> = emptyList()
) {
    fun validate() {
        require(content.isNotBlank()) { "Content cannot be blank" }
        require(content.length <= 10000) { "Content cannot exceed 10000 characters" }
        title?.let {
            require(it.length <= 200) { "Title cannot exceed 200 characters" }
        }
    }
}

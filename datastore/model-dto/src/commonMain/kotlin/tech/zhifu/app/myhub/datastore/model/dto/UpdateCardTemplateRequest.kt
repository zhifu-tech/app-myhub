package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 更新卡片模板请求 DTO（完整更新）
 */
@Serializable
data class UpdateCardTemplateRequest(
    val type: String,
    val title: String? = null,
    val content: String? = null,
    val description: String? = null
) {
    fun validate() {
        require(type.isNotBlank()) { "Type cannot be blank" }
        title?.let {
            require(it.length <= 200) { "Title cannot exceed 200 characters" }
        }
        content?.let {
            require(it.length <= 10000) { "Content cannot exceed 10000 characters" }
        }
        description?.let {
            require(it.length <= 500) { "Description cannot exceed 500 characters" }
        }
    }
}

package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 创建标签请求 DTO
 */
@Serializable
data class CreateTagRequest(
    val name: String,
    val color: String? = null,
    val description: String? = null
) {
    fun validate() {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(name.length <= 50) { "Name cannot exceed 50 characters" }
        description?.let {
            require(it.length <= 200) { "Description cannot exceed 200 characters" }
        }
    }
}

package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 创建卡集请求 DTO
 */
@Serializable
data class CreateCollectionRequest(
    val name: String,
    val topic: String? = null,
    val description: String? = null
) {
    fun validate() {
        require(name.isNotBlank()) { "Name cannot be blank" }
        require(name.length <= 100) { "Name cannot exceed 100 characters" }
        topic?.let {
            require(it.length <= 100) { "Topic cannot exceed 100 characters" }
        }
        description?.let {
            require(it.length <= 500) { "Description cannot exceed 500 characters" }
        }
    }
}

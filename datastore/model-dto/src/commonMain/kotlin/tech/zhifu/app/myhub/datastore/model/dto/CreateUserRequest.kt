package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 创建用户请求 DTO
 */
@Serializable
data class CreateUserRequest(
    val username: String,
    val displayName: String,
    val avatarUrl: String = "",
    val avatarText: String = "",
    val status: String = "active"
) {
    fun validate() {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length <= 50) { "Username cannot exceed 50 characters" }
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(displayName.length <= 100) { "Display name cannot exceed 100 characters" }
    }
}

package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 更新用户请求 DTO（完整更新）
 */
@Serializable
data class UpdateUserRequest(
    val username: String,
    val displayName: String,
    val avatarUrl: String,
    val avatarText: String,
    val status: String
) {
    fun validate() {
        require(username.isNotBlank()) { "Username cannot be blank" }
        require(username.length <= 50) { "Username cannot exceed 50 characters" }
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(displayName.length <= 100) { "Display name cannot exceed 100 characters" }
    }
}

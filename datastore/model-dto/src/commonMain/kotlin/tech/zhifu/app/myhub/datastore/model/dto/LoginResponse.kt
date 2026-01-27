package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 登录响应 DTO
 */
@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long, // 秒
    val tokenType: String,
    val userId: String,
    val username: String
)

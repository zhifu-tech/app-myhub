package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 刷新 Token 响应 DTO
 */
@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val expiresIn: Long, // 秒
    val tokenType: String
)

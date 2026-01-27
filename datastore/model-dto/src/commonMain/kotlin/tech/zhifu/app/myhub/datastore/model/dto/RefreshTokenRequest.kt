package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 刷新 Token 请求 DTO
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

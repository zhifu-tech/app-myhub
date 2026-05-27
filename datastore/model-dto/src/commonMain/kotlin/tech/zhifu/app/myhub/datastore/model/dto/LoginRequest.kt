package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 登录请求 DTO
 * 支持客户端提供用户信息（本地匿名用户已有）
 */
@Serializable
data class LoginRequest(
    val userId: String,
    val username: String? = null, // 可选：客户端提供的用户名
    val displayName: String? = null, // 可选：客户端提供的显示名称
    val avatarUrl: String? = null, // 可选：客户端提供的头像URL
    val avatarText: String? = null // 可选：客户端提供的头像文本
)

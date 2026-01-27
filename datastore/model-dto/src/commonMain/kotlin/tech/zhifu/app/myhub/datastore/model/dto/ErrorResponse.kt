package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 错误响应 DTO
 */
@Serializable
data class ErrorResponse(
    val error: ErrorDetail
)

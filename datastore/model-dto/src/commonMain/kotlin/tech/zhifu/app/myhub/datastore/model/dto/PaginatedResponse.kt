package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 分页响应
 */
@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: PaginationInfo
)

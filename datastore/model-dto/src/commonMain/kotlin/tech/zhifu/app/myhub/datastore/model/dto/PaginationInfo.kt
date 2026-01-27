package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable

/**
 * 分页信息
 */
@Serializable
data class PaginationInfo(
    val page: Int,
    val limit: Int,
    val total: Long,
    val totalPages: Int
)

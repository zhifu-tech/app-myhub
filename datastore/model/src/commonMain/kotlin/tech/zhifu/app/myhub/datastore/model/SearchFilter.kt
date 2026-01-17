package tech.zhifu.app.myhub.datastore.model

import kotlinx.serialization.Serializable

/**
 * 搜索和筛选条件
 */
@Serializable
data class SearchFilter(
    val query: String? = null,
    val cardTypes: List<CardType> = emptyList(),
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean? = null,
    val isTemplate: Boolean? = null,
    val dateRange: DateRange? = null,
    val sortBy: SortBy = SortBy.UPDATED_AT_DESC
)

/**
 * 日期范围
 */
@Serializable
data class DateRange(
    val start: Long? = null, // Unix timestamp
    val end: Long? = null    // Unix timestamp
)

/**
 * 排序方式
 */
@Serializable
enum class SortBy {
    CREATED_AT_ASC,
    CREATED_AT_DESC,
    UPDATED_AT_ASC,
    UPDATED_AT_DESC,
    TITLE_ASC,
    TITLE_DESC,
    LAST_REVIEWED_AT_ASC,
    LAST_REVIEWED_AT_DESC
}


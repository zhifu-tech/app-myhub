package tech.zhifu.app.myhub.datastore.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * 卡片类型枚举
 */
@Serializable
enum class CardType {
    QUOTE,      // 引言卡片
    CODE,       // 代码片段
    IDEA,       // 想法
    ARTICLE,    // 文章
    DICTIONARY, // 字典
    CHECKLIST   // 待办清单
}

/**
 * 卡片实体 - 核心数据模型
 * 这是应用内部使用的领域模型
 */
@Serializable
data class Card(
    val id: String,
    val type: CardType,
    val title: String? = null,
    val content: String,
    val author: String? = null,
    val source: String? = null,
    val language: String? = null, // 用于代码卡片
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isTemplate: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastReviewedAt: Instant? = null,
    val metadata: CardMetadata? = null
)

/**
 * 卡片元数据 - 根据不同类型存储特定信息
 */
@Serializable
data class CardMetadata(
    // Quote 卡片
    val quoteAuthor: String? = null,
    val quoteCategory: String? = null, // 如 "Literature"

    // Code 卡片
    val codeLanguage: String? = null,
    val codeSnippet: String? = null,

    // Article 卡片
    val articleUrl: String? = null,
    val articleSummary: String? = null,
    val articleImageUrl: String? = null,

    // Dictionary 卡片
    val wordPronunciation: String? = null,
    val wordDefinition: String? = null,
    val wordExample: String? = null,

    // Checklist 卡片
    val checklistItems: List<ChecklistItem> = emptyList(),

    // Idea 卡片
    val ideaPriority: String? = null, // "high", "medium", "low"
    val ideaStatus: String? = null    // "new", "in-progress", "completed"
)

/**
 * 待办清单项
 */
@Serializable
data class ChecklistItem(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)

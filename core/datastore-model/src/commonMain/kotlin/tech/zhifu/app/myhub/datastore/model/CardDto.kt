package tech.zhifu.app.myhub.datastore.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * 卡片数据传输对象 - 用于API请求和响应
 */
@Serializable
data class CardDto(
    val id: String,
    val type: String, // 使用String便于API兼容
    val title: String? = null,
    val content: String,
    val author: String? = null,
    val source: String? = null,
    val language: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isTemplate: Boolean = false,
    val createdAt: String, // ISO 8601格式
    val updatedAt: String,
    val lastReviewedAt: String? = null,
    val metadata: CardMetadataDto? = null
)

/**
 * 卡片元数据DTO
 */
@Serializable
data class CardMetadataDto(
    val quoteAuthor: String? = null,
    val quoteCategory: String? = null,
    val codeLanguage: String? = null,
    val codeSnippet: String? = null,
    val articleUrl: String? = null,
    val articleSummary: String? = null,
    val articleImageUrl: String? = null,
    val wordPronunciation: String? = null,
    val wordDefinition: String? = null,
    val wordExample: String? = null,
    val checklistItems: List<ChecklistItemDto> = emptyList(),
    val ideaPriority: String? = null,
    val ideaStatus: String? = null
)

/**
 * 待办清单项DTO
 */
@Serializable
data class ChecklistItemDto(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)

/**
 * 创建卡片请求DTO
 */
@Serializable
data class CreateCardRequest(
    val type: String,
    val title: String? = null,
    val content: String,
    val author: String? = null,
    val source: String? = null,
    val language: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isTemplate: Boolean = false,
    val metadata: CardMetadataDto? = null
)

/**
 * 更新卡片请求DTO
 */
@Serializable
data class UpdateCardRequest(
    val title: String? = null,
    val content: String? = null,
    val author: String? = null,
    val source: String? = null,
    val language: String? = null,
    val tags: List<String>? = null,
    val isFavorite: Boolean? = null,
    val isTemplate: Boolean? = null,
    val metadata: CardMetadataDto? = null
)

/**
 * DTO转换扩展函数
 */
fun CreateCardRequest.toDomain(): Card {
    val now = kotlin.time.Clock.System.now()
    return Card(
        id = "", // 由 Repository 生成
        type = CardType.valueOf(type.uppercase()),
        title = title,
        content = content,
        author = author,
        source = source,
        language = language,
        tags = tags,
        isFavorite = isFavorite,
        isTemplate = isTemplate,
        createdAt = now,
        updatedAt = now,
        lastReviewedAt = null,
        metadata = metadata?.toDomain()
    )
}

fun CardDto.toDomain(): Card {
    return Card(
        id = id,
        type = CardType.valueOf(type.uppercase()),
        title = title,
        content = content,
        author = author,
        source = source,
        language = language,
        tags = tags,
        isFavorite = isFavorite,
        isTemplate = isTemplate,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        lastReviewedAt = lastReviewedAt?.let { Instant.parse(it) },
        metadata = metadata?.toDomain()
    )
}

fun Card.toDto(): CardDto {
    return CardDto(
        id = id,
        type = type.name.lowercase(),
        title = title,
        content = content,
        author = author,
        source = source,
        language = language,
        tags = tags,
        isFavorite = isFavorite,
        isTemplate = isTemplate,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        lastReviewedAt = lastReviewedAt?.toString(),
        metadata = metadata?.toDto()
    )
}

fun CardMetadataDto.toDomain(): CardMetadata {
    return CardMetadata(
        quoteAuthor = quoteAuthor,
        quoteCategory = quoteCategory,
        codeLanguage = codeLanguage,
        codeSnippet = codeSnippet,
        articleUrl = articleUrl,
        articleSummary = articleSummary,
        articleImageUrl = articleImageUrl,
        wordPronunciation = wordPronunciation,
        wordDefinition = wordDefinition,
        wordExample = wordExample,
        checklistItems = checklistItems.map { it.toDomain() },
        ideaPriority = ideaPriority,
        ideaStatus = ideaStatus
    )
}

fun CardMetadata.toDto(): CardMetadataDto {
    return CardMetadataDto(
        quoteAuthor = quoteAuthor,
        quoteCategory = quoteCategory,
        codeLanguage = codeLanguage,
        codeSnippet = codeSnippet,
        articleUrl = articleUrl,
        articleSummary = articleSummary,
        articleImageUrl = articleImageUrl,
        wordPronunciation = wordPronunciation,
        wordDefinition = wordDefinition,
        wordExample = wordExample,
        checklistItems = checklistItems.map { it.toDto() },
        ideaPriority = ideaPriority,
        ideaStatus = ideaStatus
    )
}

fun ChecklistItemDto.toDomain(): ChecklistItem {
    return ChecklistItem(
        id = id,
        text = text,
        isCompleted = isCompleted,
        order = order
    )
}

fun ChecklistItem.toDto(): ChecklistItemDto {
    return ChecklistItemDto(
        id = id,
        text = text,
        isCompleted = isCompleted,
        order = order
    )
}


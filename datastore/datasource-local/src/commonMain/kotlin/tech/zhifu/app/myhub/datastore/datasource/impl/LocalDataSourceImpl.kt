package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.database.SelectTagsByCardIds
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataArticle
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataCode
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataIdea
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataQuote
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataTodo
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataWord
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.model.domain.isArticle
import tech.zhifu.app.myhub.datastore.model.domain.isCode
import tech.zhifu.app.myhub.datastore.model.domain.isIdea
import tech.zhifu.app.myhub.datastore.model.domain.isQuote
import tech.zhifu.app.myhub.datastore.model.domain.isTodo
import tech.zhifu.app.myhub.datastore.model.domain.isWord
import kotlin.time.Clock
import kotlin.time.Instant

internal typealias DbUser = tech.zhifu.app.myhub.datastore.database.User
internal typealias DbCurrentUser = tech.zhifu.app.myhub.datastore.database.SelectCurrentUser
internal typealias DbUserPreferences = tech.zhifu.app.myhub.datastore.database.User_preferences
internal typealias DbTag = tech.zhifu.app.myhub.datastore.database.Tag
internal typealias DbCollection = tech.zhifu.app.myhub.datastore.database.Collection
internal typealias DbCard = tech.zhifu.app.myhub.datastore.database.Card_with_metadata_view
internal typealias DbCardTemplate = tech.zhifu.app.myhub.datastore.database.Card_template

private fun String?.toInstant(def: Instant = Clock.System.now()) =
    this?.let { Instant.parse(it) } ?: def

private fun Long?.toBoolean(def: Boolean = false) =
    this?.let { it != 0L } ?: def

internal fun DbCard.toDomain(
    tags: List<Tag> = emptyList()
) = Card(
    id = card_id,
    type = card_type,
    title = card_title,
    content = card_content,
    // fixme 删除
    userId = card_user_id,
    createdAt = card_created_at.toInstant(),
    updatedAt = card_updated_at.toInstant(),
    tags = tags,
    metadata = when {
        card_type.isArticle -> CardMetadataArticle(
            cardId = card_id,
            url = article_url.orEmpty(),
            summary = article_summary.orEmpty(),
            coverImageUrl = article_cover_image_url.orEmpty(),
            author = article_author.orEmpty()
        )

        card_type.isCode -> CardMetadataCode(
            cardId = card_id,
            language = code_language.orEmpty(),
            snippet = code_snippet.orEmpty(),
            description = code_description
        )

        card_type.isIdea -> CardMetadataIdea(
            cardId = card_id,
            priority = idea_priority,
            status = idea_status
        )

        card_type.isQuote -> CardMetadataQuote(
            cardId = card_id,
            author = quote_author,
            category = quote_category,
            source = quote_source
        )

        card_type.isTodo -> CardMetadataTodo(
            cardId = card_id,
            status = todo_status.orEmpty().ifBlank { "pending" },
            priority = todo_priority,
            dueAt = todo_due_at?.let { Instant.Companion.parse(it) },
            completedAt = todo_completed_at?.let { Instant.Companion.parse(it) }
        )

        card_type.isWord -> CardMetadataWord(
            cardId = card_id,
            pronunciation = word_pronunciation,
            definition = word_definition.orEmpty(),
            example = word_example
        )

        else -> null
    }
)

internal fun DbTag.toDomain() = Tag(
    id = id,
    name = name,
    color = color,
    description = description,
    userId = user_id,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
    cardCount = card_count.toInt()
)

internal fun SelectTagsByCardIds.toDomain() = Tag(
    id = id,
    name = name,
    color = color,
    description = description,
    userId = user_id,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
    cardCount = card_count.toInt()
)

fun DbCollection.toDomain() = Collection(
    id = id,
    name = name,
    topic = topic,
    description = description,
    userId = user_id,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant()
)

fun DbUser.toDomain() = User(
    id = id,
    username = username,
    displayName = display_name.orEmpty(),
    avatarUrl = avatar_url.orEmpty(),
    avatarText = avatar_text.orEmpty(),
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
    status = status.orEmpty(),
    lastLoginAt = last_login_at.toInstant()
)

fun DbCurrentUser.toDomain() = User(
    id = id,
    username = username,
    displayName = display_name.orEmpty(),
    avatarUrl = avatar_url.orEmpty(),
    avatarText = avatar_text.orEmpty(),
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant(),
    status = status.orEmpty(),
    lastLoginAt = last_login_at.toInstant()
)

fun DbUserPreferences.toDomain() = UserPreferences(
    userId = user_id,
    theme = theme,
    language = language,
    autoSync = auto_sync.toBoolean(),
    syncInterval = sync_interval
)

fun DbCardTemplate.toDomain() = CardTemplate(
    id = id,
    type = type,
    title = title,
    content = content,
    description = description,
    createdAt = created_at.toInstant()
)

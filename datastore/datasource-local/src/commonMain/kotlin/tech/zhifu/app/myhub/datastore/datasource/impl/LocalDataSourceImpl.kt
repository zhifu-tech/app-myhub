package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.database.SelectTagsByCardIds
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadata
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.model.domain.addIfAnyNotNull
import kotlin.time.Clock
import kotlin.time.Instant

internal typealias DbUser = tech.zhifu.app.myhub.datastore.database.User
internal typealias DbCurrentUser = tech.zhifu.app.myhub.datastore.database.SelectCurrentUser
internal typealias DbUserPreferences = tech.zhifu.app.myhub.datastore.database.User_preferences
internal typealias DbTag = tech.zhifu.app.myhub.datastore.database.Tag
internal typealias DbCard = tech.zhifu.app.myhub.datastore.database.Card_with_metadata_view
internal typealias DbCardTemplate = tech.zhifu.app.myhub.datastore.database.Card_template
internal typealias DbCollection = tech.zhifu.app.myhub.datastore.database.Collection

private fun String?.toInstant(def: Instant = Clock.System.now()) =
    this?.let { Instant.parse(it) } ?: def

private fun Long?.toBoolean(def: Boolean = false) =
    this?.let { it != 0L } ?: def

private fun String?.toInstantOrNull(): Instant? =
    this?.let { runCatching { Instant.parse(it) }.getOrNull() }

internal fun DbCard.toDomain(
    tags: List<Tag> = emptyList(),
) = Card(
    id = card_id,
    type = CardType.fromWire(card_type),
    source = CardSource.fromWire(card_source),
    carriers = card_carriers,
    userId = card_user_id,
    createdAt = card_created_at.toInstant(),
    updatedAt = card_updated_at.toInstant(),
    tags = tags,
    metadata = buildList {
        addIfAnyNotNull(
            md_content_title,
            md_content_summary,
            md_content_content
        ) {
            CardMetadata.Content(
                title = md_content_title,
                summary = md_content_summary,
                content = md_content_content,
            )

        }
        addIfAnyNotNull(
            md_attr_author,
            md_attr_origin,
            md_attr_attr_language,
            md_attr_style_key,
            md_attr_style_color,
        ) {
            CardMetadata.Attribution(
                author = md_attr_author,
                origin = md_attr_origin,
                language = md_attr_attr_language,
                styleKey = md_attr_style_key,
                styleColor = md_attr_style_color,
            )
        }
        addIfAnyNotNull(
            md_carrier_image_url,
            md_carrier_image_thumbnail_url,
        ) {
            CardMetadata.CarrierImage(
                url = md_carrier_image_url,
                thumbnailUrl = md_carrier_image_thumbnail_url
            )
        }
        addIfAnyNotNull(
            md_carrier_video_url,
            md_carrier_video_duration,
            md_carrier_video_platform,
            md_carrier_video_cover_image_url,
        ) {
            CardMetadata.CarrierVideo(
                videoUrl = md_carrier_video_url,
                durationSeconds = md_carrier_video_duration,
                platform = md_carrier_video_platform,
                coverImageUrl = md_carrier_video_cover_image_url,
            )
        }
        addIfAnyNotNull(
            md_execution_status,
            md_execution_priority,
            md_execution_due_at,
            md_execution_completed_at,
            md_execution_steps,
        ) {
            CardMetadata.Execution(
                status = md_execution_status,
                priority = md_execution_priority,
                md_execution_due_at.toInstantOrNull(),
                completedAt = md_execution_completed_at.toInstantOrNull(),
                steps = md_execution_steps,
            )
        }
        addIfAnyNotNull(
            md_lexicon_pronunciation,
            md_lexicon_definition, md_lexicon_example
        ) {
            CardMetadata.Lexicon(
                pronunciation = md_lexicon_pronunciation,
                definition = md_lexicon_definition,
                example = md_lexicon_example
            )
        }
        addIfAnyNotNull(
            md_link_url
        ) {
            CardMetadata.Link(
                url = md_link_url!!
            )
        }
        addIfAnyNotNull(
            md_code_language,
            md_code_snippet,
        ) {
            CardMetadata.Code(
                language = md_code_language,
                snippet = md_code_snippet,
            )
        }
        addIfAnyNotNull(
            md_site_id,
            md_site_name,
            md_site_fav_icon,
        ) {
            CardMetadata.Site(
                id = md_site_id,
                name = md_site_name,
                favIcon = md_site_fav_icon,
            )
        }
    }
)

internal fun DbTag.toDomain() = Tag(
    id = id,
    name = name,
    color = null,
    description = null,
    userId = user_id,
    createdAt = created_at.toInstant(),
    updatedAt = created_at.toInstant(),
    cardCount = 0
)

internal fun SelectTagsByCardIds.toDomain() = Tag(
    id = id,
    name = name,
    color = null,
    description = null,
    userId = user_id,
    createdAt = created_at.toInstant(),
    updatedAt = created_at.toInstant(),
    cardCount = 0
)

fun DbCollection.toDomain() = Collection(
    id = id,
    name = name,
    topic = null,
    description = null,
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

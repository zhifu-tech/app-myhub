package tech.zhifu.app.myhub.datastore.datasource.user

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import kotlin.time.Clock
import kotlin.time.Instant

internal typealias DbUser = tech.zhifu.app.myhub.datastore.database.User
internal typealias DbCurrentUser = tech.zhifu.app.myhub.datastore.database.SelectCurrentUser
internal typealias DbUserPreferences = tech.zhifu.app.myhub.datastore.database.User_preferences

private fun String?.toInstant(def: Instant = Clock.System.now()) =
    this?.let { Instant.parse(it) } ?: def

private fun Long?.toBoolean(def: Boolean = false) =
    this?.let { it != 0L } ?: def

internal fun DbUser.toDomain() = User(
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

internal fun DbCurrentUser.toDomain() = User(
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

internal fun DbUserPreferences.toDomain() = UserPreferences(
    userId = user_id,
    layoutAsList = layout_as_list.toBoolean(def = true),
    sortAsDate = sort_as_date.toBoolean(def = true),
    sortAsName = sort_as_name.toBoolean(def = true),
)

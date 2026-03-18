package tech.zhifu.app.myhub.datastore.datasource.tag

import tech.zhifu.app.myhub.datastore.model.domain.Tag
import kotlin.time.Clock
import kotlin.time.Instant

internal typealias DbTag = tech.zhifu.app.myhub.datastore.database.Tag
internal typealias SelectTagsByCardIds = tech.zhifu.app.myhub.datastore.database.SelectTagsByCardIds

private fun String?.toInstant(def: Instant = Clock.System.now()) =
    this?.let { Instant.parse(it) } ?: def

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

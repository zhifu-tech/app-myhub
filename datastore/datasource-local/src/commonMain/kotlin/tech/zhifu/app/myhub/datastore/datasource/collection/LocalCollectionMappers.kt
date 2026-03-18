package tech.zhifu.app.myhub.datastore.datasource.collection

import tech.zhifu.app.myhub.datastore.model.domain.Collection
import kotlin.time.Clock
import kotlin.time.Instant

internal typealias DbCollection = tech.zhifu.app.myhub.datastore.database.Collection

private fun String?.toInstant(def: Instant = Clock.System.now()) =
    this?.let { Instant.parse(it) } ?: def

internal fun DbCollection.toDomain() = Collection(
    id = id,
    name = name,
    topic = null,
    description = null,
    userId = user_id,
    createdAt = created_at.toInstant(),
    updatedAt = updated_at.toInstant()
)

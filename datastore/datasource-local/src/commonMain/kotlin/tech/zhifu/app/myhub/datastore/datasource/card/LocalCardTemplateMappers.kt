package tech.zhifu.app.myhub.datastore.datasource.card

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import kotlin.time.Clock
import kotlin.time.Instant

internal typealias DbCardTemplate = tech.zhifu.app.myhub.datastore.database.Card_template

private fun String?.toInstant(def: Instant = Clock.System.now()) =
    this?.let { Instant.parse(it) } ?: def

internal fun DbCardTemplate.toDomain() = CardTemplate(
    id = id,
    type = type,
    title = title,
    content = content,
    description = description,
    createdAt = created_at.toInstant()
)

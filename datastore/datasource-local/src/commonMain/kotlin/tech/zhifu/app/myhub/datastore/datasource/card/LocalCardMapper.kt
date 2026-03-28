package tech.zhifu.app.myhub.datastore.datasource.card

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import kotlin.time.Clock
import kotlin.time.Instant

internal typealias DbCard = tech.zhifu.app.myhub.datastore.database.Card

private fun Long?.toInstant(def: Instant = Clock.System.now()) =
    this?.let { Instant.fromEpochMilliseconds(it) } ?: def

internal fun List<DbCard>.toDomainList(): List<Card> =
    map { it.toDomain() }

internal fun DbCard.toDomain(): Card =
    Card(
        id = id,
        type = CardType.fromWire(type),
        status = CardStatus.fromWire(status),
        title = title,
        summary = summary.orEmpty(),
        locationRaw = location,
        tagsRaw = tags,
        uiRaw = ui,
        contentRaw = content,
        sourceRaw = source,
        createdAt = created_at.toInstant(),
        updatedAt = updated_at.toInstant(),
        version = version.toInt(),
        deleted = deleted != 0L,
    )

package tech.zhifu.app.myhub.datastore.bootstrap.model

import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardContent
import tech.zhifu.app.myhub.datastore.model.domain.CardContentType
import tech.zhifu.app.myhub.datastore.model.domain.CardLocation
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardSourceKind
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.domain.CardUi
import tech.zhifu.app.myhub.datastore.model.domain.Cover
import tech.zhifu.app.myhub.datastore.model.serializer.serialize
import kotlin.time.Instant


@Serializable
data class BootstrapCardSeed(
    val id: String,
    val title: String,
    val summary: String,
    val location: String,
    val updatedAt: Long,
    val tags: List<String> = emptyList(),
    val status: String = CardStatus.PUBLISHED.wire,
    val cover: BootstrapCardSeedCover = BootstrapCardSeedCover(),
)

@Serializable
data class BootstrapCardSeedCover(
    val iconKey: String? = null,
    val bgColor: String = "#F1F5F9",
    val tintColor: String? = null,
    val imageUrl: String? = null,
)

internal fun BootstrapCardSeed.toCard(
    now: Instant,
): Card {
    val cardUi = CardUi(
        cover = Cover(
            iconKey = cover.iconKey.orEmpty(),
            bgColor = cover.bgColor,
            tintColor = cover.tintColor.orEmpty(),
            imageUrl = cover.imageUrl.orEmpty(),
        )
    )
    return Card(
        id = id,
        type = CardType.NOTE,
        status = CardStatus.fromWire(status),
        title = title,
        summary = summary,
        version = 1,
        createdAt = now,
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        deletedAt = null,
        locationRaw = CardLocation(
            latitude = 0.0,
            longitude = 0.0,
            name = location,
            address = null,
        ).serialize(),
        tagsRaw = tags.serialize(),
        uiRaw = cardUi.serialize(),
        contentRaw = CardContent(
            type = CardContentType.TEXT,
            value = summary,
        ).serialize(),
        sourceRaw = CardSource(
            kind = CardSourceKind.MANUAL,
        ).serialize(),
    )
}

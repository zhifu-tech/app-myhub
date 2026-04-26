package tech.zhifu.app.myhub.feature.ai.content.preview

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardLocation
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.datastore.model.domain.MediaAsset
import tech.zhifu.app.myhub.datastore.model.serializer.serialize
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import kotlin.time.Clock

fun CaptureDraft.toPreviewCard(
    untitledDraft: String,
    continueHint: String,
    continueEdit: String,
): ContentCard = ContentCard(
    medias = mediaAssets
        .mapIndexed { index, asset ->
            MediaAsset(
                id = asset.sha256.ifBlank { "${asset.storageHandle}#$index" },
                cardId = id,
                mediaType = asset.mediaType,
                storageHandle = asset.storageHandle,
                accessUrl = asset.accessUrl,
                thumbStorageHandle = null,
                thumbAccessUrl = null,
                width = null,
                height = null,
                durationMs = null,
                sizeBytes = asset.sizeBytes,
                sha256 = asset.sha256,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        }
        .filterIndexed { index, media ->
            media.accessUrl.isNotBlank() && !mediaAssets[index].isMissing
        },
    card = Card(
        id = id,
        type = CardType.NOTE,
        status = CardStatus.DRAFT,
        title = title.ifBlank { untitledDraft },
        summary = summary.ifBlank { sourceText.ifBlank { continueHint } },
        version = 1,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
        deletedAt = null,
        locationRaw = location?.let {
            CardLocation(
                latitude = it.latitude ?: 0.0,
                longitude = it.longitude ?: 0.0,
                name = it.name,
                address = null,
            ).serialize()
        },
        tagsRaw = tags.serialize(),
        sourceRaw = null,
    ),
)

fun CaptureDraft.previewMediaUrl(): String? =
    mediaAssets.firstOrNull { asset ->
        !asset.isMissing && asset.accessUrl.isNotBlank()
    }?.accessUrl

package tech.zhifu.app.myhub.feature.preview

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.datastore.model.domain.MediaAsset

@Stable
class PreviewState {
    var pined by mutableStateOf(false)
    val card = mutableStateOf<ContentCard?>(null)
    val mediaSession = mutableStateOf<PreviewMediaSession?>(null)
    private var cardDeck: List<ContentCard> = emptyList()

    fun show(
        card: ContentCard,
        deck: List<ContentCard> = listOf(card),
    ) {
        this.card.value = card
        this.cardDeck = deck.ifEmpty { listOf(card) }
    }

    fun hide() {
        this.card.value = null
        hideMedia()
    }

    fun showMedia(
        card: ContentCard,
        mediaIndex: Int,
        deck: List<ContentCard> = cardDeck.ifEmpty { listOf(card) },
        allowCrossCardNavigation: Boolean = false,
    ) {
        val cards = deck.ifEmpty { listOf(card) }
        val items = buildPreviewMediaItems(
            card = card,
            deck = cards,
            allowCrossCardNavigation = allowCrossCardNavigation,
        )
        if (items.isEmpty()) return
        val initialIndex = items.indexOfFirst {
            it.cardId == card.card.id && it.mediaIndex == mediaIndex
        }.takeIf { it >= 0 } ?: 0
        this.cardDeck = cards
        this.mediaSession.value = PreviewMediaSession(
            items = items,
            initialIndex = initialIndex,
            allowCrossCardNavigation = allowCrossCardNavigation,
        )
    }

    fun hideMedia() {
        mediaSession.value = null
    }

    fun isPreviewing() = this.card.value != null

    private fun buildPreviewMediaItems(
        card: ContentCard,
        deck: List<ContentCard>,
        allowCrossCardNavigation: Boolean,
    ): List<PreviewMediaItem> {
        val sourceCards = if (allowCrossCardNavigation) deck else listOf(card)
        return sourceCards.flatMap { candidate ->
            candidate.medias.mapIndexed { index, media ->
                PreviewMediaItem(
                    cardId = candidate.card.id,
                    cardTitle = candidate.card.title,
                    mediaIndex = index,
                    media = media,
                )
            }
        }
    }
}

data class PreviewMediaSession(
    val items: List<PreviewMediaItem>,
    val initialIndex: Int,
    val allowCrossCardNavigation: Boolean,
)

data class PreviewMediaItem(
    val cardId: String,
    val cardTitle: String,
    val mediaIndex: Int,
    val media: MediaAsset,
)

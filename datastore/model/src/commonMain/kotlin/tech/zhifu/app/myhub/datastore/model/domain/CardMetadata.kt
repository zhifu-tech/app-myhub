package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed class CardMetadata {
    @Serializable
    data class Attribution(
        val author: String? = null,
        val origin: String? = null,
        val language: String? = null,
        val styleKey: String? = null,
        val styleColor: String? = null,
    ) : CardMetadata()

    @Serializable
    data class Content(
        val title: String? = null,
        val summary: String? = null,
        val content: String? = null,
    ) : CardMetadata()

    @Serializable
    data class CarrierImage(
        val url: String? = null,
        val thumbnailUrl: String? = null,
    ) : CardMetadata()


    @Serializable
    data class CarrierVideo(
        val videoUrl: String? = null,
        val durationSeconds: Long? = null,
        val platform: String? = null,
        val coverImageUrl: String? = null,
    ) : CardMetadata()

    @Serializable
    data class Execution(
        val status: String? = null,
        val priority: String? = null,
        val dueAt: Instant? = null,
        val completedAt: Instant? = null,
        val steps: String? = null,
    ) : CardMetadata()

    @Serializable
    data class Lexicon(
        val pronunciation: String? = null,
        val definition: String? = null,
        val example: String? = null,
    ) : CardMetadata()

    @Serializable
    data class Link(
        val url: String,
    ) : CardMetadata()

    @Serializable
    data class Code(
        val language: String? = null,
        val snippet: String? = null,
    ) : CardMetadata()

    @Serializable
    data class Site(
        val id: String? = null,
        val name: String? = null,
        val favIcon: String? = null,
    ) : CardMetadata()

}

val List<CardMetadata>.attribution: CardMetadata.Attribution?
    get() = filterIsInstance<CardMetadata.Attribution>().firstOrNull()

val List<CardMetadata>.content: CardMetadata.Content?
    get() = filterIsInstance<CardMetadata.Content>().firstOrNull()

val List<CardMetadata>.execution: CardMetadata.Execution?
    get() = filterIsInstance<CardMetadata.Execution>().firstOrNull()

val List<CardMetadata>.lexicon: CardMetadata.Lexicon?
    get() = filterIsInstance<CardMetadata.Lexicon>().firstOrNull()

val List<CardMetadata>.link: CardMetadata.Link?
    get() = filterIsInstance<CardMetadata.Link>().firstOrNull()

val List<CardMetadata>.carrierImage: CardMetadata.CarrierImage?
    get() = filterIsInstance<CardMetadata.CarrierImage>().firstOrNull()

val List<CardMetadata>.carrierVideo: CardMetadata.CarrierVideo?
    get() = filterIsInstance<CardMetadata.CarrierVideo>().firstOrNull()

val List<CardMetadata>.code: CardMetadata.Code?
    get() = filterIsInstance<CardMetadata.Code>().firstOrNull()

val List<CardMetadata>.site: CardMetadata.Site?
    get() = filterIsInstance<CardMetadata.Site>().firstOrNull()

fun MutableList<CardMetadata>.addIfAnyNotNull(
    vararg fields: Any?,
    create: () -> CardMetadata
) {
    if (fields.any { it != null }) {
        add(create())
    }
}

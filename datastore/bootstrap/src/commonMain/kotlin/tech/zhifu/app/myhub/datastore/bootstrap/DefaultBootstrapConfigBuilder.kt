package tech.zhifu.app.myhub.datastore.bootstrap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import tech.zhifu.app.myhub.datastore.bootstrap.resources.Res
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadata
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardTag
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.CollectionCard
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import kotlin.time.Instant

internal class DefaultBootstrapConfigBuilder : BootstrapConfigBuilder {

    override suspend fun buildConfig(
        userId: String,
        localeTag: String
    ): BootstrapConfig = withContext(Dispatchers.Default) {
        val localeDir = resolveLocaleDir(localeTag)
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            serializersModule = SerializersModule {
                contextual(creatInstantSerializer())
            }
        }
        val user = readResource("user.json", localeDir)
            .let { json.decodeFromString<User>(it) }
            .copy(id = userId)
        val userPreferences = readResource("user_preferences.json", localeDir)
            .let { json.decodeFromString<UserPreferences>(it) }
            .copy(userId = userId)

        val collections = readResource("collection.json", localeDir)
            .let { json.decodeFromString<List<Collection>>(it) }
            .map { it -> it.copy(userId = userId) }
        val collectionCards = readOptionalResource("collection_card.json", localeDir)
            ?.let { json.decodeFromString<List<CollectionCard>>(it) }
            .orEmpty()

        val tags: List<Tag> = readResource("tag.json", localeDir)
            .let { json.decodeFromString<List<Tag>>(it) }
            .map { tag -> tag.copy(userId = userId) }
        val tagsById: Map<String, Tag> = tags.associateBy { it.id }
        val cardTags: List<CardTag> = readResource("card_tag.json", localeDir)
            .let { json.decodeFromString<List<CardTag>>(it) }
        val tagIdsByCardId: Map<String, List<String>> = cardTags.groupBy { it.cardId }
            .mapValues { (_, items) -> items.map { it.tagId } }

        val cardTemplates = readResource("template.json", localeDir)
            .let { json.decodeFromString<List<CardTemplate>>(it) }

        val strictJson = Json(json) {
            ignoreUnknownKeys = false
        }
        val cards = readResource("card.json", localeDir)
            .let { strictJson.decodeFromString<List<BootstrapCard>>(it) }
            .map { card ->
                card.toDomain(
                    userId = userId,
                    tags = tagIdsByCardId[card.id]
                        .orEmpty()
                        .mapNotNull(tagsById::get)
                )
            }

        BootstrapConfig(
            userId = userId,
            user = user,
            userPreferences = userPreferences,
            tags = tags,
            collections = collections,
            cards = cards,
            templates = cardTemplates,
            collectionCards = collectionCards
        )
    }

    private suspend fun readResource(fileName: String, localeDir: String): String {
        return Res.readBytes("files/$localeDir/$fileName").decodeToString()
    }

    private suspend fun readOptionalResource(fileName: String, localeDir: String): String? {
        return runCatching {
            Res.readBytes("files/$localeDir/$fileName").decodeToString()
        }.getOrNull()
    }

    private fun resolveLocaleDir(localeTag: String): String {
        val normalized = localeTag.trim().lowercase()
            .replace('-', '_')
        return "bootstrap_$normalized"
    }

    private fun creatInstantSerializer() = object : KSerializer<Instant> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Instant) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): Instant {
            return Instant.parse(decoder.decodeString())
        }
    }
}

@Serializable
private data class BootstrapCard(
    val id: String,
    val type: CardType,
    val source: CardSource,
    val carriers: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val metadata: BootstrapCardMetadata,
)

@Serializable
private data class BootstrapCardMetadata(
    val content: CardMetadata.Content? = null,
    val attribution: CardMetadata.Attribution? = null,
    val carrierImage: CardMetadata.CarrierImage? = null,
    val carrierVideo: CardMetadata.CarrierVideo? = null,
    val execution: CardMetadata.Execution? = null,
    val lexicon: CardMetadata.Lexicon? = null,
    val link: CardMetadata.Link? = null,
    val code: CardMetadata.Code? = null,
    val site: CardMetadata.Site? = null,
)

private fun BootstrapCard.toDomain(
    userId: String,
    tags: List<Tag>,
): Card {
    val metadataEntries = buildList {
        metadata.content?.let(::add)
        metadata.attribution?.let(::add)
        metadata.carrierImage?.let(::add)
        metadata.carrierVideo?.let(::add)
        metadata.execution?.let(::add)
        metadata.lexicon?.let(::add)
        metadata.link?.let(::add)
        metadata.code?.let(::add)
        metadata.site?.let(::add)
    }

    return Card(
        id = id,
        type = type,
        source = source,
        carriers = carriers,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        metadata = metadataEntries,
        tags = tags,
    )
}

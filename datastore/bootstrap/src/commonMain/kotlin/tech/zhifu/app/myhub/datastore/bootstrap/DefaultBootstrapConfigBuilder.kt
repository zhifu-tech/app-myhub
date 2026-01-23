package tech.zhifu.app.myhub.datastore.bootstrap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
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
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataArticle
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataCode
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataIdea
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataQuote
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataTodo
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadataWord
import tech.zhifu.app.myhub.datastore.model.domain.CardTag
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.model.domain.isArticle
import tech.zhifu.app.myhub.datastore.model.domain.isCode
import tech.zhifu.app.myhub.datastore.model.domain.isIdea
import tech.zhifu.app.myhub.datastore.model.domain.isQuote
import tech.zhifu.app.myhub.datastore.model.domain.isTodo
import tech.zhifu.app.myhub.datastore.model.domain.isWord
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
            .map { it.copy(userId = userId) }
        val tags: List<Tag> = readResource("tag.json", localeDir)
            .let { json.decodeFromString<List<Tag>>(it) }
            .map { tag -> tag.copy(userId = userId) }
        val tagsById: Map<String, Tag> = tags.associateBy { it.id }
        val cardTags: List<CardTag> = readResource("card_tag.json", localeDir)
            .let { json.decodeFromString<List<CardTag>>(it) }
        val tagIdsByCardId: Map<String, List<String>> = cardTags.groupBy { it.cardId }
            .mapValues { (_, items) -> items.map { it.tagId } }

        val cardMetadataQuotes = readOptionalResource("card_metadata_quote.json", localeDir)
            ?.let { json.decodeFromString<List<CardMetadataQuote>>(it) }
            .orEmpty()
        val cardMetadataArticles = readOptionalResource("card_metadata_article.json", localeDir)
            ?.let { json.decodeFromString<List<CardMetadataArticle>>(it) }
            .orEmpty()

        val cardMetadataCodes = readOptionalResource("card_metadata_code.json", localeDir)
            ?.let { json.decodeFromString<List<CardMetadataCode>>(it) }
            .orEmpty()
        val cardMetadataIdeas = readOptionalResource("card_metadata_idea.json", localeDir)
            ?.let { json.decodeFromString<List<CardMetadataIdea>>(it) }
            .orEmpty()
        val cardMetadataTodos = readOptionalResource("card_metadata_todo.json", localeDir)
            ?.let { json.decodeFromString<List<CardMetadataTodo>>(it) }
            .orEmpty()
        val cardMetadataWords = readOptionalResource("card_metadata_word.json", localeDir)
            ?.let { json.decodeFromString<List<CardMetadataWord>>(it) }
            .orEmpty()

        val cardTemplates = readResource("template.json", localeDir)
            .let { json.decodeFromString<List<CardTemplate>>(it) }

        val quoteMetadataByCardId = cardMetadataQuotes.associateBy { it.cardId }
        val codeMetadataByCardId = cardMetadataCodes.associateBy { it.cardId }
        val articleMetadataByCardId = cardMetadataArticles.associateBy { it.cardId }
        val ideaMetadataByCardId = cardMetadataIdeas.associateBy { it.cardId }
        val todoMetadataByCardId = cardMetadataTodos.associateBy { it.cardId }
        val wordMetadataByCardId = cardMetadataWords.associateBy { it.cardId }

        val cards = readResource("card.json", localeDir)
            .let { json.decodeFromString<List<Card>>(it) }
            .map { card ->
                val cardType = card.type.lowercase()
                val metadata = when {
                    cardType.isQuote -> quoteMetadataByCardId[card.id]
                    cardType.isCode -> codeMetadataByCardId[card.id]
                    cardType.isArticle -> articleMetadataByCardId[card.id]
                    cardType.isIdea -> ideaMetadataByCardId[card.id]
                    cardType.isTodo -> todoMetadataByCardId[card.id]
                    cardType.isWord -> wordMetadataByCardId[card.id]
                    else -> null
                }
                card.copy(
                    userId = userId,
                    metadata = metadata,
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
            templates = cardTemplates
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

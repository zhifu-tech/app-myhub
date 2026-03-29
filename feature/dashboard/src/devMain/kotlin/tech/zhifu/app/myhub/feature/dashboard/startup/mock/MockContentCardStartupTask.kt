package tech.zhifu.app.myhub.feature.dashboard.startup.mock

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import tech.zhifu.app.myhub.datastore.repository.card.CardRepository
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.startup.StartupTask
import tech.zhifu.app.myhub.startup.StartupTaskIds
import tech.zhifu.app.myhub.ui.model.ContentCard
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.Instant

/** 调试模式下，我们插入一些模拟数据*/
internal class MockContentCardStartupTask(
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
) : StartupTask {
    override val id: String = StartupTaskIds.MOCK_CONTENT_CARD
    override val critical: Boolean = false
    override val dependencies: Set<String> = setOf(StartupTaskIds.BOOTSTRAP)

    override suspend fun run() {
        val userId = userRepository.getUserOrNull()?.id
            ?: run {
                logger.error { "MockContentCardStartupTask: failed as user not found" }
                return
            }
        val exists = cardRepository
            .flowCards(
                userId = userId,
                cursorCardId = null,
                cursorTitle = null,
                cursorUpdatedAt = null,
                orderByUpdated = true,
                orderByTitle = false,
                limit = 1
            )
            .first()
            .isNotEmpty()

        if (exists) {
            logger.debug { "MockContentCardStartupTask: skip as cards already exists" }
            return
        }
        logger.debug { "MockContentCardStartupTask: not skip" }

        mockContentCards()
            .map {
                it.toSeedCard(now = Clock.System.now())
            }
            .forEach { card ->
                cardRepository.insertCard(card = card, userId = userId)
            }
    }
}

fun ContentCard.toSeedCard(now: Instant): Card {
    val iconKey = cover.iconKey.orEmpty()
    val bgColor = toHexColor(cover.background)
    val tintColor = cover.tint?.let(::toHexColor).orEmpty()
    val imageUrl = cover.url.orEmpty()

    val status = when (action.label) {
        ContentItemTokens.draftActionLabel -> CardStatus.DRAFT
        else -> CardStatus.PUBLISHED
    }

    return Card(
        id = id,
        type = CardType.NOTE,
        status = status,
        title = title,
        summary = summary,
        version = 1,
        deleted = false,
        createdAt = now,
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        locationRaw = """{"name":"${escapeJson(location)}","latitude":0.0,"longitude":0.0}""",
        tagsRaw = tags.joinToString(
            prefix = "[",
            postfix = "]",
            separator = ","
        ) { "\"${escapeJson(it)}\"" },
        uiRaw = """
            {"cover":{"iconKey":"${escapeJson(iconKey)}","bgColor":"$bgColor","tintColor":"$tintColor","imageUrl":"${
            escapeJson(imageUrl)
        }"}}
        """.trimIndent(),
        contentRaw = """{"type":"text","value":"${escapeJson(summary)}"}""",
        sourceRaw = """{"kind":{"value":"manual"}}""",
    )
}


private fun toHexColor(color: Color): String {
    val r = (color.red * 255).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255).roundToInt().coerceIn(0, 255)
    fun Int.toHex2(): String = toString(16).uppercase().padStart(2, '0')
    return "#${r.toHex2()}${g.toHex2()}${b.toHex2()}"
}

private fun escapeJson(input: String): String = input
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")

package tech.zhifu.app.myhub.carddetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import tech.zhifu.app.myhub.component.card.di.cardModule
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.repository.ReactiveCardRepository
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.ProvideWindowSizeClass
import tech.zhifu.app.myhub.ui.WindowSizeClass
import kotlin.time.Clock

/**
 * Mock ReactiveCardRepository 用于 Preview
 */
private class MockReactiveCardRepository : ReactiveCardRepository {
    private val sampleCard = createSampleCard()

    override fun observeAllCards(): Flow<List<Card>> {
        return flowOf(listOf(sampleCard))
    }

    override fun observeCard(id: String): Flow<Card?> {
        return flowOf(if (id == "preview-card-1") sampleCard else null)
    }

    override fun observeFavoriteCards(): Flow<List<Card>> {
        return flowOf(emptyList())
    }

    override fun observeCardsByType(type: CardType): Flow<List<Card>> {
        return flowOf(if (sampleCard.type == type) listOf(sampleCard) else emptyList())
    }

    override fun observeCardsByTag(tag: String): Flow<List<Card>> {
        return flowOf(if (sampleCard.tags.contains(tag)) listOf(sampleCard) else emptyList())
    }

    override fun observeSearchCards(filter: tech.zhifu.app.myhub.datastore.model.SearchFilter): Flow<List<Card>> {
        return flowOf(listOf(sampleCard))
    }

    // CardRepository 接口方法
    override suspend fun getAllCards(): List<Card> = listOf(sampleCard)
    
    override suspend fun getCardById(id: String): Card? = if (id == "preview-card-1") sampleCard else null
    
    override suspend fun searchCards(filter: tech.zhifu.app.myhub.datastore.model.SearchFilter): List<Card> {
        return listOf(sampleCard)
    }
    
    override suspend fun createCard(card: Card): Card = card
    
    override suspend fun updateCard(card: Card): Card = card
    
    override suspend fun deleteCard(id: String): Boolean = true
    
    override suspend fun toggleFavorite(cardId: String): Card = sampleCard.copy(isFavorite = !sampleCard.isFavorite)
}

/**
 * 创建示例卡片数据（带标签和完整元数据）
 */
private fun createSampleCard(): Card {
    val now = Clock.System.now()
    return Card(
        id = "preview-card-1",
        type = CardType.QUOTE,
        title = null,
        content = "The art of reading is in great part that of acquiring a better understanding of life from one's encounter with it in a book.",
        author = "André Maurois",
        isFavorite = false,
        tags = listOf("literature", "philosophy", "reading", "books"),
        createdAt = now,
        updatedAt = now,
        metadata = CardMetadata(
            quoteAuthor = "André Maurois",
            quoteCategory = "LITERATURE",
            articleUrl = "https://example.com/article"
        )
    )
}

/**
 * 初始化预览环境所需的 Koin 依赖
 * 只初始化必要的模块，避免完整应用初始化
 */
private fun initPreviewKoin() {
    // 使用 KoinPlatformTools 检查上下文是否已存在，避免异常抛出
    val koin = KoinPlatformTools.defaultContext().getOrNull()
    if (koin == null) {
        // Koin 未启动，执行初始化
        startKoin {
            modules(
                cardModule,
                // 提供 ViewModel 需要的依赖
                module {
                    factory<CoroutineScope> {
                        CoroutineScope(Dispatchers.Default)
                    }
                    // 提供 Mock Repository
                    factory<ReactiveCardRepository> {
                        MockReactiveCardRepository()
                    }
                }
            )
        }
    }
}

/**
 * Preview 函数 - 浅色主题（移动端 Compact）
 */
@Preview
@Composable
private fun CardDetailScreenLightCompactPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        ProvideWindowSizeClass(WindowSizeClass.Compact) {
            CardDetailScreen(
                cardId = "preview-card-1",
                onNavigateBack = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Preview 函数 - 深色主题（移动端 Compact）
 */
@Preview
@Composable
private fun CardDetailScreenDarkCompactPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = true) {
        ProvideWindowSizeClass(WindowSizeClass.Compact) {
            CardDetailScreen(
                cardId = "preview-card-1",
                onNavigateBack = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Preview 函数 - 浅色主题（平板 Medium）
 */
@Preview
@Composable
private fun CardDetailScreenLightMediumPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        ProvideWindowSizeClass(WindowSizeClass.Medium) {
            CardDetailScreen(
                cardId = "preview-card-1",
                onNavigateBack = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Preview 函数 - 深色主题（平板 Medium）
 */
@Preview
@Composable
private fun CardDetailScreenDarkMediumPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = true) {
        ProvideWindowSizeClass(WindowSizeClass.Medium) {
            CardDetailScreen(
                cardId = "preview-card-1",
                onNavigateBack = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Preview 函数 - 浅色主题（桌面端 Expanded）
 */
@Preview
@Composable
private fun CardDetailScreenLightExpandedPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        ProvideWindowSizeClass(WindowSizeClass.Expanded) {
            CardDetailScreen(
                cardId = "preview-card-1",
                onNavigateBack = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Preview 函数 - 深色主题（桌面端 Expanded）
 */
@Preview
@Composable
private fun CardDetailScreenDarkExpandedPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = true) {
        ProvideWindowSizeClass(WindowSizeClass.Expanded) {
            CardDetailScreen(
                cardId = "preview-card-1",
                onNavigateBack = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

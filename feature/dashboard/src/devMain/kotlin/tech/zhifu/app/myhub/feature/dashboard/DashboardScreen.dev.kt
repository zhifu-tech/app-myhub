package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import tech.zhifu.app.myhub.component.card.di.cardModule
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.ChecklistItem
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.isWidthCompact
import tech.zhifu.app.myhub.ui.isWidthMedium
import tech.zhifu.app.myhub.ui.mockWindowSizeClassCompact
import tech.zhifu.app.myhub.ui.mockWindowSizeClassExpanded
import tech.zhifu.app.myhub.ui.mockWindowSizeClassMedium
import kotlin.time.Clock

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
                cardModule
            )
        }
    }
}

/**
 * Preview 函数 - DashboardScreen 浅色主题（网格视图）
 */
@Preview
@Composable
private fun DashboardScreenLightGridPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        val sampleCards = createSampleCards()
        val statistics = createSampleStatistics()
        val sizeClass = mockWindowSizeClassMedium()
        val columns = when {
            sizeClass.isWidthCompact() -> 1
            sizeClass.isWidthMedium() -> 2
            else -> 3
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DashboardHeader(
                    recentEditsCount = statistics.recentEdits,
                    lastSyncTime = Clock.System.now().toEpochMilliseconds() - 300_000, // 5分钟前
                    isLoading = false,
                    onRefresh = {}
                )

                var searchQuery by remember { mutableStateOf("") }
                DashboardToolbar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    statistics = statistics,
                    sizeClass = sizeClass,
                    viewType = ViewType.GRID,
                    onViewTypeChange = {}
                )

                DashboardGridView(
                    cards = sampleCards,
                    columns = columns,
                    statistics = statistics,
                    sizeClass = sizeClass,
                    onEdit = {},
                    onFavorite = {},
                    onCardClick = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Preview 函数 - DashboardScreen 深色主题（列表视图）
 */
@Preview
@Composable
private fun DashboardScreenDarkListPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = true) {
        val sampleCards = createSampleCards()
        val statistics = createSampleStatistics()
        val sizeClass = mockWindowSizeClassExpanded()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DashboardHeader(
                    recentEditsCount = statistics.recentEdits,
                    lastSyncTime = Clock.System.now().toEpochMilliseconds() - 3600_000, // 1小时前
                    isLoading = false,
                    onRefresh = {}
                )

                var searchQuery by remember { mutableStateOf("") }
                DashboardToolbar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    statistics = statistics,
                    sizeClass = sizeClass,
                    viewType = ViewType.LIST,
                    onViewTypeChange = {}
                )

                DashboardListView(
                    cards = sampleCards,
                    columns = when {
                        sizeClass.isWidthCompact() -> 1
                        sizeClass.isWidthMedium() -> 2
                        else -> 3
                    },
                    statistics = null,
                    sizeClass = sizeClass,
                    onEdit = {},
                    onFavorite = {},
                    onCardClick = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Preview 函数 - DashboardScreen 加载状态
 */
@Preview
@Composable
private fun DashboardScreenLoadingPreview() {
    AppTheme(darkTheme = false) {
        val statistics = createSampleStatistics()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DashboardHeader(
                    recentEditsCount = statistics.recentEdits,
                    lastSyncTime = Clock.System.now().toEpochMilliseconds() - 60_000, // 1分钟前
                    isLoading = true,
                    onRefresh = {}
                )

                var searchQuery by remember { mutableStateOf("") }
                DashboardToolbar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    statistics = statistics,
                    sizeClass = mockWindowSizeClassMedium(),
                    viewType = ViewType.GRID,
                    onViewTypeChange = {}
                )
            }
        }
    }
}

/**
 * Preview 函数 - DashboardHeader 浅色主题
 */
@Preview
@Composable
private fun DashboardHeaderLightPreview() {
    AppTheme(darkTheme = false) {
        DashboardHeader(
            recentEditsCount = 5,
            lastSyncTime = Clock.System.now().toEpochMilliseconds() - 300_000, // 5分钟前
            isLoading = false,
            onRefresh = {}
        )
    }
}

/**
 * Preview 函数 - DashboardHeader 深色主题
 */
@Preview
@Composable
private fun DashboardHeaderDarkPreview() {
    AppTheme(darkTheme = true) {
        DashboardHeader(
            recentEditsCount = 0,
            lastSyncTime = null,
            isLoading = false,
            onRefresh = {}
        )
    }
}

/**
 * Preview 函数 - DashboardHeader 加载状态
 */
@Preview
@Composable
private fun DashboardHeaderLoadingPreview() {
    AppTheme(darkTheme = false) {
        DashboardHeader(
            recentEditsCount = 3,
            lastSyncTime = Clock.System.now().toEpochMilliseconds() - 60_000, // 1分钟前
            isLoading = true,
            onRefresh = {}
        )
    }
}

/**
 * Preview 函数 - DashboardToolbar 浅色主题
 */
@Preview
@Composable
private fun DashboardToolbarLightPreview() {
    AppTheme(darkTheme = false) {
        var searchQuery by remember { mutableStateOf("") }
        val statistics = createSampleStatistics()

        DashboardToolbar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            statistics = statistics,
            sizeClass = mockWindowSizeClassMedium(),
            viewType = ViewType.GRID,
            onViewTypeChange = {}
        )
    }
}

/**
 * Preview 函数 - DashboardToolbar 深色主题
 */
@Preview
@Composable
private fun DashboardToolbarDarkPreview() {
    AppTheme(darkTheme = true) {
        var searchQuery by remember { mutableStateOf("搜索卡片...") }
        val statistics = createSampleStatistics()

        DashboardToolbar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            statistics = statistics,
            sizeClass = mockWindowSizeClassExpanded(),
            viewType = ViewType.LIST,
            onViewTypeChange = {}
        )
    }
}

/**
 * Preview 函数 - StatsCardsRow
 */
@Preview
@Composable
private fun StatsCardsRowPreview() {
    AppTheme(darkTheme = false) {
        StatsCardsRow(statistics = createSampleStatistics())
    }
}

/**
 * Preview 函数 - DashboardGridView 紧凑布局
 */
@Preview
@Composable
private fun DashboardGridViewCompactPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        val sampleCards = createSampleCards()
        val statistics = createSampleStatistics()
        val sizeClass = mockWindowSizeClassCompact()

        DashboardGridView(
            cards = sampleCards,
            columns = 1,
            statistics = statistics,
            sizeClass = sizeClass,
            onEdit = {},
            onFavorite = {},
            onCardClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Preview 函数 - DashboardGridView 中等布局
 */
@Preview
@Composable
private fun DashboardGridViewMediumPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        val sampleCards = createSampleCards()
        val statistics = createSampleStatistics()
        val sizeClass = mockWindowSizeClassMedium()

        DashboardGridView(
            cards = sampleCards,
            columns = 2,
            statistics = statistics,
            sizeClass = sizeClass,
            onEdit = {},
            onFavorite = {},
            onCardClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Preview 函数 - DashboardGridView 扩展布局
 */
@Preview
@Composable
private fun DashboardGridViewExpandedPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        val sampleCards = createSampleCards()
        val statistics = createSampleStatistics()
        val sizeClass = mockWindowSizeClassExpanded()

        DashboardGridView(
            cards = sampleCards,
            columns = 3,
            statistics = null, // 3列时统计卡片在工具栏中显示
            sizeClass = sizeClass,
            onEdit = {},
            onFavorite = {},
            onCardClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Preview 函数 - DashboardListView 紧凑布局
 */
@Preview
@Composable
private fun DashboardListViewCompactPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        val sampleCards = createSampleCards()

        DashboardListView(
            cards = sampleCards,
            columns = 1,
            statistics = null,
            sizeClass = mockWindowSizeClassCompact(),
            onEdit = {},
            onFavorite = {},
            onCardClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Preview 函数 - DashboardListView 扩展布局
 */
@Preview
@Composable
private fun DashboardListViewExpandedPreview() {
    // 初始化预览所需的 Koin（只初始化一次）
    remember { initPreviewKoin() }

    AppTheme(darkTheme = false) {
        val sampleCards = createSampleCards()

        DashboardListView(
            cards = sampleCards,
            columns = 3,
            statistics = null,
            sizeClass = mockWindowSizeClassExpanded(),
            onEdit = {},
            onFavorite = {},
            onCardClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 创建示例统计信息
 */
private fun createSampleStatistics(): Statistics {
    return Statistics(
        totalCards = 42,
        favoriteCards = 8,
        recentEdits = 5,
        cardsByType = mapOf(
            CardType.QUOTE to 15,
            CardType.CODE to 10,
            CardType.IDEA to 8,
            CardType.ARTICLE to 5,
            CardType.DICTIONARY to 3,
            CardType.CHECKLIST to 1
        ),
        cardsByTag = mapOf(
            "编程" to 12,
            "设计" to 8,
            "学习" to 15,
            "工作" to 7
        ),
        lastSyncTime = Clock.System.now().toEpochMilliseconds()
    )
}

/**
 * 创建示例卡片列表
 */
private fun createSampleCards(): List<Card> {
    val now = Clock.System.now()
    return listOf(
        Card(
            id = "preview-card-1",
            type = CardType.QUOTE,
            content = "The only way to do great work is to love what you do.",
            author = "Steve Jobs",
            tags = listOf("励志", "工作"),
            isFavorite = true,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                quoteAuthor = "Steve Jobs",
                quoteCategory = "MOTIVATION"
            )
        ),
        Card(
            id = "preview-card-2",
            type = CardType.CODE,
            title = "Kotlin 协程示例",
            content = "fun main() = runBlocking {\n    launch {\n        delay(1000L)\n        println(\"World!\")\n    }\n    println(\"Hello\")\n}",
            language = "kotlin",
            tags = listOf("编程", "Kotlin"),
            isFavorite = false,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                codeLanguage = "kotlin",
                codeSnippet = "fun main() = runBlocking { ... }"
            )
        ),
        Card(
            id = "preview-card-3",
            type = CardType.IDEA,
            content = "创建一个新的功能模块来管理用户偏好设置",
            tags = listOf("功能", "设计"),
            isFavorite = true,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                ideaPriority = "high",
                ideaStatus = "in-progress"
            )
        ),
        Card(
            id = "preview-card-4",
            type = CardType.ARTICLE,
            title = "Compose Multiplatform 最佳实践",
            content = "学习如何使用 Compose Multiplatform 构建跨平台应用的最佳实践和技巧。",
            tags = listOf("技术", "学习"),
            isFavorite = false,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                articleUrl = "https://example.com/article",
                articleSummary = "Compose Multiplatform 最佳实践"
            )
        ),
        Card(
            id = "preview-card-5",
            type = CardType.DICTIONARY,
            title = "Serendipity",
            content = "The occurrence and development of events by chance in a happy or beneficial way.",
            tags = listOf("英语", "词汇"),
            isFavorite = false,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                wordPronunciation = "/ˌserənˈdɪpəti/",
                wordDefinition = "意外发现美好事物的能力",
                wordExample = "A fortunate stroke of serendipity brought the two old friends together."
            )
        ),
        Card(
            id = "preview-card-6",
            type = CardType.CHECKLIST,
            title = "项目启动清单",
            content = "",
            tags = listOf("工作", "项目管理"),
            isFavorite = false,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                checklistItems = listOf(
                    ChecklistItem("1", "需求分析", true, 0),
                    ChecklistItem("2", "技术选型", true, 1),
                    ChecklistItem("3", "架构设计", false, 2),
                    ChecklistItem("4", "开发环境搭建", false, 3)
                )
            )
        ),
        Card(
            id = "preview-card-7",
            type = CardType.QUOTE,
            content = "Innovation distinguishes between a leader and a follower.",
            author = "Steve Jobs",
            tags = listOf("创新", "领导力"),
            isFavorite = true,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                quoteAuthor = "Steve Jobs",
                quoteCategory = "BUSINESS"
            )
        ),
        Card(
            id = "preview-card-8",
            type = CardType.IDEA,
            content = "优化应用的启动性能，减少首次加载时间",
            tags = listOf("性能", "优化"),
            isFavorite = false,
            createdAt = now,
            updatedAt = now,
            metadata = CardMetadata(
                ideaPriority = "medium",
                ideaStatus = "new"
            )
        )
    )
}

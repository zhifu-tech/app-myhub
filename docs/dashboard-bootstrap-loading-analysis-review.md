# Dashboard Bootstrap 加载流程分析报告 - 专业评审

**评审视角**：资深 KMP 工程师  
**评审日期**：2026-01-28  
**评审对象**：`dashboard-bootstrap-loading-analysis.md`

---

## 📋 执行摘要

本评审从 KMP 架构设计、Kotlin Coroutines 最佳实践、Store5 使用模式、以及业内常见方案等维度，对原分析报告进行了深度评估。

**核心评价**：
- ✅ 报告结构清晰，问题识别准确
- ✅ 对竞态条件的分析到位
- ⚠️ 部分方案建议存在过度设计风险
- ⚠️ 缺少对 KMP 跨平台特性的考虑
- ⚠️ 对 Store5 的响应式特性利用不足

---

## 1. 报告分析

### 1.1 报告优点

#### ✅ 问题识别准确
- **竞态条件分析**：准确识别了 Bootstrap 异步初始化与 ViewModel 数据加载的时序问题
- **硬编码 userId**：正确指出了多用户支持的缺失
- **重复加载问题**：识别了 ViewModel 生命周期管理的问题

#### ✅ 技术理解深入
- 对 Store5 的 SourceOfTruth 机制理解正确
- 理解了离线优先的设计理念
- 对响应式数据流的分析到位

#### ✅ 方案设计全面
- 提供了多个可选方案（A/B/C/D）
- 考虑了不同场景的适用性
- 给出了实施步骤

### 1.2 报告不足

#### ⚠️ 方案 A 的问题：runBlocking 在 KMP 中的风险

**问题描述**：
报告推荐的方案 A 使用 `runBlocking` 在 `initKoin` 中同步等待 Bootstrap。这在 KMP 跨平台场景下存在严重问题：

1. **iOS 平台风险**：iOS 主线程（Main Thread）是 UI 线程，`runBlocking` 会阻塞 UI，导致应用启动时卡顿甚至 ANR
2. **Web 平台风险**：Web 平台（JS/WASM）是单线程模型，`runBlocking` 会阻塞整个事件循环
3. **Android 平台风险**：虽然 Android 可以在后台线程执行，但 `initKoin` 通常在 Application.onCreate 中调用，可能影响启动性能

**代码示例（问题代码）**：
```kotlin
// ❌ 不推荐：阻塞主线程
runBlocking {
    val userRepository = koinApplication.koin.get<UserRepository>()
    if (userRepository.hasUser().not()) {
        koinApplication.koin.get<Bootstrap>().initialize("default")
    }
}
```

#### ⚠️ 方案 B 的过度设计

**问题描述**：
方案 B 引入了 `BootstrapStateManager`，增加了系统复杂度，但实际收益有限：

1. **状态管理冗余**：Store5 本身已经提供了响应式状态管理
2. **额外依赖**：需要维护额外的状态管理基础设施
3. **测试复杂度**：增加了测试的复杂度

**更好的方案**：利用 Store5 的 `StoreReadResponse` 状态，无需额外状态管理。

#### ⚠️ 缺少对 KMP 平台特性的考虑

**问题描述**：
报告没有充分考虑 KMP 跨平台特性：

1. **平台差异**：iOS/Android/Web 的线程模型不同
2. **启动流程差异**：各平台的 Application 生命周期不同
3. **性能要求差异**：移动端对启动性能更敏感

#### ⚠️ 对 Store5 响应式特性利用不足

**问题描述**：
报告虽然提到了 Store5 的自动更新特性，但在方案设计中没有充分利用：

1. **SourceOfTruth 的 Flow 特性**：当数据库写入后，Flow 会自动推送更新
2. **StoreReadResponse 状态**：可以利用 `Loading`、`Data`、`Error` 状态，无需额外状态管理
3. **幂等性保证**：Store5 已经提供了幂等性保证

---

## 2. 专业优化建议

### 2.1 推荐方案：通过 Stream 获取用户 ID，然后执行 streamCards（最佳方案）⭐

#### 2.1.1 设计思路

**核心原则**：
1. **完全响应式**：通过 `observeUser()` Flow 监听用户创建，无需轮询
2. **链式响应**：用户存在 → 获取 userId → streamCards，形成响应式数据流
3. **不阻塞主线程**：Bootstrap 保持异步执行
4. **利用 Store5 响应式特性**：数据写入后自动推送更新
5. **平台无关**：适用于所有 KMP 平台

**数据流设计**：
```
observeUser() Flow
  └─> 用户创建/存在
      └─> 获取 userId
          └─> streamCards(userId)
              └─> Store5 自动推送数据更新
```

#### 2.1.2 实现方案

**修改 `Koin.kt`**（保持异步，但添加完成标记）：
```kotlin
@OptIn(ExperimentalStoreApi::class)
fun initKoin(platformSpecificConfig: (KoinApplication.() -> Unit)? = null) {
    // ... 现有代码 ...

    val koinApplication = startKoin {
        // ... 现有代码 ...
    }

    // Bootstrap 异步初始化（不阻塞）
    appScope.launch {
        try {
            val userRepository = koinApplication.koin.get<UserRepository>()
            if (userRepository.hasUser().not()) {
                logger.info { "First launch detected, initializing bootstrap..." }
                koinApplication.koin.get<Bootstrap>().initialize("default")
                logger.info { "Bootstrap initialization completed" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Bootstrap initialization failed" }
            // 可以选择继续启动或抛出异常
        }
    }

    // ... 其他初始化代码 ...
}
```

**优化 `DashboardViewModel.kt`**（完全响应式方案）：
```kotlin
class DashboardViewModel(
    private val cardRepository: CardRepository,
    private val localUserDataSource: LocalUserDataSource,
    private val coroutineScope: CoroutineScope
) {
    private val logger = logger("Dashboard")

    private val _uiState = MutableStateFlow<DashboardUiState>(
        DashboardUiState.InitialLoading()
    )

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * 加载 Dashboard 数据
     * 通过响应式流链式获取：用户 → userId → cards
     */
    private fun loadDashboardData() {
        logger.info { "Loading dashboard data" }
        
        coroutineScope.launch {
            try {
                // 第一步：通过 observeUser() Flow 监听用户创建
                // 当 Bootstrap 创建用户后，Flow 会自动推送更新
                localUserDataSource.observeUser()
                    .onEach { user ->
                        logger.info { "User observed: ${user.id}" }
                        
                        // 第二步：用户存在后，获取 userId 并 streamCards
                        loadCardsForUser(user.id)
                    }
                    .catch { e ->
                        logger.error(e) { "Failed to observe user" }
                        handleError(e)
                    }
                    .launchIn(coroutineScope)
            } catch (e: Exception) {
                logger.error(e) { "Failed to start user observation" }
                handleError(e)
            }
        }
    }

    /**
     * 为指定用户加载卡片数据
     * 使用 streamCards 监听数据变化，Store5 会在数据写入后自动推送更新
     */
    private fun loadCardsForUser(userId: String) {
        logger.info { "Loading cards for user: $userId" }
        
        cardRepository.streamCards(userId, refresh = false)
            .onEach { response ->
                handleStoreResponse(response)
            }
            .catch { e ->
                logger.error(e) { "Failed to load cards for user: $userId" }
                handleError(e)
            }
            .launchIn(coroutineScope)
    }

    private fun handleStoreResponse(response: StoreReadResponse<CardStoreData.Collection>) {
        when (response) {
            is StoreReadResponse.Loading -> {
                logger.info { "Loading cards from ${response.origin}" }
                // 保持 InitialLoading 状态，不更新 UI
            }

            is StoreReadResponse.Data -> {
                val cards = response.value.cards
                logger.info { "Received ${cards.size} cards from ${response.origin}" }
                
                if (cards.isNotEmpty()) {
                    updateUiStateWithCards(cards)
                }
                // 如果 cards 为空，保持 InitialLoading 状态
                // Store5 会在 Bootstrap 写入数据后自动推送更新
            }

            is StoreReadResponse.Error -> {
                val errorMessage = response.errorMessageOrNull() ?: "Unknown error"
                logger.error { "Error loading cards: $errorMessage" }
                
                // 网络错误时，尝试从本地加载
                handleNetworkError(errorMessage)
            }

            else -> {
                // 其他状态（NoNewData, Initial）不需要特殊处理
            }
        }
    }

    private fun updateUiStateWithCards(cards: List<Card>) {
        val recentCards = cards
            .sortedByDescending { it.updatedAt }
            .take(10)

        val favoriteCards = cards.filter { it.isFavorite }

        val reviewProgress = ReviewProgress(
            completed = 10,
            total = 15
        )

        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is DashboardUiState.InitialLoading -> DashboardUiState.Content(
                statistics = Statistics(
                    totalCards = cards.size,
                    favoriteCards = favoriteCards.size,
                    recentEdits = recentCards.size,
                    lastSyncTime = Clock.System.now().toEpochMilliseconds()
                ),
                recentCards = recentCards,
                favoriteCards = favoriteCards,
                lastSyncTime = Clock.System.now().toEpochMilliseconds(),
                reviewProgress = reviewProgress,
                showFocusReview = true
            )

            is DashboardUiState.Content -> currentState.copy(
                statistics = Statistics(
                    totalCards = cards.size,
                    favoriteCards = favoriteCards.size,
                    recentEdits = recentCards.size,
                    lastSyncTime = Clock.System.now().toEpochMilliseconds()
                ),
                recentCards = recentCards,
                favoriteCards = favoriteCards,
                lastSyncTime = Clock.System.now().toEpochMilliseconds(),
                isRefreshing = false,
                reviewProgress = reviewProgress
            )
        }
    }

    private fun handleNetworkError(errorMessage: String) {
        val currentState = _uiState.value
        
        if (currentState is DashboardUiState.Content && currentState.recentCards.isNotEmpty()) {
            // 有本地数据，只更新错误信息
            _uiState.value = currentState.copy(
                error = "网络连接失败，显示本地数据",
                isRefreshing = false
            )
        }
        // 如果没有数据，保持 InitialLoading 状态，等待 Store5 从本地加载
    }

    private fun handleError(e: Exception) {
        _uiState.value = DashboardUiState.Content(
            statistics = Statistics(),
            recentCards = emptyList(),
            favoriteCards = emptyList(),
            lastSyncTime = null,
            error = e.message ?: "无法加载数据",
            showFocusReview = false
        )
    }
}
```

#### 2.1.3 方案优势

1. **✅ 完全响应式**：通过 Flow 监听用户创建，无需轮询或等待
2. **✅ 链式响应**：用户 → userId → cards，形成完整的响应式数据流
3. **✅ 不阻塞主线程**：适用于所有 KMP 平台
4. **✅ 利用 Store5 响应式特性**：数据写入后自动推送更新
5. **✅ 代码简洁优雅**：无需额外的状态管理基础设施，无需轮询
6. **✅ 自动处理时序问题**：无论 Bootstrap 何时完成，Flow 都会自动推送更新
7. **✅ 平台无关**：适用于 iOS/Android/Web

#### 2.1.4 方案对比

| 特性 | 方案 A (runBlocking) | 方案 B (状态管理) | 方案 C (轮询等待) | **本方案 (Stream)** |
|------|---------------------|-----------------|------------------|-------------------|
| 响应式 | ❌ | ⚠️ | ❌ | ✅ |
| 不阻塞主线程 | ❌ | ✅ | ✅ | ✅ |
| 代码简洁 | ✅ | ❌ | ⚠️ | ✅ |
| 处理时序问题 | ✅ | ✅ | ⚠️ | ✅ |
| 平台兼容性 | ❌ | ✅ | ✅ | ✅ |
| 性能 | ⚠️ | ✅ | ⚠️ | ✅ |

#### 2.1.5 潜在问题与解决方案

**问题 1：首次启动可能短暂显示空状态**

**解决方案**：
- 这是可接受的设计权衡
- Store5 会在数据写入后自动推送更新（通常在 100-500ms 内）
- 可以通过优化 Bootstrap 性能（批量写入、事务）来减少等待时间
- UI 可以显示优雅的加载状态

**问题 2：observeUser() 在用户不存在时可能抛出异常**

**解决方案**：
```kotlin
localUserDataSource.observeUser()
    .catch { e ->
        // 用户不存在时，Flow 可能抛出异常
        // 可以捕获并保持 InitialLoading 状态
        logger.warn(e) { "User not found, waiting for bootstrap..." }
        // 不更新 UI 状态，保持 InitialLoading
    }
    .filter { it != null } // 过滤掉 null 值
    .onEach { user ->
        loadCardsForUser(user.id)
    }
    .launchIn(coroutineScope)
```

或者使用 `observeUserOrNull()` 如果存在的话：
```kotlin
// 如果 LocalUserDataSource 提供 observeUserOrNull()
localUserDataSource.observeUserOrNull()
    .filterNotNull() // 只处理非 null 的用户
    .onEach { user ->
        loadCardsForUser(user.id)
    }
    .launchIn(coroutineScope)
```

**修改后的 `DashboardModule.kt`**：
```kotlin
fun dashboardModule() = module {
    factory {
        DashboardViewModel(
            cardRepository = get<CardRepository>(),
            localUserDataSource = get<LocalUserDataSource>(),
            coroutineScope = get<CoroutineScope>()
        )
    }
}
```

**注意**：需要确保 `LocalUserDataSource` 在 Koin 中已注册。如果还没有，需要在 `LocalDataSourceModule` 中注册。

### 2.2 优化建议：Bootstrap 性能优化

#### 2.2.1 批量写入优化

**当前问题**：
Bootstrap 逐个写入数据，性能较差。

**优化方案**：
```kotlin
class Bootstrap(
    // ... 现有依赖 ...
) {
    suspend fun initialize(localeTag: String) {
        logger.info { "Initializing bootstrap with locale tag: $localeTag" }
        val config = configBuilder().buildConfig(
            userId = generateUUId(),
            localeTag = localeTag,
        )
        
        // 使用事务批量写入
        withTransaction {
            userRepository.insertUser(config.user)
            userRepository.insertUserPreferences(config.userPreferences)
            
            // 批量插入标签
            tagRepository.insertTags(config.tags)
            
            // 批量插入合集
            collectionRepository.insertCollections(config.collections)
            
            // 批量插入模板
            cardTemplateRepository.insertTemplates(config.templates, config.userId, needSync = false)
            
            // 批量插入卡片
            cardRepository.insertCards(config.cards, needSync = false)
        }
    }
}
```

#### 2.2.2 异步资源加载

**优化方案**：
```kotlin
class DefaultBootstrapConfigBuilder(
    private val resourceLoader: BootstrapResourceLoader
) {
    suspend fun buildConfig(
        userId: String,
        localeTag: String
    ): BootstrapConfig {
        // 并行加载所有资源
        val (tags, collections, templates, cards) = coroutineScope {
            awaitAll(
                async { resourceLoader.loadTags(localeTag) },
                async { resourceLoader.loadCollections(localeTag) },
                async { resourceLoader.loadTemplates(localeTag) },
                async { resourceLoader.loadCards(localeTag) }
            )
        }
        
        // ... 组装配置 ...
    }
}
```

### 2.3 优化建议：ViewModel 生命周期优化

#### 2.3.1 避免重复订阅

**问题**：
每次创建 ViewModel 都会创建新的 Flow 订阅，可能导致资源浪费。

**解决方案**：
```kotlin
class DashboardViewModel(
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val coroutineScope: CoroutineScope
) {
    private var dataLoadJob: Job? = null

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // 取消之前的订阅
        dataLoadJob?.cancel()
        
        dataLoadJob = coroutineScope.launch {
            // ... 数据加载逻辑 ...
        }
    }
    
    // 在 ViewModel 清理时取消订阅
    fun onCleared() {
        dataLoadJob?.cancel()
    }
}
```

**注意**：在 Compose 中，ViewModel 的生命周期由 `viewModel()` 管理，通常不需要手动清理。但如果使用 `factory` 创建，需要注意资源释放。

---

## 3. 业内常见方案

### 3.1 Android 平台常见方案

#### 方案 1：WorkManager + 启动画面（Splash Screen）

**实现方式**：
```kotlin
// Application.onCreate
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 显示启动画面
        showSplashScreen()
        
        // 异步初始化
        WorkManager.getInstance(this)
            .enqueue(OneTimeWorkRequestBuilder<BootstrapWorker>().build())
    }
}

// ViewModel
class DashboardViewModel : ViewModel() {
    init {
        viewModelScope.launch {
            // 等待 Bootstrap 完成
            val workInfo = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork("bootstrap")
                .first()
            
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                loadDashboardData()
            }
        }
    }
}
```

**优点**：
- 利用系统级任务调度
- 支持后台初始化
- 可以显示启动进度

**缺点**：
- 依赖 Android 特定 API
- 不适合 KMP 跨平台场景

#### 方案 2：App Startup + Initializer

**实现方式**：
```kotlin
class BootstrapInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // 同步初始化（在后台线程）
        runBlocking(Dispatchers.IO) {
            val userRepository = koinApplication.koin.get<UserRepository>()
            if (userRepository.hasUser().not()) {
                koinApplication.koin.get<Bootstrap>().initialize("default")
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
```

**优点**：
- 系统级初始化顺序控制
- 可以指定依赖关系

**缺点**：
- 仅适用于 Android
- 仍然可能阻塞启动

### 3.2 iOS 平台常见方案

#### 方案：AppDelegate + DispatchQueue

**实现方式**：
```swift
func application(_ application: UIApplication, 
                 didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    
    // 显示启动画面
    showLaunchScreen()
    
    // 异步初始化
    DispatchQueue.global(qos: .userInitiated).async {
        let userRepository = Koin.shared.get() as UserRepository
        if !userRepository.hasUser() {
            let bootstrap = Koin.shared.get() as Bootstrap
            bootstrap.initialize(localeTag: "default")
        }
        
        // 初始化完成后，切换到主线程更新 UI
        DispatchQueue.main.async {
            hideLaunchScreen()
        }
    }
    
    return true
}
```

**优点**：
- 不阻塞主线程
- 可以显示启动画面

**缺点**：
- 需要平台特定代码
- 不适合 KMP 共享逻辑

### 3.3 KMP 跨平台方案（推荐）

#### 方案：响应式初始化 + 状态管理

**实现方式**（已在 2.1 节详细说明）：

**核心思想**：
1. Bootstrap 异步初始化，不阻塞主线程
2. ViewModel 订阅数据流，自动接收更新
3. 利用 Store5 的响应式特性，数据写入后自动推送

**优点**：
- ✅ 跨平台一致
- ✅ 不阻塞主线程
- ✅ 利用现有 Store5 基础设施
- ✅ 代码简洁

**适用场景**：
- KMP 跨平台应用
- 使用 Store5 作为数据层
- 需要离线优先支持

### 3.4 其他业内方案

#### 方案 1：Preference DataStore / SharedPreferences 标记

**实现方式**：
```kotlin
// 使用 DataStore 存储初始化状态
val bootstrapComplete = dataStore.data
    .map { it[BootstrapKeys.INITIALIZED] ?: false }
    .first()

if (!bootstrapComplete) {
    bootstrap.initialize("default")
    dataStore.edit { it[BootstrapKeys.INITIALIZED] = true }
}
```

**优点**：
- 简单直接
- 可以持久化状态

**缺点**：
- 需要额外的存储机制
- 状态管理分散

#### 方案 2：Flow 状态管理

**实现方式**：
```kotlin
class BootstrapStateManager {
    private val _state = MutableStateFlow<BootstrapState>(BootstrapState.NotStarted)
    val state: StateFlow<BootstrapState> = _state.asStateFlow()
    
    suspend fun initialize() {
        _state.value = BootstrapState.InProgress
        try {
            bootstrap.initialize("default")
            _state.value = BootstrapState.Completed
        } catch (e: Exception) {
            _state.value = BootstrapState.Failed(e)
        }
    }
}

// ViewModel
class DashboardViewModel(
    private val bootstrapStateManager: BootstrapStateManager
) {
    init {
        viewModelScope.launch {
            bootstrapStateManager.state
                .first { it is BootstrapState.Completed || it is BootstrapState.Failed }
            
            if (bootstrapStateManager.state.value is BootstrapState.Completed) {
                loadDashboardData()
            }
        }
    }
}
```

**优点**：
- 明确的状态管理
- 可以显示初始化进度

**缺点**：
- 增加系统复杂度
- 与 Store5 的状态管理可能重复

---

## 4. 可借鉴的业内最佳实践

### 4.1 启动性能优化

#### ✅ 延迟初始化（Lazy Initialization）

**实践**：
- 只初始化必要的组件
- 非关键组件延迟到首次使用时初始化
- 使用 `lazy` 委托或 `by lazy` 语法

**示例**：
```kotlin
class DashboardViewModel {
    // 延迟初始化统计服务
    private val analytics by lazy { AnalyticsManager() }
    
    fun trackEvent(event: String) {
        analytics.track(event) // 首次调用时才初始化
    }
}
```

#### ✅ 并行初始化

**实践**：
- 使用 `coroutineScope { awaitAll(...) }` 并行初始化独立组件
- 减少总初始化时间

**示例**：
```kotlin
coroutineScope {
    awaitAll(
        async { initializeAnalytics() },
        async { initializeCrashReporting() },
        async { initializeBootstrap() }
    )
}
```

### 4.2 状态管理最佳实践

#### ✅ 单一数据源（Single Source of Truth）

**实践**：
- Store5 的 SourceOfTruth 就是单一数据源
- ViewModel 只负责 UI 状态转换，不存储业务数据

**示例**：
```kotlin
// ✅ 正确：从 Repository 获取数据
val cards = cardRepository.streamCards(userId)

// ❌ 错误：在 ViewModel 中缓存数据
private var cachedCards: List<Card>? = null
```

#### ✅ 响应式数据流

**实践**：
- 使用 Flow 监听数据变化
- 避免轮询或手动刷新
- 利用 Store5 的自动更新特性

### 4.3 错误处理最佳实践

#### ✅ 优雅降级（Graceful Degradation）

**实践**：
- 网络错误时使用本地数据
- 显示明确的错误提示
- 提供重试机制

**示例**：
```kotlin
when (response) {
    is StoreReadResponse.Error -> {
        // 尝试从本地加载
        val localData = loadFromLocal()
        if (localData != null) {
            showData(localData)
            showError("网络连接失败，显示本地数据")
        } else {
            showError("无法加载数据")
            showRetryButton()
        }
    }
}
```

### 4.4 测试最佳实践

#### ✅ 依赖注入

**实践**：
- 使用 Koin 等 DI 框架
- 便于测试时替换实现

**示例**：
```kotlin
// 生产环境
single<CardRepository> { CardRepositoryImpl(...) }

// 测试环境
single<CardRepository> { FakeCardRepository() }
```

#### ✅ 可测试性设计

**实践**：
- ViewModel 逻辑与平台代码分离
- 使用协程测试工具（如 `runTest`）

**示例**：
```kotlin
@Test
fun `test dashboard loads data after bootstrap`() = runTest {
    val viewModel = DashboardViewModel(
        cardRepository = fakeRepository,
        userRepository = fakeUserRepository,
        coroutineScope = testScope
    )
    
    // 等待数据加载
    advanceUntilIdle()
    
    // 验证状态
    assertEquals(DashboardUiState.Content(...), viewModel.uiState.value)
}
```

---

## 5. 总结与建议

### 5.1 报告评价

**总体评价**：⭐⭐⭐⭐（4/5）

**优点**：
- 问题识别准确
- 技术理解深入
- 方案设计全面

**不足**：
- 部分方案存在平台兼容性问题
- 对 Store5 响应式特性利用不足
- 缺少对 KMP 跨平台特性的考虑

### 5.2 推荐实施方案

**短期（1-2 周）**：
1. ✅ **采用 2.1 节的 Stream 响应式方案**（通过 observeUser() → streamCards）
2. ✅ 移除硬编码 userId，通过 observeUser() Flow 获取
3. ✅ 优化 Bootstrap 批量写入性能
4. ✅ 确保 LocalUserDataSource 在 Koin 中已注册

**中期（1-2 月）**：
1. ✅ 添加初始化超时机制
2. ✅ 优化错误处理和用户提示
3. ✅ 添加单元测试和集成测试

**长期（3-6 月）**：
1. ✅ 考虑引入启动画面（Splash Screen）
2. ✅ 优化首次启动体验
3. ✅ 考虑引入初始化进度指示

### 5.3 关键决策点

1. **是否阻塞主线程**：❌ 不推荐（KMP 跨平台考虑）
2. **是否引入额外状态管理**：❌ 不推荐（Store5 已提供）
3. **是否使用轮询等待**：❌ 不推荐（使用 Flow 监听更优雅）
4. **是否使用 Stream 响应式方案**：✅ **强烈推荐**（通过 observeUser() → streamCards）
5. **是否优化 Bootstrap 性能**：✅ 强烈推荐（批量写入、并行加载）

---

## 附录

### A. 相关资源

- [Store5 官方文档](https://github.com/MobileNativeFoundation/Store)
- [Kotlin Coroutines 最佳实践](https://kotlinlang.org/docs/coroutines-guide.html)
- [Android App Startup](https://developer.android.com/topic/libraries/app-startup)
- [iOS App Lifecycle](https://developer.apple.com/documentation/uikit/app_and_environment/managing_your_app_s_life_cycle)

### B. 代码审查清单

- [ ] Bootstrap 初始化不阻塞主线程
- [ ] **ViewModel 通过 observeUser() Flow 获取 userId（推荐）**
- [ ] **使用 streamCards(userId) 监听卡片数据变化**
- [ ] 利用 Store5 响应式特性，避免轮询
- [ ] 错误处理优雅降级
- [ ] 处理 observeUser() 在用户不存在时的异常情况
- [ ] 优化 Bootstrap 批量写入性能
- [ ] 确保 LocalUserDataSource 在 Koin 中已注册
- [ ] 添加单元测试
- [ ] 添加集成测试

---

**评审完成日期**：2026-01-28  
**评审人**：资深 KMP 工程师

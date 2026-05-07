# 卡片详情页设计方案

## 📋 概述

本文档描述了卡片详情页的完整设计方案。卡片详情页是用户查看、编辑和管理单个卡片的完整功能页面，支持分享、编辑、标签管理、元数据查看、个人笔记等复杂功能。

## 🎯 设计目标

1. **功能完整性**：提供查看、编辑、分享、删除等完整的卡片管理功能
2. **多卡片类型支持**：通过复用现有 CardComponent，自动支持所有卡片类型
3. **响应式设计**：适配移动端、平板和桌面端的不同布局需求
4. **实时数据同步**：基于 Flow 实现详情页与列表页的数据实时同步
5. **可扩展性**：易于添加新的功能模块（如分享、导出等）
6. **架构一致性**：与现有 feature 模块（Dashboard、Settings）保持一致的架构设计

## 🏗️ 架构设计

### 模块组织方案

**推荐方案：创建独立的 `feature:card` 模块**

#### 为什么选择独立模块？

1. **职责分离**：

    - `component/card` 是纯展示型组件，不应该包含业务逻辑
    - 详情页是完整功能（包含 ViewModel、业务逻辑、导航），属于功能层

2. **符合现有架构**：

    - 与 `feature/dashboard`、`feature/settings` 保持一致
    - 遵循 MVVM 架构模式

3. **可维护性**：

    - 独立模块便于测试和维护
    - 清晰的模块边界，减少耦合

4. **可扩展性**：
    - 后续可独立扩展（如分享、导出等）
    - 不影响其他模块

#### 架构对比

```
❌ 不推荐：合并在 component/card 中
- component/card 是纯展示型组件，不应该包含业务逻辑
- 会破坏现有的架构分层（组件层 vs 功能层）
- 难以维护和测试

✅ 推荐：独立的 feature:card 模块
- 符合 MVVM 架构（ViewModel + Screen）
- 与现有 feature 模块保持一致
- 职责清晰，易于扩展
```

### 模块结构

```
feature/card/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/tech/zhifu/app/myhub/feature/card/
│   │   │   ├── CardDetailScreen.kt          # 主界面
│   │   │   ├── CardDetailViewModel.kt      # ViewModel
│   │   │   ├── CardDetailUiState.kt         # UI 状态
│   │   │   ├── components/                  # 子组件
│   │   │   │   ├── CardDetailHeader.kt      # 头部导航
│   │   │   │   ├── CardDetailContent.kt    # 卡片内容展示区
│   │   │   │   ├── CardDetailActions.kt     # 操作面板（分享、编辑等）
│   │   │   │   ├── CardDetailTags.kt        # 标签管理
│   │   │   │   ├── CardDetailMetadata.kt   # 元数据展示
│   │   │   │   └── CardDetailNotes.kt      # 个人笔记
│   │   │   └── di/
│   │   │       └── CardDetailModule.kt       # Koin 模块
│   │   └── composeResources/                # 多语言资源
│   │       └── values/
│   │           ├── strings.xml
│   │           ├── values-zh-rCN/strings.xml
│   │           ├── values-zh-rTW/strings.xml
│   │           └── values-ja/strings.xml
│   └── devMain/                              # Preview 支持
│       └── kotlin/.../feature/card/
│           └── CardDetailScreen.dev.kt
├── build.gradle.kts
└── README.md
```

### 分层架构

```text
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  CardDetailScreen, CardDetailHeader, CardDetailContent   │
│  CardDetailActions, CardDetailTags, etc.                 │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              ViewModel Layer                             │
│  ┌──────────────────────────────────────────────────┐   │
│  │  CardDetailViewModel                            │   │
│  │  - loadCard(cardId)                             │   │
│  │  - shareCard()                                    │   │
│  │  - editCard()                                    │   │
│  │  - deleteCard()                                  │   │
│  │  - toggleFavorite()                              │   │
│  │  - updateTags(tags)                              │   │
│  │  - updateNotes(notes)                            │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Repository Layer                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ReactiveCardRepository                          │   │
│  │  - observeCard(id): Flow<Card?>                  │   │
│  │  - updateCard(card)                              │   │
│  │  - deleteCard(id)                                 │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ShareService (未来扩展)                         │   │
│  │  - shareCard(card): ShareResult                  │   │
│  │  - exportCard(card, format): ExportResult       │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Component Layer                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │  CardComponent (复用现有组件)                    │   │
│  │  - 自动支持所有卡片类型                          │   │
│  │  - 详情页模式渲染                                │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## 📦 核心组件设计

### 1. CardDetailScreen（主界面）

```kotlin
/**
 * 卡片详情页主界面
 *
 * @param cardId 卡片 ID
 * @param onNavigateBack 返回导航回调
 */
@Composable
fun CardDetailScreen(
    cardId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CardDetailViewModel = koinInject<CardDetailViewModel>()
)
```

**布局结构**（参考 demo）：

- **移动端（Compact）**：垂直布局

    - 头部导航
    - 卡片内容展示区
    - 操作面板（底部）

- **桌面端（Expanded）**：左右分栏布局
    - 左侧（8/12 列）：卡片内容展示区
    - 右侧（4/12 列）：操作面板（分享、编辑、标签、元数据、笔记）

### 2. CardDetailViewModel

```kotlin
/**
 * 卡片详情页 ViewModel
 * 管理详情页的状态和业务逻辑
 *
 * @param cardId 卡片 ID（作为构造参数，确保生命周期正确）
 * @param cardRepository 卡片仓库
 * @param shareService 分享服务（未来扩展，可选）
 */
class CardDetailViewModel(
    private val cardId: String,
    private val cardRepository: ReactiveCardRepository,
    private val shareService: ShareService? = null
) : ViewModel() {

    // 使用 viewModelScope，而不是外部传入的 CoroutineScope
    // 确保生命周期正确，跨平台行为一致
    private val scope = viewModelScope

    private val _uiState = MutableStateFlow<CardDetailUiState>(
        CardDetailUiState.Loading(cardId = cardId)
    )
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    // 删除确认对话框状态
    private val _showDeleteConfirm = MutableStateFlow(false)
    val showDeleteConfirm: StateFlow<Boolean> = _showDeleteConfirm.asStateFlow()

    // 笔记输入流（用于防抖保存）
    private val notesInputFlow = MutableStateFlow<String>("")

    init {
        // 初始化时开始观察卡片数据
        observeCard()

        // 笔记防抖保存（500ms debounce + distinctUntilChanged）
        notesInputFlow
            .debounce(500)
            .distinctUntilChanged()
            .onEach { notes ->
                saveNotes(notes)
            }
            .catch { e ->
                // 处理保存错误
                updateError("Failed to save notes: ${e.message}")
            }
            .launchIn(scope)
    }

    /**
     * 观察卡片数据变化（响应式）
     *
     * 关键设计：
     * - 当 card 为 null 时，明确处理为"卡片不存在或已删除"的错误状态
     * - 避免使用 `card ?: return@onEach` 吞掉删除语义
     */
    private fun observeCard() {
        cardRepository.observeCard(cardId)
            .onEach { card ->
                if (card == null) {
                    // 卡片不存在或已被删除，明确转换为错误状态
                    _uiState.value = CardDetailUiState.Error(
                        message = "Card not found or deleted",
                        cardId = cardId,
                        retryable = false  // 已删除的卡片不可重试
                    )
                } else {
                    // 卡片存在，更新状态
                    _uiState.value = when (val current = _uiState.value) {
                        is CardDetailUiState.Loading ->
                            CardDetailUiState.Content(card = card)
                        is CardDetailUiState.Content ->
                            current.copy(card = card)
                        is CardDetailUiState.Error ->
                            CardDetailUiState.Content(card = card)
                    }
                }
            }
            .catch { e ->
                _uiState.value = CardDetailUiState.Error(
                    message = e.message ?: "Unknown error",
                    cardId = cardId,
                    retryable = true
                )
            }
            .launchIn(scope)
    }

    /**
     * 分享卡片
     */
    fun shareCard()

    /**
     * 编辑卡片
     */
    fun editCard()

    /**
     * 显示删除确认对话框
     */
    fun showDeleteConfirm() {
        _showDeleteConfirm.value = true
    }

    /**
     * 取消删除
     */
    fun cancelDelete() {
        _showDeleteConfirm.value = false
    }

    /**
     * 确认删除卡片
     */
    fun confirmDelete()

    /**
     * 切换收藏状态
     */
    fun toggleFavorite()

    /**
     * 更新标签
     */
    fun updateTags(tags: List<String>)

    /**
     * 更新个人笔记（UI 层调用，内部会防抖保存）
     *
     * 数据流设计原则：
     * - UI 层：立即更新 ViewModel State（用户看到即时反馈）
     * - ViewModel State：是 UI 展示的"真源"（Single Source of Truth for UI）
     * - Repository：是最终一致性存储（通过防抖流异步保存）
     *
     * 这样设计的好处：
     * - 用户体验：输入即时反馈，无延迟感
     * - 性能：防抖减少数据库写入频率
     * - 一致性：UI 始终以 ViewModel State 为准，不受 Repository 写入时机影响
     */
    fun updateNotes(notes: String) {
        notesInputFlow.value = notes
        // UI 层立即更新显示，实际保存通过防抖流处理
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(
                card = currentState.card.copy(
                    // 假设 Card 有 notes 字段，或使用 metadata
                    // 这里需要根据实际 Card 模型调整
                )
            )
        }
    }

    /**
     * 实际保存笔记到 Repository（由防抖流调用）
     *
     * 注意：此方法由防抖流异步调用，不影响 UI 的即时显示
     */
    private suspend fun saveNotes(notes: String) {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(isSaving = true)
            try {
                val updatedCard = currentState.card.copy(
                    // 更新笔记字段
                )
                cardRepository.updateCard(updatedCard)
                _uiState.value = currentState.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    error = "Failed to save notes: ${e.message}"
                )
            }
        }
    }

    /**
     * 复制卡片内容
     */
    fun copyContent()

    /**
     * 清除错误状态
     */
    fun clearError()

    /**
     * 更新错误状态
     */
    private fun updateError(message: String) {
        val currentState = _uiState.value
        if (currentState is CardDetailUiState.Content) {
            _uiState.value = currentState.copy(error = message)
        }
    }
}
```

**关键设计要点**：

1. **cardId 作为构造参数**：确保 ViewModel 创建时就有 cardId，避免 init 中使用未定义变量
2. **使用 viewModelScope**：不接收外部 CoroutineScope，确保生命周期正确
3. **笔记防抖保存**：使用 `debounce(500) + distinctUntilChanged()` 避免频繁写入数据库
4. **删除确认机制**：通过 `showDeleteConfirm` StateFlow 控制确认对话框显示

### 3. CardDetailUiState

```kotlin
/**
 * 卡片详情页 UI 状态
 *
 * 使用 sealed class 表示不同的状态，确保状态互斥和类型安全
 */
sealed class CardDetailUiState {
    /**
     * 加载状态
     * 正在加载卡片数据
     */
    data class Loading(
        val cardId: String
    ) : CardDetailUiState()

    /**
     * 内容状态（有数据）
     * 卡片数据已加载，可以正常显示
     */
    data class Content(
        val card: Card,
        val isSharing: Boolean = false,      // 是否正在分享
        val isEditing: Boolean = false,      // 是否正在编辑
        val isSaving: Boolean = false,       // 是否正在保存
        val error: String? = null             // 错误信息（如果有）
    ) : CardDetailUiState()

    /**
     * 错误状态
     * 加载失败或操作失败
     */
    data class Error(
        val message: String,
        val cardId: String,
        val retryable: Boolean = true        // 是否可重试
    ) : CardDetailUiState()
}
```

**状态说明**：

- **`Loading`**: 初始加载状态，显示加载指示器
- **`Content`**: 内容状态，有数据可正常显示
    - `isSharing`: 分享操作进行中
    - `isEditing`: 编辑操作进行中
    - `isSaving`: 保存操作进行中
    - `error`: 错误信息（错误时仍显示数据）
- **`Error`**: 错误状态，加载失败
    - `retryable`: 是否可重试

### 4. CardDetailContent（卡片内容展示）

```kotlin
/**
 * 卡片内容展示区
 * 复用现有的 CardComponent 渲染卡片
 *
 * 使用 CardRenderMode.Detail 模式，显式表达详情页语义
 */
@Composable
fun CardDetailContent(
    card: Card,
    modifier: Modifier = Modifier
) {
    // 使用统一的 CardComponent 渲染卡片
    // 详情页使用 Detail 模式，禁用交互、放大显示
    CardComponent(
        card = card,
        mode = CardRenderMode.Detail,  // 显式指定详情页模式
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        // 详情页模式下，CardComponent 内部会：
        // - 禁用 hover / click 交互
        // - 放大字号和间距
        // - 显示更多信息（如果有）
        onCardClick = {}  // 详情页中点击卡片不跳转
    )
}
```

**CardRenderMode 枚举定义**（需要在 component/card 中添加）：

```kotlin
/**
 * 卡片渲染模式
 *
 * 设计考虑：预留扩展性，支持未来分享/导出场景
 */
enum class CardRenderMode {
    /**
     * 列表模式（默认）
     * 用于 Dashboard、搜索列表等场景
     */
    List,

    /**
     * 详情模式
     * 用于详情页，禁用交互、放大显示
     */
    Detail,

    /**
     * 预览模式（未来扩展）
     * 用于分享、导出等场景，只读展示
     */
    Preview
}
```

**注意**：`Preview` 模式目前不需要实现，但提前预留可以避免未来扩展时的重构。

**SectionCard 组件定义**（需要在 feature/card 中实现）：

```kotlin
/**
 * 统一的区域卡片容器
 * 用于包装 Tags、Metadata、Notes 等区域
 *
 * 提供统一的视觉样式：
 * - 卡片背景和边框
 * - 标题样式
 * - 内边距和间距
 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
```

**设计要点**：

- **复用现有组件**：使用 `CardComponent` 渲染，无需为每种类型重写
- **自动支持新类型**：新增卡片类型自动支持详情页
- **保持一致性**：与列表页的卡片样式保持一致

### 5. CardDetailActions（操作面板）

```kotlin
/**
 * 操作面板组件
 * 包含分享、编辑、删除、复制等操作按钮
 *
 * @param showDeleteConfirm 是否显示删除确认对话框（由 ViewModel 控制）
 */
@Composable
fun CardDetailActions(
    card: Card,
    isSharing: Boolean = false,
    showDeleteConfirm: Boolean = false,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,  // 显示确认对话框，而不是直接删除
    onConfirmDelete: () -> Unit,  // 确认删除
    onCancelDelete: () -> Unit,    // 取消删除
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 主要操作按钮
        Button(
            onClick = onShare,
            enabled = !isSharing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSharing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.Default.Share, contentDescription = null)
                Text("Share / Export")
            }
        }

        // 次要操作按钮组
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Default.Edit,
                label = "Edit",
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                onClick = onCopy,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.Delete,
                label = "Delete",
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            )
        }
    }
}
```

### 6. CardDetailTags（标签管理）

```kotlin
/**
 * 标签管理组件
 * 显示和管理卡片的标签
 *
 * 使用 SectionCard 包装，提供统一的视觉样式
 */
@Composable
fun CardDetailTags(
    tags: List<String>,
    onAddTag: () -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Tags",
        modifier = modifier
    ) {

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                TagChip(
                    tag = tag,
                    onRemove = { onRemoveTag(tag) }
                )
            }

            // 添加标签按钮
            AddTagButton(onClick = onAddTag)
        }
    }
}
```

### 7. CardDetailMetadata（元数据展示）

```kotlin
/**
 * 元数据展示组件
 * 显示卡片的创建时间、来源、模板等信息
 *
 * 使用 SectionCard 包装，提供统一的视觉样式
 */
@Composable
fun CardDetailMetadata(
    card: Card,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Metadata",
        modifier = modifier
    ) {

        MetadataRow(
            label = "Created",
            value = card.formatCreatedTime()
        )
        MetadataRow(
            label = "Source",
            value = card.metadata?.source ?: "N/A"
        )
        MetadataRow(
            label = "Template",
            value = card.metadata?.template ?: "Default"
        )
        MetadataRow(
            label = "Format",
            value = card.metadata?.format ?: "N/A"
        )
    }
}
```

**注意**：`SectionCard` 的使用方式：

```kotlin
SectionCard(title = "Metadata") {
    // 元数据内容
    MetadataRow(...)
}
```

### 8. CardDetailNotes（个人笔记）

```kotlin
/**
 * 个人笔记组件
 * 允许用户为卡片添加个人笔记和反思
 *
 * 使用 SectionCard 包装，提供统一的视觉样式
 *
 * 注意：onNotesChange 会触发防抖保存（500ms），由 ViewModel 处理
 */
@Composable
fun CardDetailNotes(
    notes: String,
    onNotesChange: (String) -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    SectionCard(
        title = "Personal Notes",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = "Saved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        TextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text("Add your reflections on this card here...") },
            maxLines = 5
        )
    }
}
```

## 🔄 数据同步策略

### 使用 ReactiveCardRepository.observeCard(id)

详情页使用 `observeCard(id)` 方法实时监听单个卡片的变化。

**关键设计**：

- ✅ **cardId 作为构造参数**：确保 ViewModel 创建时就有 cardId
- ✅ **使用 viewModelScope**：不接收外部 CoroutineScope，确保生命周期正确
- ✅ **在 init 中观察**：确保 ViewModel 创建后立即开始观察数据

**注意**：观察逻辑在 ViewModel 的 `init` 中，使用 `viewModelScope`，确保生命周期正确：

```kotlin
// CardDetailViewModel.kt
init {
    observeCard()  // 在 init 中调用，使用 viewModelScope
}

private fun observeCard() {
    cardRepository.observeCard(cardId)
        .onEach { card ->
            // 明确处理 null 情况：卡片不存在或已被删除
            if (card == null) {
                _uiState.value = CardDetailUiState.Error(
                    message = "Card not found or deleted",
                    cardId = cardId,
                    retryable = false
                )
            } else {
                _uiState.value = when (val current = _uiState.value) {
                    is CardDetailUiState.Loading ->
                        CardDetailUiState.Content(card = card)
                    is CardDetailUiState.Content ->
                        current.copy(card = card)
                    is CardDetailUiState.Error ->
                        CardDetailUiState.Content(card = card)
                }
            }
        }
        .catch { e ->
            _uiState.value = CardDetailUiState.Error(
                message = e.message ?: "Unknown error",
                cardId = cardId,
                retryable = true
            )
        }
        .launchIn(scope)  // 使用 viewModelScope，不是外部传入的 scope
}
```

**优势**：

- ✅ 效率更高，只监听单个卡片
- ✅ 自动同步更新（当其他页面修改卡片时，详情页自动更新）
- ✅ 适合详情页场景

### 数据同步场景

#### 场景 1：Dashboard 编辑卡片 → 详情页自动更新

```kotlin
// Dashboard 页面
CardComponent(
    card = card,
    onEdit = { card ->
        viewModel.updateCard(card)
        // 详情页自动更新（通过 observeCard）
    }
)

// 详情页
// 通过 observeCard 自动接收更新，无需手动刷新
```

#### 场景 2：详情页编辑卡片 → Dashboard 自动更新

```kotlin
// 详情页
fun editCard() {
    viewModelScope.launch {
        val updatedCard = card.copy(...)
        cardRepository.updateCard(updatedCard)
        // Dashboard 自动更新（通过 observeAllCards）
        // 详情页自动更新（通过 observeCard）
    }
}
```

## 🧭 导航集成

### 1. 定义路由常量

```kotlin
// navigation/Routes.kt
object Routes {
    const val CARD_DETAIL = "card_detail/{cardId}"

    fun cardDetail(cardId: String) = "card_detail/$cardId"
}
```

### 2. 扩展 Screen sealed class

```kotlin
// navigation/Navigation.kt
sealed class Screen(val route: String, val title: String) {
    // ... 现有 Screen

    /**
     * 卡片详情页
     *
     * @param cardId 卡片 ID
     */
    data class CardDetail(
        val cardId: String
    ) : Screen(Routes.CARD_DETAIL, "Card Detail") {
        companion object {
            fun createRoute(cardId: String) = Routes.cardDetail(cardId)

            fun fromRoute(route: String): CardDetail? {
                // 使用统一的路由常量，避免硬编码
                val pattern = Routes.CARD_DETAIL.replace("{cardId}", "(.+)")
                val match = Regex(pattern).find(route)
                return match?.groupValues?.get(1)?.let { cardId ->
                    CardDetail(cardId = cardId)
                }
            }
        }
    }
}
```

### 3. Koin 依赖注入配置

```kotlin
// feature/card/di/CardDetailModule.kt
val cardDetailModule = module {
    viewModel { (cardId: String) ->
        CardDetailViewModel(
            cardId = cardId,
            cardRepository = get(),
            shareService = getOrNull()  // 可选服务
        )
    }
}
```

### 4. 导航使用

```kotlin
// 在 Dashboard 或其他地方
CardComponent(
    card = card,
    onCardClick = {
        navigateTo(Screen.CardDetail(card.id))
    }
)

// 在 AppNavigation 中
@Composable
fun AppNavigation(currentScreen: Screen) {
    when (currentScreen) {
        is Screen.Dashboard -> DashboardScreen()
        is Screen.CardDetail -> CardDetailScreen(
            cardId = currentScreen.cardId,
            onNavigateBack = { /* 返回逻辑 */ }
        )
        // ...
    }
}
```

### 3. 返回导航

```kotlin
// CardDetailHeader.kt
@Composable
fun CardDetailHeader(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text("Back to Library")
        Text("/")
        Text("Card Details")
    }
}
```

## 📱 响应式布局设计

### 移动端（Compact）

```kotlin
@Composable
fun CardDetailScreen(...) {
    val windowSizeClass = windowSizeClass()

    when {
        windowSizeClass.isCompact -> {
            // 移动端：垂直布局
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                CardDetailHeader(
                    onNavigateBack = onNavigateBack
                )

                CardDetailContent(
                    card = card,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                CardDetailActions(...)
                CardDetailTags(...)
                CardDetailMetadata(...)
                CardDetailNotes(...)
            }
        }
        // ...
    }
}
```

### 桌面端（Expanded）

```kotlin
@Composable
fun CardDetailScreen(...) {
    val windowSizeClass = windowSizeClass()

    when {
        windowSizeClass.isExpanded -> {
            // 桌面端：左右分栏布局（参考 demo）
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 左侧：卡片内容展示区（8/12 列）
                Column(
                    modifier = Modifier
                        .weight(8f)
                        .padding(end = 16.dp)
                ) {
                    CardDetailHeader(onNavigateBack = onNavigateBack)
                    Spacer(modifier = Modifier.height(16.dp))
                    CardDetailContent(card = card)
                }

                // 右侧：操作面板（4/12 列）
                Column(
                    modifier = Modifier
                        .weight(4f)
                        .fillMaxHeight()
                ) {
                    CardDetailActions(...)
                    Spacer(modifier = Modifier.height(16.dp))
                    CardDetailTags(...)
                    Spacer(modifier = Modifier.height(16.dp))
                    CardDetailMetadata(...)
                    Spacer(modifier = Modifier.height(16.dp))
                    CardDetailNotes(...)
                }
            }
        }
    }
}
```

## 🔧 多卡片类型支持策略

### 方案：使用 CardComponent 接口 + 详情页适配器模式

**核心思路**：复用现有的 `CardComponent` 渲染卡片，详情页只是提供更大的展示空间和额外的操作功能。

```kotlin
// CardDetailContent.kt
@Composable
fun CardDetailContent(
    card: Card,
    modifier: Modifier = Modifier
) {
    // 使用统一的 CardComponent 渲染卡片
    // 详情页使用更大的尺寸和不同的布局
    CardComponent(
        card = card,
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        // 详情页模式下，可以禁用某些交互
        onCardClick = {}  // 详情页中点击卡片不跳转
    )
}
```

**优势**：

- ✅ **复用现有组件**：无需为每种类型重写详情页
- ✅ **自动支持新类型**：新增卡片类型自动支持详情页
- ✅ **保持一致性**：与列表页的卡片样式保持一致
- ✅ **易于维护**：只需维护一套卡片组件

## 🚀 实施计划

### 阶段 1：基础框架（MVP）

**目标**：实现基础的详情页框架，能够查看卡片内容

- [ ] 创建 `feature/card` 模块
- [ ] 实现基础 Screen、ViewModel、UiState
- [ ] 集成导航（扩展 Screen sealed class）
- [ ] 实现基础布局（移动端垂直布局）
- [ ] 集成 CardComponent 展示卡片内容
- [ ] 实现数据加载和错误处理

**验收标准**：

- 可以从 Dashboard 导航到详情页
- 能够显示卡片内容
- 能够返回上一页

### 阶段 2：核心功能

**目标**：实现完整的操作功能

- [ ] 实现操作面板（编辑、删除、收藏、复制）
    - [ ] 删除操作添加确认对话框（showDeleteConfirm）
    - [ ] 分享按钮（UI，功能在阶段 3）
- [ ] 实现标签管理（显示、添加、删除）
    - [ ] 使用 SectionCard 包装
- [ ] 实现元数据展示
    - [ ] 使用 SectionCard 包装
- [ ] 实现个人笔记功能
    - [ ] 笔记输入框
    - [ ] **防抖保存机制**（500ms debounce）
    - [ ] 保存状态指示
    - [ ] 使用 SectionCard 包装
- [ ] 实现响应式布局（桌面端左右分栏）
- [ ] 添加 CardRenderMode.Detail 支持（如果阶段 1 未完成）

**验收标准**：

- 所有操作按钮功能正常
- 删除操作有确认对话框
- 标签可以添加和删除
- 元数据正确显示
- 个人笔记可以编辑和保存（防抖保存正常工作）
- 桌面端布局正确
- SectionCard 样式统一

### 阶段 3：高级功能

**目标**：实现分享、导出等高级功能

- [ ] 实现分享功能（ShareService）
- [ ] 实现导出功能（图片、PDF 等）
- [ ] 实现复制内容功能
- [ ] 优化用户体验（加载动画、错误提示等）

**验收标准**：

- 分享功能正常工作
- 导出功能正常工作
- 复制功能正常工作
- 用户体验流畅

### 阶段 4：优化和完善

**目标**：优化性能、完善细节

- [ ] 响应式布局优化
- [ ] 动画效果（页面切换、操作反馈）
- [ ] 错误处理完善
- [ ] 性能优化（懒加载、缓存等）
- [ ] 可访问性支持
- [ ] 单元测试和 UI 测试

**验收标准**：

- 所有平台布局正确
- 动画流畅
- 错误处理完善
- 性能良好
- 测试覆盖率达到要求

## 📦 依赖关系

```
feature/card
├── component/card              # 使用 CardComponent 渲染卡片
├── datastore/repository-client  # 数据访问（ReactiveCardRepository）
├── datastore/model        # 数据模型（Card）
└── core/platform-compose       # 平台抽象（WindowSizeClass 等）
```

### build.gradle.kts 配置

```kotlin
dependencies {
    commonMain.dependencies {
        // Compose UI 依赖
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.ui)
        implementation(compose.components.resources)
        implementation(compose.materialIconsExtended)

        // 组件依赖
        implementation(projects.component.card)

        // 数据层依赖
        implementation(projects.datastoreRepositoryClient)
        implementation(projects.datastoreModel)

        // 平台抽象
        implementation(projects.core.platformCompose)

        // 依赖注入
        implementation(libs.koin.core)
        implementation(libs.koin.compose.viewmodel)

        // kotlinx-datetime 用于日期格式化
        implementation(libs.kotlinx.datetime)

        if (project.isDev()) {
            implementation(compose.components.uiToolingPreview)
        }
    }
}
```

## 🎨 UI 设计参考

### Demo 分析

参考提供的 demo (`code.html`)，详情页包含以下元素：

1. **头部导航**：

    - 返回按钮和面包屑导航
    - 标题显示

2. **左侧内容区（8/12 列）**：

    - 卡片标题
    - 卡片内容展示（大尺寸）
    - 背景装饰效果

3. **右侧操作面板（4/12 列）**：
    - **操作按钮组**：
        - 主要操作：分享/导出按钮（大按钮）
        - 次要操作：编辑、复制、删除（小按钮组）
    - **标签管理**：
        - 标签列表（带颜色标识）
        - 添加标签按钮
    - **元数据展示**：
        - 创建时间
        - 来源
        - 模板
        - 格式
    - **个人笔记**：
        - 笔记输入框
        - 保存状态指示

### 设计要点

1. **视觉层次**：

    - 主要操作（分享）使用大按钮突出显示
    - 次要操作使用小按钮组
    - 危险操作（删除）使用错误色

2. **信息组织**：

    - 使用卡片容器分组相关功能
    - 清晰的标题和分隔线
    - 合理的间距和留白

3. **交互反馈**：
    - 按钮 hover 效果
    - 加载状态指示
    - 保存状态提示

## 🔍 关键设计原则

1. **复用现有组件**：使用 `CardComponent` 渲染卡片内容，避免重复实现
2. **模块化设计**：复杂功能（分享、导出）独立为服务接口，便于扩展
3. **响应式数据流**：使用 Flow 实现数据同步，确保多页面数据一致性
4. **可扩展性**：新增卡片类型自动支持详情页，无需修改代码
5. **架构一致性**：与现有 feature 模块保持架构一致，便于维护

## 📚 相关文档

- [Card Component 模块设计](../component/card/README.md)
- [Dashboard 模块设计](../feature/dashboard/README.md)
- [卡片数据同步机制设计](../component/card/docs/card_data_synchronization.md)
- [项目整体架构](./myhub_architecture.md)

## 🚧 待解决问题

1. **分享服务设计**：

    - 需要设计 ShareService 接口
    - 需要实现平台特定的分享功能（Android、iOS、Web）

2. **导出功能设计**：

    - 需要设计导出格式（图片、PDF、Markdown 等）
    - 需要实现导出逻辑

3. **导航返回逻辑**：

    - 需要确定返回的目标页面（Dashboard、上一页等）
    - 需要处理深层链接场景

4. **性能优化**：
    - 大卡片内容的渲染优化
    - 图片加载和缓存策略

## 📝 更新日志

- **2026-01-09**: 初始版本，完成设计方案文档
- **2026-01-09**: 根据评审反馈修复关键问题：
    - ✅ ViewModel 使用 viewModelScope 而不是外部 CoroutineScope
    - ✅ cardId 作为构造参数
    - ✅ 笔记保存添加防抖机制
    - ✅ 删除操作添加确认对话框
    - ✅ 添加 CardRenderMode.Detail 模式支持
    - ✅ 使用 SectionCard 统一容器样式
    - ✅ 路由使用常量管理
- **2026-01-09**: 终稿级优化（根据架构评审）：
    - ✅ observeCard 明确处理 null 情况（卡片被删除）
    - ✅ 补充 notes 数据流设计原则说明（UI State 为真源）
    - ✅ CardRenderMode 预留 Preview 模式（未来扩展）

---

**维护者**: MyHub Team  
**最后更新**: 2025-01-09

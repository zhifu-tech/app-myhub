# Card Detail 模块

## 📋 概述

Card Detail 模块提供卡片详情页功能，允许用户查看、编辑和管理单个卡片。本模块采用响应式架构设计，基于 Kotlin Flow 实现实时数据更新，支持多平台（Android、iOS、Desktop、Web）统一体验。

## 🎯 当前状态

### ✅ 已完成（v1.0）

#### 核心功能

- [x] 模块基础结构创建
- [x] CardDetailScreen UI 实现（移动端、平板、桌面端响应式布局）
- [x] 基础组件实现（Header、Content）
- [x] CardDetailViewModel 完整实现（真实数据集成）
- [x] CardDetailUiState 状态定义（Loading、Content、Error）
- [x] 真实数据集成（`observeCard` 方法）
- [x] 导航集成（从 Dashboard 点击进入详情页）
- [x] 响应式布局完善（桌面端左右分栏）

#### 操作面板组件

- [x] **SectionCard** - 统一的区域卡片容器
- [x] **CardDetailActions** - 操作按钮（分享、编辑、删除、复制）
- [x] **CardDetailTags** - 标签管理（显示、删除标签）
- [x] **CardDetailMetadata** - 元数据展示（创建时间、来源、模板、格式）
- [x] **CardDetailNotes** - 个人笔记（多行输入、防抖保存）

#### 业务逻辑

- [x] 响应式数据观察（`observeCard` 实时更新）
- [x] 删除确认对话框
- [x] 笔记防抖保存（500ms debounce）
- [x] 标签更新功能
- [x] 错误处理和状态管理

### 🔄 待完善功能

#### 高优先级

##### 1. 添加标签对话框

**功能描述**：实现标签输入和添加功能

**实现要求**：

- 创建标签输入对话框组件（`AddTagDialog`）
- 验证标签格式（长度、字符限制）
- 检查标签重复性（避免重复标签）
- 集成到 `CardDetailTags` 的 `onAddTag` 回调
- 支持键盘输入和确认

**相关文件**：

- `components/CardDetailTags.kt` - 需要实现对话框
- `CardDetailViewModel.kt` - 可能需要添加验证逻辑

**验收标准**：

- [ ] 点击添加标签按钮显示输入对话框
- [ ] 输入标签后可以添加到卡片
- [ ] 重复标签会被拒绝并提示
- [ ] 标签格式验证（长度、特殊字符）

---

##### 2. 笔记字段确认与实现

**功能描述**：确认 Card 模型是否支持 notes 字段，并实现笔记保存功能

**实现要求**：

- 检查 `Card` 模型是否有 `notes` 字段
- 如果没有，选择以下方案之一：
  - **方案 A**：使用 `metadata` 存储笔记（推荐，无需修改模型）
  - **方案 B**：扩展 Card 模型添加 `notes` 字段（需要数据库迁移）
- 更新 `CardDetailViewModel.updateNotes()` 和 `saveNotes()` 方法
- 确保笔记数据能正确保存和加载

**相关文件**：

- `datastore/model/` - Card 模型定义
- `CardDetailViewModel.kt` - 笔记保存逻辑
- `components/CardDetailNotes.kt` - 笔记输入组件

**验收标准**：

- [ ] 笔记能正确保存到数据库
- [ ] 重新打开详情页时笔记能正确加载
- [ ] 防抖保存功能正常工作（500ms debounce）
- [ ] 保存状态指示器正确显示

---

##### 3. 编辑功能

**功能描述**：实现卡片编辑功能

**实现要求**：

- 选择实现方式：
  - **方案 A**：导航到独立编辑页面（推荐，适合复杂编辑）
  - **方案 B**：显示编辑对话框（适合简单编辑）
- 支持编辑以下字段：
  - 卡片内容（content）
  - 标题（title，如果有）
  - 作者（author）
  - 其他可编辑字段
- 编辑后自动保存并更新详情页

**相关文件**：

- `CardDetailViewModel.kt` - 编辑逻辑
- `components/CardDetailActions.kt` - 编辑按钮
- 可能需要创建 `feature/card-edit/` 模块（如果使用方案 A）

**验收标准**：

- [ ] 点击编辑按钮能进入编辑模式
- [ ] 编辑后能正确保存
- [ ] 详情页自动更新显示最新内容
- [ ] 编辑取消时恢复原内容

---

#### 中优先级

##### 4. 分享功能

**功能描述**：实现平台特定的分享逻辑

**实现要求**：

- 创建 `ShareService` 抽象接口（`core/platform/`）
- 实现平台特定实现：
  - **Android**: 使用 Intent 分享
  - **iOS**: 使用 UIActivityViewController
  - **Desktop**: 文件导出或剪贴板
  - **Web**: Navigator.share API
- 支持分享格式：
  - 纯文本
  - Markdown 格式
  - 图片（未来扩展）

**相关文件**：

- `core/platform/` - 平台特定实现
- `CardDetailViewModel.kt` - 分享逻辑
- `components/CardDetailActions.kt` - 分享按钮

**验收标准**：

- [ ] 各平台能正确调用分享功能
- [ ] 分享内容格式正确
- [ ] 分享状态正确显示（loading/success/error）

---

##### 5. 复制功能

**功能描述**：实现平台特定的剪贴板功能

**实现要求**：

- 创建 `ClipboardService` 抽象接口（`core/platform/`）
- 实现平台特定实现：
  - **Android**: 使用 ClipboardManager
  - **iOS**: 使用 UIPasteboard
  - **Desktop**: 使用系统剪贴板 API
  - **Web**: 使用 Clipboard API
- 支持复制卡片内容、标题、作者等

**相关文件**：

- `core/platform/` - 平台特定实现
- `CardDetailViewModel.kt` - 复制逻辑
- `components/CardDetailActions.kt` - 复制按钮

**验收标准**：

- [ ] 各平台能正确复制到剪贴板
- [ ] 复制后显示成功提示
- [ ] 复制内容格式正确

---

#### 低优先级

##### 6. 错误重试按钮

**功能描述**：在错误状态下添加重试功能

**实现要求**：

- 在 `CardDetailUiState.Error` 状态下显示重试按钮
- 实现 `retry()` 方法重新加载卡片
- 重试时显示加载状态

**相关文件**：

- `CardDetailScreen.kt` - 错误状态 UI
- `CardDetailViewModel.kt` - 重试逻辑

**验收标准**：

- [ ] 错误状态下显示重试按钮
- [ ] 点击重试能重新加载卡片
- [ ] 重试时显示加载状态

---

##### 7. 收藏功能

**功能描述**：在详情页添加收藏/取消收藏按钮

**实现要求**：

- 在 Header 或 Actions 中添加收藏按钮
- 显示当前收藏状态（已收藏/未收藏）
- 调用 `viewModel.toggleFavorite()`
- 收藏状态实时更新

**相关文件**：

- `components/CardDetailHeader.kt` 或 `CardDetailActions.kt`
- `CardDetailViewModel.kt` - 已有 `toggleFavorite()` 方法

**验收标准**：

- [ ] 收藏按钮正确显示状态
- [ ] 点击能切换收藏状态
- [ ] 状态实时更新（通过 observeCard）

---

##### 8. 加载状态优化

**功能描述**：优化加载状态的用户体验

**实现要求**：

- 添加骨架屏（Skeleton Loading）
- 优化加载动画
- 显示加载进度（如果可能）

**相关文件**：

- `CardDetailScreen.kt` - 加载状态 UI

**验收标准**：

- [ ] 加载时显示骨架屏
- [ ] 加载动画流畅
- [ ] 用户体验良好

---

##### 9. 深色模式优化

**功能描述**：优化深色模式下的视觉效果

**实现要求**：

- 检查所有组件的颜色对比度
- 优化 SectionCard 的背景色和边框色
- 确保所有文本可读性良好

**相关文件**：

- 所有组件文件
- `components/SectionCard.kt` - 重点优化

**验收标准**：

- [ ] 深色模式下所有文本清晰可读
- [ ] 颜色对比度符合 WCAG 标准
- [ ] 视觉效果统一协调

## 🚀 快速开始

### 查看 Preview

Card Detail 模块提供了完整的 Preview 支持，位于 `devMain` source set 中。

#### Preview 文件结构

```text
src/devMain/kotlin/tech/zhifu/app/myhub/carddetail/
└── CardDetailScreen.dev.kt
```

#### Preview 特性

Preview 文件包含以下预览场景，展示不同屏幕尺寸和主题下的完整界面（包括所有 Section 组件）：

##### 完整界面预览

- **浅色主题（移动端 Compact）** - 展示移动端垂直布局，操作面板在内容下方
- **深色主题（移动端 Compact）** - 展示移动端深色主题效果
- **浅色主题（平板 Medium）** - 展示平板端垂直布局
- **深色主题（平板 Medium）** - 展示平板端深色主题效果
- **浅色主题（桌面端 Expanded）** - 展示桌面端左右分栏布局，操作面板在右侧
- **深色主题（桌面端 Expanded）** - 展示桌面端深色主题效果

#### 使用 Preview

在 Android Studio 或 IntelliJ IDEA 中：

1. 打开 `CardDetailScreen.dev.kt` 文件
2. 点击 Preview 函数左侧的预览图标
3. 查看详情页在不同主题、屏幕尺寸下的外观
4. 所有预览都包含完整的操作面板（Actions、Tags、Metadata、Notes）

#### Preview 环境初始化

Preview 文件会自动初始化必要的 Koin 依赖（如 `cardModule`），确保预览环境正常工作。

```kotlin
// Preview 文件会自动调用 initPreviewKoin() 初始化 Koin
private fun initPreviewKoin() {
    val koin = KoinPlatformTools.defaultContext().getOrNull()
    if (koin == null) {
        startKoin {
            modules(
                cardModule,
                module {
                    factory<CoroutineScope> { CoroutineScope(Dispatchers.Default) }
                    factory<ReactiveCardRepository> { MockReactiveCardRepository() }
                }
            )
        }
    }
}
```

#### Preview 示例数据

Preview 使用 `createSampleCard()` 生成示例数据，包含：

- 完整的卡片内容（Quote 类型）
- 多个标签（literature, philosophy, reading, books）
- 完整的元数据（作者、分类、来源、模板、格式）
- 创建和更新时间

### 运行应用

1. 启动应用
2. 在 Dashboard 中点击任意卡片
3. 进入卡片详情页
4. 查看操作面板（Actions、Tags、Metadata、Notes）

### 测试功能

- **数据同步**：在 Dashboard 中编辑卡片，详情页会自动更新
- **删除功能**：点击删除按钮，会显示确认对话框
- **标签管理**：删除标签，标签会立即从列表中移除
- **笔记输入**：输入笔记，500ms 后自动保存（防抖）

## 📁 模块结构

```
feature/card/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/tech/zhifu/app/myhub/carddetail/
│   │   │   ├── CardDetailScreen.kt          # 主界面（响应式布局）
│   │   │   ├── CardDetailViewModel.kt       # ViewModel（真实数据集成）
│   │   │   ├── CardDetailUiState.kt         # UI 状态定义
│   │   │   ├── components/                  # 子组件
│   │   │   │   ├── CardDetailHeader.kt     # 头部导航
│   │   │   │   ├── CardDetailContent.kt    # 卡片内容展示区
│   │   │   │   ├── SectionCard.kt           # 统一区域容器
│   │   │   │   ├── CardDetailActions.kt     # 操作按钮面板
│   │   │   │   ├── CardDetailTags.kt        # 标签管理面板
│   │   │   │   ├── CardDetailMetadata.kt    # 元数据展示面板
│   │   │   │   └── CardDetailNotes.kt       # 个人笔记面板
│   │   │   └── di/
│   │   │       └── CardDetailModule.kt     # Koin 模块
│   │   └── composeResources/                # 多语言资源
│   │       ├── values/strings.xml           # 英文资源
│   │       └── values-zh-rCN/strings.xml   # 中文资源
│   └── devMain/                              # Preview 支持
│       └── kotlin/.../carddetail/
│           └── CardDetailScreen.dev.kt      # Preview 函数
├── build.gradle.kts
└── README.md
```

## 🏗️ 架构设计

### 数据流

```text
用户操作
   │
   ├─► 点击卡片 ──► Dashboard ──► 导航到 CardDetailScreen
   │                                              │
   │                                              ├─► CardDetailViewModel.observeCard()
   │                                              │
   │                                              └─► ReactiveCardRepository.observeCard()
   │                                                      │
   │                                                      ├─► Flow<Card?> ──► UI 自动更新
   │                                                      │
   │                                                      └─► 卡片删除 ──► Flow 发出 null ──► Error 状态
   │
   └─► 编辑操作 ──► ViewModel.updateCard() ──► Repository.updateCard()
                                                      │
                                                      ├─► 更新本地数据库
                                                      │
                                                      └─► Flow 自动发出新值 ──► UI 自动更新
```

### 响应式布局

- **Compact（移动端）**：垂直滚动，操作面板在内容下方
- **Medium（平板）**：垂直滚动，操作面板在内容下方
- **Expanded（桌面端）**：左右分栏，操作面板在右侧（4/12 列）

### 状态管理

```kotlin
sealed class CardDetailUiState {
    data class Loading(val cardId: String) : CardDetailUiState()

    data class Content(
        val card: Card,
        val isSharing: Boolean = false,
        val isEditing: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null
    ) : CardDetailUiState()

    data class Error(
        val message: String,
        val cardId: String,
        val retryable: Boolean = true
    ) : CardDetailUiState()
}
```

## 💡 关键设计决策

### 1. 响应式数据观察

使用 `ReactiveCardRepository.observeCard(id)` 实现实时数据更新：

- ✅ 多入口编辑时自动同步
- ✅ 卡片删除时自动显示错误状态
- ✅ 无需手动刷新

### 2. 防抖保存笔记

使用 `debounce(500).distinctUntilChanged()` 优化笔记保存：

- ✅ 减少数据库写入频率
- ✅ 提升性能
- ✅ 改善用户体验

### 3. 删除确认对话框

删除操作需要用户确认：

- ✅ 防止误操作
- ✅ 提升用户体验
- ✅ 符合产品级要求

### 4. 统一区域容器（SectionCard）

所有操作面板使用统一的 `SectionCard` 容器：

- ✅ 统一的视觉样式
- ✅ 易于维护
- ✅ 桌面端视觉更稳定

## 📚 相关文档

- [完整设计方案](../../docs/CARD_DETAIL_DESIGN.md) - 详细的设计文档和实现指南
- [Card Component 文档](../../component/card/README.md) - 卡片组件使用说明
- [数据同步机制](../../component/card/docs/card_data_synchronization.md) - 卡片数据同步原理

## 🔧 开发指南

### 添加新功能

1. 在 `components/` 目录下创建新组件
2. 使用 `SectionCard` 包装（如果需要）
3. 在 `CardDetailViewModel` 中添加业务逻辑
4. 在 `CardDetailScreen` 中集成组件
5. 更新 `CardDetailUiState`（如果需要新状态）

### 测试建议

- 测试不同窗口大小下的布局
- 测试数据同步（多窗口、多入口编辑）
- 测试错误处理（卡片删除、网络错误）
- 测试防抖保存（快速输入笔记）

## 📝 更新日志

### v1.0 (当前版本)

- ✅ 完成核心功能实现
- ✅ 完成操作面板组件（Actions、Tags、Metadata、Notes）
- ✅ 完成真实数据集成（observeCard）
- ✅ 完成导航集成（从 Dashboard 点击进入）
- ✅ 完成响应式布局（移动端、平板、桌面端）
- ✅ 完成删除确认对话框
- ✅ 完成笔记防抖保存（500ms debounce）
- ✅ 完成标签更新功能
- ✅ 完成错误处理和状态管理
- ✅ 完成 Preview 支持（6 个预览场景，覆盖所有屏幕尺寸和主题）

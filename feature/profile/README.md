# Profile 模块架构设计

## 📋 概述

Profile 模块是个人页面，用于展示用户个人信息和应用内信息管理配置入口。本模块采用 MVVM 架构设计，基于 Kotlin Flow 实现实时数据更新，支持多平台（Android、iOS、Desktop、Web）统一体验。

## 🎯 设计目标

1. **个人信息展示**：提供用户基本信息、统计数据和账户状态展示
2. **响应式更新**：基于 Flow 的实时数据同步和更新
3. **多平台支持**：使用 Compose Multiplatform 实现跨平台 UI
4. **用户体验**：直观的界面设计，快速访问配置入口
5. **可扩展性**：易于添加新的功能模块和配置项
6. **模块独立性**：与 Settings 和 Dashboard 模块保持清晰的职责划分

## 🏗️ 架构设计

### 分层架构

```text
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  ProfileScreen, ProfileHeaderCard, ProfileStatsCard     │
│  ProfileSettingsList, ProfileAboutSection               │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              ViewModel Layer                             │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ProfileViewModel                                │   │
│  │  - loadProfileData()                             │   │
│  │  - refreshProfile()                              │   │
│  │  - startEditProfile()                            │   │
│  │  - saveProfile()                                 │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Repository Layer                           │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ReactiveUserRepository                          │   │
│  │  - observeCurrentUser(): Flow<User?>             │   │
│  │  - updateUser(user: User): User                  │   │
│  └──────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────┐   │
│  │  ReactiveStatisticsRepository                    │   │
│  │  - observeStatistics(): Flow<Statistics>          │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 数据流

```text
用户操作
   │
   ├─► 编辑资料 ──► ViewModel.startEditProfile() ──► 显示编辑对话框
   │                                              │
   │                                              └─► 用户输入 ──► ViewModel.saveProfile()
   │                                                                  │
   │                                                                  ├─► UserRepository.updateUser()
   │                                                                  │
   │                                                                  └─► Flow 触发 ──► UI 自动更新
   │
   └─► 自动同步 ──► Flow.observe() ──► 实时更新 UI
```

## 📦 核心组件

### 1. ProfileViewModel

```kotlin
/**
 * Profile ViewModel
 * 管理 Profile 页面的状态和业务逻辑
 */
class ProfileViewModel(
    private val coroutineScope: CoroutineScope,
    private val userRepository: ReactiveUserRepository,
    private val statisticsRepository: ReactiveStatisticsRepository
) {
    val uiState: StateFlow<ProfileUiState>

    /**
     * 加载 Profile 数据
     * 同时监听用户信息和统计数据的变化
     */
    private fun loadProfileData()

    /**
     * 刷新 Profile 数据
     */
    fun refreshProfile()

    /**
     * 开始编辑资料
     */
    fun startEditProfile()

    /**
     * 取消编辑
     */
    fun cancelEditProfile()

    /**
     * 保存资料
     */
    suspend fun saveProfile(displayName: String, email: String, avatarUrl: String)

    /**
     * 清除错误状态
     */
    fun clearError()
}
```

**关键特性**：

- **响应式数据流**：使用 Flow 监听数据变化，自动更新 UI
- **多数据源同步**：同时监听用户信息和统计数据
- **错误处理**：完善的错误捕获和状态管理
- **编辑功能**：支持用户信息编辑和保存

### 2. ProfileUiState

```kotlin
/**
 * Profile UI状态
 */
data class ProfileUiState(
    // 用户信息
    val user: User? = null,
    
    // 统计数据
    val statistics: Statistics? = null,
    
    // UI 状态
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // 编辑状态
    val isEditingProfile: Boolean = false,
    val editProfileDialog: EditProfileDialogState? = null
)

/**
 * 编辑资料对话框状态
 */
data class EditProfileDialogState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = ""
)
```

**状态说明**：

- **`user`**: 当前用户信息（如果未登录则为 null）
- **`statistics`**: 用户统计数据（总卡片数、收藏数、最近编辑数等）
- **`isLoading`**: 是否正在加载
- **`error`**: 错误信息（如果有）
- **`isEditingProfile`**: 是否正在编辑资料
- **`editProfileDialog`**: 编辑对话框状态（如果显示则为非 null）

### 3. ProfileScreen

```kotlin
/**
 * Profile 主界面
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinInject<ProfileViewModel>(),
    onNavigateToSettings: () -> Unit = {}
)
```

**UI 组件**：

- **ProfileHeaderCard**: 用户信息卡片（头像、用户名、显示名称、邮箱、注册时间）
- **ProfileStatsCard**: 统计数据卡片（总卡片数、收藏数、最近编辑数）
- **ProfileSettingsList**: 配置入口列表（应用设置、数据管理、账户管理）
- **ProfileAboutSection**: 关于信息（应用版本、构建信息）
- **EditProfileDialog**: 编辑资料对话框

## 📁 模块结构

```text
feature/profile/
├── src/
│   ├── commonMain/
│   │   ├── kotlin/
│   │   │   └── tech/zhifu/app/myhub/profile/
│   │   │       ├── ProfileScreen.kt             # UI 组件
│   │   │       ├── ProfileViewModel.kt           # ViewModel
│   │   │       ├── ProfileUiState.kt             # UI 状态
│   │   │       └── di/
│   │   │           └── ProfileModule.kt         # 依赖注入模块
│   │   └── composeResources/
│   │       └── values/
│   │           ├── strings.xml                   # 默认字符串资源
│   │           ├── values-zh-rCN/
│   │           │   └── strings.xml               # 简体中文
│   │           ├── values-zh-rTW/
│   │           │   └── strings.xml               # 繁体中文
│   │           └── values-ja/
│   │               └── strings.xml               # 日语
│   └── devMain/                                  # 预览支持
│       └── kotlin/
│           └── tech/zhifu/app/myhub/profile/
│               └── ProfileScreen.dev.kt          # Preview 函数
├── build.gradle.kts                              # 构建配置
└── README.md                                     # 本文档
```

## 🔧 实现细节

### 响应式数据监听

Profile 使用 Flow 实现响应式数据更新：

```kotlin
// 监听用户信息
userRepository.observeCurrentUser()
    .catch { e ->
        _uiState.value = _uiState.value.copy(
            error = e.message ?: "Failed to load user"
        )
    }
    .onEach { user ->
        _uiState.value = _uiState.value.copy(
            user = user,
            isLoading = false
        )
    }
    .launchIn(coroutineScope)

// 监听统计数据
statisticsRepository.observeStatistics()
    .catch { e ->
        logger.error(e) { "Failed to load statistics" }
    }
    .onEach { statistics ->
        _uiState.value = _uiState.value.copy(
            statistics = statistics
        )
    }
    .launchIn(coroutineScope)
```

### 响应式布局

Profile 支持响应式布局，根据窗口大小自动调整：

```kotlin
@Composable
fun ProfileScreen(...) {
    val sizeClass = windowSizeClass()
    
    // 根据状态类型显示不同的 UI
    when {
        uiState.isLoading && uiState.user == null -> {
            // 显示加载状态
        }
        uiState.user == null -> {
            // 显示无用户状态
        }
        else -> {
            // 显示用户信息
            Column {
                ProfileHeaderCard(...)
                ProfileStatsCard(...)
                ProfileSettingsList(...)
                ProfileAboutSection()
            }
        }
    }
}
```

**响应式特性**：

- **移动端（Compact）**：单列布局，垂直滚动
- **平板（Medium）**：统计卡片 2 列布局
- **桌面（Expanded）**：统计卡片 3 列布局，更宽的间距

## 💡 使用示例

### 1. 在应用中使用 Profile

```kotlin
// 在导航配置中
@Composable
fun AppNavigation() {
    NavHost(...) {
        composable("profile") {
            ProfileScreen(
                onNavigateToSettings = { /* 导航到设置页面 */ }
            )
        }
    }
}
```

### 2. 依赖注入配置

```kotlin
// 在 Koin 模块中（已配置在 ProfileModule.kt）
fun profileModule() = module {
    // Profile ViewModel
    factoryOf(::ProfileViewModel)
}
```

### 3. 自定义 Profile

```kotlin
@Composable
fun CustomProfileScreen() {
    val viewModel: ProfileViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    // 自定义 UI 实现
    when {
        uiState.isLoading && uiState.user == null -> {
            CircularProgressIndicator()
        }
        uiState.user == null -> {
            Text("No user data")
        }
        else -> {
            // 显示用户信息
        }
    }
}
```

## 📊 功能特性

### 个人信息展示

- **用户基本信息**
  - 头像（Avatar）
  - 用户名（Username）
  - 显示名称（Display Name）
  - 邮箱（Email）
  - 注册时间（Created At）

- **用户统计数据**
  - 总卡片数（Total Cards）
  - 收藏卡片数（Favorite Cards）
  - 最近编辑数（Recent Edits）

### 配置入口

- **应用设置**：跳转到 Settings 页面（主题、语言等）
- **数据管理**：数据同步设置、数据导出/导入、缓存清理（未来功能）
- **账户管理**：编辑个人信息、修改密码、账户注销（未来功能）

### 其他功能

- **关于信息**：应用版本、构建信息
- **编辑资料**：支持编辑显示名称、邮箱、头像 URL

## 🌐 国际化支持

Profile 模块支持多语言：

- **英语** (en): 默认语言
- **简体中文** (zh-rCN)
- **繁体中文** (zh-rTW)
- **日语** (ja)

所有字符串资源都通过 `composeResources` 管理，支持编译时类型安全。

## 🎨 UI 设计

### 布局结构

```text
┌─────────────────────────────────────────┐
│  Profile Header (TopAppBar)             │
├─────────────────────────────────────────┤
│  Profile Header Card                     │
│  - Avatar (居中，带编辑图标)              │
│  - Display Name                         │
│  - Username                             │
│  - Email                                │
│  - Joined Date                          │
├─────────────────────────────────────────┤
│  Statistics Card                        │
│  ┌─────┐ ┌─────┐ ┌─────┐               │
│  │Total│ │Fav  │ │Edit │               │
│  └─────┘ └─────┘ └─────┘               │
├─────────────────────────────────────────┤
│  Settings List                          │
│  - Settings                             │
│  - Data Management                      │
│  - Account Management                   │
│  - About                                │
├─────────────────────────────────────────┤
│  About Section                          │
│  - Version                              │
└─────────────────────────────────────────┘
```

### 响应式设计

- **移动端（Compact）**：单列布局，统计卡片 3 列
- **平板（Medium）**：统计卡片 3 列布局
- **桌面（Expanded）**：统计卡片 3 列布局，更宽的间距

## 🧪 Preview 支持

Profile 模块提供了完整的 Preview 支持，位于 `devMain` source set 中。

### Preview 文件结构

```text
src/devMain/kotlin/tech/zhifu/app/myhub/profile/
└── ProfileScreen.dev.kt
```

### Preview 特性

Preview 文件包含以下预览场景：

#### ProfileScreen 完整预览

- **浅色主题（有用户数据）** - 展示 Profile 在浅色主题下的完整界面
- **深色主题（有用户数据）** - 展示 Profile 在深色主题下的完整界面
- **加载状态** - 展示加载时的界面
- **无用户状态** - 展示无用户数据时的界面

#### 组件独立预览

- **ProfileHeaderCard** - 用户信息卡片预览
- **ProfileStatsCard** - 统计数据卡片预览
- **ProfileSettingsList** - 配置入口列表预览
- **ProfileAboutSection** - 关于信息预览
- **EditProfileDialog** - 编辑资料对话框预览

### 示例数据生成

Preview 文件提供了示例数据生成函数：

- `createSampleUser()` - 创建示例用户数据
- `createSampleStatistics()` - 创建示例统计信息

### 使用 Preview

在 Android Studio 或 IntelliJ IDEA 中：

1. 打开 `ProfileScreen.dev.kt` 文件
2. 点击 Preview 函数左侧的预览图标
3. 查看 Profile 在不同主题和状态下的外观

### Preview 环境初始化

Preview 文件使用示例数据，无需额外的依赖初始化。

## 🔄 数据同步策略

1. **初始化加载**：
   - 从 Repository 获取用户信息和统计数据
   - 开始监听数据变化

2. **自动更新**：
   - 通过 Flow 监听本地数据库变化
   - 实时更新 UI 状态

3. **手动刷新**：
   - 用户触发刷新
   - 重新从 Repository 获取数据

4. **编辑保存**：
   - 用户编辑资料后保存
   - 通过 UserRepository 更新用户信息
   - Flow 自动触发 UI 更新

## 🚀 未来计划

### 短期计划

- [ ] 实现数据管理功能（数据导出/导入、缓存清理）
- [ ] 实现账户管理功能（修改密码、账户注销）
- [ ] 优化头像显示（支持网络图片）
- [ ] 添加更多统计指标

### 中期计划

- [ ] 添加用户偏好设置快捷入口
- [ ] 添加数据可视化图表
- [ ] 添加账户安全设置

### 长期计划

- [ ] 添加多账户切换功能
- [ ] 添加账户备份和恢复
- [ ] 添加个性化推荐

## 📝 当前状态

**已完成**：

- ✅ Profile 基础 UI 实现
- ✅ 用户信息展示
- ✅ 统计数据展示
- ✅ 响应式数据监听
- ✅ 多平台支持（Android、iOS、Desktop、Web）
- ✅ 国际化支持
- ✅ 响应式布局
- ✅ 错误处理
- ✅ 编辑资料功能
- ✅ Preview 支持（devMain source set）

**待实现**：

- 🔄 数据管理功能
- 🔄 账户管理功能
- 🔄 头像网络图片支持
- 🔄 更多统计指标

## 📚 参考

### 相关文档

- [项目整体架构](../../docs/myhub_architecture.md)
- [数据层架构](../../core/datastore/docs/datastore_architecture.md)
- [Settings 模块](../settings/README.md)
- [Dashboard 模块](../dashboard/README.md)

### 设计原则

- **单一职责原则**：ViewModel 只负责状态管理和业务逻辑
- **响应式编程**：使用 Flow 实现数据驱动的 UI 更新
- **可测试性**：ViewModel 可独立测试，不依赖 UI
- **可扩展性**：易于添加新的功能模块和配置项

## 🔗 相关模块

- **core/datastore-repository-client**: 提供用户和统计数据的 Repository
- **core/datastore-model**: 提供数据模型（User、Statistics）
- **component/mixed**: 提供 Avatar 组件
- **core/platform**: 提供平台抽象（窗口大小、主题等）
- **feature/settings**: 应用设置模块（通过导航跳转）

## 🔍 注意事项

1. **与 Settings 模块的关系**
   - Profile 提供跳转到 Settings 的入口
   - Settings 处理具体的设置项（主题、语言等）
   - Profile 专注于个人信息展示和管理
   - 避免功能重复

2. **用户信息更新**
   - 使用 `ReactiveUserRepository` 监听用户信息变化
   - 编辑后自动同步到服务器（如果已登录）
   - 本地更新立即反映到 UI（通过 Flow）

3. **统计数据**
   - 使用 `ReactiveStatisticsRepository` 获取统计数据
   - 与 Dashboard 使用相同的数据源
   - 统计数据实时更新

4. **导航**
   - Profile 页面通过导航参数接收回调函数
   - 或者使用导航组件进行页面跳转
   - 建议使用回调函数，保持模块独立性

5. **权限和安全**
   - 敏感操作（如删除账户）需要确认对话框
   - 导出数据需要考虑隐私保护
   - 编辑个人信息需要验证（如邮箱格式）

6. **Avatar 组件扩展**
   - 当前 `component.mixed.Avatar` 只支持首字母显示
   - Profile 页面可能需要支持网络图片
   - 可以考虑扩展 Avatar 组件或创建 ProfileAvatar 组件

7. **空状态处理**
   - 用户未登录时显示登录提示
   - 统计数据为空时显示占位符
   - 优雅降级处理

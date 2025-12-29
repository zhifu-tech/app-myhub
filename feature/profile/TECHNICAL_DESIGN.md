# Feature/Profile 模块技术方案

## 📋 概述

Profile 模块是个人页面，用于展示用户个人信息和应用内信息管理配置入口。该模块遵循项目的 MVVM 架构模式，与 `feature/settings` 和 `feature/dashboard` 模块保持一致的设计风格。

## 🎯 功能需求

### 1. 个人信息展示
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
  - 模板数量（Templates Count）
  - 账户状态（Premium/Free）

### 2. 配置入口
- **应用设置**
  - 跳转到 Settings 页面（主题、语言等）
  
- **数据管理**
  - 数据同步设置
  - 数据导出/导入
  - 缓存清理
  
- **账户管理**
  - 编辑个人信息
  - 修改密码（如果支持）
  - 账户注销

### 3. 其他功能
- **关于信息**
  - 应用版本
  - 构建信息
  - 开源许可

## 🏗️ 架构设计

### 模块结构

```text
feature/profile/
├── build.gradle.kts
├── TECHNICAL_DESIGN.md
├── src/commonMain/
│   ├── kotlin/tech/zhifu/app/myhub/profile/
│   │   ├── ProfileViewModel.kt          # ViewModel
│   │   ├── ProfileUiState.kt           # UI 状态
│   │   ├── ProfileScreen.kt             # 主界面
│   │   └── di/
│   │       └── ProfileModule.kt        # Koin 模块
│   └── composeResources/
│       ├── values/strings.xml           # 英文资源
│       ├── values-zh-rCN/strings.xml    # 简体中文资源
│       ├── values-zh-rTW/strings.xml    # 繁体中文资源
│       └── values-ja/strings.xml        # 日文资源
```

### 分层架构

```text
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  ProfileScreen, ProfileHeader, ProfileStats, etc.       │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              ViewModel Layer                            │
│  ProfileViewModel                                       │
│  - 管理 UI 状态                                         │
│  - 处理用户交互                                         │
│  - 协调数据获取                                         │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Repository Layer                           │
│  UserRepository (已存在)                                │
│  ReactiveStatisticsRepository (已存在)                  │
└─────────────────────────────────────────────────────────┘
```

## 📦 核心组件

### 1. ProfileUiState

```kotlin
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

data class EditProfileDialogState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = ""
)
```

### 2. ProfileViewModel

```kotlin
class ProfileViewModel(
    private val coroutineScope: CoroutineScope,
    private val userRepository: ReactiveUserRepository,
    private val statisticsRepository: ReactiveStatisticsRepository
) {
    private val logger = logger("Profile")
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfileData()
    }
    
    private fun loadProfileData() {
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
    }
    
    fun refreshProfile() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 触发数据刷新
                userRepository.getCurrentUser()
                statisticsRepository.refreshStatistics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to refresh"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun startEditProfile() {
        val user = _uiState.value.user
        _uiState.value = _uiState.value.copy(
            isEditingProfile = true,
            editProfileDialog = EditProfileDialogState(
                displayName = user?.displayName ?: "",
                email = user?.email ?: "",
                avatarUrl = user?.avatarUrl ?: ""
            )
        )
    }
    
    fun cancelEditProfile() {
        _uiState.value = _uiState.value.copy(
            isEditingProfile = false,
            editProfileDialog = null
        )
    }
    
    suspend fun saveProfile(displayName: String, email: String, avatarUrl: String) {
        val currentUser = _uiState.value.user ?: return
        
        val updatedUser = currentUser.copy(
            displayName = displayName.takeIf { it.isNotBlank() },
            email = email.takeIf { it.isNotBlank() },
            avatarUrl = avatarUrl.takeIf { it.isNotBlank() }
        )
        
        try {
            userRepository.updateUser(updatedUser)
            _uiState.value = _uiState.value.copy(
                isEditingProfile = false,
                editProfileDialog = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Failed to save profile"
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
```

### 3. ProfileScreen

```kotlin
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinInject<ProfileViewModel>(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_profile_title)) }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 用户信息卡片
            ProfileHeaderCard(
                user = uiState.user,
                onEditClick = { viewModel.startEditProfile() }
            )
            
            // 统计数据卡片
            ProfileStatsCard(
                statistics = uiState.statistics
            )
            
            // 配置入口列表
            ProfileSettingsList(
                onSettingsClick = onNavigateToSettings,
                onDataManagementClick = { /* TODO */ },
                onAccountManagementClick = { /* TODO */ }
            )
            
            // 关于信息
            ProfileAboutSection()
        }
    }
    
    // 编辑对话框
    uiState.editProfileDialog?.let { dialogState ->
        EditProfileDialog(
            state = dialogState,
            onDismiss = { viewModel.cancelEditProfile() },
            onSave = { displayName, email, avatarUrl ->
                viewModel.saveProfile(displayName, email, avatarUrl)
            }
        )
    }
}
```

## 🎨 UI 组件设计

### 1. ProfileHeaderCard
- **用户头像**
  - 使用 `component.mixed.Avatar` 组件
  - 如果用户有 `avatarUrl`，显示网络图片（需要扩展 Avatar 组件支持图片）
  - 否则显示用户名首字母的渐变背景
  - 支持点击编辑头像
  
- **用户信息**
  - 显示名称（Display Name）或用户名（Username）
  - 显示邮箱（如果存在）
  - 显示注册时间（格式化显示，如 "Joined in 2024"）
  
- **操作按钮**
  - 编辑按钮（右上角或底部）
  - 点击后打开编辑对话框

### 2. ProfileStatsCard
- 使用 Grid 布局展示统计数据
- 每个统计项包含图标、标签和数值
- 响应式布局（移动端 2 列，桌面端 4 列）

### 3. ProfileSettingsList
- 使用 ListItem 组件
- 分组显示：
  - **应用设置**：跳转到 Settings
  - **数据管理**：同步设置、导出/导入、缓存清理
  - **账户管理**：编辑信息、修改密码、账户注销

### 4. ProfileAboutSection
- 应用版本信息
- 构建信息（开发环境显示）
- 开源许可链接

## 📱 响应式设计

- **移动端（Compact）**：单列布局，垂直滚动
- **平板（Medium）**：统计卡片 2 列布局
- **桌面（Expanded）**：统计卡片 4 列布局，更宽的间距

## 🔗 依赖关系

### 模块依赖
```kotlin
dependencies {
    // Compose UI
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(compose.materialIconsExtended)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    
    // Dependency Injection
    implementation(libs.koin.core)
    implementation(libs.koin.compose.viewmodel)
    
    // 项目模块
    implementation(projects.core.platform)
    implementation(projects.core.platformCompose)
    implementation(projects.core.logger)
    implementation(projects.core.datastoreModel)
    implementation(projects.core.datastoreRepositoryClient)
    
    // 共享组件
    implementation(projects.component.mixed) // Avatar 组件
}
```

### 数据源
- **UserRepository** (`ReactiveUserRepository`)
  - `observeCurrentUser(): Flow<User?>` - 监听当前用户信息
  - `updateUser(user: User): User` - 更新用户信息
  
- **StatisticsRepository** (`ReactiveStatisticsRepository`)
  - `observeStatistics(): Flow<Statistics>` - 监听统计数据

## 🌐 国际化资源

### 资源键命名规范
使用 `feature_profile_` 前缀：

```xml
<!-- values/strings.xml -->
<string name="feature_profile_title">Profile</string>
<string name="feature_profile_edit_profile">Edit Profile</string>
<string name="feature_profile_user_info">User Information</string>
<string name="feature_profile_statistics">Statistics</string>
<string name="feature_profile_settings">Settings</string>
<string name="feature_profile_data_management">Data Management</string>
<string name="feature_profile_account_management">Account Management</string>
<string name="feature_profile_about">About</string>
<string name="feature_profile_version">Version</string>
<string name="feature_profile_export_data">Export Data</string>
<string name="feature_profile_import_data">Import Data</string>
<string name="feature_profile_clear_cache">Clear Cache</string>
<string name="feature_profile_sign_out">Sign Out</string>
<string name="feature_profile_delete_account">Delete Account</string>
```

## 🔄 数据流

### 加载流程
1. ViewModel 初始化时开始监听用户信息和统计数据
2. 使用 `combine` 合并多个 Flow
3. 更新 UIState
4. UI 自动响应更新

### 编辑流程
1. 用户点击编辑按钮
2. ViewModel 设置 `isEditingProfile = true`
3. 显示编辑对话框
4. 用户输入新信息
5. 调用 `saveProfile()`
6. 通过 UserRepository 更新用户信息
7. 关闭对话框，刷新 UI

## 🧪 测试策略

### 单元测试
- `ProfileViewModelTest`
  - 测试数据加载
  - 测试编辑功能
  - 测试错误处理

### UI 测试
- ProfileScreen 的渲染测试
- 用户交互测试

## 📝 实现步骤

### Phase 1: 基础结构
1. ✅ 创建模块目录结构
2. ✅ 创建 `build.gradle.kts`
3. ✅ 创建资源文件
4. ✅ 创建 `ProfileUiState`
5. ✅ 创建 `ProfileViewModel`（基础版本）

### Phase 2: UI 实现
1. ✅ 实现 `ProfileScreen`
2. ✅ 实现 `ProfileHeaderCard`
3. ✅ 实现 `ProfileStatsCard`
4. ✅ 实现 `ProfileSettingsList`

### Phase 3: 功能实现
1. ✅ 实现用户信息加载
2. ✅ 实现统计数据加载
3. ✅ 实现编辑功能
4. ✅ 实现配置入口跳转

### Phase 4: 完善
1. ✅ 添加错误处理
2. ✅ 添加加载状态
3. ✅ 添加国际化
4. ✅ 添加单元测试

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

## 📚 参考

- `feature/settings` 模块：MVVM 架构参考
- `feature/dashboard` 模块：统计数据展示参考
- `core/datastore-repository-client`：数据访问层参考


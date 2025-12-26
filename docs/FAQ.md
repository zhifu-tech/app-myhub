# 常见问题解答 (FAQ)

本文档包含项目开发过程中遇到的常见问题及解决方案。

---

## iOS 开发问题

### Q1: TLS 错误导致安全连接失败

**问题描述：**

Xcode 与 Apple 服务器通信时出现 TLS/SSL 证书验证失败。

**解决方案：**

#### 方案 1: 检查网络和代理设置

```bash
# 检查网络连接
ping developer.apple.com

# 如果使用代理，检查代理设置
echo $http_proxy
echo $https_proxy
```

#### 方案 2: 重新登录 Apple ID

1. 打开 Xcode
2. 进入 `Xcode` > `Settings` (或 `Preferences`)
3. 选择 `Accounts` 标签
4. 移除当前的 Apple ID，然后重新添加
5. 确保选择了正确的 Team (R9JS3478MA)

#### 方案 3: 清理 Xcode 缓存

```bash
# 清理 DerivedData
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# 清理 Archives
rm -rf ~/Library/Developer/Xcode/Archives/*

# 清理 Provisioning Profiles 缓存
rm -rf ~/Library/MobileDevice/Provisioning\ Profiles/*

# 重启 Xcode
```

#### 方案 4: 更新 Xcode 和证书

1. 确保 Xcode 是最新版本
2. 在 Xcode 中：`Xcode` > `Settings` > `Accounts` > 选择你的账号 > `Download Manual Profiles`
3. 或者运行：

```bash
# 更新证书
security find-identity -v -p codesigning
```

**注意事项：**

- 如果问题持续出现，尝试检查防火墙/代理设置、使用 VPN 或更换网络
- 联系网络管理员检查企业网络限制
- 更新 macOS 和 Xcode 到最新版本

---

### Q2: Provisioning Profile 不包含当前设备

**问题描述：**

出现以下错误信息：

```text
Provisioning profile "iOS Team Provisioning Profile: tech.zhifu.app.myhub.MyHub"
doesn't include the currently selected device "Zhifu's MacBook Pro"
(identifier 00006030-001638A234D0001C).
```

**原因分析：**

Mac 设备不应该作为 iOS 应用的运行目标。这个错误通常发生在：

- Xcode 错误地将 Mac 识别为运行设备
- 项目配置了 Mac Catalyst 但未正确设置
- 设备选择器选择了错误的设备类型

**解决方案：**

#### 方案 1: 选择正确的运行目标（推荐）

1. 在 Xcode 顶部工具栏，点击设备选择器（显示 "Zhifu's MacBook Pro" 的地方）
2. 选择以下之一：
   - **iOS Simulator**（如 iPhone 15 Pro, iPad Pro 等）
   - **真实的 iOS 设备**（如果已连接）
3. **不要选择 Mac 设备**

#### 方案 2: 检查项目设置

1. 在 Xcode 中选择项目文件
2. 选择 `iosApp` target
3. 进入 `Signing & Capabilities` 标签
4. 确保：
   - ✅ `Automatically manage signing` 已勾选
   - ✅ `Team` 设置为正确的团队 (R9JS3478MA)
   - ✅ `Bundle Identifier` 为 `tech.zhifu.app.myhub.MyHub`

#### 方案 3: 如果确实需要在 Mac 上运行（Mac Catalyst）

如果项目需要支持 Mac，需要额外配置：

1. 在 `Signing & Capabilities` 中：
   - 添加 `Mac (Designed for iPad)` capability
   - 或启用 `Mac Catalyst`

2. 更新 `project.pbxproj` 中的配置：

   ```pbxproj
   SUPPORTS_MACCATALYST = YES;
   SUPPORTS_MAC_DESIGNED_FOR_IPAD_IPHONE = YES;
   ```

#### 方案 4: 手动刷新 Provisioning Profiles

```bash
# 在 Xcode 中
# Xcode > Settings > Accounts > [你的账号] > Download Manual Profiles

# 或使用命令行
xcrun altool --list-providers
```

**注意事项：**

- iOS 应用需要在 iOS 设备或 iOS 模拟器上运行
- Mac 需要 Mac Catalyst 或 "Designed for iPad" 配置才能在 Mac 上运行
- 如果使用自动签名，Xcode 会自动管理设备；如果使用手动签名，需要在 [Apple Developer Portal](https://developer.apple.com/account) 中添加设备 UDID

---

### Q3: 如何添加设备到 Provisioning Profile？

**问题描述：**

需要将新设备添加到 Provisioning Profile 以便进行真机调试。

**解决方案：**

- **自动签名（推荐）**：Xcode 会自动管理，无需手动操作
- **手动签名**：需要在 [Apple Developer Portal](https://developer.apple.com/account) 中添加设备 UDID

---

## Android 开发问题

### Q1: Android 手机或模拟器如何连接电脑本地部署的服务器？

**问题描述：**

在开发过程中，需要在 Android 手机或模拟器上连接运行在电脑本地的服务器（默认端口 8083）。

**解决方案：**

#### 方案 1: Android 模拟器连接本地服务器（推荐）

Android 模拟器使用特殊的 IP 地址 `10.0.2.2` 来访问主机的 `localhost`。

**步骤：**

1. **确保服务器正在运行**

   ```bash
   # 在项目根目录运行服务器
   ./gradlew :server:run
   # 或
   cd server && ./gradlew run
   ```

   服务器默认监听在 `http://localhost:8083`

2. **配置应用使用模拟器地址**

   应用已默认配置为使用 `http://10.0.2.2:8083`（见 `ApiConfig.kt`），无需额外配置。

   如果需要修改，可以通过以下方式：

   - **方式 1：** 在应用启动时设置（推荐）

     ```kotlin
     ApiConfig.setBaseUrl("http://10.0.2.2:8083")
     ```

   - **方式 2：** 通过系统属性设置（Android 平台）

     ```bash
     # 在运行应用时设置
     adb shell setprop myhub.api.base.url http://10.0.2.2:8083
     ```

3. **验证连接**

   在模拟器中运行应用，检查日志输出，应该能看到：

   ```text
   Using default API base URL: http://10.0.2.2:8083
   ```

**注意事项：**

- ✅ 模拟器已配置允许明文 HTTP 流量（见 `network_security_config.xml`）
- ✅ 默认端口为 8083，如果修改了服务器端口，需要同步更新应用配置

#### 方案 2: Android 真机连接本地服务器

真机需要通过电脑的局域网 IP 地址访问，或使用 adb 端口转发。

**方法 A：使用电脑的局域网 IP（推荐）**

1. **获取电脑的局域网 IP 地址**

   **macOS/Linux:**

   ```bash
   ifconfig | grep "inet " | grep -v 127.0.0.1
   # 或
   ip addr show | grep "inet " | grep -v 127.0.0.1
   ```

   **Windows:**

   ```cmd
   ipconfig
   ```

   找到类似 `192.168.x.x` 或 `10.x.x.x` 的 IP 地址

2. **确保手机和电脑在同一局域网**

   - 手机和电脑连接到同一个 Wi-Fi 网络

3. **配置应用使用电脑 IP**

   **方式 1：** 在应用启动时设置

   ```kotlin
   ApiConfig.setBaseUrl("http://192.168.x.x:8083")  // 替换为你的电脑 IP
   ```

   **方式 2：** 通过系统属性设置

   ```bash
   adb shell setprop myhub.api.base.url http://192.168.x.x:8083
   ```

4. **确保防火墙允许连接**

   **macOS:**

   ```bash
   # 检查防火墙设置
   # 系统设置 > 网络 > 防火墙
   ```

   **Linux:**

   ```bash
   # 如果使用 ufw
   sudo ufw allow 8083/tcp
   ```

   **Windows:**

   - 控制面板 > Windows Defender 防火墙 > 高级设置
   - 添加入站规则，允许端口 8083

**方法 B：使用 adb 端口转发（无需同一网络）**

1. **设置端口转发**

   ```bash
   adb reverse tcp:8083 tcp:8083
   ```

   这会将设备的 8083 端口转发到主机的 8083 端口

2. **配置应用使用 localhost**

   ```kotlin
   ApiConfig.setBaseUrl("http://localhost:8083")
   ```

   或

   ```bash
   adb shell setprop myhub.api.base.url http://localhost:8083
   ```

3. **验证转发**

   ```bash
   adb reverse --list
   ```

   应该看到：`tcp:8083 tcp:8083`

**注意事项：**

- ⚠️ 真机使用 HTTP 需要配置网络安全策略（已配置，见 `network_security_config.xml`）
- ⚠️ 使用局域网 IP 时，确保手机和电脑在同一 Wi-Fi 网络
- ⚠️ 使用 adb 端口转发时，需要保持 USB 连接或使用无线调试

#### 方案 3: 使用环境变量或构建配置

如果需要在不同环境使用不同的服务器地址，可以通过构建配置设置：

1. **查看构建配置**

   检查 `AppBuildConfig.kt` 和对应的平台实现，修改 `apiBaseUrl` 属性。

2. **开发环境配置**

   在开发变体的配置中设置：

   ```kotlin
   val apiBaseUrl: String
       get() = when {
           // 可以通过环境变量覆盖
           System.getenv("MYHUB_API_URL") != null -> System.getenv("MYHUB_API_URL")!!
           else -> "http://10.0.2.2:8083"  // 模拟器默认
       }
   ```

#### 常见问题排查

**问题 1：连接超时或无法连接**

- ✅ 检查服务器是否正在运行：`curl http://localhost:8083/health`
- ✅ 检查端口是否正确（默认 8083）
- ✅ 检查防火墙设置
- ✅ 检查网络连接（真机需要同一 Wi-Fi）

**问题 2：SSL/TLS 错误**

- ✅ 确保使用 HTTP（开发环境），不是 HTTPS
- ✅ 检查 `network_security_config.xml` 是否正确配置
- ✅ 检查 AndroidManifest.xml 是否引用了网络安全配置

**问题 3：模拟器无法连接**

- ✅ 确认使用 `10.0.2.2` 而不是 `localhost` 或 `127.0.0.1`
- ✅ 检查服务器是否监听在 `0.0.0.0`（所有接口），而不是只监听 `127.0.0.1`

**问题 4：真机无法连接（使用局域网 IP）**

- ✅ 确认手机和电脑在同一 Wi-Fi 网络
- ✅ 确认防火墙允许端口 8083
- ✅ 尝试 ping 电脑 IP：在手机上使用网络工具 ping `192.168.x.x`
- ✅ 检查路由器是否启用了 AP 隔离（某些路由器会阻止设备间通信）

**相关配置文件：**

- `androidApp/src/main/res/xml/network_security_config.xml` - 网络安全配置
- `core/datastore-datasource-remote/src/commonMain/kotlin/.../ApiConfig.kt` - API 配置
- `composeApp/src/commonMain/kotlin/.../AppBuildConfig.kt` - 构建配置

---

## Kotlin Multiplatform 构建问题

### Q1: `android.experimental.kmp.enableAndroidResources` 参数是什么？什么时候需要开启？

**问题描述：**

在 KMP 项目的 `androidLibrary` 配置中，遇到 Compose Resources 无法加载的错误：

```text
MissingResourceException: Missing resource with path: composeResources/...
```

**原因分析：**

`android.experimental.kmp.enableAndroidResources` 用来决定 KMP 的 Android sourceSet 是否支持 Android 资源系统（R、res、Manifest 合并）。默认：关闭（false）。打开后：KMP Android sourceSet 才能像普通 Android Module 一样用 `res/`。

**解决方案：**

**什么时候需要开启？**

✅ **需要开启的场景：**

- shared 模块包含 `res/` 或 `composeResources/`
- shared 模块包含 Compose UI（Android 端）
- shared 模块是 Android UI Library

❌ **不需要开启的场景：**

- shared 模块只是数据层/业务逻辑（SQLDelight、ViewModel 等）
- Android UI 在独立的 `androidApp` 模块中

**判断口诀：**

> **shared 里有没有 `res/`？**
>
> - 有 → 打开
> - 没有 → 不要打开

**配置方法：**

在 `composeApp/build.gradle.kts` 的 `androidLibrary` 块中：

```kotlin
kotlin {
    androidLibrary {
        // ... 其他配置

        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        // ... 其他配置
    }
}
```

**注意事项：**

- ❌ "打开这个就能生成 APK？" → 不对，它只影响资源系统
- ❌ "不打开就不能用 Android API？" → 可以用，只是不能用资源
- ❌ "所有 KMP 项目都应该打开？" → 不对，这是 UI 级别的能力

---

### Q2: 如何决定 JVM 目标版本？

**问题描述：**

在配置 Kotlin 编译选项时，如何选择合适的 JVM 目标版本（如 `JVM_11`、`JVM_17`）？

**解决方案：**

**决定因素（按优先级）：**

#### 1. Android 平台的最低要求

| Android 版本           | 最低支持的 Java 版本 | 推荐 JVM Target |
| ---------------------- | -------------------- | --------------- |
| Android 7.0 (API 24)  | Java 8               | JVM_1_8         |
| Android 8.0 (API 26)  | Java 8               | JVM_1_8         |
| Android 9.0 (API 28)  | Java 8               | JVM_1_8         |
| Android 10+ (API 29+) | Java 8+              | JVM_11 或更高   |

**你的项目：**

- `minSdk = 24` → 最低需要 Java 8
- `targetSdk = 36` → 可以使用 Java 11 或更高

#### 2. Android Gradle Plugin (AGP) 的要求

| AGP 版本 | 最低 Java 版本 | 推荐 JVM Target              |
| -------- | -------------- | ---------------------------- |
| AGP 7.x  | Java 11        | JVM_11                       |
| AGP 8.x  | Java 17        | JVM_17 或 JVM_11（向后兼容） |

**你的项目：**

- `agp = "8.13.2"` → 最低需要 Java 17，但可以设置为 JVM_11（向后兼容）

#### 3. 依赖库要求

检查主要依赖库的最低 Java 版本要求。常见库通常支持 Java 8+：

- Compose Multiplatform → 支持 Java 8+
- Kotlin → 支持 Java 8+
- Ktor → 支持 Java 8+
- SQLDelight → 支持 Java 8+

#### 4. 项目统一性

- ✅ 所有模块应使用相同的 JVM 目标版本
- ✅ `compileOptions` 和 `jvmTarget` 必须一致

**配置示例：**

```kotlin
// Android Application 模块
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)  // 必须与 compileOptions 一致
        }
    }
}

// KMP Android Library 模块
kotlin {
    androidLibrary {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)  // 保持一致
        }
    }
}
```

**当前项目推荐：JVM_11**

- ✅ 兼容 minSdk 24+
- ✅ AGP 8.x 兼容
- ✅ 主要依赖库都支持

**何时升级到 JVM_17？**

- AGP 9.x+（未来版本可能要求）
- 需要使用 Java 17+ 特性
- 依赖库要求 Java 17+

---

## Kotlin Multiplatform 测试问题

### Q1: 为什么在 Kotlin/JS 测试中，`@BeforeTest` 里的 `runTest {}` 不会等待完成？

**问题描述：**

在 Kotlin/JS 或 wasmJs 测试环境中，`@BeforeTest` 中使用 `runTest {}` 时，测试方法可能在数据库初始化完成之前就开始执行，导致测试失败。

**原因分析：**

1. **JS 没有"阻塞线程"这回事**：JS 是单线程 + event loop，`runTest {}` 返回 Promise，但测试框架不会等待它完成
2. **`@BeforeTest` 在 JS 是"fire-and-forget"**：`@BeforeTest` 不能是 suspend，返回值不会被 await，coroutine 直接丢进 event loop
3. **平台行为差异**：JVM/Android 上 `runTest` 会阻塞直到完成，但 JS/wasmJs 上 `runTest` 启动协程后立即返回

**解决方案：**

使用 `runDatabaseTest` 辅助函数，它已经处理了平台差异，自动创建和清理数据库：

```kotlin
@Test
fun `test database schema creation`() = runDatabaseTest { database ->
    val result = database.cardQueries.selectAll().awaitAsList()
    assertEquals(0, result.size)
}
```

**优点：**

- ✅ 平台一致：JVM、Android、iOS、JS 都使用相同写法
- ✅ 自动管理：自动创建和清理数据库，无需 `@BeforeTest` / `@AfterTest`
- ✅ 避免 JS 平台问题：不依赖测试框架等待 coroutine 完成

---

## 数据架构问题

### Q1: Domain Model 和 DTO 有什么区别？为什么要分离？

**问题描述：**

在代码中看到 `CardDto.toDomain()` 和 `Card.toDto()` 这样的转换函数，不理解为什么要区分 Domain Model 和 DTO。

**解决方案：**

**核心概念：**

#### Domain Model（领域模型）- `Card`

**定义：** 应用内部使用的核心业务模型，面向业务逻辑。

**特点：**

- ✅ 使用强类型（如 `CardType` 枚举、`Instant` 时间类型）
- ✅ 适合业务逻辑和计算
- ✅ 在应用内部流转（Repository、Service、ViewModel 等）

**示例：**

```kotlin
// core/datastore-model/src/commonMain/kotlin/.../model/Card.kt
data class Card(
    val id: String,
    val type: CardType,              // ✅ 枚举类型，类型安全
    val createdAt: Instant,          // ✅ Instant，便于时间计算
    val updatedAt: Instant,
    // ...
)
```

#### DTO（数据传输对象）- `CardDto`

**定义：** 用于跨边界传输的数据格式，面向序列化和 API 兼容性。

**特点：**

- ✅ 使用基础类型（如 `String`），便于序列化
- ✅ 适配外部接口（JSON、网络协议）
- ✅ 在边界处使用（API 请求/响应）

**示例：**

```kotlin
// core/datastore-model/src/commonMain/kotlin/.../model/CardDto.kt
@Serializable
data class CardDto(
    val id: String,
    val type: String,                // ✅ String，便于 API 兼容
    val createdAt: String,            // ✅ ISO 8601 格式字符串
    val updatedAt: String,
    // ...
)
```

#### 转换函数的作用

```kotlin
// DTO → Domain（从外部接收数据时）
fun CardDto.toDomain(): Card {
    return Card(
        type = CardType.valueOf(type.uppercase()),  // String → 枚举
        createdAt = Instant.parse(createdAt),       // String → Instant
        // ...
    )
}

// Domain → DTO（向外部发送数据时）
fun Card.toDto(): CardDto {
    return CardDto(
        type = type.name.lowercase(),              // 枚举 → String
        createdAt = createdAt.toString(),          // Instant → String
        // ...
    )
}
```

#### 使用场景示例

```kotlin
// 1. 从远程 API 接收数据 → 转换为 Domain
val remoteCard = remoteDataSource.getCardById(id)?.toDomain()

// 2. 在应用内部使用 Domain 模型
localDataSource.insertCard(remoteCard)  // Domain 模型

// 3. 发送到远程 API → 转换为 DTO
val request = card.toDto()
remoteDataSource.createCard(request)
```

#### 为什么需要分离？

1. **解耦：** Domain 不受外部 API 变化影响，API 变更只需修改 DTO 和转换逻辑
2. **类型安全：** Domain 使用强类型，减少运行时错误
3. **灵活性：** 可以独立修改 Domain 和 DTO，互不影响
4. **序列化：** DTO 专门为 JSON/网络传输优化，Domain 专注于业务逻辑

**总结：**

- **Domain Model** = 业务核心，应用内部使用
- **DTO** = 传输格式，用于 API 通信
- **转换函数** = 两者之间的桥梁，保持内部模型稳定，同时适配外部接口

---

## 其他问题

如果您遇到其他问题，请：

1. 查看项目 [README.md](../README.md)
2. 检查 [build-logic/README.md](../build-logic/README.md) 了解构建配置
3. 提交 Issue 到项目仓库

---

## 相关资源

- [Apple Developer Documentation - Code Signing](https://developer.apple.com/documentation/security/code_signing_services)
- [Xcode Help - Managing Signing](https://help.apple.com/xcode/mac/current/#/dev60b6fbbc7)
- [Troubleshooting Code Signing Issues](https://developer.apple.com/forums/tags/code-signing)
- [iOS Framework: 静态库 vs 动态库](./iOS_FRAMEWORK_STATIC_VS_DYNAMIC.md) - 详细说明静态库和动态库的区别及选择标准

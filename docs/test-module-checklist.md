# MyHub 测试模块清单

本文档列出需要编写/补全测试的模块，用于按清单编写测试用例并执行测试。

## 项目平台范围（KMP 全平台）

MyHub 为 **Kotlin Multiplatform (KMP) 全平台项目**，支持：

| 平台 | 说明 | 测试源集说明 |
|------|------|----------------|
| **Android** | 移动端 | `commonTest` 可参与；可选 `androidTest` 做 UI/仪器测试 |
| **iOS** | 移动端 | `commonTest` 可参与 |
| **JVM** | 桌面端（Desktop） | `commonTest`、`jvmTest` 在此运行；CI/本地常用 JVM 跑全量单元测试 |
| **JS** | Web 端 | `commonTest` 可参与；可用 `jsTest` 或 Karma 等 |
| **wasmJs** | WebAssembly 端 | `commonTest` 可参与 |

- **commonTest**：跨平台共享单元测试，在 JVM/JS 等已配置的 target 上运行；编写时避免平台专有 API，保证在各平台可执行。
- **jvmTest**：仅 JVM（含 Desktop）的测试，可依赖 JVM 专属库（如 JUnit 5、Ktor TestEngine）。
- 执行 `./gradlew allTests` 时，通常以当前启用的 target（如 `enabledPlatforms`）编译并运行对应测试；默认/CI 下常用 **JVM** 跑全量测试以保证速度与兼容性。

---

## 一、已有测试的模块（可作参考，可按需补充）

| 序号 | 模块路径                    | 测试类型                 | 已有测试文件                                                                                                                                             | 执行命令参考                                      |
|----|-------------------------|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------|
| 1  | `core/analytics`        | commonTest + jvmTest | AnalyticsEventTest, AnalyticsManagerTest, AnalyticsValueTest, BaseAnalyticsProviderTest, ConsoleProviderTest, MockProvider; FileProviderTest (jvm) | `./gradlew :core:analytics:allTests`        |
| 2  | `core/platform`         | commonTest           | PlatformTest, SystemPropertyTest, PlatformModuleTest                                                                                               | `./gradlew :core:platform:allTests`         |
| 3  | `core/logger`           | commonTest           | LoggerConfigTest, LoggerExtensionsTest, LoggerFactoryTest, LoggerImplTest, LoggerIntegrationTest                                                   | `./gradlew :core:logger:allTests`           |
| 4  | `core/platform-compose` | commonTest           | LanguageTest                                                                                                                                       | `./gradlew :core:platform-compose:allTests` |
| 5  | `core/network`          | commonTest           | ApiConfigTest, KtorClientFactoryTest, NetworkExceptionTest                                                                                         | `./gradlew :core:network:allTests`          |
| 6  | `core/network-test`     | commonTest           | KtorMockClientFactoryTest                                                                                                                          | `./gradlew :core:network-test:allTests`     |
| 7  | `core/cache`            | commonTest           | CacheConfigTest, CacheTest（CacheConfig 默认/自定义/校验、Cache get/put/getOrPut/invalidate/invalidateAll/maximumSize）                             | `./gradlew :core:cache:allTests`            |
| 8  | `feature/settings`      | commonTest           | SettingSerializerTest, SettingValueResolverTest, SettingImplTest, SettingsRepositoryImplTest, LanguageSettingTest, ThemeSettingTest, MockHelpers   | `./gradlew :feature:settings:allTests`      |
| 9  | `core/navigation`       | commonTest, jvmTest  | ListDetailSceneStrategyTest（策略可实例化）                                 | `./gradlew :core:navigation:allTests`        |
| 10 | `datastore/model`       | commonTest, jvmTest  | CardTest, TagTest, CollectionTest（相等性/拷贝/默认值/扩展）               | `./gradlew :datastore:model:allTests`        |
| 11 | `datastore/model-dto`   | commonTest, jvmTest  | ErrorDetailTest, ErrorResponseTest, LoginRequestTest, LoginResponseTest, ApiExceptionTest | `./gradlew :datastore:model-dto:allTests`     |
| 12 | `datastore/sync`        | commonTest, jvmTest  | SyncEntityTypeTest, SyncOperationsTest, SyncModelsTest（枚举与 SyncRequest/Config） | `./gradlew :datastore:sync:allTests`          |

---

## 二、已配置测试但无测试用例的模块（需补全）

这些模块的 `build.gradle.kts` 中已配置 `commonTest` 或 `jvmTest` 依赖，但 **尚无测试源文件**，需按优先级补全。

| 序号 | 模块路径                          | 已配置                 | 建议测试范围                                                       | 优先级 |
|----|-------------------------------|---------------------|--------------------------------------------------------------|-----|
| 1  | `core/app-build-config`       | commonTest, jvmTest | AppBuildConfig 及各平台 ConfigImpl 行为                            | 中   |
| 2  | `core/settings`               | commonTest          | LocalSettingStore, LocalSettingStoreImpl, SettingsModule     | 中   |
| 3  | `datastore/bootstrap`         | commonTest, jvmTest | BootstrapConfig, DefaultBootstrapConfigBuilder, 加载逻辑         | 中   |
| 4  | `datastore/repository-client` | commonTest          | Store 逻辑、RepositoryImpl、SyncChangeApplier（可配合 database-test） | 高   |
| 5  | `datastore/datasource-local`  | commonTest          | Local*DataSourceImpl（需 DatabaseTestHelper / 内存库）             | 高   |
| 6  | `datastore/datasource-remote` | commonTest          | Remote*DataSourceImpl（需 Mock 或 TestEngine）                   | 高   |
| 7  | `component/card`              | commonTest, jvmTest | Card 组件状态/样式、CardStyles、各 CardComponent 逻辑                   | 中   |

---

## 三、未配置测试的模块（需先加配置再写用例）

这些模块当前 **没有** `commonTest`/`jvmTest`/`test` 配置，需要先在对应 `build.gradle.kts` 中增加测试源集与依赖，再编写用例。

| 序号 | 模块路径                          | 模块说明           | 建议测试类型          | 建议测试范围                                                                                                                                     | 优先级            |
|----|-------------------------------|----------------|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------|----------------|
| 1  | `server`                      | Ktor 服务端       | JVM test        | AuthApi, CardsApi, CollectionsApi, SyncApi, TagsApi, CardTemplatesApi, UsersApi; TokenService, JwtConfig; CardService, CollectionService 等 | 高              |
| 2  | `composeApp`                  | 主应用 UI         | commonTest（可选）  | AppState, Navigation, 纯逻辑部分                                                                                                                | 低              |
| 3  | `feature/card`                | 卡片详情功能         | commonTest      | CardDetailViewModel, CardDetailUiState                                                                                                     | 中              |
| 4  | `feature/dashboard`           | 仪表盘功能          | commonTest      | DashboardViewModel, DashboardUiState                                                                                                       | 中              |
| 5  | `feature/profile`             | 个人资料功能         | commonTest      | ProfileViewModel, ProfileUiState                                                                                                           | 中              |
| 6  | `component/mixed`             | 混合组件（如 Avatar） | commonTest      | 可测试的纯逻辑/状态                                                                                                                                 | 低              |
| 7  | `datastore/repository-server` | 服务端仓库实现        | JVM test        | RepositoryImpl（依赖 database-test）                                                                                                           | 中（随 server 启用） |
| 8  | `datastore/database-client`   | 数据库驱动工厂        | 各平台 test / 集成测试 | DriverFactory 行为（可选）                                                                                                                       | 低              |
| 9  | `datastore/database-server`   | 服务端数据库配置       | JVM test（可选）    | DatabaseConfig（若可测）                                                                                                                        | 低              |
| 10 | `feature/favorite-api`        | 收藏 API 接口      | 多为接口，可省略        | -                                                                                                                                          | 低              |
| 11 | `feature/*-api`               | 各 feature-api  | 多为 API 定义，可省略   | -                                                                                                                                          | 低              |

---

## 四、特殊/基础设施模块（按需测试）

| 模块路径                              | 说明                 | 建议                                |
|-----------------------------------|--------------------|-----------------------------------|
| `datastore/database`              | SQLDelight 定义      | 一般通过 repository/datasource 集成测试覆盖 |
| `datastore/database-test`         | DatabaseTestHelper | 本身是测试基础设施，可做简单单元测试                |
| `datastore/repository-client-api` | Repository 接口定义    | 接口层，无需单独测试                        |
| `datastore/repository-server-api` | 服务端 Repository 接口  | 同上                                |
| `build-logic`                     | Gradle 脚本          | 可做脚本级或集成构建测试，非必须                  |
| `androidApp`                      | Android 壳工程        | 可选 UI/集成测试                        |

---

## 五、执行测试命令汇总

### 全项目测试（当前已配置测试的模块）

在 KMP 全平台项目下，通常通过 **JVM** 运行 commonTest + jvmTest 以覆盖大部分逻辑（与 `enabledPlatforms` 中是否包含 jvm 有关）：

```bash
./gradlew allTests
```

若需针对其他 target 运行测试（如仅编译并跑 JS 测试），可结合 `./gradlew jsTest` 或对应 target 的 test 任务；日常开发与 CI 建议以 JVM 全量测试为主。

### 按模块执行（示例）

```bash
# 单模块
./gradlew :core:logger:allTests
./gradlew :core:network:allTests
./gradlew :feature:settings:allTests

# 仅 JVM 平台（若启用）
./gradlew jvmTest

# 仅 commonTest（多平台）
./gradlew compileKotlinIosArm64 compileKotlinJs compileKotlinJvm
# 然后按需：
./gradlew :core:analytics:allTests
```

### 仅运行 commonTest（KMP）

```bash
./gradlew :core:cache:allTests
```

### Server 模块（需先补全 src/test 及用例）

```bash
./gradlew :server:test
```

---

## 六、建议实施顺序

1. **第一批（高优先级 + 已配置）**  
   `datastore/repository-client`、`datastore/datasource-local`、`datastore/datasource-remote`  
   → 直接补全 commonTest/jvmTest 用例并执行。（`core/cache`、`core/navigation`、`datastore/model`、`datastore/model-dto`、`datastore/sync` 已完成）

2. **第二批（高优先级 + 需先配置）**  
   `server`  
   → 在 `server` 中新增 `src/test` 与 JUnit/Ktor TestEngine 配置，再为 API、Service、Auth 编写测试。

3. **第三批（中优先级）**  
   `core/app-build-config`、`core/settings`、`datastore/bootstrap`、`component/card`、`feature/card`、`feature/dashboard`、`feature/profile`、`datastore/repository-server`（若启用 server）。

4. **第四批（低优先级）**  
   `composeApp`、`component/mixed`、各 `*-api`、`database-client`/`database-server` 等按需补全。

---

## 七、清单使用方式

- **编写用例**：按「二、已配置测试但无测试用例」和「三、未配置测试」中的「建议测试范围」逐模块补全。
- **执行测试**：用「五、执行测试命令汇总」在本地或 CI 中跑 `./gradlew allTests` 或单模块命令。
- **进度跟踪**：可在本文档中为每个模块增加状态列（如：未开始 / 进行中 / 已完成），或与 issue/看板联动。

完成一批后，建议在项目根目录执行一次全量测试并确认通过：

```bash
./gradlew allTests
```

若某模块尚未配置测试，对应任务会不存在或跳过，属正常；按清单补全配置与用例即可。

---

## 八、已知问题与修复

- **core/logger**：`LoggerConfigTest` 中曾断言 `useAndroidLogger` 默认值为 `true`，与 `LoggerConfig` 实际默认值 `false` 不一致，已修正测试断言。
- **feature/settings**：测试存在编译错误（`SettingsRepositoryImplTest` 中 `data` 未解析；`SettingValueResolverTest` 中 `UserPreferences`、`MockUserRepository`、`createTestUser` 未解析；`MockHelpers.MockLocalSettingStore` 未实现 `getSync`/`setSync`/`removeSync`）。需在补全测试时一并修复依赖与接口实现。

# 网络层实现文档

## 概述

本文档描述了使用 Ktor Client 实现的远程数据源（RemoteCardDataSource）。

## 架构

```
RemoteCardDataSource
    ↓
RemoteCardDataSourceImpl (使用 Ktor Client)
    ↓
HttpClient (配置了序列化、日志等)
    ↓
API Server
```

## 实现组件

### 1. ApiConfig

API 配置类，包含：
- 基础 URL：`http://localhost:8083`
- API 路径定义
- 超时配置

### 2. KtorClientFactory

跨平台的 Ktor Client 引擎工厂：
- **Android**: `Android` 引擎
- **iOS**: `Darwin` 引擎
- **Desktop (JVM)**: `CIO` 引擎
- **Web (JS/WASM)**: `Js` 引擎

### 3. HttpClient 配置

创建的 HttpClient 包含以下配置：
- **ContentNegotiation**: JSON 序列化/反序列化
- **DefaultRequest**: 默认请求头（Content-Type, Accept）
- **Logging**: 请求/响应日志（INFO 级别）

### 4. RemoteCardDataSourceImpl

实现了所有远程数据源接口方法：
- `getAllCards()`: 获取所有卡片
- `getCardById(id)`: 根据ID获取卡片
- `searchCards(filter)`: 搜索卡片
- `createCard(request)`: 创建卡片
- `updateCard(id, request)`: 更新卡片
- `deleteCard(id)`: 删除卡片
- `toggleFavorite(id)`: 切换收藏状态

## 错误处理

### ApiException
API 返回错误状态码时抛出。

### NetworkException
网络连接错误时抛出。

## API 端点

所有 API 端点基于 `ApiConfig.BASE_URL`：

- `GET /api/cards` - 获取所有卡片
- `GET /api/cards/{id}` - 获取指定卡片
- `GET /api/cards/search?q=...&types=...&tags=...` - 搜索卡片
- `POST /api/cards` - 创建卡片
- `PUT /api/cards/{id}` - 更新卡片
- `DELETE /api/cards/{id}` - 删除卡片
- `POST /api/cards/{id}/favorite` - 切换收藏状态

## 使用示例

```kotlin
// 在 ViewModel 中使用
class DashboardViewModel(
    private val cardRepository: CardRepository
) : ViewModel() {
    
    fun loadCards() {
        viewModelScope.launch {
            try {
                cardRepository.sync() // 从远程同步数据
            } catch (e: NetworkException) {
                // 处理网络错误
            } catch (e: ApiException) {
                // 处理API错误
            }
        }
    }
}
```

## 依赖注入

网络模块已配置在 `NetworkModule` 中：

```kotlin
val networkModule = module {
    single<KtorClientFactory> { KtorClientFactory() }
    single<HttpClient> { createHttpClient(get()) }
}
```

## 注意事项

1. **超时配置**: 默认连接超时和请求超时都是 30 秒
2. **错误处理**: 所有网络操作都应该捕获 `NetworkException` 和 `ApiException`
3. **序列化**: 使用 `kotlinx.serialization` 进行 JSON 序列化
4. **日志**: 生产环境可以禁用或降低日志级别

## 下一步

- [ ] 添加请求重试机制
- [ ] 添加认证拦截器（如果需要）
- [ ] 实现其他 RemoteDataSource（Tag, Template, User, Statistics）
- [ ] 添加请求缓存策略
- [ ] 实现离线队列（待同步操作）


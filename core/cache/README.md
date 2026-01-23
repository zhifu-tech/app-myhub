# Core Cache Module

本模块用于**规范**和**实现** MyHub 应用的缓存基础设施（Cache Infra），为各功能模块**提供统一、跨平台的内存缓存能力**。它基于 [cache4k](https://reactivecircus.github.io/cache4k/) 库并封装为稳定的 `Cache` 接口，避免业务层直接依赖第三方 API，从而降低接口变更带来的风险。

## 功能特性

- ✅ 跨平台支持（Android、iOS、JVM、JS、WASM）
- ✅ 统一的缓存接口（`Cache`）
- ✅ 缓存大小限制（`maximumSize`）
- ✅ 过期策略支持（`expireAfterWrite` / `expireAfterAccess`）

## 核心组件

### 1. Cache 接口

统一的缓存访问接口：

- **`get(key)`**：读取缓存，未命中返回 null
- **`getOrPut(key) { }`**：读取缓存，未命中则加载并写入
- **`put(key, value)`**：写入缓存
- **`invalidate(key)`**：删除单个缓存
- **`invalidateAll()`**：清空缓存

### 2. CacheConfig

缓存配置：

- **`maximumSize`**：最大缓存条目数
- **`expireAfterWrite`**：写入后过期时间（可选）
- **`expireAfterAccess`**：访问后过期时间（可选）

### 3. CacheFactory

缓存工厂接口，负责创建具体缓存实例，默认实现基于 cache4k。

## 使用示例

```kotlin
import kotlin.time.Duration.Companion.minutes
import tech.zhifu.app.myhub.cache.CacheConfig
import tech.zhifu.app.myhub.cache.cache

// 方式一：直接创建缓存
val cache = cache<String, String>(
    CacheConfig(
        maximumSize = 1_000,
        expireAfterWrite = 10.minutes
    )
)

cache.put("key", "value")
val value = cache.get("key")
```

## 文档

- [MyHub 缓存模块方案设计](./docs/myhub-cache-infra-v1.0.md)


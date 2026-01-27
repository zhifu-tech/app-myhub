# Store5 架构设计及使用指南

> 基于 Mobile Native Foundation 的 Store5 库（版本 5.1.0-alpha08）
>
> 本文档介绍 Store5 的核心概念、架构设计和使用方法，并结合 MyHub 项目的集成实践。

---

## 目录

1. [概述](#1-概述)
2. [核心概念](#2-核心概念)
3. [架构设计](#3-架构设计)
4. [内存缓存机制深入分析](#4-内存缓存机制深入分析)
   - 4.12 [StoreMultiCache 深入分析](#412-storemulticache-深入分析)
5. [使用指南](#5-使用指南)
6. [MyHub 集成实践](#6-myhub-集成实践)
7. [最佳实践](#7-最佳实践)
8. [常见问题](#8-常见问题)

---

## 1. 概述

### 1.1 什么是 Store5？

Store5 是一个 **Kotlin Multiplatform 数据存储库**，提供统一的数据层抽象，核心功能包括：

- **网络数据获取**（Fetcher）
- **内存缓存**（Cache）
- **本地持久化**（SourceOfTruth）
- **响应式数据流**（Flow）
- **可变存储与冲突解决**（MutableStore）

### 1.2 设计理念

| 理念       | 说明                                    |
|----------|---------------------------------------|
| **离线优先** | 优先使用本地数据，网络作为补充                       |
| **响应式**  | 基于 Kotlin Flow，实时响应数据变化               |
| **一致性**  | 通过 SourceOfTruth 保证数据一致性              |
| **高性能**  | Multicaster 共享请求，避免重复网络调用             |
| **可观测**  | 响应包含数据来源（Cache/SourceOfTruth/Fetcher） |

### 1.3 支持平台

- Android
- iOS
- JVM（Java 11+）
- JavaScript（JS IR）
- WebAssembly（WasmJS）
- Linux（x64）

---

## 2. 核心概念

### 2.1 Store vs MutableStore

#### Store（只读存储）

```kotlin
interface Store<Key : Any, Output : Any> :
    Read.Stream<Key, Output>,
    Clear.Key<Key>,
    Clear.All
```

- 只读数据管理
- 提供流式读取（`stream()`）
- 不支持本地写入

#### MutableStore（可变存储）

```kotlin
interface MutableStore<Key : Any, Output : Any> :
    Read.StreamWithConflictResolution<Key, Output>,
    Write<Key, Output>,
    Write.Stream<Key, Output>,
    Clear.Key<Key>,
    Clear
```

- 扩展 Store，支持写入
- 提供冲突解决机制
- 支持流式写入

### 2.2 Fetcher（网络获取器）

Fetcher 负责从网络获取数据：

```kotlin
interface Fetcher<Key : Any, Network : Any> {
    val name: String?
    val fallback: Fetcher<Key, Network>?
    operator fun invoke(key: Key): Flow<FetcherResult<Network>>
}
```

#### 创建方式

```kotlin
// 单次响应（HTTP 请求）
Fetcher.of { key: String ->
    api.fetchData(key)
}

// 流式响应（WebSocket）
Fetcher.ofFlow { key: String ->
    api.streamData(key)
}

// 返回 FetcherResult（支持自定义错误）
Fetcher.ofResult { key: String ->
    try {
        FetcherResult.Data(api.fetchData(key))
    } catch (e: Exception) {
        FetcherResult.Error.Exception(e)
    }
}
```

#### FetcherResult

```kotlin
sealed class FetcherResult<out Network : Any> {
    data class Data<Network : Any>(val value: Network)
    sealed class Error : FetcherResult<Nothing>() {
        data class Exception(val error: Throwable)
        data class Message(val message: String)
        data class Custom<E : Any>(val error: E)
    }
}
```

### 2.3 SourceOfTruth（本地数据源）

SourceOfTruth 是本地持久化层的抽象：

```kotlin
interface SourceOfTruth<Key : Any, Local : Any, Output : Any> {
    fun reader(key: Key): Flow<Output?>
    suspend fun write(key: Key, value: Local)
    suspend fun delete(key: Key)
    suspend fun deleteAll()
}
```

#### 创建方式

```kotlin
// 使用 SQLDelight 或 Room
SourceOfTruth.of<String, Card, Card>(
    reader = { key -> database.cardDao().observeCard(key) },
    writer = { key, card -> database.cardDao().insert(card) },
    delete = { key -> database.cardDao().delete(key) },
    deleteAll = { database.cardDao().deleteAll() }
)
```

### 2.4 Converter（类型转换器）

当网络模型、本地模型和输出模型不同时使用：

```kotlin
interface Converter<Network : Any, Local : Any, Output : Any> {
    fun fromNetworkToLocal(network: Network): Local
    fun fromOutputToLocal(output: Output): Local
}
```

#### 示例

```kotlin
Converter.Builder<CardDto, CardEntity, Card>()
    .fromNetworkToLocal { dto -> dto.toEntity() }
    .fromOutputToLocal { card -> card.toEntity() }
    .build()
```

### 2.5 Validator（数据验证器）

验证缓存数据是否有效：

```kotlin
interface Validator<Output : Any> {
    suspend fun isValid(item: Output): Boolean
}
```

#### 示例

```kotlin
Validator<Card> { card ->
    // 检查数据是否过期
    System.currentTimeMillis() - card.updatedAt < TimeUnit.HOURS.toMillis(1)
}
```

### 2.6 Bookkeeper（同步记录器）

用于 MutableStore，记录同步失败的时间戳：

```kotlin
interface Bookkeeper<Key : Any> {
    suspend fun getLastFailedSync(key: Key): Long?
    suspend fun setLastFailedSync(key: Key, timestamp: Long): Boolean
    suspend fun clear(key: Key): Boolean
    suspend fun clearAll(): Boolean
}
```

### 2.7 Updater（远程更新器）

用于 MutableStore，将本地变更同步到服务器：

```kotlin
interface Updater<Key : Any, Output : Any, Response : Any> {
    suspend fun post(key: Key, value: Output): UpdaterResult<Response>
    val onCompletion: OnUpdaterCompletion<Response>?
}
```

---

## 3. 架构设计

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        ViewModel                             │
│                           │                                  │
│                     store.stream()                           │
│                           ↓                                  │
├─────────────────────────────────────────────────────────────┤
│                         Store                                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    MemoryCache                       │    │
│  │              (expireAfterWrite/Access)               │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │               SourceOfTruth (SOT)                    │    │
│  │           (SQLDelight / Room / DataStore)            │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                     Fetcher                          │    │
│  │              (HTTP / WebSocket / gRPC)               │    │
│  └─────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                      Network / API                           │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 读取流程

```
StoreReadRequest
       │
       ▼
┌──────────────────┐
│  检查内存缓存     │ ──命中──▶ 返回 Data(origin=Cache)
└──────────────────┘
       │ 未命中
       ▼
┌──────────────────┐
│  检查 SourceOfTruth │ ──有数据──▶ 返回 Data(origin=SourceOfTruth)
└──────────────────┘              ↓ (如果 refresh=true)
       │ 无数据                   ▼
       ▼                  ┌──────────────────┐
┌──────────────────┐      │  调用 Fetcher     │
│  调用 Fetcher     │      └──────────────────┘
└──────────────────┘              │
       │                          ▼
       ▼               写入 SourceOfTruth + 更新内存缓存
写入 SourceOfTruth              │
+ 更新内存缓存                   ▼
       │              返回 Data(origin=Fetcher)
       ▼
返回 Data(origin=Fetcher)
```

### 3.3 写入流程（MutableStore）

```
StoreWriteRequest
       │
       ▼
┌──────────────────┐
│  写入本地 SOT     │
└──────────────────┘
       │
       ▼
┌──────────────────┐
│  调用 Updater     │ ──成功──▶ 清理写入队列
└──────────────────┘
       │ 失败
       ▼
┌──────────────────┐
│  记录到 Bookkeeper │ (用于冲突解决)
└──────────────────┘
```

### 3.4 请求类型（StoreReadRequest）

| 方法                           | 内存缓存 | SourceOfTruth | Fetcher | 用途    |
|------------------------------|:----:|:-------------:|:-------:|-------|
| `fresh(key)`                 |  跳过  |      跳过       |    ✓    | 强制刷新  |
| `cached(key, refresh=false)` |  ✓   |       ✓       |   按需    | 默认读取  |
| `cached(key, refresh=true)`  |  ✓   |       ✓       |    ✓    | 读取并刷新 |
| `localOnly(key)`             |  ✓   |       ✓       |    ✗    | 仅本地   |
| `skipMemory(key, refresh)`   |  跳过  |       ✓       |   按需    | 跳过内存  |

### 3.5 响应类型（StoreReadResponse）

```kotlin
sealed class StoreReadResponse<out Output> {
    object Initial                    // 初始状态
    data class Loading(origin)        // 加载中
    data class Data(value, origin)    // 数据
    data class NoNewData(origin)      // 无新数据（验证失败）
    sealed class Error {
        data class Exception(error, origin)
        data class Message(message, origin)
        data class Custom(error, origin)
    }
}
```

**origin（数据来源）**:

- `Cache` - 内存缓存
- `SourceOfTruth` - 本地存储
- `Fetcher(name)` - 网络

---

## 4. 内存缓存机制深入分析

> 本章节深入分析 Store5 的内存缓存实现，基于源码剖析其工作原理。

### 4.1 缓存在 Store 架构中的位置

```kotlin
// 源码：store/src/commonMain/kotlin/.../impl/RealStore.kt
internal class RealStore<Key : Any, Network : Any, Output : Any, Local : Any>(
    scope: CoroutineScope,
    fetcher: Fetcher<Key, Network>,
    sourceOfTruth: SourceOfTruth<Key, Local, Output>? = null,
    private val converter: Converter<Network, Local, Output>,
    private val validator: Validator<Output>?,
    private val memCache: Cache<Key, Output>?,  // ⬅️ 内存缓存
) : Store<Key, Output>
```

**关键点**：每个 `Store` 实例持有一个独立的 `memCache` 实例。

### 4.2 缓存创建过程

```kotlin
// 源码：store/src/commonMain/kotlin/.../impl/RealStoreBuilder.kt
override fun build(): Store<Key, Output> =
    RealStore(
        // ...
        memCache = memoryCache ?: cachePolicy?.let {
            CacheBuilder<Key, Output>().apply {
                if (cachePolicy!!.hasAccessPolicy) {
                    expireAfterAccess(cachePolicy!!.expireAfterAccess)
                }
                if (cachePolicy!!.hasWritePolicy) {
                    expireAfterWrite(cachePolicy!!.expireAfterWrite)
                }
                if (cachePolicy!!.hasMaxSize) {
                    maximumSize(cachePolicy!!.maxSize)
                }
                if (cachePolicy!!.hasMaxWeight) {
                    weigher(cachePolicy!!.maxWeight) { key, value ->
                        cachePolicy!!.weigher.weigh(key, value)
                    }
                }
            }.build()  // ⬅️ 每次 build() 创建新的 Cache 实例
        },
    )
```

**结论**：每次调用 `StoreBuilder.build()` 都会创建一个新的、独立的 `Cache` 实例。

### 4.3 缓存读取流程（源码分析）

```kotlin
// 源码：store/src/commonMain/kotlin/.../impl/RealStore.kt
override fun stream(request: StoreReadRequest<Key>): Flow<StoreReadResponse<Output>> =
    flow {
        // 步骤 1: 检查是否应该跳过内存缓存
        val cachedToEmit = if (request.shouldSkipCache(CacheType.MEMORY)) {
            null
        } else {
            // 步骤 2: 尝试从缓存获取（按 Key 精确匹配）
            val output: Output? = memCache?.getIfPresent(request.key)
            val isInvalid = output != null && validator?.isValid(output) == false
            when {
                output == null || isInvalid -> null
                else -> output
            }
        }

        // 步骤 3: 如果缓存命中，立即发射
        cachedToEmit?.let { it: Output ->
            emit(StoreReadResponse.Data(value = it, origin = StoreReadResponseOrigin.Cache))
        }
        // ... 继续处理 SourceOfTruth 和 Fetcher
    }
```

**关键**：`memCache?.getIfPresent(request.key)` - 使用 `Key` 的 `hashCode()` 和 `equals()` 进行精确匹配。

### 4.4 缓存写入流程（源码分析）

```kotlin
// 源码：store/src/commonMain/kotlin/.../impl/RealStore.kt
}.onEach {
    // 每当有数据被分发时，保存到内存缓存
    if (it.origin != StoreReadResponseOrigin.Cache) {
        it.dataOrNull()?.let { data ->
            memCache?.put(request.key, data)  // ⬅️ 按 Key 存储
        }
    }
}

// 写入操作时也会更新缓存
internal suspend fun write(key: Key, value: Output): StoreDelegateWriteResult =
    try {
        val writeException = sourceOfTruth?.write(key, converter.fromOutputToLocal(value))
        if (writeException != null) {
            StoreDelegateWriteResult.Error.Exception(writeException)
        } else {
            memCache?.put(key, value)  // ⬅️ 写入成功后更新缓存
            StoreDelegateWriteResult.Success
        }
    } catch (error: Throwable) {
        StoreDelegateWriteResult.Error.Exception(error)
    }
```

### 4.5 LocalCache 核心实现（Guava Cache 的 KMP 移植）

```kotlin
// 源码：cache/src/commonMain/kotlin/.../cache5/LocalCache.kt
internal class LocalCache<K : Any, V : Any>(builder: CacheBuilder<K, V>) {
    // 分段锁设计（类似 ConcurrentHashMap）
    private val segments: Array<Segment<K, V>?>
    
    // 按 Key 的 hashCode 选择 segment
    private fun segmentFor(hash: Int): Segment<K, V> =
        segments[hash ushr segmentShift and segmentMask] as Segment<K, V>
    
    // hash 函数（Wang/Jenkins hash）
    private fun hash(key: K): Int = rehash(key.hashCode())
    
    // 获取缓存值
    fun getIfPresent(key: K): V? {
        val hash = hash(key)  // ⬅️ 使用 Key 的 hashCode
        return segmentFor(hash).get(key, hash)
    }
    
    // 存储缓存值
    fun put(key: K, value: V): V? {
        val hash = hash(key)  // ⬅️ 使用 Key 的 hashCode
        return segmentFor(hash).put(key, hash, value, false)
    }
    
    // 清除指定 key
    fun remove(key: K): V? {
        val hash = hash(key)
        return segmentFor(hash).remove(key, hash)
    }
}
```

### 4.6 缓存过期策略

```kotlin
// 源码：cache/src/commonMain/kotlin/.../cache5/LocalCache.kt
private val expireAfterAccessNanos: Long  // 访问后过期时间
private val expireAfterWriteNanos: Long   // 写入后过期时间

// 检查是否过期
private fun isExpired(entry: ReferenceEntry<K, V>, now: Long): Boolean =
    if (expiresAfterAccess && now - entry.accessTime >= expireAfterAccessNanos) {
        true
    } else {
        expiresAfterWrite && now - entry.writeTime >= expireAfterWriteNanos
    }

// 过期条目清理（惰性清理）
private fun expireEntries(now: Long) {
    drainRecencyQueue()
    // 清理写入队列中的过期条目
    while (true) {
        val e = writeQueue.peek()?.takeIf { map.isExpired(it, now) } ?: break
        if (!removeEntry(e, e.hash, RemovalCause.EXPIRED)) {
            throw AssertionError()
        }
    }
    // 清理访问队列中的过期条目
    while (true) {
        val e = accessQueue.peek()?.takeIf { map.isExpired(it, now) } ?: break
        if (!removeEntry(e, e.hash, RemovalCause.EXPIRED)) {
            throw AssertionError()
        }
    }
}
```

### 4.7 缓存淘汰策略（LRU）

```kotlin
// 源码：cache/src/commonMain/kotlin/.../cache5/LocalCache.kt
// 基于 AccessQueue 实现 LRU
private class AccessQueue<K : Any, V : Any> : MutableQueue<ReferenceEntry<K, V>> {
    // 双向链表头
    private val head: ReferenceEntry<K, V> = ...
    
    // 添加到队尾（最近访问）
    override fun add(value: ReferenceEntry<K, V>) {
        connectAccessOrder(value.previousInAccessQueue, value.nextInAccessQueue)
        connectAccessOrder(head.previousInAccessQueue, value)
        connectAccessOrder(value, head)
    }
    
    // 从队头获取（最久未访问）
    override fun peek(): ReferenceEntry<K, V>? {
        val next = head.nextInAccessQueue
        return if (next === head) null else next
    }
}

// 超出容量时淘汰
private fun evictEntries(newest: ReferenceEntry<K, V>) {
    if (!map.evictsBySize) return
    drainRecencyQueue()
    
    while (totalWeight > maxSegmentWeight) {
        val e = nextEvictable  // 获取队头（最久未访问）
        if (!removeEntry(e, e.hash, RemovalCause.SIZE)) {
            throw AssertionError()
        }
    }
}
```

### 4.8 多 Store 缓存隔离示意图

```
┌─────────────────────────────────────────────────────────────────┐
│                        cardsStore                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  memCache (Cache<CardStoreKey.ByUser, List<Card>>)      │    │
│  │                                                          │    │
│  │    ByUser("user-1")                                      │    │
│  │      └─ hashCode() = 12345                              │    │
│  │      └─ value = [card1, card2, card3]                   │    │
│  │                                                          │    │
│  │    ByUser("user-2")                                      │    │
│  │      └─ hashCode() = 67890                              │    │
│  │      └─ value = [card4, card5]                          │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                         cardStore                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  memCache (Cache<CardStoreKey.ById, Card>)              │    │
│  │                                                          │    │
│  │    ById("card-1")                                        │    │
│  │      └─ hashCode() = 11111                              │    │
│  │      └─ value = card1                                   │    │
│  │                                                          │    │
│  │    ById("card-2")                                        │    │
│  │      └─ hashCode() = 22222                              │    │
│  │      └─ value = card2                                   │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘

⚠️ 注意：两个 Store 的内存缓存完全隔离，即使数据相关也不共享
```

### 4.9 单 Store 多 Key 缓存示意图（如 Trails 模式）

```
┌─────────────────────────────────────────────────────────────────┐
│                     单个 Store (Operation 模式)                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │  memCache (Cache<Operation, PostOutput>)                │    │
│  │                                                          │    │
│  │    FindOne(Key(1))                                       │    │
│  │      └─ hashCode() = 100001                             │    │
│  │      └─ value = PostOutput.Single(post1)  ← 独立缓存    │    │
│  │                                                          │    │
│  │    FindOne(Key(2))                                       │    │
│  │      └─ hashCode() = 100002                             │    │
│  │      └─ value = PostOutput.Single(post2)  ← 独立缓存    │    │
│  │                                                          │    │
│  │    QueryMany(query)                                      │    │
│  │      └─ hashCode() = 200001                             │    │
│  │      └─ value = PostOutput.Collection([...]) ← 独立缓存 │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ⚠️ 即使在同一个 Store 中，不同 Key 的缓存也是独立的           │
│     FindOne(Key(1)) 和 QueryMany(...) 不共享缓存               │
└─────────────────────────────────────────────────────────────────┘
```

### 4.10 缓存共享的真相

| 缓存层 | 是否共享 | 说明 |
|-------|---------|------|
| **内存缓存** | ❌ 不共享 | 每个 Key 有独立的缓存条目，不同 Store 有独立的 Cache 实例 |
| **SourceOfTruth** | ✅ 共享 | 多个 Store 可以使用同一个数据库/DataSource |

**Store5 设计原则**：内存缓存采用简单的 Key-Value 映射，不做任何规范化（normalization）或跨 Key 关联。

### 4.11 数据共享的两种方式

#### 方式 1：通过 SourceOfTruth（数据库）共享

```
1. cardsStore 从网络获取 [card1, card2, card3]
   └─ 写入数据库：INSERT card1, card2, card3
   └─ 写入 cardsStore 内存缓存

2. cardStore 查询 card1
   └─ cardStore 内存缓存 MISS
   └─ 从数据库读取 card1 ✅ (数据已存在)
   └─ 写入 cardStore 内存缓存
   └─ 不需要网络请求！
```

#### 方式 2：通过 StoreMultiCache 实现内存缓存共享（推荐）

Store5 提供了 `StoreMultiCache`，可以实现列表自动分解到单项缓存：

```
1. Store 请求 ByUser("user-1")
   └─ Fetcher 返回 Collection([card1, card2, card3])
   └─ Store 自动调用 memCache.put(ByUser, Collection)
   └─ StoreMultiCache 自动分解：
       ├─ collectionsCache.put(ByUser("user-1"), Collection)
       ├─ singlesCache.put(ById("card1"), Single(card1))  ← 自动
       ├─ singlesCache.put(ById("card2"), Single(card2))  ← 自动
       └─ singlesCache.put(ById("card3"), Single(card3))  ← 自动

2. Store 请求 ById("card2")
   └─ Store 自动调用 memCache.getIfPresent(ById("card2"))
   └─ StoreMultiCache 从 singlesCache 返回
   └─ ✅ 内存缓存命中！无需数据库或网络请求
```

### 4.12 StoreMultiCache 深入分析

#### 4.12.1 什么是 StoreMultiCache

`StoreMultiCache` 是 Store5 提供的高级缓存实现，支持**列表自动分解**功能：

```kotlin
// 源码：cache/src/commonMain/kotlin/.../cache5/StoreMultiCache.kt
class StoreMultiCache<Id, Key, Single, Collection, Output>(
    private val keyProvider: KeyProvider<Id, Single>,
    singlesCache: Cache<StoreKey.Single<Id>, Single>,
    collectionsCache: Cache<StoreKey.Collection<Id>, Collection>,
) : Cache<Key, Output>  // ⬅️ 实现 Cache 接口
```

**关键点**：`StoreMultiCache` 实现了 `Cache<Key, Output>` 接口，因此可以作为 `memoryCache` 参数传入 `StoreBuilder`。

#### 4.12.2 使用前提

要使用 `StoreMultiCache`，需要满足以下条件：

| 组件 | 要求 |
|-----|-----|
| **Key** | 实现 `StoreKey` 接口的 sealed class，包含 `Single` 和 `Collection` 子类型 |
| **Output** | 实现 `StoreData` 接口的 sealed class，包含 `Single` 和 `Collection` 子类型 |
| **KeyProvider** | 实现 Key 类型之间的转换逻辑 |

#### 4.12.3 正确的使用方式

```kotlin
// 1. 定义 Key（实现 StoreKey）
@OptIn(ExperimentalStoreApi::class)
sealed class CardStoreKey : StoreKey<String> {
    data class ById(override val id: String) : CardStoreKey(), StoreKey.Single<String>
    data class ByUser(
        val userId: String,
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : CardStoreKey(), StoreKey.Collection<String>
}

// 2. 定义 Output（实现 StoreData）
@OptIn(ExperimentalStoreApi::class)
sealed class CardOutput : StoreData<String> {
    data class Single(val card: Card) : CardOutput(), StoreData.Single<String> {
        override val id: String get() = card.id
    }
    data class Collection(
        override val items: List<Single>,
        val userId: String
    ) : CardOutput(), StoreData.Collection<String, Single> {
        override fun copyWith(items: List<Single>) = copy(items = items)
        override fun insertItems(strategy: InsertionStrategy, items: List<Single>) = ...
    }
}

// 3. 实现 KeyProvider
class CardKeyProvider : KeyProvider<String, CardOutput.Single> {
    override fun fromCollection(
        key: StoreKey.Collection<String>,
        value: CardOutput.Single
    ): StoreKey.Single<String> = CardStoreKey.ById(value.id)

    override fun fromSingle(
        key: StoreKey.Single<String>,
        value: CardOutput.Single
    ): StoreKey.Collection<String> = CardStoreKey.ByUser(value.card.userId)
}

// 4. 创建 StoreMultiCache
val multiCache = StoreMultiCache<String, CardStoreKey, CardOutput.Single, 
                                  CardOutput.Collection, CardOutput>(
    keyProvider = CardKeyProvider(),
    singlesCache = CacheBuilder<StoreKey.Single<String>, CardOutput.Single>()
        .maximumSize(500)
        .expireAfterWrite(1.hours)
        .build(),
    collectionsCache = CacheBuilder<StoreKey.Collection<String>, CardOutput.Collection>()
        .maximumSize(50)
        .expireAfterWrite(1.hours)
        .build()
)

// 5. 创建 Store，传入 multiCache 作为 memoryCache
val store: MutableStore<CardStoreKey, CardOutput> = StoreBuilder.from(
    fetcher = createFetcher(),
    sourceOfTruth = createSourceOfTruth(),
    memoryCache = multiCache  // ⬅️ 关键：传入 StoreMultiCache
).toMutableStoreBuilder(
    converter = createConverter()
).build(
    updater = createUpdater(),
    bookkeeper = createBookkeeper()
)
```

#### 4.12.4 StoreMultiCache 内部原理

```kotlin
// 源码：cache/src/commonMain/kotlin/.../cache5/StoreMultiCache.kt
override fun put(key: Key, value: Output) {
    when (key) {
        is StoreKey.Single<*> -> {
            val single = value as Single
            // 1. 存入单项缓存
            accessor.putSingle(key.castSingle(), single)

            // 2. 更新对应的列表缓存（如果存在）
            val collectionKey = keyProvider.fromSingle(key.castSingle(), single)
            val existingCollection = accessor.getCollection(collectionKey)
            if (existingCollection != null) {
                val updatedItems = existingCollection.items.map {
                    if (it.id == single.id) single else it
                }
                accessor.putCollection(collectionKey, existingCollection.copyWith(updatedItems))
            }
        }

        is StoreKey.Collection<*> -> {
            val collection = value as Collection
            // 1. 存入列表缓存
            accessor.putCollection(key.castCollection(), collection)

            // 2. 分解列表，存入单项缓存
            collection.items.forEach { single ->
                accessor.putSingle(
                    keyProvider.fromCollection(key.castCollection(), single),
                    single
                )
            }
        }
    }
}
```

#### 4.12.5 注意事项

| 注意点 | 说明 |
|-------|------|
| **实验性 API** | 标记为 `@ExperimentalStoreApi`，API 可能变化 |
| **单 Store 设计** | 必须使用单个 Store，Key 和 Output 都是 sealed class |
| **不要手动调用** | Store 内部自动调用 `memCache.put()`，不要在外部手动调用 |
| **KeyProvider 实现** | 需要正确实现 `fromCollection` 和 `fromSingle` 方法 |

### 4.13 MemoryPolicy 配置详解

```kotlin
// 源码：store/src/commonMain/kotlin/.../store5/MemoryPolicy.kt
class MemoryPolicy<in Key : Any, in Value : Any>(
    val expireAfterWrite: Duration,    // 写入后过期时间
    val expireAfterAccess: Duration,   // 访问后过期时间（与 write 互斥）
    val maxSize: Long,                 // 最大条目数
    val maxWeight: Long,               // 最大权重（与 maxSize 互斥）
    val weigher: Weigher<Key, Value>,  // 权重计算器
)

// 使用示例
MemoryPolicy.builder<String, Card>()
    .setMaxSize(100)                   // 最多缓存 100 个条目
    .setExpireAfterWrite(1.hours)      // 写入 1 小时后过期
    .build()

// 或使用权重控制
MemoryPolicy.builder<String, List<Card>>()
    .setWeigherAndMaxWeight(
        weigher = { _, cards -> cards.size },  // 按列表大小计算权重
        maxWeight = 1000                       // 最多缓存 1000 张卡片
    )
    .build()
```

### 4.13 缓存调试技巧

```kotlin
// 1. 检查缓存命中情况
store.stream(request).collect { response ->
    when (response.origin) {
        is StoreReadResponseOrigin.Cache -> println("✅ 内存缓存命中")
        is StoreReadResponseOrigin.SourceOfTruth -> println("📁 从数据库读取")
        is StoreReadResponseOrigin.Fetcher -> println("🌐 从网络获取")
    }
}

// 2. 禁用缓存进行调试
val store = StoreBuilder.from(fetcher, sourceOfTruth)
    .disableCache()  // 禁用内存缓存
    .build()

// 3. 强制刷新跳过缓存
store.stream(StoreReadRequest.fresh(key))  // 跳过内存和 SOT
store.stream(StoreReadRequest.skipMemory(key, refresh = true))  // 仅跳过内存
```

---

## 5. 使用指南

### 4.1 添加依赖

```kotlin
// gradle/libs.versions.toml
[versions]
store5 = "5.1.0-alpha08"

[libraries]
mnf - store - store5 = { module = "org.mobilenativefoundation.store:store5", version.ref = "store5" }
mnf - store - cache5 = { module = "org.mobilenativefoundation.store:cache5", version.ref = "store5" }
mnf - store - core5 = { module = "org.mobilenativefoundation.store:core5", version.ref = "store5" }
```

```kotlin
// build.gradle.kts
dependencies {
    api(libs.mnf.store.store5)
    api(libs.mnf.store.cache5)
    implementation(libs.mnf.store.core5)
}
```

### 4.2 创建只读 Store

```kotlin
// 简单 Store（仅网络 + 内存缓存）
val store: Store<String, Card> = StoreBuilder
    .from(
        fetcher = Fetcher.of { id: String ->
            api.getCard(id)
        }
    )
    .build()

// 带 SourceOfTruth 的 Store
val store: Store<String, Card> = StoreBuilder
    .from(
        fetcher = Fetcher.of { id: String ->
            api.getCard(id)
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { id -> database.cardDao().observeCard(id) },
            writer = { id, card -> database.cardDao().insert(card) },
            delete = { id -> database.cardDao().delete(id) },
            deleteAll = { database.cardDao().deleteAll() }
        )
    )
    .cachePolicy(
        MemoryPolicy.builder<String, Card>()
            .setMaxSize(100)
            .setExpireAfterWrite(1.hours)
            .build()
    )
    .build()
```

### 4.3 创建可变 MutableStore

```kotlin
val mutableStore: MutableStore<String, Card> = MutableStoreBuilder
    .from(
        fetcher = Fetcher.of { id: String -> api.getCard(id) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { id -> database.cardDao().observeCard(id) },
            writer = { id, card -> database.cardDao().insert(card) },
            delete = { id -> database.cardDao().delete(id) },
            deleteAll = { database.cardDao().deleteAll() }
        ),
        converter = Converter.Builder<CardDto, CardEntity, Card>()
            .fromNetworkToLocal { it.toEntity() }
            .fromOutputToLocal { it.toEntity() }
            .build()
    )
    .build(
        updater = Updater.by(
            post = { key, value ->
                val response = api.updateCard(key, value)
                UpdaterResult.Success.Typed(response)
            }
        ),
        bookkeeper = Bookkeeper.by(
            getLastFailedSync = { key -> failedSyncStore.get(key) },
            setLastFailedSync = { key, timestamp ->
                failedSyncStore.set(key, timestamp)
                true
            },
            clear = { key -> failedSyncStore.remove(key); true },
            clearAll = { failedSyncStore.clear(); true }
        )
    )
```

### 4.4 读取数据

```kotlin
// 流式读取（推荐）
viewModelScope.launch {
    store.stream(StoreReadRequest.cached(key, refresh = true))
        .collect { response ->
            when (response) {
                is StoreReadResponse.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is StoreReadResponse.Data -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            data = response.value,
                            origin = response.origin.toString()
                        )
                    }
                }
                is StoreReadResponse.Error.Exception -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = response.error.message)
                    }
                }
                is StoreReadResponse.NoNewData -> {
                    // 缓存数据验证失败，无新数据
                }
                else -> {}
            }
        }
}

// 一次性读取
val card = store.fresh(key)  // 强制网络
val card = store.get(key)    // 优先缓存
```

### 4.5 写入数据（MutableStore）

```kotlin
// 写入并同步
viewModelScope.launch {
    val response = mutableStore.write(
        StoreWriteRequest.of(
            key = cardId,
            value = updatedCard
        )
    )
    when (response) {
        is StoreWriteResponse.Success -> {
            // 写入成功
        }
        is StoreWriteResponse.Error -> {
            // 写入失败（会记录到 Bookkeeper）
        }
    }
}
```

### 4.6 清除缓存

```kotlin
// 清除单个 key
store.clear(key)

// 清除所有
store.clearAll()
```

---

## 6. MyHub 集成实践

### 6.1 当前实现概览

MyHub 项目中使用 Store5 + StoreMultiCache 封装了 `CardStore`：

```
datastore/repository-client/src/commonMain/kotlin/.../store/
├── CardStore.kt           # Store 接口定义
├── CardStoreImpl.kt       # Store 实现（单 Store + StoreMultiCache）
├── CardStoreKey.kt        # Key 定义（实现 StoreKey 接口）
├── CardOutput.kt          # Output 定义（实现 StoreData 接口）
├── CardKeyProvider.kt     # KeyProvider 实现
└── InMemoryBookkeeper.kt  # Bookkeeper 内存实现
```

### 6.2 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                      CardStore                               │
│                                                              │
│  Key: CardStoreKey (sealed class)                           │
│    ├── ById(id) : StoreKey.Single<String>                   │
│    └── ByUser(userId) : StoreKey.Collection<String>         │
│                                                              │
│  Output: CardOutput (sealed class)                          │
│    ├── Single(card) : StoreData.Single<String>              │
│    └── Collection(items) : StoreData.Collection<String>     │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    MutableStore                              │
│                                                              │
│  创建路径：                                                    │
│  StoreBuilder.from(fetcher, sourceOfTruth, memoryCache)     │
│    .toMutableStoreBuilder(converter)                        │
│    .build(updater, bookkeeper)                              │
│                                                              │
│  memoryCache = StoreMultiCache  ◄── 实现列表自动分解          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   StoreMultiCache                            │
│                                                              │
│  ┌──────────────────┐    ┌───────────────────────────┐      │
│  │  singlesCache    │    │   collectionsCache        │      │
│  │                  │    │                           │      │
│  │ ById → Single    │◄───│ ByUser → Collection       │      │
│  │                  │    │    (自动分解)               │      │
│  └──────────────────┘    └───────────────────────────┘      │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 6.3 CardStore 接口设计

```kotlin
@OptIn(ExperimentalStoreApi::class)
interface CardStore {
    // 统一 API（使用 CardStoreKey）
    fun <T : Any> stream(key: CardStoreKey, refresh: Boolean = false): Flow<StoreReadResponse<T>>
    suspend fun <T : Any> get(key: CardStoreKey): T?
    suspend fun <T : Any> fresh(key: CardStoreKey): T?

    // 便捷方法（类型安全）
    fun streamCards(userId: String, refresh: Boolean = false): Flow<StoreReadResponse<List<Card>>>
    fun streamCard(cardId: String, refresh: Boolean = false): Flow<StoreReadResponse<Card>>
    suspend fun getCards(userId: String): List<Card>
    suspend fun getCard(cardId: String): Card?
    suspend fun freshCards(userId: String): List<Card>
    suspend fun freshCard(cardId: String): Card?

    // 写入操作
    suspend fun write(card: Card): StoreWriteResponse
    suspend fun delete(cardId: String): StoreWriteResponse

    // 缓存操作
    suspend fun clear(key: CardStoreKey)
    suspend fun clearAllCache()
}
```

### 6.4 CardStoreImpl 实现

```kotlin
@OptIn(ExperimentalStoreApi::class)
class CardStoreImpl(
    private val localCardDataSource: LocalCardDataSource,
    private val remoteCardDataSource: RemoteCardDataSource,
    private val bookkeeperStorage: InMemoryBookkeeperStorage = InMemoryBookkeeperStorage(),
) : CardStore {

    // StoreMultiCache - 支持列表自动分解
    private val multiCache: StoreMultiCache<String, CardStoreKey, 
                                            CardOutput.Single, CardOutput.Collection, 
                                            CardOutput> by lazy {
        StoreMultiCache(
            keyProvider = CardKeyProvider(),
            singlesCache = CacheBuilder<StoreKey.Single<String>, CardOutput.Single>()
                .maximumSize(500)
                .expireAfterWrite(1.hours)
                .build(),
            collectionsCache = CacheBuilder<StoreKey.Collection<String>, CardOutput.Collection>()
                .maximumSize(50)
                .expireAfterWrite(1.hours)
                .build()
        )
    }

    // 单个 MutableStore（正确的设计）
    private val store: MutableStore<CardStoreKey, CardOutput> by lazy {
        StoreBuilder.from(
            fetcher = createFetcher(),
            sourceOfTruth = createSourceOfTruth(),
            memoryCache = multiCache  // ✅ 传入 StoreMultiCache
        ).toMutableStoreBuilder(
            converter = createConverter()
        ).build(
            updater = createUpdater(),
            bookkeeper = createBookkeeper()
        )
    }

    private fun createFetcher(): Fetcher<CardStoreKey, CardOutput> = Fetcher.of { key ->
        when (key) {
            is CardStoreKey.ById -> {
                val card = remoteCardDataSource.getCardById(key.id)
                    ?: throw NoSuchElementException("Card not found: ${key.id}")
                CardOutput.Single(card)
            }
            is CardStoreKey.ByUser -> {
                val cards = remoteCardDataSource.getCards(key.userId)
                CardOutput.Collection.fromCards(cards, key.userId)
            }
        }
    }

    private fun createSourceOfTruth(): SourceOfTruth<CardStoreKey, CardOutput, CardOutput> =
        SourceOfTruth.of(
            reader = { key ->
                when (key) {
                    is CardStoreKey.ById -> localCardDataSource.observeCard(key.id)
                        .map { card -> CardOutput.Single(card) }
                    is CardStoreKey.ByUser -> localCardDataSource.observeCards(key.userId)
                        .map { cards -> CardOutput.Collection.fromCards(cards, key.userId) }
                }
            },
            writer = { key, output ->
                when {
                    key is CardStoreKey.ById && output is CardOutput.Single ->
                        localCardDataSource.insertCard(output.card)
                    key is CardStoreKey.ByUser && output is CardOutput.Collection ->
                        output.cards.forEach { localCardDataSource.insertCard(it) }
                }
            },
            delete = { key ->
                when (key) {
                    is CardStoreKey.ById -> localCardDataSource.deleteCard(key.id)
                    is CardStoreKey.ByUser -> localCardDataSource.deleteCards(key.userId)
                }
            },
            deleteAll = { /* 全部删除需要特殊处理 */ }
        )

    // ... 其他实现
}
```

### 6.5 ViewModel 中使用

```kotlin
class DashboardViewModel(
    private val cardStore: CardStore,
    coroutineScope: CoroutineScope
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            cardStore.streamCards(userId, refresh = true)
                .collect { response ->
                    when (response) {
                        is StoreReadResponse.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        is StoreReadResponse.Data -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    cards = response.value
                                )
                            }
                        }
                        is StoreReadResponse.Error.Exception -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = response.error.message
                                )
                            }
                        }
                        else -> {}
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            cardStore.freshCards(userId)
        }
    }
}
```

### 6.6 DI 配置

```kotlin
// RepositoryModule.kt
val repositoryModule = module {
    // Bookkeeper 存储
    single { InMemoryBookkeeperStorage() }

    // CardStore
    single<CardStore> {
        CardStoreImpl(
            localCardDataSource = get(),
            remoteCardDataSource = get(),
            bookkeeperStorage = get()
        )
    }
}

// DashboardModule.kt
val dashboardModule = module {
    factory {
        DashboardViewModel(
            cardStore = get(),
            coroutineScope = get()
        )
    }
}
```

### 6.7 缓存效果验证

使用 StoreMultiCache 后的缓存行为：

```
// 场景：先加载列表，再查看单个卡片

1. 用户打开 Dashboard，加载卡片列表
   └─ streamCards("user-1") 
   └─ Fetcher 返回 [card1, card2, card3]
   └─ StoreMultiCache 自动分解：
       ├─ singlesCache["card1"] = Single(card1)
       ├─ singlesCache["card2"] = Single(card2)
       └─ singlesCache["card3"] = Single(card3)

2. 用户点击 card2 进入详情页
   └─ streamCard("card2")
   └─ Store 调用 memCache.getIfPresent(ById("card2"))
   └─ ✅ 内存缓存命中！立即返回数据
   └─ 无需数据库查询，无需网络请求
   └─ 用户体验：瞬间加载
```

### 6.8 待优化项

| 问题              | 当前状态                    | 建议               |
|-----------------|-------------------------|------------------|
| Bookkeeper 持久化  | 内存实现，重启丢失               | 改为 SQLDelight 存储 |
| Updater         | 部分实现                    | 完善远程同步           |
| 与 Repository 重叠 | 两套数据层并存                 | 统一为 Store 模式     |
| 错误处理           | 基础实现                    | 添加重试和降级策略        |

---

## 7. 最佳实践

### 7.1 Key 设计

```kotlin
// 推荐：使用 sealed class 明确 Key 类型
sealed class CardStoreKey {
    data class ById(val id: String) : CardStoreKey()
    data class ByUser(val userId: String) : CardStoreKey()
    data class ByTag(val tagId: String) : CardStoreKey()
}

// 然后使用单一 Store
val store: Store<CardStoreKey, CardResult>
```

### 7.2 错误处理

```kotlin
store.stream(request)
    .filterNot { it is StoreReadResponse.Loading }
    .map { response ->
        when (response) {
            is StoreReadResponse.Data -> Result.success(response.value)
            is StoreReadResponse.Error.Exception -> Result.failure(response.error)
            is StoreReadResponse.Error.Message -> Result.failure(Exception(response.message))
            else -> null
        }
    }
    .filterNotNull()
```

### 7.3 缓存策略

```kotlin
// 高频更新数据：短 TTL
MemoryPolicy.builder<String, Card>()
    .setMaxSize(50)
    .setExpireAfterWrite(5.minutes)
    .build()

// 低频更新数据：长 TTL
MemoryPolicy.builder<String, UserProfile>()
    .setMaxSize(10)
    .setExpireAfterWrite(24.hours)
    .build()
```

### 7.4 离线优先

```kotlin
// 优先使用本地数据，后台刷新
fun loadData(key: String) {
    viewModelScope.launch {
        // 1. 立即显示缓存
        store.stream(StoreReadRequest.localOnly(key))
            .filterIsInstance<StoreReadResponse.Data<T>>()
            .first()
            .let { showData(it.value) }

        // 2. 后台刷新
        store.fresh(key)
    }
}
```

### 7.5 测试

```kotlin
// 使用 fake 实现进行测试
class FakeCardStore : CardStore {
    private val cards = mutableMapOf<String, Card>()

    override fun streamCard(cardId: String) = flow {
        emit(StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()))
        emit(
            StoreReadResponse.Data(
                cards[cardId] ?: throw NotFoundException(),
                StoreReadResponseOrigin.Cache
            )
        )
    }

    // ...
}
```

---

## 8. 常见问题

### Q1: Store 和 Repository 有什么区别？

| 特性   | Store                 | Repository |
|------|-----------------------|------------|
| 核心关注 | 数据获取与缓存               | 业务逻辑封装     |
| 状态管理 | 内置 Loading/Error/Data | 需自行处理      |
| 响应式  | 原生 Flow 支持            | 需自行实现      |
| 缓存   | 内置多级缓存                | 需自行实现      |
| 复杂度  | 较低                    | 可能较高       |

**建议**：简单场景直接使用 Store，复杂业务逻辑在 Store 上层封装 Repository。

### Q2: 如何处理分页？

```kotlin
// 方案 1：Key 包含分页信息
data class PagedKey(val userId: String, val page: Int, val pageSize: Int)

// 方案 2：使用 Store5 的 Pager（实验性）
val pager = StorePager.from(store)
```

### Q3: 如何处理数据关联？

```kotlin
// 方案：在 SourceOfTruth 的 writer 中处理关联
SourceOfTruth.of(
    reader = { cardId -> database.cardWithTags(cardId) },
    writer = { _, cardWithTags ->
        database.transaction {
            database.cardDao().insert(cardWithTags.card)
            cardWithTags.tags.forEach { database.tagDao().insert(it) }
            cardWithTags.tags.forEach { database.cardTagDao().insert(cardId, it.id) }
        }
    }
)
```

### Q4: 多个 Store 的内存缓存能共享吗？

**默认不能**。每个 Store 实例都有独立的 `Cache` 实例。

```kotlin
// 这两个 Store 的内存缓存完全独立
val cardsStore: Store<String, List<Card>>  // Cache A
val cardStore: Store<String, Card>         // Cache B
```

**数据共享方案**：

| 方案 | 说明 | 推荐场景 |
|-----|------|---------|
| **通过 SourceOfTruth** | 多个 Store 使用同一个数据库，数据自动共享 | 简单场景 |
| **单一 Store + StoreMultiCache** | 一个 Store + 列表自动分解到单项缓存 | **推荐** |

详见第 4 章"内存缓存机制深入分析"。

### Q5: 如何正确使用 StoreMultiCache？

**核心要点**：

1. **单 Store 设计**：Key 和 Output 都是 sealed class
2. **传入 StoreBuilder**：作为 `memoryCache` 参数传入
3. **不要手动调用**：Store 内部自动调用 `memCache.put()` 和 `getIfPresent()`

```kotlin
// ✅ 正确：传入 StoreBuilder
val store = StoreBuilder.from(
    fetcher = ...,
    sourceOfTruth = ...,
    memoryCache = storeMultiCache  // 正确用法
).toMutableStoreBuilder(converter).build(updater, bookkeeper)

// ❌ 错误：在外部手动调用
storeMultiCache.put(key, value)  // 不要这样做！
```

### Q6: StoreMultiCache 和普通 Cache 有什么区别？

| 特性 | 普通 Cache | StoreMultiCache |
|-----|-----------|-----------------|
| 列表分解 | ❌ 不支持 | ✅ 自动将列表分解到单项缓存 |
| 单项更新同步 | ❌ 不支持 | ✅ 更新单项时自动更新对应列表 |
| Key 要求 | 任意类型 | 必须实现 `StoreKey` 接口 |
| Output 要求 | 任意类型 | 必须实现 `StoreData` 接口 |
| API 稳定性 | 稳定 | `@ExperimentalStoreApi` |

### Q7: alpha 版本是否稳定？

Store5 5.1.0-alpha08 的核心 API 已相对稳定，但仍可能有小改动。建议：

- 封装 Store 接口，降低直接依赖
- 关注 GitHub releases
- 生产环境谨慎升级

---

## 参考资料

- [Store5 GitHub](https://github.com/MobileNativeFoundation/Store)
- [Store5 官方文档](https://mobilenativefoundation.github.io/Store/)
- [KMP 官方文档](https://kotlinlang.org/docs/multiplatform.html)

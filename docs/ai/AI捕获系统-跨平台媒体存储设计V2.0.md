# AI捕获系统-跨平台媒体存储设计 V2.0

## 1. 文档定位

- 版本：V2.0
- 状态：唯一有效方案
- 目标：为 AI 捕获系统提供可发布的跨平台媒体存储架构，覆盖 native 与 web 强支持。
- 范围：Android / iOS / JVM / WasmJs
- 备注：当前正式 web target 为 `wasmJs`；`js` 暂不支持，但 `webMain` 设计需保留后续扩展能力
- 原则：彻底替换、不保留兼容、平台隔离、Local-first、产品级可发布

本方案替换以下旧设计：

- `managed:// + storedRef + resolvedUrl`
- 将 `ManagedFileStorage` 当路径转换器使用的旧模式
- 将 native 文件系统路径抽象错误复用到 web/wasm 的做法
- 文件存储实现不再放在 `repository-client-api`，而是独立到 `datastore/file-storage`

---

## 2. 设计目标 

1. Native 与 web 都是正式平台，必须支持媒体导入、草稿恢复、发布后恢复、重启后恢复。
2. Native 与 web 的底层存储实现彻底隔离，彼此不可见。
3. 公共业务层不感知 native/web 私有存储引用类型。
4. 公共 UI/播放链路统一消费运行时访问地址，不消费平台私有存储结构。
5. 所有持久化引用必须稳定、可序列化、可校验、可删除。
6. 不保留旧路径兼容、旧 schema 兼容、旧草稿兼容。

---

## 3. 核心原则

### 3.1 平台私有引用不可跨层泄漏

不允许在 `commonMain` 暴露如下跨平台混合模型：

```kotlin
sealed interface MediaRef {
    data class Managed(...)
    data class External(...)
    data class WebStored(...)
}
```

原因：

- native 不应知道 `WebStored`
- web 不应知道 `Managed`
- 公共层不应基于“所有平台存储类型全集”编程

### 3.2 公共层只持有不透明持久化句柄

公共层只认识：

- 持久化句柄：`storageHandle`
- 运行时访问地址：`accessUrl`

公共层不解析句柄内部结构，不拼路径，不判断 native/web 私有 scheme。

### 3.3 平台内部各自维护私有引用模型

native 与 web 各自拥有私有的序列化引用结构，并各自负责：

- 持久化编码
- 运行时解析
- 删除
- 存在性校验
- 缩略图衍生资源管理

### 3.4 `External` 只作为平台内部共享概念

`External(url)` 允许在 native/web 内部分别存在，但不作为公共层联合类型暴露。

---

## 4. 分层架构

```text
Common Business Layer
  -> MediaFileStore
  -> datastore/file-storage (platform storage facade)
    -> Native file storage
    -> Web IndexedDB Blob Store
  -> Local DB
  -> Draft JSON
```

职责划分：

- `commonMain`
  - 定义媒体句柄字段
  - 定义业务流程与调用契约
  - 不持有平台私有引用类型
- `datastore/file-storage`
  - 承载媒体文件存储实现
  - 仅对上层暴露稳定的存储句柄与运行时访问地址
  - 不属于 repository API 层
- `nativeMain`
  - 管理应用私有文件目录
  - 解析 native 私有媒体句柄
- `webMain`
  - 作为真正的中间源，承载 web 共享实现
  - 管理浏览器持久媒体存储
  - 解析 web 私有媒体句柄
- `wasmJsMain`
  - 提供当前正式 web target 的浏览器 API bridge
- `jsMain`
  - 当前不作为正式支持目标
  - 仅允许保留最薄的未来扩展 bridge，不承载业务逻辑

---

## 5. 公共模型

### 5.1 持久化模型

公共层统一持久化字段：

```kotlin
@Serializable
data class PersistedMediaPointer(
    val storageHandle: String,
)
```

规则：

- `storageHandle` 是不透明字符串
- 公共层只允许透传、比较、存储、删除时回传给平台层
- 公共层禁止按前缀/scheme 解析 `storageHandle`

### 5.2 运行时模型

```kotlin
data class ResolvedMedia(
    val accessUrl: String,
    val localFile: PlatformFile? = null,
)
```

规则：

- UI、播放器、缩略图、图片组件统一使用 `accessUrl`
- `localFile` 仅为 non-web 可选补充能力
- web/wasm 下 `localFile` 固定为 `null`

### 5.3 业务实体

替换现有 `storedRef + resolvedUrl`：

```kotlin
@Serializable
data class CaptureMediaAsset(
    val storageHandle: String,
    val mediaType: String,
    val sizeBytes: Long,
    val sha256: String,
    val isMissing: Boolean = false,
    @Transient val accessUrl: String = "",
)
```

数据库快照同理：

```kotlin
data class MediaAssetSnapshot(
    val id: String,
    val cardId: String,
    val mediaType: String,
    val storageHandle: String,
    val accessUrl: String,
    val thumbStorageHandle: String? = null,
    val thumbAccessUrl: String? = null,
    ...
)
```

---

## 6. 平台私有引用模型

### 6.1 Native 私有引用

定义位置：`nativeMain`

```kotlin
@Serializable
internal sealed interface NativeMediaRef {
    @Serializable
    data class ManagedAsset(
        val key: String,
    ) : NativeMediaRef

    @Serializable
    data class ExternalUrl(
        val url: String,
    ) : NativeMediaRef
}
```

说明：

- `ManagedAsset` 表示 native 应用私有文件
- `ExternalUrl` 表示远程或系统外部引用
- native 代码看不到 `WebMediaRef`

### 6.2 Web 私有引用

定义位置：`webMain`

```kotlin
@Serializable
internal sealed interface WebMediaRef {
    @Serializable
    data class BrowserStoredAsset(
        val key: String,
    ) : WebMediaRef

    @Serializable
    data class ExternalUrl(
        val url: String,
    ) : WebMediaRef
}
```

说明：

- `BrowserStoredAsset` 表示浏览器持久化对象
- `ExternalUrl` 表示远程或外部可直接访问地址
- web 代码看不到 `NativeMediaRef`

### 6.3 公共层不可见性要求

禁止：

- 在 `commonMain` 声明 `ManagedAsset` / `BrowserStoredAsset`
- 在公共数据库 schema 中使用 `managed://...` 或 `web://...` 作为公共协议规范
- 在公共逻辑里通过字符串前缀判断所属平台

允许：

- 将平台私有引用整体序列化为 `storageHandle`
- 平台层自行反序列化

---

## 7. 持久化编码策略

### 7.1 `storageHandle` 编码

`storageHandle` 是平台私有引用的序列化结果。

当前实现：

- native：`nh1:` + `base64(json(NativeStorageHandle.ManagedAsset(...)))`
- web：`wh2:` + `base64(json(WebStorageHandle.BrowserStoredAsset(...)))`

约束：

- 编码细节不对公共层公开
- 不要求不同平台可互相解析
- 同一平台版本内必须可稳定反序列化

### 7.2 不再使用的编码

以下方案全部废弃：

- `managed://...`
- 持久化绝对路径
- 持久化 `blob:` 作为长期草稿主键
- 通过 `PlatformFile(path)` 恢复 web 文件

---

## 8. Native 存储设计

### 8.1 目录

Native 私有文件目录：

```text
<app-private-root>
  /ai-capture
    /drafts/{draftId}/media/{mediaId}.{ext}
    /generated/{draftId}/{mediaId}.{ext}
  /cards/{cardId}/media/{mediaId}.{ext}
```

平台根目录：

- Android：`filesDir/app-data`
- iOS：`Library/Application Support/app-data`
- JVM：平台私有 data dir

### 8.2 句柄解析

- `ManagedAsset(key)` -> `accessUrl=file://<absolute-path>`
- `ExternalUrl(url)` -> `accessUrl=url`

### 8.3 生命周期

- 草稿导入：复制到 draft 私有目录
- 生成图片：写入 generated 私有目录
- 发布：复制/移动到 cards 私有目录
- 删除：删除实体文件与缩略图

### 8.4 强支持要求

- App 重启后媒体仍可恢复
- 草稿态媒体必须持久化到私有目录
- 不允许把临时系统 URI 直接写入草稿 JSON

---

## 9. Web 存储设计

### 9.1 正式存储后端

Web 强支持必须落地浏览器持久存储。

本期方案：

- 元数据：现有本地 DB
- 二进制：IndexedDB Blob Store
- 运行时访问：从 IndexedDB 读取 Blob，生成 `blob:` URL

可选未来增强：

- OPFS 作为更大文件/更强持久能力后端

### 9.2 Web Blob Store 结构

建议对象仓：

- `capture_media`
  - key: `mediaKey`
  - fields:
    - `blob`
    - `mimeType`
    - `sizeBytes`
    - `createdAt`

### 9.3 句柄解析

- `BrowserStoredAsset(key)` -> 从 IndexedDB 读取 -> 生成 `blob:` URL
- `ExternalUrl(url)` -> `accessUrl=url`

### 9.4 `blob:` URL 生命周期

规则：

- `blob:` URL 只作为运行时访问地址
- 不直接持久化到数据库/草稿 JSON
- 页面/组件销毁时回收旧 URL
- 重新加载页面后通过 `BrowserStoredAsset(key)` 重新生成

### 9.5 强支持要求

Web 平台必须满足：

1. 上传媒体后刷新页面仍可恢复
2. 草稿保存后重新打开仍可恢复
3. 发布后的本地卡片媒体仍可展示
4. 缩略图、原图、视频预览地址都能重建

---

## 10. 存储接口设计

### 10.1 公共接口

```kotlin
interface MediaFileStore {
    suspend fun persistDraftMedia(
        draftId: String,
        mediaId: String,
        source: MediaImportSource,
    ): ImportedMedia

    suspend fun importToManagedStorage(
        cardId: String,
        mediaId: String,
        source: MediaImportSource,
    ): ImportedMedia

    suspend fun saveGeneratedImage(
        draftId: String,
        mediaId: String,
        bytes: ByteArray,
        mimeType: String,
    ): ImportedMedia
}
```

```kotlin
data class MediaImportSource(
    val storageHandle: String = "",
    val accessUrl: String = "",
    val platformFile: PlatformFile? = null,
    val sizeBytes: Long? = null,
)
```

### 10.2 返回模型

```kotlin
data class ImportedMedia(
    val storageHandle: String,
    val accessUrl: String,
    val sizeBytes: Long,
)
```

说明：

- 公共业务层只接受 `storageHandle` 与 `accessUrl`
- 具体 handle 结构由平台层私有决定

---

## 11. 数据库与 JSON 设计

### 11.1 Schema

`media_asset` 替换为：

- `storage_handle` TEXT NOT NULL
- `thumb_storage_handle` TEXT NULL

彻底移除：

- `stored_ref`
- `thumb_stored_ref`

### 11.2 Draft JSON

草稿媒体字段统一为：

- `storageHandle`
- `accessUrl` 使用 `@Transient`

### 11.3 Card 结构约束

本地受控媒体不再直接把平台私有句柄写入 `card.ui.cover.imageUrl` / `card.content.ref` / `card.source.ref`。

替代规则：

1. 本地媒体关系通过 `media_asset` 表维护
2. `card.ui` / `card.content` / `card.source` 内不再持久化平台私有本地引用
3. 外部远程资源才允许直接存 URL

---

## 12. 读取与展示策略

### 12.1 加载草稿

1. 读取草稿 JSON
2. 对每个 `storageHandle` 调用平台存储 `resolve()`
3. 成功则填充 `accessUrl`
4. 失败则标记 `isMissing = true`

### 12.2 加载卡片

1. 读 `card`
2. 读 `media_asset`
3. 逐个解析 `storageHandle`
4. 由上层组装封面/内容展示

### 12.3 UI 约束

- UI 统一只消费 `accessUrl`
- `PlatformFile` 只作为组件内部可选优化
- web/wasm 不允许从路径恢复本地文件

---

## 13. 删除与垃圾回收

平台层必须支持：

1. 删除原始媒体
2. 删除缩略图
3. 删除派生 blob/url 记录
4. 删除悬挂对象

Native：

- 删除私有目录文件

Web：

- 删除 IndexedDB 中的 blob 记录
- 回收已生成的 `blob:` URL

---

## 14. 实施顺序

### 阶段 1：文档与基线

1. 以本文件为唯一设计基线
2. 冻结旧 `ManagedFileStorage` 方案

### 阶段 2：模型替换

1. `storedRef -> storageHandle`
2. `resolvedUrl -> accessUrl`
3. `thumbStoredRef -> thumbStorageHandle`
4. `thumbResolvedUrl -> thumbAccessUrl`

### 阶段 3：存储接口替换

1. 移除 `ManagedFileStorage`
2. 引入 `AppMediaStorage`
3. native/web 各自实现私有引用编解码

### 阶段 4：Web 强支持落地

1. 引入 IndexedDB Blob Store
2. 实现 `BrowserStoredAsset`
3. 实现刷新后重建 `blob:` URL

### 阶段 5：Card 结构替换

1. 本地媒体从 URL 引用迁移为 `mediaAssetId`
2. 封面与内容展示改为走 `media_asset`

### 阶段 6：清理

1. 删除旧字段、旧 helper、旧文档
2. 删除 `managed://` 相关逻辑

---

## 15. 迁移策略

本次迁移原则：

- 不保留兼容
- 不做旧数据迁移
- 不做旧路径解析

执行要求：

1. 直接替换 schema、模型、接口
2. 必要时通过清库/重装/清草稿进入新版本
3. 所有旧文档标记为已废弃

---

## 16. 验收标准

### 16.1 Native

- 导入图片/视频后，重启应用仍可恢复
- 草稿、发布、删除、缩略图都正常
- iOS/Android/JVM 均不持久化绝对路径

### 16.2 Web

- 上传图片/视频后，刷新页面仍可恢复
- 草稿、发布、删除、缩略图都正常
- 不依赖路径型 `PlatformFile`
- `blob:` URL 仅为运行时地址，持久化后可重建

### 16.3 架构

- commonMain 看不到 native/web 私有引用类型
- native 看不到 web 私有引用类型
- web 看不到 native 私有引用类型

---

## 17. 风险与防范

### 风险 1：再次把平台私有类型放回 commonMain

防范：

- 代码评审禁止公共 `sealed MediaRef` 全平台全集模型

### 风险 2：web 继续把 `blob:` 当长期持久化主键

防范：

- 文档与实现明确禁止
- `blob:` 只允许出现在 `ResolvedMedia.accessUrl`

### 风险 3：card 结构继续嵌入本地 URL

防范：

- 本地媒体统一走 `mediaAssetId`
- 仅外部资源允许直接 URL 引用

### 风险 4：native/web 实现彼此感知

防范：

- 私有引用模型只允许定义在平台 source set
- 公共层只持有 opaque `storageHandle`

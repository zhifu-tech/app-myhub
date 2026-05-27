# 图片存储（Server）与加载方案调研报告

> 面向「本地优先、多端同步」的 MyHub App，梳理图片相关需处理的问题、业内常见方案、资源存放策略及 KMP 生态下的开源选型。**本文为调研报告，不涉及落地实现。**

**文档版本**：v1.0  
**编写日期**：2026-01-29  
**适用范围**：Server 图片存储 API、客户端（KMP）图片加载与缓存。

---

## 一、需要处理的问题清单

### 1.1 存储与上传

| 问题        | 说明               | 业内常见做法                                                         |
|-----------|------------------|----------------------------------------------------------------|
| **存储介质**  | 图片二进制存哪里、如何扩容    | 对象存储（S3/MinIO/OSS）+ CDN 加速；自建用 MinIO，云上用 S3/OSS                |
| **上传入口**  | 谁负责接收上传、是否经业务服务器 | **经服务器**：业务校验、鉴权、转存；**直传**：服务端签发预签名 URL，客户端直传对象存储，减轻服务器带宽与 CPU |
| **安全与鉴权** | 谁可以上传/下载、防止盗链与越权 | 预签名 URL（时间+对象范围限定）、服务端鉴权后下发 URL、CDN 鉴权/Token                   |
| **格式与大小** | 格式限制、单文件/总容量限制   | 服务端/网关限制 Content-Type、Content-Length；大文件用分片上传（Multipart）       |
| **命名与版本** | 避免冲突、覆盖与历史版本     | 对象 Key 使用 UUID 或「租户/类型/日期/UUID」；需要版本时可加 version 或 overwrite 策略 |

### 1.2 访问与分发

| 问题          | 说明                      | 业内常见做法                                                      |
|-------------|-------------------------|-------------------------------------------------------------|
| **访问速度与成本** | 就近访问、减少回源               | CDN 边缘缓存；图片 CDN 支持 URL 参数化裁剪/压缩（如 imgix、Cloudinary、阿里云图片处理） |
| **多尺寸/多格式** | 列表缩略图 vs 详情大图、WebP/AVIF | 上传原图，通过 CDN/图片服务按参数生成多种尺寸与格式（on-the-fly）；或上传时生成多规格          |
| **URL 稳定性** | 迁移存储或 CDN 时链接不失效        | 用自有域名做 CDN 域名，背后切换源站；或网关/Redirect 做一层 URL 重写                |

### 1.3 客户端加载（KMP/Compose）

| 问题            | 说明                                    | 业内常见做法                                                           |
|---------------|---------------------------------------|------------------------------------------------------------------|
| **异步加载**      | 不阻塞 UI、占位与错误态                         | 专用图片库（Coil/Kamel）异步加载 + 占位图/错误图                                  |
| **内存缓存**      | 避免重复解码、OOM                            | 图片库内置 LRU 内存缓存，可配置大小                                             |
| **磁盘缓存**      | 离线/弱网可看、减少流量                          | 图片库内置磁盘缓存；本地优先场景可配合「先本地后网络」策略                                    |
| **与 Ktor 复用** | 与现有 API 请求共用 HttpClient、Cookie/Header | 使用支持 Ktor Engine 的图片库（如 Coil 的 coil-network-ktor），或共享 HttpClient |
| **多平台一致**     | Android / iOS / JVM / JS(WASM) 行为一致   | 选用 KMP 图片库，在 commonMain 配置，各平台仅挂载 Engine                         |

### 1.4 本地优先（Local-First）特有

| 问题         | 说明                   | 业内常见做法                                                               |
|------------|----------------------|----------------------------------------------------------------------|
| **资源落盘**   | 图片是「仅 URL」还是「本地也存一份」 | **仅 URL**：本地只存 URL，加载时网络+磁盘缓存；**落盘**：重要图片随 Sync 下载到本地路径，UI 优先读本地文件   |
| **同步策略**   | 何时从服务器拉取/上传图片        | 与卡片/用户等业务数据一起同步：元数据带 URL，图片可懒加载或按需批量下载；上传在创建/编辑时触发，成功后写回 URL         |
| **冲突与一致性** | 同一资源多端修改、离线编辑        | 以「资源 ID + 版本/ETag」为一致性依据；图片一般以覆盖为主，冲突时采用 last-write-wins 或由业务决定保留哪一侧 |
| **离线可用**   | 无网时已看过的图片是否可见        | 依赖磁盘缓存或已同步的本地文件；缓存策略可用 Cache-First（先本地后网络）或 Stale-While-Revalidate   |

---

## 二、资源保存在哪里？本地优先如何做

### 2.1 资源放在哪里

- **服务端（权威源）**
    - 图片二进制放在 **对象存储**（如 S3、MinIO、阿里云 OSS），不推荐放在业务服务器本地盘（难扩容、难备份）。
    - 业务库（如你们现有 SQLite/PostgreSQL）只存 **URL 或资源 ID**（如 `card_metadata_article.cover_image_url`、`user.avatar_url`）。
    - 访问时：要么直接返回可访问的 URL（含 CDN 或预签名），要么由网关/鉴权层校验后再 302 到对象存储/CDN。

- **客户端（本地）**
    - **不落盘**：只存 URL，展示时通过「网络 + 内存/磁盘缓存」加载；实现简单，适合「以在线为主」的场景。
    - **落盘**：在 Sync 或「打开详情」时把图片下载到 App 私有目录（或你们已有的 datastore 相关路径），UI 优先用本地路径加载；适合本地优先、离线要看到图的需求。
    - 本地优先更彻底的方案会把「图片是否已下载」作为同步状态的一部分（例如 per-resource 的 `local_path` 或 `synced_at`），便于离线列表只显示已同步缩略图。

### 2.2 本地优先下的推荐策略（与当前架构对齐）

结合你们已有的 **Sync + LocalDataSource + 卡片/用户元数据**：

1. **元数据里只存 URL**  
   保持现状：`cover_image_url`、`avatar_url`、`url`/`thumbnail_url` 等均为「最终可访问的 URL」。服务端负责生成/更新这些 URL（上传成功后写入或通过 CDN 规则生成）。

2. **加载策略：本地优先**
    - **有本地文件时**：若实现了「图片落盘」，则用 `file://` 或本地 Path 加载。
    - **无本地文件时**：用 URL 走图片库（Coil/Kamel）加载，并开启磁盘缓存，这样第二次即可从缓存读，相当于「用过即本地」。

3. **同步与上传**
    - **下载**：可与现有 Sync 解耦——Sync 只同步结构化数据（含 URL）；图片可在首次展示时按 URL 懒加载并写入磁盘缓存，或由后台任务按需/批量下载到本地目录并更新「本地路径」映射。
    - **上传**：创建/编辑卡片或头像时，先向你们 Server 申请上传（或预签名 URL），客户端上传到对象存储后，再调现有 API 把最终 URL 写回卡片/用户元数据，再由 Sync 同步到其他端。

这样既保留「资源以 URL 为唯一权威」的简单模型，又能在客户端通过缓存或落盘做到「本地优先」体验。

---

## 三、业内常见解决方案小结

- **服务端**
    - 对象存储：AWS S3、阿里云 OSS、MinIO（自建）、Cloudflare R2 等。
    - 上传：应用服务器接收 multipart 后转存对象存储；或预签名 URL + 客户端直传 S3/MinIO。
    - 分发：CDN + 可选「图片处理」（裁剪、压缩、格式转换），或用 imgix/Cloudinary 等 Image CDN。

- **客户端**
    - 通用：异步加载 + 内存 LRU + 磁盘缓存；与现有 HTTP 客户端共享（Cookie/鉴权）。
    - 本地优先：Cache-First 或「本地路径优先 + URL 回退」；可选「同步时下载到本地文件」以强化离线。

---

## 四、KMP 生态下的开源图片加载方案

### 4.1 Coil 3（coil-kt/coil）

- **定位**：主流 Android 图片库的 KMP 版本，支持 Compose。
- **平台**：Android、JVM、iOS、macOS、JavaScript、WASM。
- **能力**：异步加载、内存缓存、**磁盘缓存**（Coil 3 的 `DiskCache` 在 KMP 可用），可配置缓存目录与大小（如 `maxSizePercent`）。
- **网络**：通过 **coil-network-ktor3** 用 Ktor 作为 HTTP 引擎，便于与现有 Ktor Client 共享（鉴权、Cookie、域名等）。
- **使用方式**：`AsyncImage`/`Image` + `ImageLoader`；推荐单例 `ImageLoader`，统一配置 `diskCache`、`memoryCache` 和 Ktor Engine。
- **参考**：
    - [Coil 3 文档](https://coil-kt.github.io/coil/)
    - [Coil + Ktor in KMP Compose](https://dev.to/gochev/coil-and-ktor-in-ktor-in-kotlin-multiplatform-compose-project-5d3i)
    - [Sharing HttpClient between Ktor and Coil](https://stackoverflow.com/questions/68592479/how-to-share-httpclient-between-multiplatform-ktor-and-coil)

### 4.2 Kamel Image（Kamel-Media/Kamel）

- **定位**：面向 KMP + Compose 的轻量异步媒体（图片）加载与缓存。
- **平台**：Android、iOS、JVM、Web（WASM）。
- **能力**：异步加载、占位/错误态、内存与磁盘缓存（可配置大小），默认使用 Ktor 拉取。
- **使用方式**：`KamelImage()` 或 `asyncPainterResource()`，支持 URL、Ktor URL、URI、本地文件等。
- **依赖**：`media.kamel:kamel-image` + `kamel-image-default`（或按需选模块），发布在 Maven Central。
- **参考**：
    - [Kamel: Image Loading in KMP+CMP](https://medium.com/@csabhionline/kamel-the-answer-to-image-loading-in-kmp-cmp-projects-68975751e7c0)
    - [GitHub - Kamel-Media/Kamel](https://github.com/Kamel-Media/Kamel)

### 4.3 简要对比（供选型）

| 维度        | Coil 3                               | Kamel                                   |
|-----------|--------------------------------------|-----------------------------------------|
| 成熟度/生态    | 更成熟，Android 端广泛使用                    | 相对较新，专注 KMP                             |
| KMP 支持    | 全平台（含 WASM）                          | Android / iOS / JVM / Web WASM          |
| 与 Ktor 集成 | 官方 coil-network-ktor3，可共享 HttpClient | 默认用 Ktor，易与现有 Client 一致                 |
| 磁盘缓存      | 内置，可配置目录与大小                          | 内置，可配置                                  |
| API 风格    | `AsyncImage` + `ImageLoader`         | `KamelImage` / `asyncPainterResource()` |
| 依赖体积      | 相对略大                                 | 偏轻量                                     |

**建议**：若项目已大量使用 Ktor Client，且希望与现有网络层（含鉴权）统一，**Coil 3 + coil-network-ktor3** 是稳妥选择；若更看重轻量与 KMP 原生体验，可评估 **Kamel**。二者都支持「URL + 磁盘缓存」的本地优先用法。

---

## 五、与本项目现状的对应关系

- **已有数据模型**
    - `CardMetadataArticle.coverImageUrl`、`user.avatar_url`、Video 的 `url`/`thumbnail_url` 已具备「存 URL」的形态；无需改表结构即可接入「服务端存对象存储 + 返回 URL」的方案。

- **Server**
    - 当前无「图片上传/存储」API；后续可新增：
        - 上传：`POST /api/v1/media/upload`（或按业务拆成 `/cards/.../cover`、`/users/me/avatar`）返回 URL；或 `POST /api/v1/media/presigned-url` 返回预签名 URL，客户端直传后把 Key 或最终 URL 写回业务接口。
    - 存储实现：对接 MinIO/S3 等，保存到 bucket，写入 DB 的仍是 URL。

- **客户端**
    - 目前未见统一图片加载组件；引入 Coil 3 或 Kamel 后，在 commonMain 配置 `ImageLoader`（或 Kamel 等价物），对所有 `coverImageUrl`、`avatar_url` 等统一使用该 loader，并开启磁盘缓存即可实现「先内存/磁盘，再网络」的本地优先体验。若要做「同步时落盘」，再在 Sync 或单独模块中增加「按 URL 下载到本地路径」的逻辑，并在 UI 层优先使用本地路径。

---

## 六、后续可落地的方向（不在此报告实现）

1. **服务端**
    - 选定对象存储（如 MinIO/S3），实现上传 API 或预签名 URL API。
    - 可选：CDN 或图片处理（裁剪/压缩）URL 规则，在返回给客户端的 URL 中体现。

2. **客户端**
    - 引入 Coil 3（或 Kamel），在 commonMain 配置单例 ImageLoader，与 Ktor Client 共享。
    - 所有远程图片统一经该 loader，开启磁盘缓存；必要时增加「本地路径优先」的 DataSource 或 Uri 解析。

3. **本地优先增强**
    - 定义「图片同步」策略：仅缓存 vs 明确落盘；若落盘，在 Sync 或单独任务中下载并记录本地路径，UI 优先读本地再回退 URL。

以上内容仅作调研与方案梳理，具体技术选型与实现可在确定方向后另行设计文档与排期。

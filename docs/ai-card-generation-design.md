# AI 辅助生成卡片 — 设计文档

> 文档版本：v1.0  
> 文档类型：功能设计 / Feature Design  
> 关联文档：[myhub-app-design.md](Stitch/myhub-app-design.md) §8.6 New Capture；[ai-card-generation-product-design.md](ai-card-generation-product-design.md)（产品摘要）；[ai-card-generation-backend-design.md](ai-card-generation-backend-design.md)（后端设计）；[ai-card-generation-chatgpt-prompt.md](ai-card-generation-chatgpt-prompt.md)（ChatGPT 提示词）  
> 适用对象：产品、后端、客户端、AI 集成开发

---

## 1. 目标与范围

### 1.1 目标

在 **New Capture** 流程中，用户提供「原始素材」后，由 **AI 自动分析并生成一张符合 MyHub 数据模型的卡片草稿**，用户可在此基础上编辑、补充后保存。

### 1.2 输入类型（三种）

| 输入类型    | 说明           | 典型场景            |
|---------|--------------|-----------------|
| **URL** | 一个网页链接       | 文章、博客、文档、视频页    |
| **图片**  | 一张本地或网络图片    | 截图、照片、示意图、信息图   |
| **文本**  | 用户粘贴/输入的一段文字 | 文学摘录、名言、代码片段、笔记 |

### 1.3 输出

- **一张卡片草稿**：包含 `type`、`title`、`content`、以及对应类型的 `metadata`。
- **可选增强**：建议标签（tags）、建议封面样式（纯色或「AI 分配」的色值/图片），与 New Capture §2.4、§2.5 对齐。

---

## 2. 与现有设计的关系

### 2.1 与 New Capture 模块（§8.6）的衔接

- **Source Content（§2.1）**：用户输入 = 上述三种输入之一（或组合，见下文「组合输入」）。
- **卡片类型（§2.2）**：AI 输出类型需落在现有类型集合内（见 2.2 节）。
- **标题（§2.3）**：AI 生成「显示在卡片中的标题」，用户可改。
- **封面（§2.4）**：AI 可输出「建议封面」：纯色（M3 七色之一）或建议使用图片，与「AI 智能分配」一致。
- **标签（§2.5）**：AI 可输出建议标签（M3 Chips），用户可增删。

### 2.2 与卡片数据模型对齐

当前卡片类型与元数据（见 `datastore/model`）：

| 类型常量      | 说明    | 主要 metadata 字段                                    |
|-----------|-------|---------------------------------------------------|
| `article` | 链接/文章 | url, summary, coverImageUrl, author               |
| `code`    | 代码片段  | language, snippet, description                    |
| `quote`   | 引言/文学 | author, category, source                          |
| `idea`    | 笔记/想法 | priority, status                                  |
| `video`   | 视频    | videoUrl, thumbnailUrl, durationSeconds, platform |
| `word`    | 字典    | （按需扩展）                                            |
| `todo`    | 待办    | （按需扩展）                                            |

AI 生成的卡片必须符合上述 `Card` + 对应 `CardMetadata*` 结构，便于直接写入 `CardRepository` 与本地/服务端存储。

---

## 3. 输入 → 类型与能力映射

### 3.1 按输入类型的能力

| 输入         | 主要产出类型          | AI 能力简述                                                                    |
|------------|-----------------|----------------------------------------------------------------------------|
| **URL**    | article / video | 抓取正文/OG 信息，生成 title、summary、author、coverImageUrl；若为视频链接则识别平台并产出 video 元数据。 |
| **图片**     | idea / article  | 识别图中文字（OCR）、场景或图表含义，生成 title、content；若有链接/二维码可建议 article。                  |
| **文本（文学）** | quote / idea    | 区分名言/摘录 vs 自由笔记，生成 title、content、author/category/source（quote）或 idea。      |
| **文本（代码）** | code            | 检测语言、提取片段、生成简短 description，产出 code metadata。                               |

### 3.2 类型推断规则（建议）

- **URL**
    - 若可解析为视频站点（如 youtube、bilibili、小红书视频）→ `video`。
    - 否则 → `article`（默认）。
- **图片**
    - 默认 → `idea`；若识别到「文章/链接」特征 → 可建议 `article`。
- **文本**
    - 若检测到代码块或明显编程语言 → `code`。
    - 若为短句、名言、诗句、带明显出处 → `quote`。
    - 否则 → `idea`。

用户可在 New Capture 中通过「卡片类型 Chips」覆盖 AI 建议类型。

### 3.3 组合输入（可选扩展）

- **URL + 文本**：以 URL 为主（article/video），文本作为用户备注并入 content 或 description。
- **图片 + 文本**：以图片 OCR/理解为主，文本作为补充说明。

首版可实现「单输入」优先，组合输入作为后续迭代。

---

## 4. 接口与数据契约

### 4.1 请求（客户端 → 后端 / AI 服务）

建议统一为一个「生成卡片草稿」接口，通过 `inputKind` 区分输入类型：

```kotlin
// 请求体示例（概念层，具体可放在 datastore/model-dto 或 feature 层）
@Serializable
data class GenerateCardRequest(
    val inputKind: String,           // "url" | "image" | "text"
    val url: String? = null,         // inputKind == "url"
    val imageBase64: String? = null, // inputKind == "image"（或 imageUrl）
    val text: String? = null,       // inputKind == "text"
    val options: GenerateCardOptions? = null
)

@Serializable
data class GenerateCardOptions(
    val preferredType: String? = null,  // 用户首选类型，AI 可参考
    val maxTitleLength: Int = 200,
    val maxContentLength: Int = 10000,
    val includeSuggestedTags: Boolean = true,
    val includeSuggestedCover: Boolean = true
)
```

- 校验：与 `CreateCardRequest` 一致，`content` 长度等上限可复用（如 10000 字符）。
- 图片：若先上传再生成，可传 `imageUrl` 替代 `imageBase64`，由后端拉取。

### 4.2 响应（后端 / AI 服务 → 客户端）

与现有 `Card` + metadata 结构对齐，便于直接填入 New Capture 表单并调用 `CardRepository`：

```kotlin
@Serializable
data class GeneratedCardDraft(
    val type: String,                    // 与 CARD_TYPE_* 一致
    val title: String?,
    val content: String,
    val metadata: CardMetadataDto?,      // 与现有 DTO 结构一致，见下
    val suggestedTags: List<String> = emptyList(),
    val suggestedCover: SuggestedCover? = null,
    val sourceInputKind: String,         // "url" | "image" | "text"
    val confidence: String? = null       // 可选：如 "high" | "medium" | "low"
)

@Serializable
data class SuggestedCover(
    val kind: String,                    // "color" | "image"
    val colorName: String? = null,       // M3: Red, Orange, Yellow, Green, Blue, Purple, Gray
    val colorHex: String? = null,
    val imageUrl: String? = null
)

// CardMetadataDto：与现有 article/code/quote/idea/video 等 DTO 对应，此处不展开字段
```

- `metadata` 需与当前 `Card` 的 `metadata` 反序列化兼容（或通过已有 DTO 转换）。
- 客户端拿到 `GeneratedCardDraft` 后：填入表单 → 用户编辑 → 调用现有「创建卡片」接口（如 `CreateCardRequest` + 本地/远程 `CardRepository`）。

### 4.3 错误与降级

- **超时 / 限流**：返回明确错误码，客户端提示「AI 生成暂时不可用，请手动填写」。
- **无法识别类型**：返回 `type = "idea"`，`content` 为原始文本，由用户自行改类型。
- **URL 无法抓取**：返回以 URL 为 title、content 为「链接保存」的 article 草稿，或仅 title+url，summary 为空由用户补全。
- **图片 OCR 失败**：返回 `type = "idea"`，title 为「来自图片」，content 为空或简短说明，建议用户手动输入。

---

## 5. 流程与交互

### 5.1 主流程

1. 用户在 **New Capture** 的「Source Content」中粘贴 URL / 上传图片 / 输入文本。
2. 用户（可选）选择「首选卡片类型」或交给 AI 自动判断。
3. 用户点击「用 AI 生成」或等价操作。
4. 客户端发送 `GenerateCardRequest`，展示 loading（骨架屏或进度）。
5. 服务端调用 AI/爬虫/OCR 管道，返回 `GeneratedCardDraft`。
6. 客户端将草稿填入表单：标题、内容、类型、标签、封面建议等；用户可编辑。
7. 用户点击「Capture Asset」保存，走现有创建卡片逻辑。

### 5.2 多端与 New Capture 布局（§2.6）

- **Medium 及以下**：生成结果可填入同一全屏表单，FAB 打开卡片预览。
- **大屏**：生成结果填入 Side Sheet 一侧，另一侧实时预览卡片。
- 生成中可取消请求；失败时保留用户已输入内容，仅清空 AI 填充部分或提示重试。

### 5.3 与「封面 AI」的关系

New Capture §2.4 中「第一个 item：AI——用户粘贴内容时，AI 智能分配色值或图片」可与本能力共用后端：

- 若已调用「生成卡片草稿」，则 `suggestedCover` 直接用于封面。
- 若用户仅先选封面、未做全文生成，可单独提供「仅生成封面建议」的轻量接口（可选）。

---

## 6. 技术方案要点

### 6.1 服务端职责

- **URL**：安全抓取（限域、限大小、超时）、正文提取或 OG 解析；视频 URL 解析出平台与元数据。
- **图片**：OCR（如 Tesseract/云 OCR）、可选视觉模型理解场景；输出文本后再按「文本」路径生成卡片。
- **文本**：调用 LLM，按「类型推断 + 结构化输出」生成 `GeneratedCardDraft`；prompt 中约束 `type` 枚举、字段长度、JSON 格式。
- 统一入口：一个 API 根据 `inputKind` 分发到上述三种管道，最后归一为 `GeneratedCardDraft`。

### 6.2 客户端职责

- 组装 `GenerateCardRequest`（含可选 `preferredType`、`includeSuggestedTags/Cover`）。
- 展示 loading / 错误 / 降级提示。
- 将 `GeneratedCardDraft` 映射到 New Capture 表单状态（含 metadata 与标签、封面）。
- 不持久化「草稿」到服务器时，仅保存在内存/本地状态，直到用户点击保存。

### 6.3 安全与合规

- 用户内容仅用于生成当前用户卡片，不用于训练；需在隐私政策与界面中说明。
- URL 抓取需遵守目标站点的 robots.txt 与版权；图片需符合上传与存储规范。
- 若使用第三方 LLM/OCR，需符合其使用条款与数据出境要求。

---

## 7. 验收与后续迭代

### 7.1 首版验收

- [ ] URL（文章链接）→ 稳定产出 `article` 卡片，title/summary/author/cover 可用。
- [ ] URL（视频链接）→ 产出 `video` 卡片，平台与基础元数据正确。
- [ ] 文本（代码）→ 产出 `code` 卡片，language/snippet/description 正确。
- [ ] 文本（文学/名言）→ 产出 `quote` 或 `idea`，用户可切换类型并保存。
- [ ] 图片 → 至少产出 `idea` 草稿（OCR 或场景描述），可选建议封面。
- [ ] 错误与超时均有明确提示，且支持「手动填写」不依赖 AI。

### 7.2 后续可做

- 组合输入（URL+文本、图片+文本）。
- 单独「仅生成封面建议」接口与 UI。
- 用户偏好：默认类型、是否自动带出建议标签等。
- 更细的类型与 metadata（如 `word` 生词本）与多语言优化。

---

## 8. 文档变更记录

| 版本   | 日期         | 变更说明                                        |
|------|------------|---------------------------------------------|
| v1.0 | 2025-02-02 | 初稿：目标、输入输出、与 New Capture/卡片模型对齐、接口与流程、技术要点。 |

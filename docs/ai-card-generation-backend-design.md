# AI 卡片生成 — 后端设计文档

> 文档版本：v1.0  
> 关联文档：[ai-card-generation-design.md](ai-card-generation-design.md)、[ai-card-generation-product-design.md](ai-card-generation-product-design.md)  
> 适用对象：后端、客户端、AI 集成

---

## 1. 概述

后端提供**统一入口**：根据 `inputKind`（url / image / text）分发到不同管道，最终归一为 `GeneratedCardDraft`，供客户端填入 New Capture 表单。

---

## 2. API 设计

### 2.1 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/cards/generate-draft` | 根据 URL / 图片 / 文本生成卡片草稿 |

- **认证**：需 Bearer Token（与现有 `/api/cards` 一致）。
- **限流**：建议按用户/分钟限制（如 10 次/分钟），避免滥用与成本失控。

### 2.2 请求体（GenerateCardRequest）

```kotlin
@Serializable
data class GenerateCardRequest(
    val inputKind: String,              // "url" | "image" | "text"
    val url: String? = null,            // inputKind == "url" 时必填
    val imageBase64: String? = null,    // inputKind == "image" 时必填（或 imageUrl）
    val imageUrl: String? = null,       // 可选：已上传的图片 URL，后端拉取
    val text: String? = null,           // inputKind == "text" 时必填
    val options: GenerateCardOptions? = null
)

@Serializable
data class GenerateCardOptions(
    val preferredType: String? = null,   // 用户首选类型，AI 可参考
    val maxTitleLength: Int = 200,
    val maxContentLength: Int = 10000,
    val includeSuggestedTags: Boolean = true,
    val includeSuggestedCover: Boolean = true
)
```

**校验规则**：

- `inputKind` 必填，且为 `url` / `image` / `text` 之一。
- `inputKind == "url"` 时，`url` 必填且为合法 URL。
- `inputKind == "image"` 时，`imageBase64` 或 `imageUrl` 至少其一。
- `inputKind == "text"` 时，`text` 必填且非空；长度 ≤ `maxContentLength`（默认 10000）。
- `title` 长度 ≤ `maxTitleLength`（默认 200）。

### 2.3 响应体（GeneratedCardDraft）

与现有 `Card` + metadata 结构对齐，便于客户端直接填入表单并后续调用 `CreateCardRequest`：

```kotlin
@Serializable
data class GeneratedCardDraft(
    val type: String,                      // 与 CARD_TYPE_* 一致
    val title: String?,
    val content: String,
    val metadata: CardMetadataDraft? = null,  // 按 type 不同结构见下
    val suggestedTags: List<String> = emptyList(),
    val suggestedCover: SuggestedCover? = null,
    val sourceInputKind: String,           // "url" | "image" | "text"
    val confidence: String? = null         // 可选: "high" | "medium" | "low"
)

@Serializable
data class SuggestedCover(
    val kind: String,                      // "color" | "image"
    val colorName: String? = null,         // M3: Red, Orange, Yellow, Green, Blue, Purple, Gray
    val colorHex: String? = null,
    val imageUrl: String? = null
)
```

**metadata 按 type 的 DTO 结构**（与 `datastore/model` 域模型对应，不含 `cardId`，创建卡片时由后端填充）：

| type | metadata 形状（示例名） |
|------|--------------------------|
| article | url, summary, coverImageUrl, author |
| video | videoUrl, thumbnailUrl, durationSeconds, platform |
| code | language, snippet, description |
| quote | author, category, source |
| idea | priority, status |
| todo / word | 按现有域模型扩展 |

可使用 `kotlinx.serialization` 的 `JsonObject` 或独立 `@Serializable` 子类 + 多态序列化，保证与现有 `CardMetadata*` 可互转。

### 2.4 错误码与降级

| 场景 | HTTP | 说明 |
|------|------|------|
| 参数校验失败 | 400 | 如 inputKind 错误、必填缺失 |
| 未认证 | 401 | 与现有 API 一致 |
| 限流 | 429 | 提示稍后重试 |
| URL 抓取失败 / 超时 | 200 + 降级草稿 | 返回 type=article，title=URL，content 简短说明，summary 空 |
| 图片 OCR 失败 | 200 + 降级草稿 | 返回 type=idea，title=「来自图片」，content 空或简短说明 |
| LLM/第三方服务超时 | 503 或 200+降级 | 视策略：可返回 503「AI 暂时不可用」或降级草稿 |
| 内部错误 | 500 | 通用错误信息，不暴露内部细节 |

---

## 3. 后端架构

### 3.1 分层

```
Controller (CardsApi 或独立 GenerateDraftApi)
    → GenerateCardService（统一编排）
        → UrlDraftPipeline / ImageDraftPipeline / TextDraftPipeline
            → 外部依赖：HttpClient（抓取）、OCR 服务、LLM 服务
```

### 3.2 服务职责

**GenerateCardService**

- 校验 `GenerateCardRequest`，根据 `inputKind` 调用对应 Pipeline。
- 各 Pipeline 返回统一的中间表示（或直接返回 `GeneratedCardDraft`），由 Service 归一为 `GeneratedCardDraft`（含 suggestedTags、suggestedCover）。
- 超时控制、限流、日志、监控。

**UrlDraftPipeline**

- 输入：`url`、`options.preferredType`。
- 步骤：安全抓取（限域、限大小、超时）→ 正文提取或 OG 解析；若为视频 URL 则解析平台与元数据。
- 输出：若可识别为视频站点 → `video` + videoMetadata；否则 → `article` + articleMetadata（title、summary、author、coverImageUrl）。
- 抓取失败时返回降级草稿（见 2.4）。

**ImageDraftPipeline**

- 输入：`imageBase64` 或 `imageUrl`、`options`。
- 步骤：若为 URL 则先拉取图片 → OCR（如 Tesseract/云 OCR）→ 得到文本；可选：视觉模型理解场景/图表。
- 将得到的文本交给「文本→卡片」逻辑（复用 TextDraftPipeline 或内联 LLM 调用），产出 `GeneratedCardDraft`（默认 type=idea；若识别到链接等可建议 article）。
- OCR 失败时返回降级草稿（见 2.4）。

**TextDraftPipeline**

- 输入：`text`、`options.preferredType`。
- 步骤：调用 LLM，输入为「文本 + 类型推断与结构化输出」的 prompt，输出 JSON 对应 `GeneratedCardDraft`（type、title、content、metadata、suggestedTags、suggestedCover）。
- Prompt 中约束：type 枚举、字段长度、JSON 格式（见《ChatGPT 提示词》文档）。
- 无法识别类型时返回 type=idea，content=原文。

### 3.3 与现有 Card 创建的关系

- **生成草稿**：`POST /api/cards/generate-draft` 仅返回草稿，**不落库**。
- **创建卡片**：用户编辑后，客户端调用现有 `POST /api/cards`（`CreateCardRequest`），此时需把 `GeneratedCardDraft` 转为 `CreateCardRequest`（并可在服务端根据 type 写入对应 metadata；若当前 `CreateCardRequest` 无 metadata 字段，需扩展或通过扩展字段传入）。

若当前 `CreateCardRequest` 只有 type/title/content/tagIds，则有两种方案：

- **方案 A**：扩展 `CreateCardRequest`，增加可选 `metadata: JsonObject?`（或按 type 的 DTO），服务端创建 Card 时写入 metadata。
- **方案 B**：客户端仅提交 type/title/content/tagIds，metadata 通过后续 `PATCH /api/cards/{id}` 写入（若 API 支持 metadata 部分更新）。

推荐方案 A，一次提交完成卡片+metadata 创建。

---

## 4. 外部依赖与安全

- **URL 抓取**：遵守目标站点 robots.txt；限域、限响应体大小、超时（如 10s）；防 SSRF。
- **图片**：存储/临时文件需符合安全与隐私规范；OCR 若用第三方，需符合其数据条款。
- **LLM**：用户内容仅用于生成当前用户卡片，不用于训练；合规与数据出境按公司规范。
- **密钥**：LLM API Key、OCR API Key 等通过配置/密钥管理注入，不写死在代码中。

---

## 5. 推荐开源 LLM 及资源需求

本任务（结构化 JSON、中英混合、类型推断）推荐使用 **Qwen2.5** 系列；其他可选模型及**资源需求**如下。均为推理（Inference）场景；若做微调需额外显存。

### 5.1 显存 / 内存 / 磁盘（推理、单卡）

| 模型 | 参数量 | FP16/BF16 显存 | Int4/4bit 显存 | 推荐 GPU（4bit） | 磁盘（模型文件） |
|------|--------|----------------|----------------|------------------|------------------|
| **Qwen2.5-7B-Instruct** | 7B | ~14 GB | **~6–8 GB** | RTX 3060 12GB / RTX 4060 8GB | ~15 GB |
| **Qwen2.5-14B-Instruct** | 14B | ~28 GB | **~9–13 GB** | RTX 3080 10GB / RTX 4080 16GB | ~28 GB |
| **Qwen2.5-32B-Instruct** | 32B | ~64 GB | **~18–20 GB** | RTX 3090 24GB / A10 24GB | ~64 GB |
| **Qwen2.5-72B-Instruct** | 72B | ~144 GB | **~41–49 GB** | A100 40GB×1 或 2×A10/3090 | ~140 GB |
| **Llama 3.2-8B-Instruct** | 8B | ~16 GB | **~6–8 GB** | RTX 3060 12GB / 4060 8GB | ~16 GB |
| **Llama 3.1-70B-Instruct** | 70B | ~140 GB | **~40–48 GB** | A100 40GB×1 或 2×24GB | ~130 GB |
| **DeepSeek-V2** | 236B MoE（激活约 21B） | 单机 8×80GB | **单卡约 20–25 GB**（量化） | A100 40GB×1 可跑量化 | ~120 GB+ |
| **Mixtral 8x7B** | 46.7B MoE（激活 2 专家） | ~90 GB | **~24 GB** | RTX 3090 24GB / A10 24GB | ~90 GB |
| **GLM-4-9B** | 9B | ~18 GB | **~6–8 GB** | RTX 3060 12GB / 4060 8GB | ~18 GB |
| **Mistral 7B** | 7B | ~14 GB | **~5–6 GB** | RTX 3060 12GB / 4060 8GB | ~14 GB |
| **Kimi K2**（月之暗面） | 1T MoE（激活约 32B） | 需多卡 | **量化约 24 GB+ 显存**（专家卸载到 CPU） | RTX 3090 24GB + **128 GB+ RAM** | **~250 GB+**（量化） |

说明：

- **FP16/BF16**：全精度，显存 ≈ 参数量 × 2 字节；72B 约 144GB，需多卡或量化。
- **Int4/4bit**：Q4_K_M、GPTQ-Int4、AWQ 等，显存约为 FP16 的 1/4～1/3；**做卡片生成推荐用 4bit 量化**即可。
- **KV Cache**：上下文 4K～8K 时，额外约 0.5–2 GB；32K 会再增加数 GB。
- **磁盘**：仅模型权重；GGUF/GPTQ 量化后略小。

### 5.2 本任务推荐配置

| 场景 | 推荐模型 | 最低显存（4bit） | 推荐配置 |
|------|----------|------------------|----------|
| 单机/轻量 | Qwen2.5-7B-Instruct | 8 GB | 1×RTX 3060 12GB 或 4060 8GB |
| 质量优先 | Qwen2.5-14B-Instruct | 12 GB | 1×RTX 3080 10GB 或 4080 16GB |
| 大模型/多租户 | Qwen2.5-72B-Instruct 或 Llama 3.1-70B | 48 GB | 1×A100 40GB 或 2×RTX 3090 24GB |

**内存（RAM）**：建议不低于 16 GB 系统内存；7B/8B 量化 16 GB 够用，14B 建议 32 GB，70B+ 建议 64 GB+。  
**磁盘**：预留 2–3 倍模型体积（下载 + 解压/转换）；7B 约 20–30 GB，72B 约 200 GB+。

### 5.3 云端 API（无本地显存）

若**不自建 GPU**，可直接调用云端大模型 API，按量计费，无需显存/磁盘：

| 服务 | 说明 | 资源需求 | 接入方式 |
|------|------|----------|----------|
| **Kimi API**（月之暗面 Moonshot） | 中文能力强、长上下文（128K）、兼容 OpenAI 格式 | **无本地显存**，按 Token 计费 | `https://api.moonshot.cn/v1/chat/completions`，需 API Key |
| Kimi K2 专用 API | K2 模型云端版 | 同上 | `https://kimi-k2.ai/api`，有免费额度 |
| 阿里云百炼（Qwen 等） | Qwen-Plus / Qwen-Turbo | 同上 | 阿里云控制台开通 |
| OpenAI / 其他 | GPT-4、Claude 等 | 同上 | 各自 API |

**Kimi 小结**：  
- **用 API**：适合「不想买显卡、希望快速接入、中文体验好」的场景；后端只需调 `api.moonshot.cn`，把《ChatGPT 提示词》当 system/user message 即可。  
- **本地跑 Kimi K2**：开源、可商用（Modified MIT），但模型极大（1T MoE），量化后仍需约 **24 GB 显存 + 128 GB+ 内存 + 250 GB+ 磁盘**，适合有强算力或追求完全自管的团队。

### 5.4 图片生成（卡片封面）开源模型

若需**根据标题/描述生成卡片封面图**（对应 New Capture §2.4「AI 智能分配」中的图片建议），可选用以下开源文生图模型。均为**文生图（Text-to-Image）**，输入为提示词，输出为图片 URL 或二进制。

| 模型 | 说明 | 显存（推理） | 中文支持 | 许可 / 商用 |
|------|------|----------------|----------|-------------|
| **Qwen-Image**（阿里） | 20B MMDiT，中英文本渲染强，9 项基准第一 | **单卡 RTX 3090 可跑**（约 24GB） | 强 | 开源可商用 |
| **Z-Image**（阿里） | 6B，极速生成（8 步），8GB 显存起步 | **8 GB+** | 支持 | 开源可商用 |
| **混元 DiT / HunyuanDiT**（腾讯） | 1.5B，中文原生 DiT，与 Sora 同架构 | **消费级 GPU 可跑**（约 6–8 GB） | 强 | 开源可免费商用 |
| **FLUX.1 [schnell]**（Black Forest Labs） | 蒸馏版，快速生成 | **约 12–24 GB**（FP8 约 16GB，NF4 量化约 7GB） | 英文为主 | Apache-2.0 可商用 |
| **FLUX.1 [dev]** | 高质量版 | **约 24 GB**（量化可更低） | 英文为主 | Non-Commercial |
| **Stable Diffusion XL (SDXL)** | 经典开源文生图 | **约 6–12 GB**（取决于分辨率与优化） | 需中文 LoRA 等 | 开源可商用 |
| **Stable Diffusion 3** | 新一代，画质与排版更好 | **约 12–24 GB** | 需中文 LoRA 等 | 依版本而定 |

**本任务推荐**：

- **中文封面、质量优先**：**Qwen-Image** 或 **混元 DiT**（中文理解好，适合「标题/标签 → 封面图」）。
- **显存紧张（8GB）**：**Z-Image** 或 **SDXL + 量化**。
- **英文提示词、画质优先**：**FLUX.1 [schnell]**（Apache-2.0，可商用）。

**部署方式**：  
- 本地：ComfyUI、Diffusers、官方推理仓库等，暴露 HTTP API 或内部调用。  
- 云端：部分提供 API（如 Replicate、FAL、阿里云等），按张/按量计费，无本地显存需求。

---

## 6. 本地构建后如何通过 API 调用

本地部署的 LLM 通过 **OpenAI 兼容的 HTTP 接口** 暴露，后端只需用同一套「Chat Completions」请求格式，把 `base_url` 指向本地服务即可。

### 6.1 本地暴露 API 的常见方式

| 方式 | 适用场景 | 默认地址 | 拉取/启动示例 |
|------|----------|----------|-------------------------------|
| **Ollama** | 单机、开发/小规模，最简单 | `http://localhost:11434/v1` | `ollama pull qwen2.5:7b` → 自动提供 API |
| **vLLM** | 生产、高并发、多模型 | `http://localhost:8000/v1` | `vllm serve Qwen/Qwen2.5-7B-Instruct --dtype auto` |
| **llama.cpp server** | GGUF 量化、低显存 | `http://localhost:8080` | `./server -m qwen2.5-7b-q4_k_m.gguf -c 4096` |

**统一请求格式**（与 OpenAI / Kimi 一致）：

- **端点**：`POST {base_url}/chat/completions`
- **请求体**：`model`、`messages`（含 `system` / `user`）、可选 `temperature`、`max_tokens`
- **响应**：从 `choices[0].message.content` 取文本（即我们约定的 JSON 卡片草稿）

### 6.2 Ollama 示例（推荐入门）

```bash
# 1. 安装 Ollama（https://ollama.com）
# 2. 拉取模型
ollama pull qwen2.5:7b

# 3. 服务默认监听 http://localhost:11434，自动提供 OpenAI 兼容接口
```

**请求示例（cURL）：**

```bash
curl http://localhost:11434/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "qwen2.5:7b",
    "messages": [
      {"role": "system", "content": "你是一个卡片草稿生成助手..."},
      {"role": "user", "content": "请根据以下用户输入生成卡片草稿...\n\n用户输入：\n唯一不变的是变化本身。—— 赫拉克利特"}
    ],
    "temperature": 0.3,
    "max_tokens": 2048
  }'
```

响应中 `choices[0].message.content` 即为模型输出的 JSON 字符串，后端解析为 `GeneratedCardDraft` 即可。

### 6.3 vLLM 示例（生产/高并发）

```bash
# 安装：pip install vllm
# 启动（按显存选模型与 dtype）
vllm serve Qwen/Qwen2.5-7B-Instruct --dtype auto --api-key token-optional

# 默认监听 http://localhost:8000/v1
```

请求格式与 Ollama 相同，仅改 `base_url` 和 `model` 名（如 `Qwen/Qwen2.5-7B-Instruct`）。

### 6.4 后端如何调用（Kotlin / Ktor）

后端统一使用 **OpenAI 兼容的 Chat Completions**，通过配置切换「本地 / 云端」：

**配置示例（环境变量或 application.conf）：**

```properties
# 本地 Ollama
llm.base_url=http://localhost:11434/v1
llm.model=qwen2.5:7b
llm.api_key=ollama

# 或本地 vLLM
# llm.base_url=http://localhost:8000/v1
# llm.model=Qwen/Qwen2.5-7B-Instruct
# llm.api_key=token-optional

# 或云端 Kimi（仅改 base_url 与 api_key）
# llm.base_url=https://api.moonshot.cn/v1
# llm.model=moonshot-v1-8k
# llm.api_key=sk-xxx
```

**调用逻辑（伪代码）：**

1. 组装 `messages`：`system` = 《ChatGPT 提示词》中的系统提示词，`user` = 用户输入（URL/图片描述/文本）。
2. `POST {llm.base_url}/chat/completions`，Body：`{"model": llm.model, "messages": [...], "temperature": 0.3, "max_tokens": 2048}`，Header：`Authorization: Bearer {llm.api_key}`。
3. 从响应 JSON 取 `choices[0].message.content`，做 `Json.decodeFromString<GeneratedCardDraft>(content)`；若解析失败可降级为 type=idea、content=原文。

这样**同一套后端代码**既可连本地 Ollama/vLLM，也可连 Kimi / OpenAI 等云端 API，只需改配置。

### 6.5 小结

| 步骤 | 说明 |
|------|------|
| 1. 本地起服务 | Ollama / vLLM / llama.cpp server 任选其一，保证提供 `POST .../chat/completions` |
| 2. 配置后端 | `llm.base_url`、`llm.model`、`llm.api_key` 指向本地或云端 |
| 3. 发请求 | 与调用 OpenAI/Kimi 相同：model + messages（system + user）+ temperature/max_tokens |
| 4. 解析结果 | 从 `choices[0].message.content` 取 JSON 字符串，反序列化为 `GeneratedCardDraft` |

---

## 7. 实现清单（后端）

- [ ] 新增 DTO：`GenerateCardRequest`、`GenerateCardOptions`、`GeneratedCardDraft`、`SuggestedCover`、按 type 的 metadata DTO 或 JsonObject。
- [ ] 新增路由：`POST /api/cards/generate-draft`，认证 + 限流。
- [ ] 实现 `GenerateCardService` 与三种 Pipeline（Url / Image / Text）。
- [ ] URL：集成 HTTP 客户端 + 正文/OG 解析；视频 URL 识别与元数据解析。
- [ ] Image：集成 OCR；可选视觉模型；与文本管道复用。
- [ ] Text：集成 LLM，使用《ChatGPT 提示词》文档中的 prompt 与 JSON 约束。
- [ ] 错误与降级：按 2.4 实现 400/401/429/503/500 及降级草稿。
- [ ] （可选）扩展 `CreateCardRequest` 支持 metadata，以便一次创建卡片+metadata。

---

## 8. 文档变更记录

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| v1.0 | 2025-02-02 | 初稿：API、请求/响应、管道架构、安全与实现清单。 |
| v1.1 | 2025-02-02 | 新增 §5 推荐开源 LLM 及资源需求、§5.3 云端 API、§6 本地构建后通过 API 调用（Ollama/vLLM、后端 Kotlin 调用方式）。 |

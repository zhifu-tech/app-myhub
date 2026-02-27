# Capture Analysis Provider Integration (Ollama / Qwen)

## 1. Goal

在 Server 侧把 Capture 分析从本地 mock 逻辑升级为可配置的真实模型调用，并支持两种 Provider：

- `ollama`（默认，开发期主用）
- `qwen`（生产可切换）

要求：不改业务代码，仅通过配置切换 Provider。

---

## 2. Industry SOP (Best Practice)

### 2.1 Provider Abstraction

用统一接口封装不同模型厂商，业务层只依赖抽象：

- `AnalysisProvider.analyze(request): AnalysisModelOutput`

### 2.2 Config Driven Routing

通过配置选择 Provider（`ollama`/`qwen`），避免代码分支。

### 2.3 Structured Output First

要求模型返回结构化 JSON，禁止直接消费自由文本。

### 2.4 Reliability Guardrails

- timeout
- retry（指数退避）
- error 分类（provider timeout / invalid json / auth）
- fallback（失败时返回最小可编辑结果）

### 2.5 Observability

- 结构化日志：`jobId`, `provider`, `model`, `latencyMs`, `status`
- 指标：成功率、P95 时延、失败码分布

---

## 3. Target Architecture

### 3.1 Components

1. `AnalysisProviderConfig`
   - 从 env/system property 读取 provider 与参数
2. `AnalysisProvider` (interface)
3. `OllamaAnalysisProvider`
4. `QwenAnalysisProvider`
5. `AnalysisProviderFactory`
6. `CaptureAnalysisService`
   - 仅调用 `AnalysisProvider`

### 3.2 Data Flow

1. Client 提交 `/api/capture/analysis`
2. Server 创建 job
3. Job 执行阶段调用 `AnalysisProvider`
4. Provider 返回结构化结果
5. 映射为 `CaptureReviewDataPayload`（即 `review` 的传输载体）
6. Client 轮询拿到 `Succeeded + result`

---

## 4. Config Contract

### 4.1 Common

- `CAPTURE_ANALYSIS_PROVIDER=ollama|qwen` (default: `ollama`)
- `CAPTURE_ANALYSIS_TIMEOUT_MS` (default: `60000`)
- `CAPTURE_ANALYSIS_RETRY_MAX` (default: `2`)
- `CAPTURE_ANALYSIS_RETRY_BACKOFF_MS` (default: `500`)

### 4.2 Ollama

- `OLLAMA_BASE_URL` (default: `http://127.0.0.1:11434`)
- `OLLAMA_TEXT_MODEL` (default: `qwen2.5:7b`)
- `OLLAMA_VISION_MODEL` (default: `qwen3-vl:8b`)
- `OLLAMA_TEXT_TIMEOUT_MS` (default: `90000`)
- `OLLAMA_VISION_TIMEOUT_MS` (default: `240000`)
- `OLLAMA_VISION_IMAGE_LIMIT` (default: `6`)
- `VIDEO_FRAME_COUNT` (default: `3`)
- `VIDEO_FRAME_INTERVAL_SECONDS` (default: `2`)
- endpoint: `POST /api/chat`

说明：
- 无图片/视频帧时，服务端走 `OLLAMA_TEXT_MODEL`（文本模型）。
- 有图片或视频抽帧输入时，服务端走 `OLLAMA_VISION_MODEL`（视觉模型）。
- 服务端会把 image 媒体直接作为 `images(base64)` 传入 Ollama。
- 对 video 媒体，会先通过 `ffmpeg` 抽帧，再把抽帧结果作为 `images(base64)` 传入。
- 运行环境需要安装 `ffmpeg` 并保证 PATH 可访问。

### 4.3 Qwen (DashScope OpenAI-compatible)

- `QWEN_BASE_URL` (default: `https://dashscope-intl.aliyuncs.com/compatible-mode/v1`)
- `QWEN_API_KEY` (required when provider=qwen)
- `QWEN_MODEL` (default: `qwen-plus`)
- endpoint: `POST /chat/completions`

---

## 5. Prompt and Output Schema

模型输出 JSON（字段缺失允许，服务端补默认值）：

```json
{
  "title": "string",
  "text": "string",
  "sourceForm": "extract|link|own",
  "tags": ["string"],
  "code": "string|null",
  "codeLanguage": "string|null",
  "imageOcrSummary": "string|null",
  "imageOcrInfo": "string|null",
  "videoMetadataSummary": "string|null",
  "videoMetadataInfo": "string|null",
  "primaryContentType": "Text|Code|Image|Video"
}
```

服务端仍统一注入默认 style swatches，确保 UI 一致性。

---

## 6. Failure and Fallback Policy

### 6.1 Provider Call Failure

- 重试达到上限后：job -> `Failed`
- `errorCode`: `provider_timeout` / `provider_http_error` / `provider_invalid_json`

### 6.2 Response Parse Failure

- 记日志 + job failed
- 不返回半结构化脏数据

### 6.3 Graceful Degrade (optional toggle)

- 可选策略：返回最小结果（title/text）并标记 `degraded=true`

---

## 7. Rollout Plan

1. Phase 1: 默认 `ollama`，联调并稳定结构化输出
2. Phase 2: 接入 `qwen` 并灰度切换
3. Phase 3: 增加指标和告警阈值

---

## 8. Implementation Checklist

- [x] 新增 provider 抽象与配置模型
- [x] 新增 Ollama provider
- [x] 新增 Qwen provider
- [x] 在 DI 里按配置选择 provider
- [x] CaptureAnalysisService 替换 mock buildResult
- [x] 增加 retry/timeout/error 分类
- [x] 增加结构化日志（job/provider/model/latency）
- [ ] 联调验证（ollama -> qwen 配置切换）

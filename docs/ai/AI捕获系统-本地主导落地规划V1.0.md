# AI捕获系统-本地主导落地规划 V1.0

## 1. 文档目标
- 建立一套可落地实施方案：以客户端为主，服务端仅提供 AI 能力代理。
- 同时支持用户在客户端直接配置自己的 AI 模型 API，实现完全脱离服务端运行。
- 保持数据主权在本地设备，默认不上传个人资产正文。

## 2. 设计原则
1. Local-first：卡片、草稿、媒体索引默认本地存储。
2. Client-first：交互、状态机、草稿编排优先在客户端执行。
3. AI pluggable：同一套调用协议可切换 AI 提供方（Server 代理 or 直连用户 API）。
4. Privacy by default：默认最小上传、可配置零留存。
5. Degrade gracefully：任一 AI 通道不可用时，仍可纯本地编辑与发布。

## 3. 目标形态（最终架构）
```text
Client App
  ├─ Conversation Engine (State Machine)
  ├─ Draft/Card Engine
  ├─ Local DB + Media Index
  ├─ AI Provider Router
  │    ├─ Server Gateway Provider
  │    └─ Direct API Provider (User Config)
  └─ Publish (Local Commit)

Server (Optional)
  ├─ AI Gateway
  ├─ Key/Quota/RateLimit
  ├─ Prompt/Tool Templates
  └─ Observability (no content persistence by default)
```

## 4. 客户端与服务端分工

### 4.1 客户端（主系统）
- 对话状态机：`IDLE -> INTENT_DETECT -> DRAFT_CREATE -> INFO_COLLECT -> REVIEW -> PUBLISH`。
- 草稿生成与卡片编辑：所有字段补全与 UI 交互在本地完成。
- 本地数据存储：卡片正文、标签、媒体引用、历史版本。
- AI 路由与调用：根据用户设置切换到 Server Gateway 或 Direct API。
- 发布语义：发布=写入本地正式库（不等于上传云端）。
- 离线能力：AI 不可用时，允许手动流程继续。

### 4.2 服务端（可选能力层）
- 仅负责 AI 代理：统一模型调用、重试、超时、限流。
- 安全与成本控制：密钥托管、配额、风控。
- 默认不落正文：只保留必要运行指标（可配置关闭）。
- 不做资产主存储：不承担卡片正文托管责任。

## 5. 双 AI 模式设计

### 5.1 模式 A：Server Gateway（默认推荐）
- 优点：密钥安全、调用稳定、策略统一。
- 缺点：依赖服务端在线。
- 适用：大多数用户、希望开箱即用。

### 5.2 模式 B：Direct API（用户自配置）
- 优点：完全脱离服务端、强隐私主权。
- 缺点：密钥保管、兼容性和稳定性由客户端承担。
- 适用：高级用户、强隐私需求、私有模型用户。

### 5.3 Provider 路由规范
```text
AIProvider = server_gateway | direct_openai | direct_ollama | direct_compatible
```

统一请求模型（客户端内部 DTO）：
```json
{
  "task": "capture_analysis",
  "input": {
    "text": "string",
    "media": []
  },
  "context": {
    "state": "INFO_COLLECT",
    "missing_fields": ["tags"]
  }
}
```

统一响应模型：
```json
{
  "title": "string",
  "summary": "string",
  "tags": ["string"],
  "entities": [],
  "suggestions": [],
  "confidence": 0.0
}
```

## 6. 本地数据与隐私策略

### 6.1 本地存储范围
- 卡片结构（UCS）
- 草稿状态
- 标签与关系
- 媒体元数据与本地路径索引
- AI 调用配置（密钥/endpoint 加密保存）

### 6.2 隐私级别（用户可配置）
1. 仅本地：禁用所有网络 AI。
2. 本地 + Server AI：仅上传推理所需最小字段。
3. 本地 + 直连用户 API：不经过本产品服务端。

### 6.3 最小上传原则
- 默认只上传当前轮必要文本与必要媒体摘要。
- 不上传完整历史对话（除非用户开启“上下文增强”）。
- 支持本地脱敏插件（手机号、邮箱、身份证、地址）。

## 7. 功能落地分期

## Phase 1（2-3 周）：可用最小闭环
- 客户端本地状态机 + 草稿/发布流程。
- AI Provider Router（先接 Server Gateway）。
- 本地数据库完整保存卡片。
- 失败降级：AI 失败可手动继续。

交付标准：
- 无网络时可本地创建/编辑/发布。
- 网络可用时 AI 可返回草稿建议并合并到本地卡片。

## Phase 2（2-3 周）：直连自有模型
- 增加 Direct API Provider（OpenAI 兼容协议 + Ollama）。
- 新增 AI 配置中心（endpoint/model/api key/timeout）。
- 增加 Provider 健康检测与连通性测试。

交付标准：
- 关闭服务端后，用户仍可通过自配模型完成 AI 捕获。

## Phase 3（2 周）：隐私与可靠性强化
- 本地密钥加密与访问口令保护。
- 脱敏规则 + 上传预览确认。
- AI 结果质量评分与重试策略。

交付标准：
- 通过隐私模式切换测试与异常恢复测试。

## Phase 4（可选）：多端同步（E2EE）
- 默认关闭，用户可手动启用。
- 服务端仅存密文，不可见正文。

## 8. 关键模块实施清单

### 8.1 客户端
1. `ConversationStateMachine`
2. `DraftCardStore`（本地草稿仓库）
3. `CardRepositoryLocal`
4. `AIProviderRouter`
5. `ServerGatewayProvider`
6. `DirectApiProvider`（OpenAI/Ollama 兼容）
7. `AIConfigManager`（加密配置）
8. `PrivacyGuard`（脱敏与上传裁剪）

### 8.2 服务端（可选）
1. `POST /api/ai/capture-analysis`
2. Provider 适配层（多模型）
3. 限流/配额
4. 错误码与可观测性

## 9. 配置与开关设计

客户端新增配置：
- `ai.mode`: `server_gateway | direct_api | disabled`
- `ai.direct.endpoint`
- `ai.direct.model`
- `ai.direct.apiKey`（加密）
- `ai.request.timeoutMs`
- `privacy.upload.minimal`
- `privacy.masking.enabled`

## 10. 风险与对策

1. 直连 API 兼容差异
- 对策：实现 OpenAI 兼容层 + provider capability 探测。

2. 客户端密钥泄露风险
- 对策：系统安全存储 + 生物识别/口令解锁。

3. AI 结果不稳定
- 对策：统一 JSON 输出约束 + 本地字段校验器 + 重试。

4. 长媒体分析耗时高
- 对策：异步任务 + 进度反馈 + 可取消。

## 11. 验收标准（Definition of Done）
- 用户可在离线状态完成“输入-草稿-发布”全流程。
- 用户可切换 AI 模式（服务端代理/直连自配）并即时生效。
- 关闭服务端时，直连模式可独立工作。
- 本地资产不依赖服务端保存。
- 隐私设置生效并可验证（最小上传/脱敏/禁网模式）。

## 12. 推荐实施顺序
1. 先打通本地状态机与本地发布。
2. 接入服务端 AI 代理，验证体验。
3. 增加直连自有模型能力。
4. 完成隐私与安全强化。
5. 再考虑可选的 E2EE 同步。

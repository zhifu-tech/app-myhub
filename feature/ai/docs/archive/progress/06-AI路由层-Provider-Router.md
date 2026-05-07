# 模块进度：AI Provider Router

## 作用

- 抽象 AI Provider 差异，支持模式切换：
- `server_gateway`
- `direct_api`
- `disabled`
- 提供统一请求响应协议与降级策略。

## 已实现

- 已实现 `ProviderRouter` 统一路由接口（`mode + resolveRoute()`）。
- 已实现 `ConfigurableProviderRouter`，支持模式切换：
- `server_gateway`
- `direct_api`
- `disabled`
- 已实现配置源持久化（基于 `LocalSettingStore`）：
- `SettingsProviderConfigSource`
- `RealServerGatewayHealthChecker`（`GET /api/ai/providers/health`）
- `RealDirectApiHealthChecker`（`GET {directEndpoint}/v1/models`）
- 已实现降级链：`server_gateway -> direct_api -> disabled`、`direct_api -> server_gateway -> disabled`。
- 已实现健康检查重试与熔断（失败阈值 + 冷却窗口）。
- 已在 `CaptureOrchestrator` 接入降级策略：Provider 不可用时记录 `ai_job=failed`，并切换 `MANUAL_EDIT`。
- 已实现真实请求执行器：
- `ServerGatewayProviderClient`（`POST /api/ai/capture-analysis`）
- `DirectApiProviderClient`（OpenAI compatible `POST /v1/chat/completions`）
- 已实现统一执行入口 `RoutedProviderAnalysisExecutor` 与统一 DTO（request/output）。
- 已实现 Provider 错误分类（`config/auth/timeout/http/parse/unavailable`）。
- 已接入轻量观测统计（成功数/失败数/平均时延/错误分布）。
- 已实现后台指标上报：周期上报 `ai_provider_metrics` 事件到 Analytics 通道。
- 旧的 server-first `CaptureRepository` 与 `HttpCaptureRepository` 已标记 `Deprecated`，不再作为主路径。

## TODO

- 增加可视化配置入口（Settings UI）与配置校验提示。
- 将观测指标扩展到 provider 级别（按 mode 分桶）与卡片链路关联 ID。

## 进度

- 95%

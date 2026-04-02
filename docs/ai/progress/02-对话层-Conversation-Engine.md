# 模块进度：Conversation Engine

## 作用

- 负责会话状态机流转与流程控制。
- 维护当前草稿上下文、缺失字段、下一步动作。
- 保证所有行为符合状态机约束。

## 已实现

- 在 `feature/ai/AIModels.kt` 与 `AIViewModel.kt` 中落地核心状态：
- `IDLE`
- `INTENT_DETECT`
- `DRAFT_CREATE`
- `INFO_COLLECT`
- `CARD_REVIEW`
- `PUBLISH_CONFIRM`
- `COMPLETE`
- `MANUAL_EDIT`
- 实现了从输入到草稿，再到补全、复核、发布的客户端流转。
- 实现缺失字段检测（当前为 `title/tags`）。
- 新增 `StateGuard`，对输入与动作进行状态约束校验。
- 状态机迁移已改为表驱动映射（State + Signal -> NextState）。

## TODO

- 增加规则覆盖测试（State + Signal 全路径）。
- 增加异常恢复分支（持久化失败/工具失败时的自动回退状态）。
- 补齐跨会话消息历史恢复策略（当前已恢复草稿和状态，消息仅追加系统恢复提示）。

## 进度

- 83%

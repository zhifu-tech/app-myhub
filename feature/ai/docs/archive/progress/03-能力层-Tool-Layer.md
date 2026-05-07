# 模块进度：Tool Layer

## 作用

- 承接状态机决策，执行确定性动作。
- 统一参数校验、幂等、副作用边界。
- 作为数据写入唯一入口，禁止 AI 直接写库。

## 已实现

- 已抽离独立 `ToolDispatcher`，不再耦合 `AIViewModel`。
- 已落地 `UpdateTitle / AddTag / AttachPickedMedia / PublishCard` 等 ToolCommand。
- 发布动作统一走 `ToolDispatcher -> CardEngine -> StorageGateway`，满足“Tool-governed mutation”。
- 已接入 `ToolRequestValidator`（入参校验）与 `ToolErrorCode`（统一错误码）。
- 已新增 `ToolRegistry`，执行逻辑与调度逻辑拆分，`ToolDispatcher` 仅负责编排与错误映射。

## TODO

- 增加 schema 级入参校验器（按 Action Component 自动约束）。
- 增加 `rollback_draft_version/search_cards` 等工具。

## 进度

- 90%

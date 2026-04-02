# 模块进度：Local Storage

## 作用

- 承载本地结构化数据、媒体索引与会话数据。
- 支持离线可用、快速查询、后续同步扩展。

## 已实现

- 复用现有 `card` 表与 `CardRepository` 完成本地发布写入。
- 修复字段迁移适配：`deleted -> deleted_at`。
- 已同步更新本地数据源写入参数，避免 schema 不匹配。
- 已接入 `draft_session`：会话状态、草稿与缺失字段可持久化。
- 已接入 `ai_job`：Agent 分析任务会写入本地作业记录（running/succeeded）。
- 已接入 `media_asset`：发布后将草稿中附加媒体写入本地媒体索引。
- 媒体元数据已写入 `media_type/size_bytes/sha256/local_uri`（当前 hash 为跨平台占位实现）。
- 发布链路增加失败补偿：媒体索引写失败会回滚已发布卡片。
- `sha256` 已替换为真实 SHA-256（纯 Kotlin 实现，commonMain 生效）。
- 发布后会为每个媒体写入 `ai_job(queued)` 作为后处理任务入口（缩略图/时长管线预留）。
- 已新增 `MediaPostProcessExecutor`：消费 queued 任务并回写 `thumb_uri/duration_ms`，任务状态更新为 succeeded/failed。
- 已新增 `MediaGarbageCollector`：清理无卡片引用或本地文件丢失的 `media_asset` 记录，并尝试删除孤儿文件。
- 已新增 `AiJobRecoveryManager`：支持失败作业查询、重试（retry）、重放（replay）基础能力。
- 已补充 `ai_job` 状态查询接口（按 status 分页查询），为重试与运维调试提供底座。
- 已新增后台定时调度：周期执行 `MediaPostProcessExecutor`（任务消费）+ `MediaGarbageCollector`（媒体清理）。

## TODO

- 完成媒体文件写入事务与无引用回收。
- 增加 `ai_job` 重试、失败恢复与重放调试能力。
- 将 draft 快照与 UI 消息历史做一致性恢复。
- 接入真实缩略图生成与视频时长抽取算法（当前执行器为占位回写）。
- 增加调度策略配置化（interval/batch size）与运行状态可视化。

## 进度

- 90%

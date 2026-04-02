# 模块进度：Card Engine

## 作用

- 统一卡片创建、更新、发布与校验。
- 维持草稿与发布态转换规则。
- 管控字段格式和版本语义，避免数据污染。

## 已实现

- 已抽离独立 `CardEngine`（`CardEngine` + `LocalCardEngine` + `CardValidator`）。
- 发布时固定本地提交语义：`Publish = Local Commit`。
- 卡片核心字段按现有 domain 与数据库结构生成（title/summary/tags/content/source/status/version）。
- 已新增 `prePublishCheck()`，发布前阻断缺失标题/内容/标签等不满足条件的草稿。
- 已将字段格式化拆分为 `CardFieldFormatter`，校验与格式化职责分离。
- 已新增 `CardPrePublishChecker`，发布前规则与常规草稿校验分层。

## TODO

- 增加 location/source 等扩展字段的规范化与校验。
- 增加软删除与版本回滚策略。

## 进度

- 80%

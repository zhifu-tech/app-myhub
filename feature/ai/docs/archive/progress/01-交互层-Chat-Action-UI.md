# 模块进度：Chat + Action UI

## 作用

- 承接用户输入与对话展示。
- 显示动作组件（Action）与草稿预览（Live Preview）。
- 将用户动作转化为结构化事件，交给状态机与 Tool 层处理。

## 已实现

- `feature/ai/AiScreen.kt` 改为真实可交互页面，不再是静态 mock。
- 支持消息流展示（AI/User/System）。
- 支持 Action Component Schema 渲染（`quick_reply/card_actions/tag_selector/upload/input`）。
- 支持草稿预览卡片展示（标题、摘要、标签、状态）。
- `upload_media` 已接入真实文件选择器（MediaPicker），并回显附加媒体数量。
- 已新增 Provider Mode 快速切换入口（`disabled/server_gateway/direct_api`）。

## TODO

- 增加 Upload、TagSelector、InlineInput 等完整组件族。
- 增加组件生命周期（active/completed/expired）和禁用态。
- 将 Provider 配置入口迁移到统一 Settings 页面（当前在 AI 页面提供基础入口）。

## 进度

- 76%

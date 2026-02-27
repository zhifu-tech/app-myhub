# 基于 URL / 图片 / 文本的 AI 卡片生成 — 产品设计摘要

> 文档版本：v1.0  
> 关联文档：[ai-card-generation-design.md](ai-card-generation-design.md)（技术详述）、[ai-card-generation-backend-design.md](ai-card-generation-backend-design.md)（后端设计）、[myhub-app-design.md](Stitch/myhub-app-design.md) §8.6 New Capture  
> 适用对象：产品、前端、后端、AI 集成

---

## 1. 目标

用户提供**一种原始素材**（URL、图片或一段文本），由 **AI 自动分析并生成一张符合 MyHub 数据模型的卡片草稿**。用户可在 New Capture 中在此基础上编辑、补充后保存。

---

## 2. 三种输入与产出

| 输入类型 | 说明 | 典型场景 | AI 主要产出类型 |
|---------|------|----------|-----------------|
| **URL** | 一个网页链接 | 文章、博客、文档、视频页 | article / video |
| **图片** | 一张本地或网络图片 | 截图、照片、示意图、信息图 | idea / article（若含链接） |
| **文本** | 用户粘贴/输入的一段文字 | 文学摘录、名言、代码片段、笔记 | quote / idea / code |

---

## 3. 输出内容（卡片草稿）

- **基础字段**：卡片类型（type）、标题（title）、正文（content）。
- **类型相关元数据**：与现有卡片类型一致（如 article 的 url/summary/author，code 的 language/snippet/description 等）。
- **可选增强**：
  - 建议标签（suggestedTags）：M3 Chips，用户可增删。
  - 建议封面（suggestedCover）：纯色（M3 七色之一）或「使用图片」，与 New Capture §2.4 的「AI 智能分配」一致。

---

## 4. 类型推断规则（AI 建议，用户可覆盖）

- **URL**：视频站点（如 YouTube、B 站、小红书视频）→ `video`；否则 → `article`。
- **图片**：默认 → `idea`；若识别到文章/链接特征 → 可建议 `article`。
- **文本**：
  - 检测到代码块或明显编程语言 → `code`。
  - 短句、名言、诗句、带明显出处 → `quote`。
  - 否则 → `idea`。

用户在 New Capture 中可通过「卡片类型 Chips」覆盖 AI 建议类型。

---

## 5. 与 New Capture 的衔接

- **Source Content（§2.1）**：用户在此粘贴 URL / 上传图片 / 输入文本。
- **卡片类型（§2.2）**：AI 输出类型落在现有类型集合内（article、code、quote、idea、video、word、todo 等）。
- **标题（§2.3）**：AI 生成「显示在卡片中的标题」，用户可改。
- **封面（§2.4）**：AI 建议封面（纯色或图片）对应「AI」选项。
- **标签（§2.5）**：AI 输出建议标签，用户可增删。

---

## 6. 主流程

1. 用户在 New Capture 的「Source Content」中粘贴 URL / 上传图片 / 输入文本。
2. （可选）用户选择「首选卡片类型」或交给 AI 自动判断。
3. 用户点击「用 AI 生成」。
4. 客户端发送生成请求，展示 loading。
5. 服务端返回卡片草稿（类型、标题、内容、metadata、建议标签、建议封面）。
6. 客户端将草稿填入表单，用户可编辑。
7. 用户点击「Capture Asset」保存，走现有创建卡片逻辑。

---

## 7. 错误与降级

- **超时 / 限流**：提示「AI 生成暂时不可用，请手动填写」。
- **无法识别类型**：返回 `type = "idea"`，content 为原始文本，用户可改类型。
- **URL 无法抓取**：返回以 URL 为 title 的 article 草稿，summary 可由用户补全。
- **图片 OCR 失败**：返回 `type = "idea"`，title 为「来自图片」，content 为空或简短说明，建议用户手动输入。

---

## 8. 验收要点

- [ ] URL（文章）→ 稳定产出 article 卡片，title/summary/author/cover 可用。
- [ ] URL（视频）→ 产出 video 卡片，平台与基础元数据正确。
- [ ] 文本（代码）→ 产出 code 卡片，language/snippet/description 正确。
- [ ] 文本（文学/名言）→ 产出 quote 或 idea，用户可切换类型并保存。
- [ ] 图片 → 至少产出 idea 草稿（OCR 或场景描述），可选建议封面。
- [ ] 错误与超时均有明确提示，且支持「手动填写」不依赖 AI。

---

## 9. 文档变更记录

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| v1.0 | 2025-02-02 | 初稿：产品目标、三种输入、输出与类型推断、流程与验收。 |

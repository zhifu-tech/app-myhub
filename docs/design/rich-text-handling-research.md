# 富文本处理调研（草案）

> 范围：常见富文本存储/渲染/编辑方案调研，并给出适配 MyHub（KMP/Compose、Capture Review）的推荐路径。

---

## 1. 行业常见方案

### 1.1 HTML 作为存储格式
- **机制**：编辑器输出 HTML；渲染层直接展示 HTML。
- **优点**：Web 互通性强；导入/导出方便；生态成熟。
- **缺点**：难以约束结构；HTML 冗长、diff 不友好；必须做安全清洗。
- **常见场景**：CMS、文档平台、邮件编辑器。

### 1.2 Markdown 作为存储格式（CommonMark/GFM）
- **机制**：编辑器输出 Markdown；渲染层解析 Markdown。
- **优点**：可读性好；diff 友好；存储轻量；AI 处理友好。
- **缺点**：语义有限（表格/扩展不统一）；嵌套样式能力弱；难以保证像素级一致。
- **常见场景**：开发者工具、笔记、Wiki。

### 1.3 结构化 JSON Schema（ProseMirror/Slate/Lexical）
- **机制**：文档以 JSON 树存储（节点 + marks）。
- **优点**：结构强约束；行为可控；渲染稳定；可扩展。
- **缺点**：需自研渲染；导入/导出成本高；schema 变更带来迁移成本。
- **常见场景**：Notion/Confluence 类产品、复杂编辑器。

### 1.4 Delta/操作序列（Quill Delta、OT/CRDT）
- **机制**：存操作序列或 CRDT 结构。
- **优点**：协作友好；增量更新效率高。
- **缺点**：基础设施复杂；没有实时协作需求时收益不高。
- **常见场景**：Google Docs 类协作编辑。

### 1.5 平台原生富文本（Android Spannable / iOS NSAttributedString）
- **机制**：存平台原生 span；跨平台需要转换。
- **优点**：原生体验最好；平台内实现容易。
- **缺点**：KMP 不统一；序列化成本高；桌面/Web 不一致。
- **常见场景**：纯原生应用。

---

## 2. 典型链路（编辑器 ↔ 存储 ↔ 渲染）

- **编辑模型**：用户操作（工具条 → 样式 mark）。
- **序列化**：编辑状态转换为标准存储格式。
- **存储**：持久化到 DB/API。
- **渲染**：解析存储格式进行只读渲染。
- **交换**：可选的 HTML/Markdown 导入导出。

---

## 3. MyHub 适配考虑（KMP/Compose + Capture Review）

### 约束
- KMP：需要跨端一致的模型。
- Capture Review：内容结构化（文本/代码/媒体）。
- 预览卡片和 AI 处理需要稳定输出。
- 暂无实时协作需求。

### 核心需求
- 基础行内样式：**加粗/斜体/下划线**
- 对齐：**左/中/右**
- 严格的文本编辑框样式（与设计稿一致）
- 预览卡片渲染稳定
- 存储格式 AI 友好

---

## 3.1 Compose Rich Editor（MohamedRejeb/compose-rich-editor）

来源：https://github.com/MohamedRejeb/compose-rich-editor

### 它的实现原理（基于 README 公开信息）
- 以 **RichTextState** 作为富文本的核心状态模型。
- **RichTextEditor** 负责可编辑展示（WYSIWYG）。
- 样式通过 `toggleSpanStyle`（行内）与 `toggleParagraphStyle`（段落）应用到状态。
- 内部维护“文档 + 选区 + 样式”的状态，渲染时根据状态生成 Compose Text。
- 提供 **HTML/Markdown** 的导入导出能力（`setHtml`/`toHtml`、`setMarkdown`/`toMarkdown`）。
- 通过配置项控制 link、code span 等样式表现。

### 能力覆盖
- 行内样式：加粗/斜体/下划线
- 段落样式：对齐
- 链接、代码片段、列表
- HTML/Markdown 互转

### 与 MyHub 的适配性
- **KMP 适配**：原生支持 Compose Multiplatform。
- **富文本需求匹配**：对齐 + 行内样式覆盖 Capture Review 需求。
- **序列化**：可用 Markdown/HTML 作为第一阶段存储格式。
- **预览一致性**：可复用富文本渲染组件进行只读展示。

### 风险与注意
- 版本为 RC；API 变化/边界案例需评估。
- HTML/Markdown 转换的可控性有限（依赖库实现）。
- 未来需要更严格 schema 时可能需要迁移。

### 我们如何调用（建议落地路径）
1. 引入依赖：
   - `implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc13")`
2. 编辑态：
   - 使用 `rememberRichTextState()` 创建状态；
   - `RichTextEditor(state = state)` 渲染编辑区；
   - 工具条绑定：`toggleSpanStyle`（加粗/斜体/下划线），`toggleParagraphStyle`（对齐）。
3. 存储：
   - `state.toMarkdown()` 作为第一阶段 canonical 文本；
   - 对齐等段落信息可作为附加 metadata。
4. 回显：
   - `state.setMarkdown(savedMarkdown)` 恢复；
   - 只读场景可用 `RichText(state = state)` 渲染。

### 结论
- 若优先“快落地 + KMP 统一”，compose-rich-editor 是最直接可用方案。
- 若优先“长期结构化可控”，可先用该库作为 Phase 1，再规划 JSON schema 迁移。

---

## 3.2 compose-rich-editor 源码级实现要点（基于本地仓库）

> 基于 `~/Work/gh-compose-rich-editor` 分析。

### 核心数据结构
- **RichTextState**：编辑器核心状态（`richeditor-compose/.../model/RichTextState.kt`）。
  - `richParagraphList: MutableList<RichParagraph>` 作为文档主体。
  - `textFieldValue: TextFieldValue` 保存输入与选区。
  - `annotatedString: AnnotatedString` 作为最终渲染文本。
- **RichParagraph**：段落节点（`.../paragraph/RichParagraph.kt`）。
  - `paragraphStyle: ParagraphStyle` 存对齐等段落样式。
  - `type: ParagraphType` 记录列表/标题等块级类型。
  - `children: List<RichSpan>` 保存段内 span 树。
- **RichSpan / RichSpanStyle**：
  - `RichSpan` 保存文本 + spanStyle + richSpanStyle（如 Link、Code）。

### 对齐如何实现
- `toggleParagraphStyle(ParagraphStyle(textAlign = ...))` 直接修改当前段落或选区内段落的 `paragraphStyle`。
- 修改后会 `updateAnnotatedString()`，并刷新当前段落样式。

相关代码：
- `RichTextState.toggleParagraphStyle`（`RichTextState.kt`）
- `RichParagraph.paragraphStyle`（`RichParagraph.kt`）

### Markdown 解析/导出如何实现
- **解析**：`RichTextStateMarkdownParser.encode(input)`  
  - 使用 `org.intellij.markdown` 解析 AST。  
  - block 元素映射为 `ParagraphType`（有序/无序列表）。  
  - inline 元素映射为 `SpanStyle` + `RichSpanStyle`（Link/Code/Image）。  
- **导出**：`RichTextStateMarkdownParser.decode(state)`  
  - 递归遍历 `RichParagraph` 与 `RichSpan`，生成 Markdown。  
  - Link → `[text](url)`；Code → `` `code` ``；Underline → `<u>`.

相关代码：
- `parser/markdown/RichTextStateMarkdownParser.kt`

### 编辑器渲染如何实现
- `BasicRichTextEditor` 是 `BasicTextField` 的包装（`ui/BasicRichTextEditor.kt`）。
  - 用 `RichTextState` 驱动 `TextFieldValue` 与 `AnnotatedString`。
  - 通过 `visualTransformation` 与 `decorationBox` 实现可定制 UI。

### 我们如何调用（结合 MyHub）
1. 编辑态：`rememberRichTextState()` + `RichTextEditor(state = state)`  
2. 工具条绑定：
   - `toggleSpanStyle(SpanStyle(fontWeight = Bold))`
   - `toggleSpanStyle(SpanStyle(fontStyle = Italic))`
   - `toggleParagraphStyle(ParagraphStyle(textAlign = ...))`
3. Markdown：`state.toMarkdown()` / `state.setMarkdown(markdown)`


---

## 4. 推荐路线（分阶段）

### Phase 1（现在）：Markdown + 对齐元数据
- **格式**：Markdown（CommonMark + 必要扩展）。
- **存储**：
  - `rich_text_markdown`：正文
  - `rich_text_align`：对齐元数据
- **渲染**：Markdown 解析 → Compose 渲染。
- **优点**：落地快；diff 友好；AI 友好。
- **缺点**：对齐不在 Markdown 内。

---

## 4.1 Markdown 输入/输出的具体落地（满足对齐/链接/代码）

> 约束：输入是 Markdown，输出也必须是 Markdown，并且可还原 UI。

### 对齐方式（Markdown 原生不支持）
**推荐做法：Markdown + 对齐元数据**
- **存储**：
  - `md`: Markdown 正文
  - `align`: 段落级对齐元数据（left / center / right）
- **输出**：`md2 = toMarkdown()`，`align` 单独存储
- **回显**：`setMarkdown(md2)` 后按段落索引应用 `ParagraphStyle(textAlign = ...)`
- **优点**：不污染 Markdown；渲染一致
- **注意**：需要维护段落索引映射

**备选（不推荐）：嵌入 HTML**
- 例：`<p style="text-align:center">...</p>`
- 风险：Markdown 渲染器可能过滤 HTML；跨端不一致

### Link（Markdown 原生支持）
- **Markdown**：`[label](https://example.com)`
- **输出**：`toMarkdown()` 保持链接结构
- **回显**：`setMarkdown(md2)` 自动恢复链接样式与点击行为
- **注意**：需要统一 URL 校验与点击处理（可由 UI 层接管）

### 代码（Markdown 原生支持）
**行内代码**
- 语法：`` `inline code` ``
- 输出：`toMarkdown()` 保持

**代码块**
- 语法：  
  ````
  ```js
  const a = 1;
  ```
  ````
- 输出：`toMarkdown()` 保持
- 回显：解析语言标记（如 `js`/`kotlin`）用于代码高亮

---

### Phase 2（后续）：结构化 JSON Schema
- **格式**：JSON 节点树（block + span + marks）。
- **渲染**：自研 Compose 渲染器。
- **收益**：结构可控；易扩展。
- **迁移**：Markdown → Schema。

---

## 5. 最小 Schema（如需 JSON）

```json
{
  "type": "doc",
  "blocks": [
    {
      "type": "paragraph",
      "align": "left",
      "spans": [
        { "text": "The details ", "marks": [] },
        { "text": "make", "marks": ["italic"] },
        { "text": " the design.", "marks": [] }
      ]
    }
  ]
}
```

---

## 6. 快速对比矩阵

| Option | Fidelity | Dev Cost | Interop | AI-Friendly | KMP Fit |
|--------|----------|----------|---------|-------------|---------|
| HTML | Medium | Medium | High | Medium | Medium |
| Markdown | Medium | Low | High | High | High |
| JSON Schema | High | High | Medium | Medium | High |
| Delta/CRDT | High | Very High | Low | Low | Low |
| Native spans | High | Medium | Low | Medium | Low |

---

## 7. 推荐结论

- **Start with Markdown + alignment metadata** for Capture Review rich text.
- Keep conversion hooks for future JSON schema adoption.
- Use the preview card to validate rendering fidelity early.

---

## 8. 下一步

1. 决定 canonical 格式（Markdown / JSON）。
2. 定义 Capture Review 的最小富文本模型。
3. 在 KMP 实现序列化/反序列化。
4. 统一编辑态与只读态渲染。

# Code Block Design Calibration - Technical Implementation

> 目标：定义 Review Editing Focused 场景下代码块的结构、视觉、交互与实现边界，并给出行业常见方案调研结论与可落地的实现要点。

---

## 1. Scope & Non-Goals

- In scope: code block UI (header, language selector, actions, body), read-only behavior, copy/format/expand, accessibility.
- Out of scope: full code editor, syntax parser implementation, server-side formatting service.

---

## 2. Industry Survey (Common Patterns)

**参考产品与特点**

- GitHub / Gist: 语言标识 + 行号 + 复制；暗色内容区；等宽字体层级清晰。
- VS Code / JetBrains: 编辑器范式，头部操作统一在右侧；行号强对齐；语法高亮标准化。
- Notion: 轻量代码块，语言切换在左，动作在右；默认是内容块而非编辑器。
- Linear / Slack: 内容区略暗，行号弱化；强调可读性与复制。
- Figma / Framer (Docs): 嵌入式内容感，不抢主焦点。

**共性结论**

- 结构：Header（语言 + actions） + Body（code + gutter）。
- 交互：Copy 必备；Format/Expand 视场景可选。
- 视觉：暗色内容区 + 弱行号 + 轻分隔线。
- 语义：默认是“可读内容”，不抢主编辑焦点。
- 语法高亮：尽量对齐 GitHub/VS Code 色彩角色，避免过艳。

---

## 3. UI Anatomy

```
Header: Language pill + caret | Actions (Format / Copy / Expand)
Body: Gutter (line numbers) + Code content (syntax highlight)
Footer: (Optional) metadata / inline hint
```

---

## 4. Visual Specification

- Header
  - Height: 36-40px
  - Background: background/30
  - Divider: 1px outline/20
  - Language pill: secondaryContainer + rounded-full + text-xs uppercase
  - Action icons: 18px, hover text-primary
- Body
  - Background: #0A0A0C
  - Line numbers: onSurfaceVariant/40
  - Font: monospace, 13-14px, line-height 20-22px
  - Padding: 12px vertical, 16px horizontal
- Contrast
  - Code text vs background: >= 4.5:1
  - Line numbers vs background: >= 2.5:1

---

## 5. Behavior Specification

- Default: read-only, selectable text.
- Copy: one-click copy of code body; show non-blocking toast.
- Format: enabled only for known languages; hide/disable for unknown.
- Expand: show when > 12 lines; expand preserves scroll position.
- Language switch: uses GitHub language names.
- Focus: do not capture editor focus unless user explicitly enters edit mode.

---

## 6. Responsiveness

- < 960px: header actions collapse into overflow menu.
- < 720px: hide line numbers; reduce padding to 12px.
- < 480px: language pill becomes icon + tooltip.

---

## 7. Accessibility

- All action buttons have aria-label.
- Copy success toast is screen-reader friendly.
- Color is not the only signal for syntax; highlight remains readable in grayscale.

---

## 8. Implementation Notes

### 8.1 Component Contract

- Props
  - language: string
  - code: string
  - readOnly: boolean (default true)
  - showLineNumbers: boolean (default true)
  - onCopy: () => void
  - onFormat?: () => void
  - onExpand?: () => void
- State
  - isExpanded: boolean
  - isCopySuccess: boolean

### 8.2 Formatting Rules

- Only enable format when language is in allowlist.
- Keep formatting client-side if possible; avoid blocking UI.

### 8.3 Line Numbers

- Generated client-side by splitting lines; avoid DOM reflow by using CSS counters when possible.

### 8.4 Syntax Highlight

- Use a shared theme aligned with GitHub Dark (or existing system theme).
- Highlight should not override selection color.

### 8.5 如何实现代码高亮

**推荐实现路径（静态高亮，首选）**

- 方案：基于语法高亮库做静态渲染（HTML spans + class）。
- 理由：代码块在该场景是只读内容，静态高亮性能好、交互干扰少。

**实现步骤（框架无关）**

1. 选定高亮库并配置语言集（按需加载）。
2. 输入 `language` 与 `code`，输出高亮后的 HTML。
3. 使用安全渲染（仅允许高亮库生成的 HTML）。
4. 为 code 容器应用主题样式（token 映射到系统颜色角色）。

**可选库**

- `shiki`：基于 VS Code 语法高亮；输出 HTML，颜色质量稳定；适合静态渲染。
- `prismjs`：体积小；支持按需语言；可客户端运行。
- `highlight.js`：上手快；自动识别语言，但不建议在这里自动识别（容易误判）。

**关键实现要点**

- 避免在运行时做自动语言识别，优先用用户选择的 `language`。
- 高亮输出内容需要与 selection 高亮不冲突（不要覆盖 `::selection`）。
- 需要降级路径：未匹配语言时使用纯文本渲染。

### 8.6 如何实现格式化（Format）

**策略**

- 仅对已知语言启用格式化，未知语言禁用或隐藏。
- 优先客户端格式化，避免阻塞主线程与网络依赖。
- 统一通过 `onFormat` 触发，保持 UI 与实现解耦。

**实现路径**

- JS/TS/JSON/HTML/CSS：使用 `prettier`（或现有格式化服务）做同步/异步格式化。
- Markdown：可选 `prettier` 或 `remark` 生态。
- 其他语言：明确 allowlist，超出范围保持只读不格式化。

**实现步骤（客户端示例）**

1. 根据 `language` 映射格式化器（formatter registry）。
2. 点击 Format -> 调用对应 formatter。
3. 输出格式化后的 code 文本并更新渲染。
4. 格式化失败时显示轻量 toast，不阻塞编辑。

**注意事项**

- 长代码应在 Web Worker 中格式化，避免主线程卡顿。
- 格式化不应改变语言选择或行号逻辑。
- 与 Expand 兼容：格式化后保持展开状态与滚动位置。

---

## 9. QA Checklist

- [ ] Header + Body structure present; language + copy visible.
- [ ] Line numbers readable but subdued.
- [ ] Code block does not steal main editor focus.
- [ ] Long code supports expand/scroll.
- [ ] Contrast targets met.

---

## 10. AI-Based Formatting Strategy

> 结论：格式化交给 AI 实现，前端只负责触发与展示结果；高亮保持本地实现。

### 10.1 Behavior Rules

- Format 按钮仅在 allowlist 语言下显示或可用。
- 点击 Format -> 发送 `language + code` 给 AI 服务。
- AI 返回格式化文本后，替换当前 code 并保留展开/滚动状态。
- 失败或超时：保持原文，展示轻量提示，不阻塞编辑。

### 10.2 Safety & Limits

- 设置请求超时与重试策略（例如 3-5 秒超时，1 次重试）。
- AI 输出长度上限与截断策略，避免异常膨胀。
- 输出需为纯文本，不允许注入标记或富文本。

### 10.3 UI Contract

- 格式化过程中显示轻量 loading 状态（按钮内旋转图标或 disabled 状态）。
- 完成后展示一次性 toast。

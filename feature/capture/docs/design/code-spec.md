# Capture UI Code Spec

> 本文档承载 Capture 模块的实现级视觉与布局参数（像素、间距、颜色 token、交互态样式）。
> 状态语义定义请以各状态 `index.md` 为准。

---

<a id="ready-input-idle"></a>

## CAPTURE_READY_INPUT_IDLE / CAPTURE_READY_INPUT_FOCUSED

### Color Role / Token

| UI 区域               | Color Role / Token                 |
|---------------------|------------------------------------|
| App Background      | `background-dark` `#131316`        |
| Main Content        | `surface-dark` `#1C1B1F`           |
| AppBar Background   | `surface-dark` `#1C1B1F`           |
| Side Preview Panel  | `surface-variant` `#2D2E33`        |
| Border / Divider    | `outline` `#938F99`                |
| Focus Ring / Accent | `primary` `#B4A3FF`（低强调）           |
| Placeholder Text    | `on-surface-variant` `#CAC4D0`     |
| Add Media Button BG | `secondary-container` `#4A4458`    |
| Add Media Text      | `on-secondary-container` `#E8DEF8` |

### Disabled CTA

- 容器：`surface-variant/40`
- 文案/图标：`on-surface-variant`
- 边框：`outline`
- 透明度：`0.5`
- 交互：`cursor-not-allowed`

### Pixel-level Layout Detail

- Header 水平内边距：`24px`（large 屏 `40px`）
- 主内容最大宽度：`max-w-3xl`（约 `768px`）
- 主内容水平内边距：`24px`（large 屏 `40px`）
- 输入区底部 padding：`pb-20`（约 `80px`）
- 输入区行距：`leading-tight`
- 输入区下划线厚度：`1px`（基础）+ `2px`（聚焦）
- Add Media 按钮圆角：`2xl`（约 `24px`）
- Add Media 内部图标圆：`8px` 半径（`w-8 h-8`）
- Side Panel 宽度：`420px`
- Side Panel 内边距：`32px`
- Side Panel 顶部渐变高度：`256px`
- Preview icon 容器：`80px` 圆形
- Preview icon glow：`blur-2xl`
- 右侧卡片阴影：`shadow-2xl`

### Constraints

- 禁止大面积使用 `primary` 作为背景。

<a id="motion-ready-input"></a>
### Motion Parameters

- Focus underline expand: `700ms`, `ease-out`, width `0 -> 100%`.
- Focus underline collapse: `500ms`, `ease-in-out`, width `100% -> 0`.
- Hover transition (buttons/chips): `120-180ms`, `ease-out`.
- 禁止在 Ready 状态使用无限循环加载动效。

---

<a id="ai-processing-input-locked"></a>

## CAPTURE_AI_PROCESSING_INPUT_LOCKED

- Frozen 输入区：保持可读，禁止可编辑反馈。
- Overlay 叙事层优先于编辑控件。
- 动效：弱节奏、无布局重排。

<a id="motion-ai-processing-input-locked"></a>
### Motion Parameters

- Processing indicator: `900-1200ms` loop, `ease-in-out`.
- Overlay enter: `180-220ms`, alpha `0 -> 1`.
- Main content dim: `150-200ms`, alpha target `0.1~0.4`.
- 禁止内容位移、区域重排和高频闪烁。

---

<a id="ai-processing-result-failed"></a>

## CAPTURE_AI_PROCESSING_RESULT_FAILED

- 错误提示层需清晰可见且可关闭。
- Retry 为高可见恢复入口。
- 不使用恐慌式高饱和失败视觉。

<a id="motion-ai-processing-result-failed"></a>
### Motion Parameters

- Error toast/banner enter: `180ms`, `ease-out`, y `8 -> 0`.
- Error toast/banner exit: `140ms`, `ease-in`.
- Retry/close hover: `120ms`, color/alpha transition.
- 禁止闪烁、抖动和强振幅位移。

---

<a id="review-editing-focused"></a>

## CAPTURE_REVIEW_EDITING_FOCUSED

- 编辑区边框层级：默认 < hover < selected。
- 预览区颜色与样式配置联动。
- 富文本、代码块、媒体卡片遵循组件规范。

<a id="motion-review-editing-focused"></a>
### Motion Parameters

- Hover transition (card/chip/action): `100-160ms`, `ease-out`.
- Focus ring/border transition: `120-180ms`.
- Selection emphasis (content block): `160-220ms` shadow/border.
- 禁止持续处理态动画覆盖编辑交互。

---

<a id="post-waiting-locked"></a>

## CAPTURE_POST_WAITING_LOCKED

- 主编辑区冻结且降权。
- 提交按钮进入 waiting 样式且不可重复触发。
- 仅允许弱节奏等待反馈。

<a id="motion-post-waiting-locked"></a>
### Motion Parameters

- Waiting indicator: `1000-1400ms` loop, low amplitude.
- CTA state switch (enabled -> waiting): `120-180ms`.
- Overlay/status enter: `180-220ms`.
- 禁止布局抖动、强节奏闪烁、重复触发反馈。

---

<a id="post-result-success"></a>

## CAPTURE_POST_RESULT_SUCCESS

- 成功语义层可见，处理语义层移除。
- 主内容降权、不可编辑。
- 出口动作（自动/手动）视觉一致。

<a id="motion-post-result-success"></a>
### Motion Parameters

- Success feedback enter: `180-240ms`, scale `0.96 -> 1`, alpha `0 -> 1`.
- Optional auto-exit hint pulse: `1400ms` low intensity.
- Exit transition: `180-240ms` fade.
- 禁止继续使用 processing loop 动画。

---

<a id="post-result-failed"></a>

## CAPTURE_POST_RESULT_FAILED

- 失败提示 + Retry + Back to Review 路径并存。
- 保留上下文，不清空编辑内容。
- 错误提示层不压制主恢复任务。

<a id="motion-post-result-failed"></a>
### Motion Parameters

- Failure prompt enter: `180ms`, `ease-out`, y `8 -> 0`.
- Retry/back action hover: `120-160ms`.
- Prompt dismiss: `140ms`, `ease-in`.
- 禁止恐慌式闪烁和强振动反馈。

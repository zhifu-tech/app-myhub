# AI 捕获图片输入识别与 Prompt 优化

## 本轮目标

本轮同时完成三件事：

1. 重写 AI 生图 prompt 生成策略，去掉“卡片封面”导向，改成更通用、更适合预览的大图语义。
2. 在 AI 捕获入口增加“输入图片”能力，支持拍照或上传图片后直接识别内容并生成草稿。
3. 在两条链路上分别做三轮体验优化：
    - AI 生成图：9 项
    - 图片输入识别：9 项

## 业内 SOP

### 文生图 SOP

结合 `x/flux2-klein` 官方模型页的当前建议，以及主流文生图工作流，当前更稳的 SOP 是：

1. 先做“语义整理”，不要把原始草稿字段机械拼成 `Title / Summary / Tags`。
2. prompt 要具体，应该同时包含主体、场景、构图、光线、材质、氛围、色彩。
3. 如果图里必须出现文字，只能写“明确的原文”，否则默认不要求文字。
4. 颜色如果重要，最好直接给出 hex 颜色值，减少风格漂移。
5. 默认按 `1024x1024` 的方图心智构图，适配缩略图和大图预览。
6. 最终 prompt 最好先经过一层“prompt writer”模型重写，再送去图像模型。

这套 SOP 对应两阶段：

1. Deterministic prompt builder：把草稿压成稳定的图像语义骨架。
2. AI prompt refiner：把骨架再改写成更像 `flux` 风格的高质量英文 prompt。

### 图片输入识别 SOP

行业上更稳的图片输入卡片化流程是：

1. 用户先给文字或图片，不强迫先选模式。
2. 如果先给图片，先把图片本身保存进草稿和聊天历史，避免“识别后图片消失”。
3. 多模态分析优先识别：
    - 主体
    - 场景
    - 可见文字
    - 地点线索
    - 人物 / 物品 / 事件关系
4. 输出时直接补全 `title / summary / tags / captureType / sourceText / location`。
5. 首张输入图片默认成为封面，后续允许替换或补图。

## 本轮实现

### 1. 生图 Prompt 重构

- 取消“Create a polished cover image for a knowledge capture card”这类 UI/卡片导向表述。
- 不再直接把 `title / summary / sourceText / location / tags` 机械拼成标签串。
- 改成自然语言图像 brief：
    - 主体
    - 叙事
    - 场景
    - 标签线索
    - 类型对应的视觉方向
    - hex 调色板
- 默认加入：
    - 单一焦点
    - 方图友好构图
    - 禁止 UI / collage / watermark / logo
    - 非明确要求时不渲染文字

### 2. AI Prompt Refiner

- 新增一层 `CaptureImagePromptRefiner`。
- 先把草稿转成结构化 brief，再交给文本模型重写为更适合 `flux` 的英文 prompt。
- 若 refiner 失败，则自动回退到 deterministic prompt，不阻断生图。

### 3. 图片输入识别

- `MediaPicker` 增加：
    - 选图片
    - 拍照
- 用户在 AI 捕获页进入后，可以：
    - 输入文字
    - 上传图片
    - 拍照
- 若图片作为第一次输入：
    - 图片先进入聊天历史
    - 图片先写入 `draft.mediaAssets`
    - 再触发多模态分析
- 分析结果会直接补全草稿字段。
- 首张图片默认成为封面，后续仍可换图或补图。

### 4. 多模态路由

- 图片分析请求带 `media_inputs`。
- 直连模式下，文本分析模型和视觉模型分开配置。
- 当请求包含图片时，当前实现优先走 `directVisionModel`，避免服务端网关尚未跟上时卡住主流程。
- 本地直连 API Key 校验也做了放宽，本地 Ollama 类 endpoint 不再强制要求 key。

## 体验迭代

### AI 生成图 9 项优化

1. 从“封面图”话术改成“更贴合内容的图片”，减少单一使用场景暗示。
2. prompt builder 改成自然语言图像 brief，提升画面准确度。
3. 新增 AI prompt refiner，二次优化 prompt 质量。
4. 默认输出尺寸从低分辨率改成 `1024x1024`。
5. 生成前新增“正在优化提示词”进度提示，减少等待空窗。
6. media panel 的主次动作重排为“拍照 / 上传 / AI 生成”，符合真实 SOP。
7. media panel 支持文案明确说明“第一张图默认作为封面”。
8. 生成结果继续走本地资产链路，和上传图保持完全一致。
9. 生成失败时保持当前 media panel，不会把用户踢离当前编辑上下文。

### 图片输入识别 9 项优化

1. bootstrap 改成“输入文字或拍照 / 上传图片”双入口。
2. 输入框占位改成“输入文字，或直接拍照 / 上传图片”。
3. 底部输入区增加快捷 chips，直接暴露“拍照识别 / 上传图片”。
4. media 缺失步骤新增拍照动作，不再只有上传。
5. 图片第一次输入时先保留为用户图片消息，再开始分析。
6. 多模态分析 prompt 收紧，增强 OCR / 场景 / 类型判断的稳定性。
7. patch 结果支持 `captureType / sourceText / location`，不只 title/summary/tags。
8. 图片分析请求优先走视觉模型，减少服务端未适配造成的落空。
9. 移动端拍照走平台 actual，桌面端安全回退，不在 commonMain 硬绑移动端 API。

## 代码落点

### Prompt 与生图

- `feature/ai/.../CaptureImagePromptBuilder.kt`
- `feature/ai/.../CaptureImagePromptRefiner.kt`
- `feature/ai/.../ToolCommandRegistryImpl.kt`
- `feature/ai/.../ProviderImageGeneration.kt`

### 图片输入识别

- `component/media/.../MediaPicker.kt`
- `component/media/.../DefaultMediaPicker.kt`
- `component/media/.../CapturePhotoPicker.*.kt`
- `feature/ai/.../ProviderAnalysis.kt`
- `feature/ai/.../DirectApiProviderClient.kt`
- `feature/ai/.../ProviderAnalysisPrompts.kt`
- `feature/ai/.../PatchApplier.kt`
- `feature/ai/.../CaptureOrchestrator.kt`

### 交互与设置

- `feature/ai/.../ChatInputBar.kt`
- `feature/ai/.../ActionSectionItem.kt`
- `feature/settings/.../AIProviderSettingDialog.kt`
- `feature/settings/.../AIProviderSettingItem.kt`
- `ui/state/.../ProviderState.kt`

## 风险与后续

### 当前限制

- 桌面 JVM 目前不提供真实相机拍照，只做安全回退；主要拍照体验仍在 Android / iOS。
- 视觉模型依赖当前 direct endpoint 的多模态能力；如果 provider 本身不支持图像输入，请求仍会失败。
- OCR 和地点推断仍受底层模型影响，复杂场景下只能做到“谨慎猜测”。

### 下一步建议

1. 给图片输入增加“识别中”专属系统卡片，而不是只复用 reasoning 状态。
2. 让用户在图片识别前先裁切 / 选主图。
3. 把 prompt refiner 的最终 prompt 写入调试日志或开发者面板，方便继续迭代效果。

## 参考资料

- Flux2 Klein 模型页  
  https://ollama.com/x/flux2-klein
- FileKit Camera Picker  
  https://filekit.mintlify.app/dialogs/camera-picker
- Ollama Vision 能力说明  
  https://docs.ollama.com/capabilities/vision
- Ollama OpenAI 兼容接口  
  https://docs.ollama.com/api/openai-compatibility
- Ollama Thinking
  https://docs.ollama.com/capabilities/thinking
- Ollama OpenAI Compatibility:
  https://docs.ollama.com/api/openai-compatibility

# AI 捕获文生图 SOP 与多模态落地方案

## 目标

在 AI 捕获流程里，当系统判断当前草稿缺少图片时，不再只允许用户从本地上传，而是同时支持：

1. 一键 AI 生成图片
2. 成功后自动写入本地媒体链路，和手动上传的图片使用同一套 `CaptureMediaAsset`
3. 失败时不打断当前流程，直接回退到“请上传图片”

这份方案先落 SOP，再按同一方案落代码，保证交互、provider 调用、本地存储、失败回退是同一套闭环。

## 行业 SOP

### 1. 入口策略

- 入口放在“补图”步骤里，和“上传图片”并列。
- 默认不要求用户额外写 prompt，先提供“一键生成图片”。
- 若后续要扩展高级能力，再增加“继续改图 / 参考图生成 / 局部编辑”。

原因：

- 在 capture 场景里，用户已经提供了标题、摘要、正文、标签，这些内容本身就是最稳定的图像语义来源。
- 让用户再写一遍 prompt，会破坏“先捕获，再完善”的主流程。

### 2. Prompt 生成 SOP

文生图不是直接把原文整段丢给模型，而是先做一层“封面 prompt 整理”。

建议顺序：

1. 取 `title / summary / sourceText / tags / captureType`
2. 先提炼一个主视觉主题
3. 再补上构图约束
4. 再补上风格约束
5. 最后补上负向约束

最低可落的 prompt 结构：

```text
主主题 + 场景主体
构图要求：适合作为卡片封面，主体清晰，避免过密，预留裁切安全区
风格要求：根据 captureType 选择写实 / 编辑感插画 / 轻视觉化
负向约束：不要文字、不要水印、不要 logo、不要拼贴 UI
```

### 3. 出图参数 SOP

当前阶段建议：

- 一次只生成 1 张，避免用户选择成本暴涨
- 优先中等质量，先保证响应时间
- 输出横图，兼容卡片封面裁切
- 结果必须立即落本地，不依赖 provider 临时 URL

当前项目建议参数：

- `n = 1`
- `quality = medium`
- `size = 1536x1024`
- `output_format = png`

这样做的原因：

- 预览区封面是卡片化展示，横图更适合后续 4:3 裁切
- 中等质量在体验和成本之间更平衡
- 一次 1 张最适合 chat-like capture SOP，减少分叉决策

### 4. 结果接入 SOP

生成结果不能只保留 provider URL。

标准做法：

1. 拿到 base64 或远程 URL
2. 立即下载或解码成 bytes
3. 写入本地 managed 文件
4. 转成统一 `CaptureMediaAsset`
5. 追加到聊天历史，用户能感知“图片已经真的进来了”
6. 草稿只认本地文件 URI

原因：

- provider 的临时 URL 常常会过期
- capture 流程里后续还要预览、发布、缩略图、再次编辑
- 本地统一落库后，上传图和生成图可以共用同一套组件

### 5. 失败回退 SOP

文生图失败时，不应该把用户带到错误页，也不应该中断当前编辑卡。

标准回退：

1. 给一条简短失败提示
2. 保留当前 media action 卡
3. 继续显示“上传图片”
4. 如果还有“生成图片”能力，也保留再次尝试入口

目标是：

- 失败不是流程终点
- 用户仍然有下一步可做
- 上传图永远是兜底方案

### 6. 多模态 SOP

行业上比较稳定的多模态接法不是把所有能力挤进一个端点，而是拆成三层：

#### Stage 1: Text-to-Image

- 输入：当前草稿文本语义
- 输出：生成 1 张封面图
- 用在“补图”步骤

#### Stage 2: Image-to-Image / Edit

- 输入：用户现有图片 + 补充指令
- 输出：改图 / 扩图 / 换风格
- 用在“调图片”步骤

#### Stage 3: Multi-turn multimodal refinement

- 输入：文本 + 参考图 + 多轮反馈
- 输出：持续修图
- 适合后续高级工作流，不适合当前第一版 capture 主流程

本次实现只落 Stage 1，但设计上保留 Stage 2/3 的扩展位。

## 本项目落地决策

### 1. 交互

- 在 media 缺失步骤新增 `生成图片`
- 用户点击后不需要额外输入 prompt
- 系统基于当前草稿自动合成 prompt
- 成功则：
    - 写入本地
    - 作为图片消息进入聊天历史
    - 同步进入 `draft.mediaAssets`
- 失败则：
    - 给出友好提示
    - 保留上传入口

### 2. 架构

- 不把文生图塞进 `ProviderAnalysisExecutor`
- 新增独立 `ProviderImageGenerationExecutor`
- 复用现有 provider mode / config / router
- 文本分析模型和图片生成模型分开配置

原因：

- 文本分析是 `chat/completions`
- 文生图是 `images/generations`
- 两者的模型、参数、失败模式都不同，混在一起后续会失控

### 3. Provider 策略

- 首选当前路由决策出来的 provider
- 若当前 provider 为 `server_gateway` 且出图失败，可回退尝试 `direct_api`
- 仍失败则回退上传

这保证：

- 不破坏当前 provider routing 逻辑
- 但又不会因为 server 端暂未实现图片生成而完全卡死

### 4. 本地存储

- 新增 `MediaFileStore.saveGeneratedImage(...)`
- 非 Web 端直接写到 app 内部目录
- Web 端先用 data URL 兜底
- 统一产出 `CaptureMediaAsset`

### 5. Prompt 合成规则

当前版本按下面规则自动生成：

- 优先使用 `title`
- 再补 `summary`
- 再补 `sourceText` 的前段关键信息
- 再补 `tags`
- 再根据 `captureType` 选择偏写实或偏视觉化的风格指令
- 永远追加：
    - 适合作为卡片封面
    - 主体清晰
    - 不要文字 / 水印 / logo / UI

## 与官方能力的对齐

本方案参考了当前主流官方文档中的一致结论：

- OpenAI Image API 支持独立的图像生成端点，也支持在多模态上下文里做图片生成
- OpenAI 的 `gpt-image` 系列是原生多模态模型，适合文本或图文联合生成
- 图片结果不应依赖临时 URL，需要立即缓存到本地
- Google Vertex AI 的 Gemini 图片生成文档也采用“文本生成图片 + 后续编辑 + 最佳实践”分层方式

## 本次实施范围

本次只落：

1. capture 流程里新增 `生成图片`
2. direct provider 文生图调用
3. server gateway 预留图片生成接口
4. 成功落本地
5. 失败回退上传
6. 文案、多语言、设置项、聊天历史同步

不在本次范围：

- 局部重绘
- 参考图编辑
- 多图候选选择器
- 风格模板库

## 参考资料

- OpenAI Image generation guide  
  https://developers.openai.com/api/docs/guides/image-generation
- OpenAI Images and vision guide  
  https://developers.openai.com/api/docs/guides/images-vision
- OpenAI GPT Image API help  
  https://help.openai.com/en/articles/11128753
- OpenAI model docs: GPT Image 1 / 1.5  
  https://developers.openai.com/api/docs/models/gpt-image-1  
  https://developers.openai.com/api/docs/models/gpt-image-1.5
- Google Vertex AI image generation with Gemini  
  https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/image-generation

- Ollama 官方文生图博客，2026 年 1 月 20 日：/api/generate 支持 image generation models
  https://ollama.com/blog/image-generation
- Ollama 官方 API 文档：image generation 走标准 /api/generate，流式返回 completed/total，最终返回 image
  https://github.com/ollama/ollama/blob/main/docs/api.md
- x/flux2-klein 官方模型页
  https://ollama.com/x/flux2-klein

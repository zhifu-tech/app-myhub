# AI 捕获媒体九宫格、视频关键帧与中断 SOP

## 本轮目标

本轮主要补齐三类能力：

1. 上传媒体后的识别过程支持中断，用户不再只能被动等待。
2. 图片、多图、视频、关键帧统一进入同一套媒体展示与预览体系。
3. 多图 / 多关键帧分析时，引入稳定的冲突归一规则，避免模型在候选字段之间来回振荡。

## 业内 SOP

### 1. 媒体分析的中断 SOP

行业里更稳的做法不是“上传后一直转圈直到结束”，而是：

1. 一旦开始识别，就给用户明确的进行中状态。
2. 进行中状态必须提供停止入口。
3. 停止后要保留已上传媒体，不能把用户输入吞掉。
4. 停止后要告诉用户下一步可做什么：
   - 重新上传
   - 补一段文字
   - 补一张封面图

当前落地：

1. 会话上下文增加 `analysisRunning / analysisIncludesMedia`。
2. reasoning live card 在媒体识别时显示“停止分析”。
3. 取消后保留已上传媒体，同时回到可继续编辑的状态。

### 2. 多图 / 多关键帧冲突归一 SOP

多模态输入最容易出问题的地方不是识别不到，而是识别到多个候选后反复推理。当前规则固定为：

1. 先抽取所有媒体的共同主题，再写草稿。
2. `title` 以第一张图或第一段关键帧的稳定主体为锚点。
3. 如果不同媒体冲突，只保留共同事实进入 `summary / sourceText`。
4. 不在多个标题候选之间来回切换。
5. 同一视频抽出来的关键帧默认视为同一事件，不拆成多个主题。
6. 地点、人物名、海报标题这类信息只有在 OCR 或视觉线索足够明确时才写入。
7. 没把握时宁可留空，也不做高风险猜测。

这些规则已经同步进分析 prompt，避免模型围绕冲突字段无限自我修正。

### 3. 九宫格展示 SOP

媒体展示统一参考微信朋友圈的九宫格心智：

1. 1 条媒体：大图展示。
2. 2 条 / 4 条媒体：2 列排布。
3. 其他数量：3 列排布。
4. 最多先展示 9 个格子。
5. 超出 9 个时，最后一个格子显示余量遮罩。
6. 视频格子显示播放标识。

当前 `MediaGridNine` 是共享组件，dashboard、AI 捕获消息、AI 捕获编辑卡、预览页都复用同一套规则。

### 4. 预览边界 SOP

媒体预览最容易做乱的是“从哪里点开，就应该能滑到哪里”。当前边界固定为：

1. Dashboard 卡片封面点击：
   - 进入媒体预览
   - 允许在当前卡片内左右滑
   - 如果从 dashboard 打开，允许跨卡片滑动到上一张 / 下一张卡片的媒体
2. Dashboard 卡片非封面区域点击：
   - 进入单卡预览
3. 单卡预览中的封面点击：
   - 进入当前卡片媒体预览
   - 只允许在当前卡片内滑动
   - 不允许跨卡片滑动
4. AI 捕获中的预览卡封面点击：
   - 进入当前草稿媒体预览
   - 只允许在当前草稿内滑动

### 5. 视频关键帧预算 SOP

当前最大媒体预算为 `9`，规则如下：

1. 图片先占用预算。
2. 视频拿剩余预算提关键帧。
3. 多个视频平均分配剩余预算。
4. 不能整除时，余量优先给前面的影片。
5. 如果设备能拿到时长，就按均匀时间间隔抽帧。
6. 如果拿不到关键帧：
   - 视频资源仍会保留在草稿里
   - 会话继续前进
   - 给用户明确提示，建议补文字或补封面图

## 本轮落地

### 1. 共享媒体模型

`ContentCard` 已从单封面升级成 `cover + media[]`，让 dashboard、preview、AI 草稿都能共享同一套媒体源。

### 2. 共享九宫格与图库

新增：

1. `MediaGridNine`
2. `MediaGalleryDialog`

作用：

1. 统一九宫格样式
2. 统一图片 / 视频缩略图展示
3. 统一媒体预览与滑动逻辑

### 3. Dashboard 行为重构

1. 卡片封面改为媒体入口。
2. 文本区保持卡片预览入口。
3. Dashboard 的媒体预览支持跨卡片滑动。

### 4. 预览页行为重构

1. 单卡预览封面支持点开媒体。
2. 单卡预览内只能浏览当前卡片媒体。
3. 不会误滑到其他卡片。

### 5. AI 捕获消息 / 编辑卡统一九宫格

1. 用户媒体消息改成九宫格。
2. 媒体编辑卡改成九宫格。
3. 同一套媒体图库支持从消息或编辑卡点开。
4. 编辑卡上的移除按钮直接覆盖在媒体格子上。

### 6. 媒体分析可取消

1. 新增运行中状态。
2. 新增取消入口。
3. 取消后不清空媒体。
4. 取消后提示“可以调整上传内容后重新开始”。

### 7. 视频关键帧提取

JVM 本地方案：

1. 用 `ffprobe` 读取时长。
2. 用 `ffmpeg` 按平均时间间隔抽帧。
3. 抽出的关键帧和图片一起作为多模态输入。

其他平台当前策略：

1. 先保留视频资源。
2. 若无法抽帧，不阻塞上传。
3. 回退到“保留媒体 + 给出下一步提示”。

## 关键代码落点

### 媒体组件

- `component/media/.../MediaGridNine.kt`
- `component/media/.../MediaGalleryDialog.kt`
- `component/media/.../MediaPreviewDialog.kt`

### Dashboard / Preview

- `feature/dashboard/.../DashboardViewModel.kt`
- `feature/dashboard/.../ContentItemLeading.kt`
- `feature/dashboard/.../ContentItemHost.kt`
- `feature/preview/.../PreviewState.kt`
- `feature/preview/.../Preview.kt`
- `feature/preview/.../PreviewContentCover.kt`

### AI 捕获

- `feature/ai/.../MessageUserItem.kt`
- `feature/ai/.../ActionSectionItem.kt`
- `feature/ai/.../ReasoningCardItem.kt`
- `feature/ai/.../ConversationEngine.kt`
- `feature/ai/.../CaptureOrchestrator.kt`
- `feature/ai/.../ToolCommandRegistryImpl.kt`
- `feature/ai/.../VideoKeyframeExtractor.*.kt`
- `feature/ai/.../ProviderAnalysisPrompts.kt`

## 当前限制

1. JVM 视频关键帧提取依赖本机存在 `ffmpeg / ffprobe`。
2. Android / iOS / Web 这轮先保留视频资源，关键帧提取能力还没做平台原生实现。
3. 视频若没有可分析帧，当前会回退为“保留媒体 + 引导补文字或封面图”。

## 后续建议

1. 给视频格子补时长角标。
2. 对关键帧提取增加开发者调试信息：时长、抽帧点、成功 / 失败原因。
3. 在 AI 捕获编辑卡中补“设为封面”的显式操作，而不是只默认取第一条媒体。

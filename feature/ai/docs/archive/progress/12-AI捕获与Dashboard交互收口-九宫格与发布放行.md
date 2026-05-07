# AI 捕获与 Dashboard 交互收口（九宫格与发布放行）

## 本轮目标

1. 重做媒体九宫格布局，按指定规则支持 1-9 张差异化排布。
2. 收口 Review 阶段 action，避免“先看预览 / 回到复核 / 确认发布”多段重复抢占屏幕。
3. 发布阶段改为“提醒不拦截”，进入最终发布后不再因为预检失败直接阻断。
4. 优化 Dashboard 卡片点击行为与视觉样式，提升媒体预览与卡片预览一致性。

## 主要改动

### 1) 九宫格布局规则重做

`MediaGridNine` 已改为自定义 `Layout` 排版，使用 6x6 基础网格来表达 1、1.5、2、3 的跨度。

关键点：

1. 1 张：全屏大图。
2. 2 张：左右均分。
3. 3 张：左大竖栏 + 右上下两格。
4. 4 张：2x2。
5. 5-8 张：按“主图 + 辅图”混合结构。
6. 9 张：3x3 均匀格子。

代码：

- `component/media/.../MediaGridNine.kt`

### 2) Review Action 合并

原先 `PreviewGuideCard + EditFieldsPanel + PublishPanel` 串行显示，信息冗余且占屏。

现在改为：

1. 合并预览引导与字段编辑为一个 `ReviewActionPanel`。
2. 保留字段可点改的能力，不增加额外“先点修改”一步。
3. `PublishPanel` 独立保留，整体层级更短、更清晰。

代码：

- `feature/ai/.../ActionSectionItem.kt`

### 3) 发布预检改为提醒而不拦截

策略调整：

1. `PublishCard` 不再因为 `prePublishCheck` 失败直接返回 `PRECONDITION_FAILED`。
2. `LocalCardEngine.toPublishedCard()` 不再抛 `pre_publish_check_failed`。
3. 发布内容兜底：`summary/sourceText` 为空时，回退到 `title` 再回退到 `cover`。

这样即使 AI 不可用，只要用户已进入发布阶段，也能完成发布。

代码：

- `feature/ai/.../ToolCommandRegistryImpl.kt`
- `feature/ai/.../LocalCardEngine.kt`

### 4) Dashboard 交互与样式优化

交互修复：

1. 封面区域始终作为媒体入口。
2. 当媒体缺失但存在 `cover.url` 时，封面点击进入媒体预览（支持跨卡片滑动）。
3. 卡片其他区域进入单卡预览。

视觉优化：

1. 卡片圆角升级为 `extraLarge`。
2. 卡片阴影层级提升，按压/悬停态更明显。
3. 列表与网格间距、内容 padding、标题字重做统一增强。

代码：

- `feature/dashboard/.../ContentItemHost.kt`
- `feature/dashboard/.../ContentItemLeading.kt`
- `feature/dashboard/.../ContentGridItem.kt`
- `feature/dashboard/.../ContentListItem.kt`
- `feature/dashboard/.../ContentGrid.kt`
- `feature/dashboard/.../ContentList.kt`
- `feature/dashboard/.../Content.kt`

### 5) 预览封面可点击进入媒体预览

单卡预览中，若无 media 但有 cover URL，点击封面也可进入媒体预览。

代码：

- `feature/preview/.../PreviewState.kt`
- `feature/preview/.../PreviewContentCover.kt`

## 关于 iOS “允许 MyHub 使用无限数据”提示

结论：

1. 当前代码没有新增“请求无限数据权限”的自定义权限调用。
2. 这是 iOS 在应用首次发起联网请求（直连模型/网关）时的系统级蜂窝数据授权提示。
3. 与相机、相册权限无关。

建议：

1. 在首次需要联网前加一条轻提示，告知“将连接 AI 服务进行分析”。
2. 若希望减少首次触发时机，可延后健康检查或首个网络请求到用户明确点击发送后。

## 验证

已执行：

`./gradlew --no-daemon :feature:ai:compileKotlinJvm :feature:dashboard:compileKotlinJvm :feature:preview:compileKotlinJvm :component:media:compileKotlinJvm`

结果：通过。

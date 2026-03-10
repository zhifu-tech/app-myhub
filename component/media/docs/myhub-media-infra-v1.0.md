# Media 组件基础设施方案设计

- **文档版本**：v1.0
- **文档类型**：技术方案设计文档
- **创建日期**：2026-02-06
- **最后更新**：2026-02-06
- **作者**：MyHub Capture Team
- **评审状态**：待评审
- **方案状态**：进行中
- **修改历史**：
  - v1.0 (2026-02-06)：合并媒体选择与播放方案，统一 Media 组件基础设施设计

---

## 目录

1. 背景 / 动机
2. 设计目标
3. 技术选型
4. 架构设计
5. 实现细节
6. 平台注意事项
7. 实施计划
8. 风险评估
9. 测试策略（可选）
10. 附录

---

## 1. 背景 / 动机

Capture 模块需要支持：

- 用户通过点击按钮选择本地媒体（图片/视频）
- 用户通过拖拽将媒体添加到输入区（Desktop）
- 生成可预览缩略图并支持移除
- 提供图片、视频、音频的统一预览体验

现状：已有 UI 入口与部分预览组件，但缺乏跨平台媒体选择、拖拽与统一播放器能力的完整基础设施支持。

---

## 2. 设计目标

### 功能目标

- 支持媒体选择（图片/视频）
- 支持拖拽导入（Desktop）
- 生成缩略图并绑定到 `MediaThumbnail`
- 统一 `MediaItem` 模型用于渲染与上传
- 提供跨平台视频/音频播放预览

### 非功能目标

- 跨平台：Android / iOS / Desktop / Web(wasmJs)
- 与现有架构一致：MVVM + Koin
- 依赖可控，优先复用现有 core 模块

---

## 3. 技术选型

### 3.1 媒体选择与读取（FileKit）

- **结论**：采用 FileKit 作为跨平台媒体选择与文件读取的主方案。
- **原因**：原生 Picker 覆盖多平台、Compose 友好、API 统一，能降低多端实现成本与维护复杂度。

**依赖（统一版本 0.12.0）**

- `io.github.vinceglb:filekit-core:0.12.0`
- `io.github.vinceglb:filekit-dialogs:0.12.0`
- `io.github.vinceglb:filekit-dialogs-compose:0.12.0`
- `io.github.vinceglb:filekit-coil:0.12.0`
- `io.coil-kt.coil3:coil-compose:3.1.0`

**初始化**

- Android：使用 Core 时需在 `Activity` 调用 `FileKit.init(this)`；仅使用 dialogs/compose 可省略。
- Desktop：JVM 端需在 `main` 调用 `FileKit.init(appId = ...)`。

### 3.2 音视频播放（ComposeMultiplatformMediaPlayer）

- **结论**：采用 `ComposeMultiplatformMediaPlayer` 作为跨平台音视频播放库。
- **版本**：`network.chaintech:compose-multiplatform-media-player:1.0.53`

---

## 4. 架构设计

### 4.1 模块与依赖

- `component/media` 统一承载媒体选择与播放基础设施
- 通过 `MediaPicker`、`MediaPreviewer`、`MediaItem`、`MediaThumbnail`、`MediaPreviewDialog` 对外提供能力
- 使用 Koin 模块 `mediaModule` 提供默认实现

### 4.2 数据模型

```kotlin
data class MediaItem(
    val id: String,
    val file: PlatformFile,
    val name: String,
    val isVideo: Boolean
)
```

### 4.3 数据流

```
UI(Add Media / Drag Drop)
 -> FileKit Picker
 -> MediaItem 生成
 -> ViewModel 更新列表
 -> UI 渲染 MediaThumbnail
 -> MediaPreviewDialog 统一预览
```

---

## 5. 实现细节

### 5.1 MediaItem 与工具函数

- `PlatformFile.toMediaItem()` 生成统一模型
- `MediaItem.systemMimeType()` 统一 MIME 推断
- `PlatformFile.toPlayableUrl()` 处理平台路径与播放 URL

### 5.2 FileKit Picker 接入

- 使用 `FileKit.openFilePicker(...)` 调起系统选择
- 支持 `Image` / `Video` / `ImageAndVideo` / `File(extensions)`
- 支持单选、批量选择与最大数量限制

### 5.3 拖拽导入（Desktop）

- 桌面端通过平台拖拽 API 获取 `PlatformFile` 列表
- 与 Picker 共享 `MediaItem` 生成与渲染链路

### 5.4 预览与播放

- 图片：`SubcomposeAsyncImage` 直接加载 `PlatformFile`
- 视频/音频：使用 `MediaPlayerHost` + `VideoPlayerComposable`
- 若桌面端 VLC 不可用，走系统播放器 fallback

### 5.5 FileKit + Coil 预览

- 全局配置 `ImageLoader` 以支持 `PlatformFile`：

```kotlin
setSingletonImageLoaderFactory { context ->
    ImageLoader.Builder(context)
        .components { addPlatformFileSupport() }
        .build()
}
```

---

## 6. 平台注意事项

### 6.1 wasmJs

- 需在 `index.html` 引入 Shaka 相关脚本
- 保证脚本加载顺序（Shaka -> Global -> Helpers -> Compose App）

### 6.2 Desktop

- 视频播放需要本机安装 VLC
- YouTube 支持需要 Java 环境

### 6.3 Android

- 如启用 PiP 或 Resume Playback，需在 Manifest 与 Activity 中完成配置

---

## 7. 实施计划

1. 在 `component/media` 合并媒体选择与播放能力
2. 统一 `MediaItem` 与预览组件
3. 接入播放器并处理资源生命周期
4. 按平台补齐 Desktop/wasm 配置（如需要）
5. 完成体验验证与风险评估

---

## 8. 风险评估

- 平台权限差异：Android/iOS 权限复杂
- 文件格式兼容：视频缩略图跨平台复杂
- Desktop 依赖 VLC 与 Java，用户环境不可控
- wasmJs 仍为实验状态，性能与兼容性不确定

---

## 9. 测试策略（可选）

- Desktop：拖拽文件 + 多类型文件选择 + VLC fallback
- Android：SAF 选择 + 权限拒绝路径
- iOS：Document Picker + 取消路径
- wasmJs：播放器脚本加载与播放链路

---

## 10. 附录

- 参考：`docs/infra/myhub-infra.md`
- 参考：`docs/infra/myhub-infra-rules.md`

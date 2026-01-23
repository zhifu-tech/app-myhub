# App 设计规范

## 规范

遵循 [Google Material 3][1]

## 1. 平台及技术选型

> **Q：需要运行在哪些平台？**
>
> A：移动端（Android、iOS、HarmonyOS）；桌面端（macOS、Windows）

### 1.1 技术方案

| 平台      | 技术栈      | 备注                       |
| --------- | ----------- | -------------------------- |
| Android   | KMP Android | 原生 Compose Multiplatform |
| iOS       | KMP iOS     | 原生 Compose Multiplatform |
| HarmonyOS | Web         | 使用 WebView 方式承载      |
| macOS     | KMP JVM     | 桌面端 Compose             |
| Windows   | KMP JVM     | 桌面端 Compose             |

### 1.2 平台特性适配

- **移动端**：触控优先，支持手势操作
- **桌面端**：键鼠交互，支持快捷键

---

## 2. 导航模式

> **Q：行场景（页面）切换？**
>
> A：移动端采用底导模式；桌面端采用侧边栏导航

### 2.1 移动端导航，**底导**

- **模式**：底部导航栏（Bottom Navigation）
- **Tab 数量**：4 个
- **Tab 项**：首页、探索、收藏、我的

### 2.2 桌面端导航，**侧导**

- **模式**：侧边栏导航（Side Navigation）
- **特性**：支持折叠/展开
- **菜单项**：与移动端 Tab 对应

## 3. 新建

### 3.1. 入口

> Q: **新建卡片的入口在哪里？**
>
> A: 采用 M3的标准：FAB呈现。侧边栏：在顶部；底导：左下角

- 侧导：以FAB的方式呈现在顶部。
- 底导：以FAB的形式呈现在左下角。

### 3.2. 方式

> Q: **如何新建一张卡片？**
>
> A：提供三种方式：模版、分享 和 克隆。

- 模版：通过App内置的模版创建
  
- 分享：通过将分享链接发送给server，server分析和提取链接内容，创建
  - 链接来自：剪切板、第三方分享的链接。

- 克隆：通过已有的卡片，复制创建。

---

[1]:https://m3.material.io/
[2]:https://fonts.google.com/icons?icon.query=library&icon.size=24&icon.color=%231f1f1f
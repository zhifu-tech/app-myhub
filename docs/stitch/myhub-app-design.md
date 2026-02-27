# MyHub App 设计规范（Design Specification）

> 文档版本：v1.0  
> 文档类型：设计规范 / Design Spec  
> 设计语言：Material Design 3  
> 适用对象：产品、设计、前端 / 客户端开发

---

## 1. 设计原则（Design Principles）

- 整体设计遵循 **Google Material Design 3**
- 统一视觉语言、交互反馈与组件语义
- 保证多端一致性，同时尊重不同平台的交互习惯
- 所有设计需具备明确的功能语义与可扩展性

---

## 2. 平台与技术选型（Platform & Tech Stack）

### 2.1 支持平台

- **移动端**：Android / iOS / HarmonyOS
- **桌面端**：macOS / Windows

### 2.2 技术方案

| 平台        | 技术栈         | 说明                    |
|-----------|-------------|-----------------------|
| Android   | KMP Android | Compose Multiplatform |
| iOS       | KMP iOS     | Compose Multiplatform |
| HarmonyOS | Web         | WebView 承载            |
| macOS     | KMP JVM     | Desktop Compose       |
| Windows   | KMP JVM     | Desktop Compose       |

---

## 3. 主题系统（Theme System）

### 3.1 主题模式

- 支持 **Light / Dark / Auto**
- Auto 模式自动跟随系统主题

### 3.2 颜色系统

- 基于 Material 3 Color Roles
- 支持完整语义色（primary / surface / outline / error 等）
- Dark / Light 主题配置如下（保持原有配置）：

```json
{
  "dark": {
    "primary": "#B4A3FF",
    "onPrimary": "#381E72",
    "primaryContainer": "#4F378B",
    "onPrimaryContainer": "#EADDFF",
    "secondary": "#CCC2DC",
    "onSecondary": "#332D41",
    "secondaryContainer": "#4A4458",
    "onSecondaryContainer": "#E8DEF8",
    "tertiary": "#EFB8C8",
    "onTertiary": "#492532",
    "tertiaryContainer": "#633B48",
    "onTertiaryContainer": "#FFD8E4",
    "error": "#F2B8B5",
    "onError": "#601410",
    "errorContainer": "#8C1D18",
    "onErrorContainer": "#F9DEDC",
    "outline": "#938F99",
    "background": "#1A1B1F",
    "onBackground": "#E6E1E5",
    "surface": "#2D2E33",
    "onSurface": "#E6E1E5",
    "surfaceVariant": "#44474E",
    "onSurfaceVariant": "#CAC4D0",
    "inverseSurface": "#E6E1E5",
    "inverseOnSurface": "#313033",
    "inversePrimary": "#6750A4",
    "surfaceTint": "#B4A3FF",
    "outlineVariant": "#44474E",
    "scrim": "#000000"
  },
  "light": {
    "primary": "#8B7ACC",
    "onPrimary": "#FFFFFF",
    "primaryContainer": "#EADDFF",
    "onPrimaryContainer": "#21005D",
    "secondary": "#625B71",
    "onSecondary": "#FFFFFF",
    "secondaryContainer": "#E8DEF8",
    "onSecondaryContainer": "#1D192B",
    "tertiary": "#7D5260",
    "onTertiary": "#FFFFFF",
    "tertiaryContainer": "#FFD8E4",
    "onTertiaryContainer": "#31111D",
    "error": "#B3261E",
    "onError": "#FFFFFF",
    "errorContainer": "#F9DEDC",
    "onErrorContainer": "#410E0B",
    "outline": "#79747E",
    "background": "#FDFBFF",
    "onBackground": "#1C1B1F",
    "surface": "#F5F5F5",
    "onSurface": "#1C1B1F",
    "surfaceVariant": "#E7E0EC",
    "onSurfaceVariant": "#49454F",
    "inverseSurface": "#313033",
    "inverseOnSurface": "#F4EFF4",
    "inversePrimary": "#B4A3FF",
    "surfaceTint": "#8B7ACC",
    "outlineVariant": "#C4C6D0",
    "scrim": "#000000"
  }
}
```

---

## 4. 字体系统（Typography）

- **Display / UI**：Inter
- **Serif（阅读类）**：Playfair Display
- **Monospace（代码类）**：JetBrains Mono

---

## 5. 全局导航框架（Navigation Framework）

### 5.1 移动端：Navigation Bar

- 采用 Material 3 **Navigation Bar**
- 固定底部导航
- Tab 数量：4

| 页面 | Icon（Selected）    | Icon（Unselected）    |
|----|-------------------|---------------------|
| 首页 | Home (Filled)     | Home (Outlined)     |
| 探索 | Explore (Filled)  | Explore (Outlined)  |
| 收藏 | Favorite (Filled) | Favorite (Outlined) |
| 我的 | Profile (Filled)  | Profile (Outlined)  |

---

### 5.2 桌面端：Navigation Rail（Collapsed）

- 左侧折叠式 **Navigation Rail**
- 菜单语义与移动端保持一致
- 支持键鼠操作与快捷键扩展

---

## 6. 全局新建入口（Floating Action Button）

- 使用 **Floating Action Button（FAB）**
- 仅展示 Icon，不展示 Label
- Icon：`auto_stories`

| 平台  | 位置                 |
|-----|--------------------|
| 移动端 | 左下角                |
| 桌面端 | Navigation Rail 顶部 |

---

## 7. 模块化设计方法论（Module Design Pipeline, MDP）

> 后续所有功能模块，统一遵循以下流程设计与落档。

### Step 1：模块定位（Why）

- 模块解决的问题
- 用户的核心行为
- 在页面中的角色（核心 / 辅助 / 探索）

### Step 2：模块类型（What）

- 行动入口模块
- 内容集合模块
- 状态提醒模块
- 浏览推荐模块
- 工具模块

### Step 3：结构拆解（Structure）

```
Module
 ├─ Header（可选）
 ├─ Content（核心）
 └─ Footer / CTA（可选）
```

### Step 4：Material 3 组件映射

| 功能 | M3 组件                 |
|----|-----------------------|
| 容器 | Card / Surface        |
| 操作 | Button / FAB          |
| 导航 | Navigation Bar / Rail |
| 提示 | Chip / Badge          |

### Step 5：交互状态

- Default
- Hover（Desktop）
- Pressed
- Disabled
- Loading（如适用）

### Step 6：多端适配

- Mobile：触控优先、单列布局
- Desktop：Hover、快捷键、宽布局

---

## 8. 首页内容结构（Home）

首页是 MyHub 的核心工作台页面，围绕「今日学习行动」进行组织。
所有模块均按照 **MDP(Module Design Pipeline)** 进行设计与落档。

---

### 8.1 首页模块地图（Module Map）

| 模块名称              | 模块类型      | 优先级 | 用户目标        | 说明        |
|-------------------|-----------|-----|-------------|-----------|
| Top App Bar       | 工具 / 状态模块 | P0  | 识别身份、进入全局操作 | 全局入口与状态承载 |
| Focus & Review    | 行动入口模块    | P0  | 立即开始复习      | 核心主行动     |
| Asset Collections | 内容集合模块    | P1  | 浏览与进入知识集合   | 探索与管理入口   |
| Latest Captures   | 内容流模块     | P1  | 快速回顾最近捕获内容  | 时间序列内容入口  |

> 优先级说明：  
> **P0** = 核心任务模块（首页必须可见）  
> **P1** = 次核心模块（支持核心任务）

---

### 8.2 Top App Bar（状态 / 工具模块）

#### 1. 模块定位

- 目标：提供身份确认、今日状态提示与全局操作入口
- 用户行为：查看状态、搜索、进入更多操作

#### 2. 模块类型

- 工具 / 状态模块

#### 3. 结构组成

- Header：
    - Headline：Good evening, Scholar
    - Subtitle：You have 5 cards to review today
- Content：无
- Footer / CTA：
    - Focus & Review →（文本 CTA）

#### 4. 组件选型（Material 3）

- Container：Small Top App Bar
- Action：IconButton / Text CTA

#### 5. 交互状态

- Default：静态展示
- Active：CTA 点击高亮
- Disabled：无

#### 6. 多端适配说明

- Mobile：紧凑布局，触控优先
- Desktop：支持 Hover 与快捷键

---

### 8.3 Focus & Review 模块（核心行动入口）

#### 1. 模块定位

- 目标：驱动用户完成每日复习任务
- 用户行为：点击并开始 Review 流程

#### 2. 模块类型

- 行动入口模块（Primary Action）

#### 3. 结构组成

- Header：功能标签（FOCUS & REVIEW）
- Content：
    - 环形进度（10 / 15）
- Footer / CTA：
    - 主按钮：Start Review →

#### 4. 组件选型（Material 3）

- Container：Elevated Card
- Action：Filled Button
- Other：Progress Indicator（Circular）

#### 5. 交互状态

- Default：展示进度与 CTA
- Hover（Desktop）：卡片阴影增强
- Pressed：CTA 高亮
- Disabled：进度为 0 时可选禁用

#### 6. 多端适配说明

- Mobile：单列横向卡片，触控点击
- Desktop：支持 Hover 与快捷键启动

---

### 8.4 Asset Collections 模块（内容集合浏览）

#### 1. 模块定位

- 目标：提供内容分类入口，支持探索与管理
- 用户行为：浏览集合、进入集合详情

#### 2. 模块类型

- 内容集合模块

#### 3. 结构组成

- Header：
    - 标题：Asset Collections
    - 操作：View All
- Content：
    - Horizontal Collection Cards
- Footer / CTA：
    - View All → 集合总览

#### 4. 组件选型（Material 3）

- Container：Elevated Card / Surface
- Action：Text Button
- Other：Image / Text

#### 5. 交互状态

- Default：静态卡片
- Hover（Desktop）：描边高亮、轻微放大
- Pressed：进入集合详情

#### 6. 多端适配说明

- Mobile：横向滑动
- Desktop：横向排列 + Hover 反馈

---

### 8.5 Latest Captures 模块（最近捕获卡片）

该模块 MyHub 首页中承载「最近输入内容」的核心内容流区域，
用于将零散捕获转化为可回顾、可再利用的知识资产。

---

#### 1. 模块定位（Why）

- 目标：
    - 提供时间维度的内容回顾入口
    - 强化「捕获即资产」的产品心智
- 用户行为：
    - 快速浏览最近内容
    - 点击进入卡片详情
    - 执行轻量操作（收藏 / 编辑 / 跳转来源）

---

#### 2. 模块类型（What）

- 内容流模块（Recent Captures Feed）
- 首页次核心模块（P1）
- 非强行动模块，但高频出现、高信息密度

---

#### 3. 结构组成（Structure）

```
Latest Captures
 ├─ Header
 │   └─ Title：Latest Captures（最近捕获的卡片）
 ├─ Content
 │   └─ Adaptive Grid / Masonry Card Flow
 └─ Footer
     └─ 无（通过滚动查看更多）
```

---

#### 4. 单卡结构解析（Card Anatomy）

每一张 Capture Card 代表一条独立的知识资产，结构保持统一，但内容类型可变。

**Card 基础结构：**

```
Capture Card
 ├─ Type Chip（左上）
 ├─ Meta Info（右上：日期 / NEW）
 ├─ Main Content（核心内容区）
 ├─ Footer Meta（来源 / 阅读时间 / 标签）
 └─ Quick Actions（收藏 / 编辑）
```

**内容类型支持：**

不同类型的卡片支持的类型可以归纳为：

- **文字**（Text）：引文、摘录、个人想法、笔记、代码片段、文章、待办、词典、单词等文本类内容
- **图片**（Image）：图片、截图、图表等图像类内容
- **视频**（Video）：视频片段、视频摘要等视频类内容

---

#### 5. 视觉与层级设计（Visual Hierarchy）

- Card Container：
    - M3 Elevated Card
    - 大圆角、柔和投影
- 强调策略：
    - 当前 Hover / Active 卡片使用紫色描边
    - 非活跃卡片保持弱对比
- 信息层级：
    - 内容本身 > 类型标签 > 元信息 > 操作按钮

---

#### 6. 组件选型（Material 3 Mapping）

- Container：Elevated Card
- 内容：
    - 文字（Text / CodeBlock）/ 图片（Image）/ 视频（Video）
- 标签：
    - Chip（Type）
    - Badge（NEW）
- 操作：
    - IconButton（Star / Edit）

---

#### 7. 交互状态（Interaction）

- Default：
    - 卡片静态展示
- Hover（Desktop）：
    - 卡片轻微抬升（Elevation +）
    - 描边高亮
    - Quick Actions 显现
- Pressed：
    - 进入卡片详情页
- Active：
    - 收藏状态高亮（Star）

---

#### 8. 多端适配说明（Responsive）

- Mobile：
    - 单列 / 双列瀑布流
    - 触控操作，长按呼出操作
- Desktop：
    - 多列 Masonry Grid
    - Hover + 快捷操作
    - 支持键盘导航（↑ ↓ Enter）

---

#### 9. Design Notes（设计说明）

- Latest Captures = **时间维度**
- Asset Collections = **结构维度**
- 二者共同构成 MyHub 的内容组织双轴：
    - 先捕获（Latest）
    - 再归档（Collections）
- 该模块是提升「回访率」与「内容再利用率」的关键设计。


### 8.6. New Capture 模块设计

采用全屏设计，用于完成「收集」：用户粘贴/上传/输入内容后，系统分析并给出主类型、来源形态与 metadata，用户可修改后保存。设计对齐 [卡片分析设计（产品视角）](../产品/myhub-卡片分析设计-产品视角.md)：**主类型仅按用户诉求**（再看到 / 执行 / 素材），**来源形态**（引用 / 摘录 / 自创）与**载体**（文/图/视频/链接）由分析结果与表单共同承载，输入形态不作为用户可见的分类维度。

#### 1. AppBar 设计

- 采用 M3 的 Small Top App Bar。
- 左上角：关闭按钮（X）。
- 右上角：主操作「保存」或「Capture / 保存卡片」。

#### 2. 内容区域

**2.1. 用户输入内容（收集入口）**

- 标题：输入图标 + 来源内容 / Source Content。
- 支持输入形态：URL、文本、图片、视频或其组合；内容预览/摘要区显示限定约 3 行，用于用户确认与 AI 分析输入。
- 说明：输入形态仅在分析管道内部使用，不作为用户可见的「类型」选项。

**2.2. 主类型（Primary Type）— 按用户诉求**

- 采用 M3 Chips 控件，**仅三种选项、互斥**：
  - **再看到（review）**：存下来是为了复看、复习或随时调出。
  - **执行（do）**：存下来是为了去做、勾选、按步骤执行。
  - **素材（material）**：存下来是为了再创作、再分享或当模板用。
- 默认值：由 AI 根据内容特征推断用户诉求后预填（可执行结构 → 执行；金句/模板/站点入口等 → 素材；其余 → 再看到），用户可在保存前切换。
- 类型一旦改变，展示与表单项可随之切换（例如选「执行」时出现清单/步骤相关能力）。

**2.3. 来源形态（Link / Extract / Own）— 仅展示或轻量编辑**

- 由系统根据输入自动推断并展示，必要时支持用户修正：
  - **引用（Link）**：指向外部资源；若有 URL，展示「打开原链接」等。
  - **摘录（Extract）**：从某处摘出的片段，副本在 MyHub。
  - **自创（Own）**：用户自己写/录的，无原出处。
- 不单独作为一组 Chips 与主类型并列，可在元信息区或二级表单中展示，避免与主类型混淆。

**2.4. 显示在卡片中的标题**

- 提示标题：标题（Title）。
- 内容限定约 2 行；可由 AI 从内容中提取，用户可编辑。

**2.5. 卡片封面 / 样式设置**

- 支持纯色 + 图片。
- 第一个 item：**AI** — 用户粘贴/上传内容后，AI 智能分配色值或图片。
- 第二个 item：**图片** — 用户可上传本地图片。
- 后续补充符合 M3 规范的 7 种常用色值：Red、Orange、Yellow、Green、Blue、Purple、Gray。

**2.6. 卡片标签区域**

- 标签以 M3 Chips 显示，支持新增；可由 AI 建议，用户可增删。

**2.7. 多平台适配**

- 屏幕宽度为 Medium 及以下（按 M3 标准划分）：内容居中显示，通过 FAB 或底部按钮弹出卡片预览。
- 否则：通过 M3 Side Sheet 分屏显示预览。

#### 3. AI 辅助生成与再分析

- 基于 **URL**、**图片** 或 **一段文本（文学/代码）**，AI 自动分析并产出：
  - **主类型**（再看到 / 执行 / 素材）、**来源形态**（引用 / 摘录 / 自创）、**metadata**（作者、出处、语言、时长、平台等）、**建议标签**、**建议封面/标题**。
- 用户可在 New Capture 中粘贴/上传后点击「用 AI 生成」，在草稿上修改主类型与各字段后保存。
- 用户编辑类型或内容后、或分析能力升级后，支持对已有卡片再分析，用于补全标签、更新摘要等。
- 详细能力、接口与流程见：[AI 辅助生成卡片设计文档](../ai-card-generation-design.md)。

#### 4. 与卡片分析设计的一致性（Design Notes）

- **诉求即类型**：主类型仅由「再看到 / 执行 / 素材」定义，不在 New Capture 中按「链接、图片、笔记、Snippet」等输入形态分类。
- **类型极简**：三种类型、互斥、用户可解释；展示差异由来源形态 + metadata 在详情/列表中体现。
- **输入与类型解耦**：同一输入可对应不同诉求，类型由分析结果与用户选择共同决定。

## 9. 模块设计规范模板（Reusable Template）

```md
### 模块名称（Module Name）

#### 1. 模块定位

- 目标：
- 用户行为：

#### 2. 模块类型

- 类型：

#### 3. 结构组成

- Header：
- Content：
- Footer / CTA：

#### 4. 组件选型（Material 3）

- Container：
- Action：
- Other：

#### 5. 交互状态

- Default：
- Hover：
- Active：
- Disabled：

#### 6. 多端适配说明

- Mobile：
- Desktop：
```

---

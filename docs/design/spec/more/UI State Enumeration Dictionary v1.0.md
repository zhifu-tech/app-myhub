# UI State Enumeration Dictionary v1.0

> **关联规则：** UI Design & Analysis Rulebook v1.0 Section 3.1 & 3.2  
> **文档级别：** **最高约束（Highest Constraint）**  
> **适用范围：** 全系统所有模块、页面、组件设计与实现  
> **生效条件：** 所有 UI 状态命名必须严格源自本字典，**禁止创造未定义词汇**。

---

## 0. 核心原则（Core Principles）

1. **语义优先（Semantic First）：** 命名必须反映业务逻辑与交互能力，禁止使用视觉词（如 `Dark`, `Big`）或含糊词（如 `Normal`, `Default`）。
2. **状态机思维（State Machine）：** UI 是状态的投影。本字典定义了状态机的所有合法节点。
3. **封闭集合（Closed Set）：** 本字典为**封闭集合**。新增枚举值必须遵循 Rulebook Section 11.1 治理流程。
4. **工程映射（Engineering Mapping）：** 本字典直接对应代码中的 `Enum` / `Sealed Class`，设计稿标注必须与代码枚举值**完全一致**。

---

## 1. 命名格式（Naming Format）

所有 UI 状态必须遵循以下四段式命名结构：

```text
<Module>_<Phase>_<Context>_<Mode>
```

| 段位    | 名称          | 定义       | 核心问题           | 示例                          |
|:------|:------------|:---------|:---------------|:----------------------------|
| **1** | **Module**  | 业务模块/作用域 | 这个状态属于**谁**？   | `Auth`, `Order`, `Edit`     |
| **2** | **Phase**   | 生命周期阶段   | 系统处于**什么大阶段**？ | `Init`, `Ready`, `Result`   |
| **3** | **Context** | 业务上下文/焦点 | 用户正在对**什么**操作？ | `Input`, `List`, `Config`   |
| **4** | **Mode**    | 交互模式/权限  | 用户**能做什么**？    | `Idle`, `Focused`, `Locked` |

---

## 2. Module 字典（模块域）

**定义：** 状态机的**作用域边界**。一个独立闭环的业务能力单元。  
**约束：** 必须反映业务领域或用户任务，禁止按页面或组件划分。

| 分类       | 枚举值 (Enum)  | 语义定义         | 典型场景                 |
|:---------|:------------|:-------------|:---------------------|
| **基础模块** | `Auth`      | 用户认证与权限管理    | 登录、注册、Token 刷新、授权过期  |
| (必选)     | `Nav`       | 全局导航与路由控制    | 底部 Tab 切换、侧边栏导航、路由跳转 |
|          | `Notify`    | 系统通知与消息中心    | 全局 Toast、弹窗、消息列表     |
|          | `Setting`   | 全局配置与偏好设置    | 语言切换、主题设置、隐私配置       |
| **业务模块** | `Capture`   | 媒体/数据采集      | 拍照、录音、文件上传、扫码        |
| (按需)     | `Edit`      | 内容编辑与加工      | 文本编辑、图片裁剪、视频剪辑       |
|          | `Browse`    | 内容浏览与探索      | 信息流、列表页、Feed 流       |
|          | `View`      | 详情查看与理解      | 商品详情、文章阅读、档案查看       |
|          | `Search`    | 检索与筛选        | 搜索框、筛选器、结果页          |
|          | `Order`     | 订单管理与追踪      | 下单、支付、物流追踪、售后        |
|          | `Cart`      | 购物车与订单暂存     | 添加商品、结算准备            |
|          | `Chat`      | 即时通讯与对话      | 客服对话、IM 聊天、评论互动      |
| **特殊模块** | `AI`        | AI 能力调用与结果消费 | 生成式内容、智能分析、对话机器人     |
| (按需)     | `Analytics` | 数据看板与洞察      | 数据大屏、核心指标看板          |
|          | `Onboard`   | 新用户引导与教学     | 新手教程、功能引导            |

> **❌ 禁止使用的 Module 词：**
> - `Page` / `Screen` (这是层级，不是模块)
> - `Button` / `Form` / `Modal` (这是组件，不是模块)
> - `Home` / `Detail` (这是页面名，不是业务域)
> - `Common` / `Utils` (这是工具库，不是业务模块)

---

## 3. Phase 字典（阶段域）

**定义：** 模块在应用生命周期中的**宏观阶段**。  
**约束：** 必须互斥，覆盖全生命周期。**`Loading` 和 `Error` 不是 Phase。**

| 枚举值 (Enum)       | 语义定义                                  | 典型场景                 | 禁止混用词                                              |
|:-----------------|:--------------------------------------|:---------------------|:---------------------------------------------------|
| **`Init`**       | **初始化**。模块正在加载核心依赖，尚未就绪。              | 首次进入页面、冷启动、资源预加载     | ❌ `Loading` (视觉词)<br>❌ `Start` (含糊)                |
| **`Ready`**      | **就绪**。核心依赖已加载，等待用户触发或展示默认状态。         | 页面加载完成、表单清空待填、列表刷新完毕 | ❌ `Normal` (含糊)<br>❌ `Default` (含糊)                |
| **`Active`**     | **活跃**。用户正在与模块进行深度交互。                 | 输入中、编辑中、拖拽中、筛选中      | ❌ `Editing` (应归为 Context)<br>❌ `Using` (含糊)        |
| **`Processing`** | **处理中**。系统正在执行异步任务，用户需等待或可并行操作。       | 提交保存、AI 生成中、数据同步、上传中 | ❌ `Loading` (视觉词)<br>❌ `Waiting` (被动语态)            |
| **`Result`**     | **结果**。任务已完成或终止，展示最终 outcome（含成功/失败）。 | 提交成功页、报错页、空状态页、生成完成  | ❌ `Success` (片面)<br>❌ `Error` (片面)<br>❌ `End` (含糊) |

> **💡 关键治理规则：**
> 1. 所有“加载中”必须映射为 `Init` 或 `Processing`。
> 2. 所有“错误页”必须映射为 `Result` 阶段。
> 3. 所有“成功页”必须映射为 `Result` 阶段。

---

## 4. Context 字典（上下文域）

**定义：** 当前阶段的**业务焦点或内容类型**。  
**约束：** 必须反映业务语义，而非 UI 控件。

| 分类      | 枚举值 (Enum)  | 语义定义                 | 典型组合示例                             |
|:--------|:------------|:---------------------|:-----------------------------------|
| **通用**  | `Global`    | 全局性操作，不局限于特定内容块      | `Module_Ready_Global_Idle`         |
|         | `System`    | 系统级后台任务，用户不可见或弱感知    | `Module_Processing_System_Pending` |
| **输入类** | `Input`     | 通用数据输入，不指定具体方式       | `Module_Active_Input_Focused`      |
|         | `Capture`   | 媒体或特定信息的捕获/采集        | `Module_Active_Capture_Focused`    |
|         | `Query`     | 主动发起的检索或筛选请求         | `Module_Active_Query_Focused`      |
|         | `Config`    | 配置项的设定与调整            | `Module_Active_Config_Focused`     |
| **输出类** | `Output`    | 通用结果展示，系统生成的内容       | `Module_Result_Output_Completed`   |
|         | `Preview`   | 正式提交前的预演或草稿查看        | `Module_Ready_Preview_Idle`        |
|         | `Detail`    | 单一对象的详细信息展示          | `Module_Ready_Detail_ReadOnly`     |
|         | `List`      | 多个对象的集合展示            | `Module_Ready_List_Idle`           |
|         | `Dashboard` | 多指标聚合的概览展示           | `Module_Ready_Dashboard_Idle`      |
| **操作类** | `Edit`      | 对现有内容的修改与编辑          | `Module_Active_Edit_Focused`       |
|         | `Select`    | 从集合中选择一个或多个对象        | `Module_Active_Select_Focused`     |
|         | `Compare`   | 两个或多个对象的对比分析         | `Module_Ready_Compare_Idle`        |
|         | `Arrange`   | 对内容顺序或布局的调整          | `Module_Active_Arrange_Focused`    |
| **通信类** | `Notify`    | 系统向用户发送通知或反馈         | `Module_Result_Notify_Completed`   |
|         | `Chat`      | 双向即时通信交互             | `Module_Active_Chat_Focused`       |
| **异常类** | `Auth`      | 权限相关的上下文             | `Module_Result_Auth_Expired`       |
|         | `Error`     | 通用错误上下文（当错误不特定于某内容时） | `Module_Result_Error_Disabled`     |

> **❌ 禁止使用的 Context 词：**
> - `Button`, `Form`, `Modal`, `Page` (组件/层级词)
> - `Dark`, `Light`, `Large` (视觉词)
> - `Text`, `Image` (媒体词，除非是 `Capture` 模块特有)

---

## 5. Mode 字典（模式域）

**定义：** 当前阶段的**交互权限、焦点状态或完成度**。  
**约束：** 必须反映交互能力，而非视觉样式。**`Loading` 和 `Error` 不是 Mode。**

| 分类      | 枚举值 (Enum)  | 语义定义                | 交互能力约束              |
|:--------|:------------|:--------------------|:--------------------|
| **空闲态** | `Idle`      | 空闲，无正在进行的操作，等待用户触发  | 可交互，无焦点             |
|         | `Standby`   | 待机，系统就绪但建议用户等待特定条件  | 可交互，但有前置建议          |
| **焦点态** | `Focused`   | 焦点明确，用户正在与特定区域深度交互  | 可交互，高优先级响应          |
|         | `Active`    | 活跃，操作正在进行中（非阻塞）     | 可交互，可能有后台任务         |
|         | `Selected`  | 已被选中，作为后续操作的对象      | 可交互，但状态已标记          |
| **限制态** | `Locked`    | 锁定，因业务逻辑暂时禁止交互（可解锁） | **不可交互**，通常有时效性或条件性 |
|         | `Disabled`  | 禁用，因权限或条件不满足永久/长期禁止 | **不可交互**，通常置灰       |
|         | `ReadOnly`  | 只读，可查看但不可修改内容       | 可查看，**不可编辑**        |
|         | `Protected` | 保护，需二次验证（如密码）才能操作   | 受限交互，需验证            |
| **完成态** | `Completed` | 任务已完成，等待用户确认或消费结果   | 可确认，不可再编辑原内容        |
|         | `Confirmed` | 已确认，操作已提交且不可撤销      | 不可交互，流程终结           |
|         | `Verified`  | 已验证，内容已通过系统或人工校验    | 可查看，标记为可信           |
| **异常态** | `Pending`   | 挂起，等待外部依赖或异步结果      | 不可交互，等待中            |
|         | `Expired`   | 过期，内容或操作权限已失效       | 不可交互，需刷新或重新获取       |

> **❌ 禁止使用的 Mode 词：**
> - `Loading`, `Error`, `Success` (这些是 Phase 或语义结果)
> - `Visible`, `Hidden`, `Bold` (视觉词)
> - `Normal`, `Default` (含糊词)

---

## 6. 组合合法性矩阵（Combination Validity Matrix）

**规则：** 并非所有字典值都可以随意组合。以下规则用于**校验状态命名的合法性**。违反此矩阵的状态名视为**非法**。

| Phase            | 推荐 Context                   | 推荐 Mode                             | 禁止组合 (Invalid)                     | 理由            |
|:-----------------|:-----------------------------|:------------------------------------|:-----------------------------------|:--------------|
| **`Init`**       | `Global`, `System`           | `Pending`, `Locked`                 | ❌ `Init` + `Input` + `Focused`     | 初始化阶段用户不可输入   |
| **`Ready`**      | `Input`, `List`, `Dashboard` | `Idle`, `ReadOnly`                  | ❌ `Ready` + `Output` + `Pending`   | 就绪态不应 pending |
| **`Active`**     | `Input`, `Edit`, `Select`    | `Focused`, `Active`                 | ❌ `Active` + `Input` + `Locked`    | 活跃态不应锁定输入     |
| **`Processing`** | `System`, `Input`, `Output`  | `Pending`, `Locked`                 | ❌ `Processing` + `Global` + `Idle` | 处理中不应空闲       |
| **`Result`**     | `Output`, `Error`, `Notify`  | `Completed`, `Confirmed`, `Expired` | ❌ `Result` + `Input` + `Focused`   | 结果态不应再聚焦输入    |

> **校验规则：**
> - 若组合违反上述矩阵 → ❌ **Fail** (需重构状态定义)
> - 若组合未在上述矩阵但语义自洽 → ⚠️ **Review** (需评审是否新增标准组合)

---

## 7. 标准状态示例库（Standard Examples）

| 业务场景                    | 合法状态名                               | 逐段解析                                                               |
|-------------------------|-------------------------------------|--------------------------------------------------------------------|
| **用户刚打开 App，未登录**       | `Auth_Init_Global_Pending`          | Module: Auth, Phase: Init, Context: Global, Mode: Pending          |
| **登录成功，进入首页**           | `Nav_Ready_Global_Idle`             | Module: Nav, Phase: Ready, Context: Global, Mode: Idle             |
| **在首页点击「拍照」按钮**         | `Capture_Ready_Capture_Idle`        | Module: Capture, Phase: Ready, Context: Capture, Mode: Idle        |
| **拍照完成，AI 正在分析**        | `Capture_Processing_Output_Pending` | Module: Capture, Phase: Processing, Context: Output, Mode: Pending |
| **分析完成，进入编辑页**          | `Edit_Ready_Edit_Idle`              | Module: Edit, Phase: Ready, Context: Edit, Mode: Idle              |
| **编辑中，用户聚焦文本框**         | `Edit_Active_Edit_Focused`          | Module: Edit, Phase: Active, Context: Edit, Mode: Focused          |
| **编辑完成，点击发布**           | `Publish_Processing_System_Pending` | Module: Publish, Phase: Processing, Context: System, Mode: Pending |
| **发布成功，显示结果页**          | `Publish_Result_Output_Completed`   | Module: Publish, Phase: Result, Context: Output, Mode: Completed   |
| **用户查看订单详情**            | `Order_Ready_Detail_ReadOnly`       | Module: Order, Phase: Ready, Context: Detail, Mode: ReadOnly       |
| **订单支付超时**              | `Order_Result_Auth_Expired`         | Module: Order, Phase: Result, Context: Auth, Mode: Expired         |
| **Dashboard 区域 A 加载失败** | `Dashboard_Ready_Dashboard_Idle`    | **注意：** 模块态仍为 Ready，区域错误由 Component Behavior 处理                    |
| **Dashboard 全局初始化失败**   | `Dashboard_Result_Error_Disabled`   | 模块态变为 Result，全局禁用                                                  |

---

## 8. 治理与验收规则（Governance & Validation）

### 8.1 封闭性原则

- 本字典为**封闭集合**。原则上不允许新增枚举值。
- 若业务确有特殊需求，必须遵循 Rulebook Section 11.1 修改流程：
    1. 提交 `State Naming Extension Request`
    2. 证明现有字典无法覆盖该语义
    3. 评审通过 → 更新本字典 → 全系统同步

### 8.2 工程映射

- 本字典中的 Enum 值必须直接映射为代码中的**类型定义**。
    - ✅ `enum Phase { Init, Ready, Active, Processing, Result }`
    - ❌ `const phase = "loading"` (字符串字面量禁止)
- 设计稿中标注的状态名必须与代码枚举值**完全一致**（包括大小写）。

### 8.3 验收 Checklist (Hard Requirements)

- [ ] **格式合规：** 是否严格遵循 `<Module>_<Phase>_<Context>_<Mode>`？
- [ ] **字典封闭：** 四段值是否均来自本字典？
- [ ] **组合合法：** 是否违反组合合法性矩阵？
- [ ] **语义清晰：** 状态名是否能被「非本项目成员」理解？
- [ ] **无视觉词：** 是否混入了 `Color`, `Size`, `Loading`, `Error` 等词？

### 8.4 AI 约束

- 在使用 AI 生成 UI 或代码时，必须将此字典作为 **System Prompt** 的一部分，强制 AI 使用标准枚举值。

---

> **一句话总结：**  
> **Module 定边界，Phase 定生命周期，Context 定业务焦点，Mode 定交互权限；四者组合，唯一锚定 UI 状态。**

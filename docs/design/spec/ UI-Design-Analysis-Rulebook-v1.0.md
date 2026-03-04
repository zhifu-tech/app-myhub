# UI Design & Analysis Rulebook v1.0

> 本文档用于定义统一的 **UI 设计与分析规范**。  
> `所有模块、页面、状态的设计与实现，必须以本规则为最高约束`。  
> **`任何 UI 变更，必须先修改本规则或其派生文档，再修改设计与实现。`**

---

<!-- TOC -->

* [UI Design & Analysis Rulebook v1.0](#ui-design--analysis-rulebook-v10)
    * [0. 适用范围（Scope）](#0-适用范围scope)
    * [1. 核心设计哲学（Design Philosophy）](#1-核心设计哲学design-philosophy)
        * [1.1 语义优先于视觉（Semantic First）](#11-语义优先于视觉semantic-first)
        * [1.2 UI = 状态机，而不是画面集合](#12-ui--状态机而不是画面集合)
    * [2. 抽象层级模型（Abstraction Levels）](#2-抽象层级模型abstraction-levels)
        * [2.1 App-level Phase（应用级阶段）](#21-app-level-phase应用级阶段)
            * [定义](#定义)
            * [解决的问题](#解决的问题)
            * [示例（非枚举）](#示例非枚举)
        * [2.2 Module-level State（模块级状态）](#22-module-level-state模块级状态)
            * [定义](#定义-1)
            * [规则](#规则)
            * [示例（以 Capture 模块为例）](#示例以-capture-模块为例)
        * [2.3 Level 3：Page / Screen Layout（页面结构层）](#23-level-3page--screen-layout页面结构层)
            * [定义](#定义-2)
            * [解决的问题](#解决的问题-1)
            * [必须描述的内容](#必须描述的内容)
            * [示例（结构描述，非设计稿）](#示例结构描述非设计稿)
            * [明确禁止](#明确禁止)
            * [校验规则](#校验规则)
        * [2.4 Level 4：Component Behavior（组件行为层）](#24-level-4component-behavior组件行为层)
            * [定义](#定义-3)
            * [解决的问题](#解决的问题-2)
            * [必须描述的内容](#必须描述的内容-1)
            * [示例（行为定义）](#示例行为定义)
            * [行为与状态一致性原则](#行为与状态一致性原则)
                * [示例禁止行为](#示例禁止行为)
            * [明确禁止](#明确禁止-1)
            * [校验规则](#校验规则-1)
        * [2.5 层级之间的责任边界](#25-层级之间的责任边界)
    * [3. 状态命名规范（State Naming Convention）](#3-状态命名规范state-naming-convention)
        * [3.1 命名格式](#31-命名格式)
            * [说明](#说明)
                * [1. 🧠 **Context** 的理解](#1--context-的理解)
                * [2. 🧠 **Mode** 的理解](#2--mode-的理解)
                * [常见 `Mode` 示例：](#常见-mode-示例)
                * [3. **Context 和 Mode 的关系**](#3-context-和-mode-的关系)
                * [4. 举个例子：](#4-举个例子)
                * [5. 📝 总结](#5--总结)
        * [3.2 命名约束](#32-命名约束)
    * [4. 状态规范文档模板（Mandatory）](#4-状态规范文档模板mandatory)
        * [4.1 State Definition（状态定义）](#41-state-definition状态定义)
            * [定义](#定义-4)
            * [必须包含的内容](#必须包含的内容)
            * [推荐结构](#推荐结构)
            * [规范约束](#规范约束)
            * [示例（抽象）](#示例抽象)
            * [校验规则](#校验规则-2)
        * [4.2 State Position in Flow（状态在流程中的位置）](#42-state-position-in-flow状态在流程中的位置)
            * [定义](#定义-5)
            * [必须描述的内容](#必须描述的内容-2)
            * [推荐结构](#推荐结构-1)
            * [规范约束](#规范约束-1)
            * [校验规则](#校验规则-3)
        * [4.3 Core Semantics（核心语义）](#43-core-semantics核心语义)
            * [定义](#定义-6)
            * [必须包含的内容](#必须包含的内容-1)
            * [推荐结构（强制三点）](#推荐结构强制三点)
            * [示例（抽象）](#示例抽象-1)
            * [规范约束](#规范约束-2)
            * [校验规则](#校验规则-4)
    * [5. Layout Contract（结构约束）](#5-layout-contract结构约束)
        * [5.1 必须描述的内容](#51-必须描述的内容)
        * [5.2 规则](#52-规则)
    * [6. Interaction Rules（交互规则）](#6-interaction-rules交互规则)
    * [7. Visual Rules（视觉规则）](#7-visual-rules视觉规则)
        * [7.1 基本原则](#71-基本原则)
        * [7.2 Material / Design Token 使用原则](#72-material--design-token-使用原则)
    * [8. Motion Contract（动效约束）](#8-motion-contract动效约束)
        * [8.1 动效分类](#81-动效分类)
        * [8.2 核心规则](#82-核心规则)
    * [9. Negative Requirements（禁止规则）](#9-negative-requirements禁止规则)
    * [10. Validation Checklist（验收规则）](#10-validation-checklist验收规则)
        * [10.1 Hard Requirements](#101-hard-requirements)
        * [10.2 Soft Requirements](#102-soft-requirements)
    * [11. 修改与治理规则（Governance）](#11-修改与治理规则governance)
        * [11.1 修改流程（强制）](#111-修改流程强制)
        * [11.2 禁止行为](#112-禁止行为)
    * [12. 规则地位声明](#12-规则地位声明)
    * [13. 一句话定义](#13-一句话定义)

<!-- TOC -->

---

## 0. 适用范围（Scope）

- 适用于整个 App 的所有模块与页面
- 不限定于某一具体业务模块（如 Capture / AI / Editor）
- 适用于：
    - 设计稿产出
    - UI 分析文档
    - AI UI 生成与校验
    - 工程实现与验收

---

## 1. 核心设计哲学（Design Philosophy）

### 1.1 语义优先于视觉（Semantic First）

- UI 首先是 **状态与语义的表达系统**，其次才是视觉呈现。
- 视觉只是语义的映射结果
- 禁止“为了好看而破坏语义”

**优先级：**

**`语义 > 交互 > 结构 > 视觉 > 动效`**

---

### 1.2 UI = 状态机，而不是画面集合

- 所有 UI 必须被建模为 **状态（State）**
- 页面只是状态在某一时刻的呈现
- 状态必须
    - **可命名**
    - **可推理**
    - **可验证**

---

## 2. 抽象层级模型（Abstraction Levels）

UI 设计与分析必须严格区分以下四个层级，不得混用职责：

- Level 1: **App-level Phase（应用级阶段）**
- Level 2: **Module-level State（模块级状态）**
- Level 3: **Page / Screen Layout（页面结构）**
- Level 4: **Component Behavior（组件行为）**

---

### 2.1 App-level Phase（应用级阶段）

#### 定义

用于描述**用户当前所处的宏观阶段**，与具体模块无关。

#### 解决的问题

- 用户“现在处在什么大阶段”
- 系统当前是稳定态、交互态还是执行态

#### 示例（非枚举）

- Pre-Interaction
- Active Interaction
- System Processing
- Result Consumption

---

### 2.2 Module-level State（模块级状态）

#### 定义

描述某模块在**特定阶段下**的**明确状态**。

#### 规则

- 每个模块必须定义自己的状态集合
- 模块状态 **必须映射到 App-level Phase**
- 模块状态是 UI 行为与能力的直接依据

#### 示例（以 Capture 模块为例）

- Capture / Input Ready
- Capture / Input Confirmed
- Capture / Processing
- Capture / Result Ready

---

### 2.3 Level 3：Page / Screen Layout（页面结构层）

#### 定义

**Page / Screen Layout** 用于定义：

> 在某一个模块状态下，  
> 页面被划分为哪些功能区域，以及这些区域之间的结构关系。

#### 解决的问题

- 页面由哪些 **功能区块（Region）** 组成？
- 每个区块的 **职责是什么**？
- 区块之间的 **主次关系、并列关系、依附关系** 是什么？

---

#### 必须描述的内容

每一个页面结构定义 **必须包含**：

- 区域名称（语义化，而非视觉化）
- 区域职责（做什么 / 不做什么）
- 区域可交互性（可 / 不可）
- 区域之间的相对层级

#### 示例（结构描述，非设计稿）

```
Page Layout:
- Header Region (Global Actions, Non-blocking)
- Main Content Region (Primary User Focus)
- Side Region (Context / Preview / Expectation)
- Overlay Region (State-specific, Optional)
```

---

#### 明确禁止

- ❌ 在本层描述具体组件（Button / TextField）
- ❌ 在本层描述颜色、字体、圆角
- ❌ 在本层描述动画细节

> Page / Screen Layout 描述的是 **结构语义**，不是实现细节。

---

#### 校验规则

- 若页面缺失必要的功能区 → ❌ Fail
- 若区块职责混乱（主区做次要事） → ❌ Fail
- 若页面结构随状态变化而随意重排 → ❌ Fail

---

### 2.4 Level 4：Component Behavior（组件行为层）

#### 定义

**Component Behavior** 用于定义：

> 单个 UI 组件在不同状态下的 **行为、能力与反馈规则**。

#### 解决的问题

- 组件 **能不能被操作**？
- 操作后 **会发生什么**？
- 当前状态下 **哪些能力被限制或开放**？

---

#### 必须描述的内容

- 可交互性（Enabled / Disabled / Frozen）
- 行为结果（触发状态变化 / 触发事件）
- 反馈方式（视觉 / 触觉 / 文案）

#### 示例（行为定义）

Component: Text Input

- Editable: true
- Accepts: Text / Paste / Drag
- On Edit: Updates Input Context
- On Disabled: Content visible, caret hidden

---

#### 行为与状态一致性原则

- 组件行为 **必须服从模块状态**
- 状态禁止的行为，组件层不得“偷偷开放”

##### 示例禁止行为

- Processing 状态下输入框仍可编辑
- Disabled Button 仍触发主行为

---

#### 明确禁止

- ❌ 组件行为自行决定状态含义
- ❌ 同一组件在同一状态下行为不一致
- ❌ 组件层引入新的状态语义

---

#### 校验规则

- 若组件行为与状态冲突 → ❌ Fail
- 若组件反馈不足以让用户理解结果 → ⚠️ Partial
- 若组件行为不可预测 → ❌ Fail

---

### 2.5 层级之间的责任边界

| 层级      | 层级名称                     | 允许决定   | 禁止决定 |
|---------|--------------------------|--------|------|
| Level 1 | 应用阶段（App-level Phase）    | 用户所处阶段 | 页面结构 |
| Level 2 | 模块状态（Module State）       | 模块状态   | 组件细节 |
| Level 3 | 页面结构（Page Layout / Slot） | 页面区块结构 | 组件行为 |
| Level 4 | 组件行为（Component Behavior） | 单组件行为  | 状态语义 |

---

> 补充一句（这是设计治理层面的关键）
>
> **大多数 UI 失控，都是因为 Page Layout 和 Component Behavior 的职责边界被打穿。**

## 3. 状态命名规范（State Naming Convention）

### 3.1 命名格式

<Module>_<Phase>_<Context>_<Mode>

#### 说明

- **Module**：模块名（Capture / Editor / Viewer / AI / …）
- **Phase**：所属应用阶段（Ready / Active / Processing / Result）
- **Context**：关键上下文（Input / Output / Selection / …）
- **Mode**（可选）：补充状态（Idle / Focused / Locked / …）

---

> `Context` 和 `Mode` 这两个概念的**设计意图**是为了在 UI 状态机中进一步**细化每个阶段的语义**，让你能精准描述 **特定场景** 下的应用状态。它们不只是视觉呈现的“外在”，更是背后 **行为与交互的核心**。

##### 1. 🧠 **Context** 的理解

**Context** 是对应用内状态和任务的 **细分语义描述**，描述了 **当前操作的焦点或目标**，常常决定了用户的交互方式。

| 示例                                                                           |
|------------------------------------------------------------------------------|
| **Input**：表示用户正在编辑或输入内容。此时用户与 UI 的交互重点是 **输入行为**。例如，文本框中的输入、媒体选择。            |
| **Output**：表示用户正在查看或接收输出内容。这通常是“展示”阶段，如图像预览、结果页面等。                           |
| **Selection**：指用户正在**选择**内容，如选择文件、选择文本、选择标签等。此时，UI 可能会高亮或改变选择状态，确保用户明确选择了内容。 |
| **Editing**：表示用户正在对某些内容进行修改，可能会触发实时预览、保存按钮或撤销操作。                             |

**具体示例：**

- **Capture_Ready_Input**：捕获阶段，用户正在输入或上传媒体。
- **Editor_Active_Output**：编辑阶段，当前正在处理并展示编辑后的内容。
- **Viewer_Processing_Output**：浏览器阶段，正在对显示的内容进行处理，如加载中、处理中的数据展示。

---

##### 2. 🧠 **Mode** 的理解

**Mode** 是 **操作模式的进一步区分**，描述了当前操作的 **特殊状态**，即在特定的 **context** 下，用户与系统的交互方式如何变化。

##### 常见 `Mode` 示例：

| Mode          | 说明                                              |
|---------------|-------------------------------------------------|
| **Idle**      | 空闲状态，表示没有正在进行的操作，UI 默认处于此状态。用户可以开始新的输入或操作。      |
| **Focused**   | 表示当前焦点在某个具体区域，通常是编辑模式下，用户正在与文本框、按钮等交互。          |
| **Locked**    | 锁定状态，用户无法继续编辑或交互，UI 可能处于冻结状态（如冻结输入框，等待 AI 处理等）。 |
| **Disabled**  | 与 Locked 类似，表示某些功能或按钮因某种原因被禁用，无法进行任何操作。         |
| **Completed** | 完成状态，表示任务已经完成，通常用于 **确认操作** 或 **查看结果** 状态。      |

**具体示例：**

- **Capture_Ready_Input_Idle**：捕获状态，输入框为空，用户尚未进行任何操作。
- **Editor_Active_Output_Focused**：用户正在编辑输出内容，聚焦在编辑区域。
- **Viewer_Processing_Output_Locked**：用户无法交互，UI 正在处理内容并等待结果。

---

##### 3. **Context 和 Mode 的关系**

**Context** 定义了当前的 **操作场景**，而 **Mode** 则进一步明确了**用户操作的限制或焦点状态**。

##### 4. 举个例子：

- **Capture_Ready_Input_Idle**：表示用户处于 **Capture 模块** 的 **Ready 阶段**，准备进行输入，且**当前没有交互（Idle）**，UI 等待用户输入。
- **Capture_Active_Input_Focused**：表示 **Capture 阶段**下，用户已经开始编辑（活动状态）并且焦点在输入区域。

---

##### 5. 📝 总结

- **Context** 关注的是 **交互任务的核心**，如输入、输出、选择等；
- **Mode** 关注的是 **操作状态的细化**，如空闲、专注、锁定等。

这两个层次的结合能够清晰地描述 **UI 当前的状态和用户与之交互的方式**，为设计和开发提供更具体的指导。

### 3.2 命名约束

- 不得在名称中混入视觉描述（如 Dark / Blur / Large）
- 不得使用含糊词（如 Normal / Default / Temp）
- 状态名必须可用于工程枚举 / sealed class

---

## 4. 状态规范文档模板（Mandatory）

**每一个 UI 状态，必须拥有独立的状态规范文档，并严格包含以下章节：**

```
1. State Definition（状态定义）
2. State Position in Flow（前后关系）
3. Core Semantics（核心语义）
4. Layout Contract（结构约束）
5. Interaction Rules（交互规则）
6. Visual Rules（视觉规则）
7. Motion Contract（动效约束）
8. Negative Requirements（明确禁止）
9. Validation Checklist（验收清单）
10. One-line Definition（一句话定义）
```

---

### 4.1 State Definition（状态定义）

#### 定义

**State Definition** 用于回答一个问题：

> 👉「这个状态 *是什么*？」

它是对某一 UI 状态的**客观、静态、不可歧义的定义**。

---

#### 必须包含的内容

- 状态的**唯一身份**
- 状态成立的**前提条件**
- 状态结束的**触发条件**

#### 推荐结构

```text
State Definition:
This state represents <what the system is currently doing>,
under the condition that <preconditions>,
before transitioning to <next possible states>.
```

---

#### 规范约束

- 必须使用 **现在时**
- 不得包含视觉、动效、布局描述
- 不得描述“用户看到什么”，而要描述“系统处于什么状态”

---

#### 示例（抽象）

> This state represents the system having accepted user input and
> actively processing it, while preserving the original user context
> without allowing further modification.

---

#### 校验规则

- 若定义无法区分于其他状态 → ❌ Fail
- 若定义中出现视觉/动效词 → ❌ Fail
- 若无法判断状态何时结束 → ⚠️ Partial

---

### 4.2 State Position in Flow（状态在流程中的位置）

#### 定义

**State Position in Flow** 用于回答：

> 👉「这个状态 *在整个流程中处在哪*？」

它明确该状态的 **前置状态、后继状态，以及是否可被跳过或回退**。

---

#### 必须描述的内容

- 直接前置状态（Predecessor）
- 合法后继状态（Successors）
- 是否允许：
    - 跳过（Skip）
    - 回退（Rollback）
    - 并发存在（Parallel）

---

#### 推荐结构

```
State Position in Flow:
- Previous: <State A>
- Current: <This State>
- Next: <State B | State C>
- Skip Allowed: Yes / No
- Rollback Allowed: Yes / No
```

---

#### 规范约束

- 状态流必须 **单向可推理**
- 不允许“任意跳转”
- 不允许未声明的隐式回退

---

#### 校验规则

- 存在无法到达的状态 → ❌ Fail
- 存在未定义来源的状态 → ❌ Fail
- 前后状态语义冲突 → ❌ Fail

---

### 4.3 Core Semantics（核心语义）

#### 定义

**Core Semantics** 是该状态的**存在理由**，用于回答：

> 👉「为什么这个状态 *必须存在*？」

它是 **设计、交互、视觉、动效的最终裁判依据**。

---

#### 必须包含的内容

- **用户认知层面的含义**
- **系统行为层面的含义**
- 此状态与其他状态**不可合并的原因**

---

#### 推荐结构（强制三点）

```
Core Semantics:
- User Perspective:
- System Perspective:
- Why This State Cannot Be Merged:
```

---

#### 示例（抽象）

- **User Perspective**  
  用户确信其输入已被接收，且无需重复操作

- **System Perspective**  
  系统正在进行不可中断的中间处理步骤

- **Why This State Cannot Be Merged**  
  合并将导致用户误以为系统空闲或失败

---

#### 规范约束

- Core Semantics 必须能：
    - 推导 Layout
    - 推导 Interaction
    - 推导 Motion
- 若删除该状态，必须能明确指出**用户体验损失点**

---

#### 校验规则

- 无法解释存在意义 → ❌ Fail
- 仅重复 Definition → ⚠️ Partial
- 不能指导设计决策 → ❌ Fail

---

> 三者关系说明（关键）
> ```
> State Definition  = 我是谁
> State Position   = 我在哪
> Core Semantics   = 我为什么存在
> ```
>
> 三者缺一不可：
>
> - 只有 Definition → 状态“像个名字”
> - 没有 Position → 流程“断裂”
> - 没有 Semantics → 设计“靠感觉”

---

> 一句话总结:
>
> **一个 UI State 若不能被定义、被定位、被证明存在价值，就不应存在。**

---

## 5. Layout Contract（结构约束）

### 5.1 必须描述的内容

- 区域划分（Main / Side / Header / Overlay）
- 层级关系（Z-Index / Focus Priority）
- 是否可交互

### 5.2 规则

- Layout Contract 描述的是 **职责，不是像素**
- 禁止在此层描述颜色、动画细节

---

## 6. Interaction Rules（交互规则）

- 明确哪些元素 **可交互 / 不可交互**
- 明确哪些状态 **允许编辑 / 冻结**
- 禁止“状态语义与交互能力冲突”

**示例禁止项**

- Processing 状态下仍可编辑输入
- Ready 状态下出现 Loading 行为

---

## 7. Visual Rules（视觉规则）

### 7.1 基本原则

- 视觉必须服务于状态语义
- 颜色、对比度、密度体现“当前重点”

### 7.2 Material / Design Token 使用原则

- `primary`：仅用于当前最重要的行动或焦点
- `surface / background`：承载内容
- `outline / variant`：结构与占位
- 禁止大面积滥用 primary

---

## 8. Motion Contract（动效约束）

### 8.1 动效分类

- **State Transition Motion**：状态切换
- **Focus Motion**：聚焦 / 强调
- **Processing Motion**：时间感知

### 8.2 核心规则

- 稳定态 → 禁止连续动效
- Processing 态 → 允许呼吸 / 旋转
- 禁止“动效制造焦虑感”

---

## 9. Negative Requirements（禁止规则）

每个状态文档必须明确：

- 不是什么状态
- 不允许出现哪些 UI 元素
- 不允许出现哪些动效或文案

**禁止项具有最高优先级。**

---

## 10. Validation Checklist（验收规则）

### 10.1 Hard Requirements

- 状态语义是否清晰
- 交互能力是否与状态一致
- 是否违反禁止规则

### 10.2 Soft Requirements

- 视觉层级是否稳定
- 用户是否能理解“现在发生了什么”

**任一 Hard Requirement 未满足 → 不合格**

---

## 11. 修改与治理规则（Governance）

### 11.1 修改流程（强制）

```
修改 UI
→ 先修改状态规范文档
→ Review 通过
→ 再修改设计稿
→ 最后修改实现
```

### 11.2 禁止行为

- ❌ 直接改 UI 不改文档
- ❌ 用实现反推语义
- ❌ 用视觉解释状态

---

## 12. 规则地位声明

> **本 Rulebook 是 UI 设计与分析的最高约束文档。**  
> 任何模块级规范、页面设计稿、AI 分析文档，均必须以此为基础派生。

---

## 13. 一句话定义

> **这是一个用来约束"如何设计、如何分析、如何修改 UI"的系统级规则，而不是某个页面的说明文档。**

---

## 14. 相关文档（Related Documents）

- [UI State Enumeration Dictionary v1.0](more/UI%20State%20Enumeration%20Dictionary%20v1.0.md)
- [状态命名规范 是具体在说那一层](more/状态命名规范%20是具体在说那一层.md)
- [规则文档的深度分析](more/规则文档的深度分析.md)

# 10 UI State Patterns

**Version:** 1.0
**Purpose:** 定义常见 UI 状态机模式，用于统一 UI 设计、工程实现和 AI UI 生成。

核心理念：

> UI = **Finite State Machine**

每个 Screen 应该匹配一个 **State Pattern**。

---

# Pattern 1 — List Pattern（列表模式）

适用于：

* 首页 Feed
* 商品列表
* 消息列表
* 内容浏览

## States

```id="st_list"
Loading
Content
Empty
Error
```

## State Flow

```id="flow_list"
Loading → Content
Loading → Empty
Loading → Error
```

## UI Layout

```
TopBar
ListView
RefreshControl
```

## Kotlin Example

```kotlin
sealed interface ListState<T> {

    data object Loading : ListState<Nothing>

    data class Content<T>(
        val items: List<T>
    ) : ListState<T>

    data object Empty : ListState<Nothing>

    data class Error(
        val message: String
    ) : ListState<Nothing>
}
```

---

# Pattern 2 — Detail Pattern（详情模式）

适用于：

* 文章详情
* 商品详情
* 用户资料
* 内容页

## States

```id="st_detail"
Loading
Content
Error
```

## State Flow

```id="flow_detail"
Loading → Content
Loading → Error
```

## UI Layout

```
TopBar
ContentView
BottomActions
```

---

# Pattern 3 — Form Pattern（表单模式）

适用于：

* 登录
* 注册
* 创建内容
* 编辑资料

## States

```id="st_form"
Idle
Editing
Submitting
Success
Error
```

## State Flow

```id="flow_form"
Idle → Editing
Editing → Submitting
Submitting → Success
Submitting → Error
```

## UI Layout

```
TopBar
FormFields
SubmitButton
```

## Kotlin Example

```kotlin
sealed interface FormState {

    data object Idle : FormState

    data object Editing : FormState

    data object Submitting : FormState

    data object Success : FormState

    data class Error(
        val message: String
    ) : FormState
}
```

---

# Pattern 4 — Search Pattern（搜索模式）

适用于：

* 搜索页面
* 过滤查询
* AI 查询

## States

```id="st_search"
Idle
Searching
Result
Empty
Error
```

## State Flow

```id="flow_search"
Idle → Searching
Searching → Result
Searching → Empty
Searching → Error
```

## UI Layout

```
SearchBar
ResultList
```

---

# Pattern 5 — AI / Processing Pattern（AI处理模式）

适用于：

* AI生成
* OCR
* 翻译
* 分析工具

## States

```id="st_ai"
Idle
Input
Processing
Result
Error
```

## State Flow

```id="flow_ai"
Idle → Input
Input → Processing
Processing → Result
Processing → Error
```

## UI Layout

```
InputArea
Preview
ProcessingIndicator
ResultView
```

---

# Pattern 6 — Wizard Pattern（步骤流程）

适用于：

* 注册流程
* onboarding
* 多步骤表单

## States

```id="st_wizard"
Step1
Step2
Step3
Complete
Error
```

## State Flow

```id="flow_wizard"
Step1 → Step2
Step2 → Step3
Step3 → Complete
```

## UI Layout

```
ProgressIndicator
StepContent
NextButton
```

---

# Pattern 7 — Editor Pattern（编辑器）

适用于：

* 文档编辑
* 内容创作
* 笔记编辑

## States

```id="st_editor"
Loading
Editing
Saving
Saved
Error
```

## State Flow

```id="flow_editor"
Loading → Editing
Editing → Saving
Saving → Saved
Saving → Error
```

## UI Layout

```
EditorToolbar
EditorArea
StatusBar
```

---

# Pattern 8 — Viewer Pattern（阅读模式）

适用于：

* PDF 阅读
* 文章阅读
* 图片浏览

## States

```id="st_viewer"
Loading
Ready
Reading
Error
```

## State Flow

```id="flow_viewer"
Loading → Ready
Ready → Reading
Loading → Error
```

## UI Layout

```
TopBar
ViewerArea
Controls
```

---

# Pattern 9 — Dashboard Pattern（仪表盘）

适用于：

* 数据面板
* Analytics
* 管理后台

## States

```id="st_dashboard"
Loading
Content
Partial
Error
```

## State Flow

```id="flow_dashboard"
Loading → Content
Loading → Partial
Loading → Error
```

## UI Layout

```
TopBar
CardsGrid
Charts
```

---

# Pattern 10 — Auth Pattern（认证流程）

适用于：

* 登录
* 注册
* 验证

## States

```id="st_auth"
Idle
Input
Verifying
Authenticated
Error
```

## State Flow

```id="flow_auth"
Idle → Input
Input → Verifying
Verifying → Authenticated
Verifying → Error
```

## UI Layout

```
Logo
Form
SubmitButton
```

---

# Pattern 总览

| Pattern   | States                                           |
|-----------|--------------------------------------------------|
| List      | Loading / Content / Empty / Error                |
| Detail    | Loading / Content / Error                        |
| Form      | Idle / Editing / Submitting / Success / Error    |
| Search    | Idle / Searching / Result / Empty / Error        |
| AI        | Idle / Input / Processing / Result / Error       |
| Wizard    | Step1 / Step2 / Step3 / Complete                 |
| Editor    | Loading / Editing / Saving / Saved / Error       |
| Viewer    | Loading / Ready / Reading / Error                |
| Dashboard | Loading / Content / Partial / Error              |
| Auth      | Idle / Input / Verifying / Authenticated / Error |

---

# Pattern Selection Guide

| Screen Type    | Pattern   | States                                           |
|----------------|-----------|--------------------------------------------------|
| Feed           | List      | Loading / Content / Empty / Error                |
| Detail Page    | Detail    | Loading / Content / Error                        |                                 |
| Login / Form   | Form      | Idle / Editing / Submitting / Success / Error    |
| Search Page    | Search    | Idle / Searching / Result / Empty / Error        |
| AI Tool        | AI        | Idle / Input / Processing / Result / Error       |
| Onboarding     | Wizard    | Step1 / Step2 / Step3 / Complete                 |
| Note Editor    | Editor    | Loading / Editing / Saving / Saved / Error       |
| Article Reader | Viewer    | Loading / Ready / Reading / Error                |
| Admin Panel    | Dashboard | Loading / Content / Partial / Error              |
| Login System   | Auth      | Idle / Input / Verifying / Authenticated / Error |

---

# Engineering Rule

每个 Screen 应该：

```id="rule_pattern"
匹配一个 Pattern
```

然后：

```id="rule_state"
Pattern → State Machine
```

---

# Example — Capture Screen

Capture 可以使用：

```
AI Pattern
```

State Machine：

```
Idle
Input
Processing
Result
Error
```

---

# Design Philosophy

UI 状态设计的核心：

> **State 表示 UI 阶段，而不是 UI细节。**


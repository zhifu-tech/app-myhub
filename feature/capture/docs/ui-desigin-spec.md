# Capture UI Design Spec

- **Version:** 1.0
- **Purpose:** 定义 Capture 的 UI 规范（状态机 + 布局 + 组件），并满足 UI Architecture Rulebook 要求。
- **Rulebook:** UI Architecture Rulebook

---

# 1. UI Spec DSL

```
Screen: Capture

States:
 - Input
 - Analyzing
 - AnalyzeFailed
 - ReviewEditing
 - Publishing
 - PublishFailed
 - PublishSuccess

Layout:
 HeaderRegion
 MainRegion
 OverlayRegion
 DrawerRegion

Components:
 TopBar
 InputPanel
 MediaGrid
 PreviewPanel
 ReviewEditor
 AiProcessingOverlay
 AiFailedOverlay
 PublishProcessingOverlay
 PublishFailedOverlay
 PublishSuccessOverlay
 InlineErrorBanner

Interactions:
 Input:
  StartCapture -> Analyzing
  AddMedia -> Input
  RemoveMedia -> Input
  UpdateInput -> Input
  UpdateIntent -> Input

 Analyzing:
  AiSuccess -> ReviewEditing
  AiFail -> AnalyzeFailed

 AnalyzeFailed:
  RetryAnalyze -> Analyzing
  BackToInput -> Input
  UpdateInput -> Input

 ReviewEditing:
  UpdateReview -> ReviewEditing
  Publish -> Publishing
  UpdateIntent -> ReviewEditing

 Publishing:
  PublishSuccess -> PublishSuccess
  PublishFail -> PublishFailed

 PublishFailed:
  RetryPublish -> Publishing
  BackToReview -> ReviewEditing

 PublishSuccess:
  Exit -> Input
```

---

# 2. 状态图

```mermaid
stateDiagram-v2
    [*] --> Input
    Input --> Analyzing: Start capture
    Analyzing --> ReviewEditing: AI success
    Analyzing --> AnalyzeFailed: AI failed
    AnalyzeFailed --> Analyzing: Retry
    AnalyzeFailed --> Input: Back to input
    ReviewEditing --> Publishing: Publish
    Publishing --> PublishSuccess: Publish success
    Publishing --> PublishFailed: Publish failed
    PublishFailed --> Publishing: Retry publish
    PublishFailed --> ReviewEditing: Back to review
    PublishSuccess --> Input: Exit
```

# 3. 校验

| 编号 | 规则                                | 通过（是/否） |
|----|-----------------------------------|---------|
| 1  | State Count ≤ 7                   |         |
| 2  | State Graph Valid                 |         |
|    | R1. 单入口状态                         |         |
|    | R2. 每个状态必须可达                      |         |
|    | R3. 每个状态必须可退出                     |         |
|    | R4. 禁止死循环                         |         |
|    | R5. 明确异常路径                        |         |
| 3  | Component State 不得混入 Screen State |         |
| 4  | Layout 不得依赖 State                 |         |

---

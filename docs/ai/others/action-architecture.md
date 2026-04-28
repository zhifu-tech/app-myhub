# AI Action Architecture

## 背景

当前 action 架构存在三个根本问题：

1. `ActionPlanner` 输出的是一组未分层的 `options`，业务动作、流程动作、视觉样式动作混在一起。
2. 渲染层需要再次根据 `ActionOptionType` 做 `filter`、`merge`、`switch`，才能判断哪个是主动作、次动作、内联动作。
3. 同一个 action 的业务含义、布局位置、UI 样式都耦合在 `ActionOptionType` 上，导致边界不清晰，组件职责失控。

例如：

- `InputActionPanel` 把 `component.options` 直接当成 secondary actions。
- `MediaActionPanel` 需要 `mergeOptions(...)` 把来自不同 component 的 action 拼回同一个 panel。
- `SecondaryActions` 仍然需要按 `ActionOptionType` 推断按钮样式。

这些现象说明当前模型没有在 planner 阶段完成语义建模。

## 目标

新的 action 架构必须满足以下原则：

1. `ActionPlanner` 一次性产出最终可渲染模型。
2. 不允许在渲染层做 `merge`、`filter`、重新分组、重新判定主次动作。
3. `Action` 是功能载体，可以挂载到任意 component。
4. 除入口分发外，内部不再依赖 `switch(ActionOptionType)` 做结构判定。
5. 业务动作、布局槽位、视觉样式三者彻底解耦。

## 新模型

### 1. ActionComponent

`ActionComponent` 表达一个独立的交互面板。

- `kind`：由哪个 panel 渲染
- `payload`：该 panel 的内容数据
- `actions`：该 panel 自己持有的动作槽位

```kotlin
data class ActionComponentSchema(
    val kind: ActionComponentKind,
    val payload: ActionPayload = ActionPayload.None,
    val actions: ActionSlots = ActionSlots(),
    val status: ActionComponentStatus = ActionComponentStatus.ACTIVE,
)
```

### 2. ActionSlots

`ActionSlots` 明确描述动作在当前 panel 中的位置。

```kotlin
data class ActionSlots(
    val primary: List<ActionItemSchema> = emptyList(),
    val secondary: List<ActionItemSchema> = emptyList(),
    val inline: List<ActionItemSchema> = emptyList(),
    val overlay: List<ActionItemSchema> = emptyList(),
)
```

定义：

- `primary`：panel 主操作区，通常是强调按钮
- `secondary`：panel 辅助操作区，通常是次级按钮
- `inline`：内容区内联动作，例如 tag chip、location 示例
- `overlay`：覆盖在内容上的动作，例如 media item 右上角删除

规则：

1. 一个动作只能出现在一个槽位里。
2. 一个 panel 只能消费自己的 `actions`。
3. 不允许从别的 component 借 action。
4. 不允许在 UI 层重排槽位。

### 3. ActionItem

`ActionItem` 只表示一个可执行动作。

```kotlin
data class ActionItemSchema(
    val id: String,
    val command: ActionCommand,
    val label: String,
    val style: ActionPresentationStyle = ActionPresentationStyle.Default,
    val enabled: Boolean = true,
)
```

字段职责：

- `id`：稳定标识，用于 Compose key、测试与追踪
- `command`：业务动作
- `label`：展示文案
- `style`：展示风格
- `enabled`：是否可交互

### 4. ActionCommand

`ActionCommand` 表达业务动作，不再承担布局和样式职责。

```kotlin
sealed interface ActionCommand {
    data object CaptureMedia : ActionCommand
    data object UploadMedia : ActionCommand
    data object ReplaceMedia : ActionCommand
    data object GenerateMedia : ActionCommand
    data object RemoveMedia : ActionCommand
    data object SkipMedia : ActionCommand
    data object SkipTags : ActionCommand
    data object Review : ActionCommand
    data object EditTitle : ActionCommand
    data object EditSummary : ActionCommand
    data object EditMedia : ActionCommand
    data object EditTags : ActionCommand
    data object EditLocation : ActionCommand
    data object Publish : ActionCommand
    data object SaveDraft : ActionCommand
    data object DeleteCard : ActionCommand
    data object NewCapture : ActionCommand
    data object ClearLocation : ActionCommand
    data class AddTag(val tag: String) : ActionCommand
    data class RemoveTag(val tag: String) : ActionCommand
    data class SetLocation(val location: String) : ActionCommand
    data class RemoveMediaAt(val index: Int) : ActionCommand
}
```

规则：

1. `ActionCommand` 只描述“做什么”。
2. 是否渲染成主按钮、次按钮、chip、危险按钮，不由 command 决定。
3. 不再通过字符串前缀解析业务参数。

### 5. ActionPresentationStyle

视觉样式单独建模。

```kotlin
enum class ActionPresentationStyle {
    Default,
    Tonal,
    Destructive,
    Chip,
    SelectedChip,
    InlineSuggestion,
    Overlay,
}
```

规则：

1. 样式是 planner 的输出，不由 panel 内推断。
2. 公共渲染器根据 `style` 选择组件形态。

### 6. ActionPayload

每个 panel 的内容数据放入 `payload`，而不是塞到 `options` 中再反推。

```kotlin
sealed interface ActionPayload {
    data object None : ActionPayload

    data class Review(
        val fieldOptions: List<ReviewFieldItem>,
    ) : ActionPayload

    data class Media(
        val hasMedia: Boolean,
        val mediaCount: Int,
        val missingMediaCount: Int,
        val isGenerating: Boolean,
    ) : ActionPayload

    data class Input(
        val field: Field,
        val currentValue: String,
    ) : ActionPayload

    data class Tags(
        val selectedTags: List<String>,
    ) : ActionPayload

    data class Location(
        val currentLocation: String,
        val examples: List<String>,
    ) : ActionPayload
}
```

注意：`payload` 只放渲染所需内容，不放动作。

## Component 规范

每个 component 使用统一架构，不同点只在 `payload` 和 `actions`。

### ReviewComponent

- `kind = REVIEW`
- `payload = Review`
- `actions.inline = 编辑字段入口`
- `actions.secondary = 空`

### PublishComponent

- `kind = PUBLISH`
- `actions.primary = Publish`
- `actions.secondary = SaveDraft / DeleteCard`

### MediaComponent

- `kind = MEDIA`
- `payload = Media`
- `actions.primary = Capture / Upload / Replace / Generate`
- `actions.secondary = SkipMedia 或 Review`
- `actions.overlay = RemoveMediaAt(index)`

### TagComponent

- `kind = TAGS`
- `payload = Tags`
- `actions.inline = tag chips`
- `actions.secondary = SkipTags 或 Review`

### InputComponent

- `kind = INPUT`
- `payload = Input`
- `actions.secondary = Review`

### LocationComponent

- `kind = LOCATION`
- `payload = Location`
- `actions.inline = location examples`
- `actions.secondary = ClearLocation / Review`

### QuickReplyComponent

- `kind = QUICK_REPLY`
- `actions.primary = 快捷动作`

## Planner 责任

`ActionPlanner` 是唯一允许做动作语义分组的地方。

它必须：

1. 决定当前 state 下有哪些 component。
2. 决定每个 component 的 `payload`。
3. 决定每个 action 属于哪个 slot。
4. 决定 action 的 label、style、enabled。

它不能：

1. 输出无语义的平铺 `options`。
2. 依赖渲染层再做二次分组。
3. 把同一个动作分发给多个 component 再要求 UI 合并。

## UI 渲染规则

### 入口分发

入口处允许唯一一次按 `kind` 分发：

```kotlin
when (component.kind) {
    REVIEW -> ReviewActionPanel(component)
    PUBLISH -> PublishActionPanel(component)
    MEDIA -> MediaActionPanel(component)
    TAGS -> TagActionPanel(component)
    INPUT -> InputActionPanel(component)
    LOCATION -> LocationActionPanel(component)
    QUICK_REPLY -> QuickReplyActionPanel(component)
}
```

### Panel 内部规则

1. Panel 只能消费自己收到的 `component`。
2. Panel 内不再按 `ActionOptionType` 做主次分类。
3. Panel 内可以按 `payload` 决定文案和结构，但不能重新解释 action 语义。
4. 公共按钮渲染器只根据 `ActionPresentationStyle` 渲染。

## 执行链路规则

`AIViewModel` 和 `CaptureOrchestrator` 的输入不再是字符串 action，而是 `ActionCommand`。

```kotlin
fun doAction(command: ActionCommand)
```

`StateGuard` 也改为基于 `ActionCommand` 判定：

```kotlin
fun canAction(state: ConversationState, command: ActionCommand): Boolean
```

规则：

1. action 执行层不再解析字符串前缀。
2. 所有 payload 型动作使用强类型 command。
3. 如果确实需要日志或埋点字符串，可以单独提供 `command.analyticsKey()`。

## 不再允许的模式

以下模式在新架构中全部禁止：

1. `mergeOptions(...)`
2. `message.quickReplyOptions()`
3. panel 内 `options.filter { ... }`
4. `SecondaryActions` 按 `ActionOptionType` 推断样式
5. planner 通过 `options + REVIEW`、`options + SKIP_XXX` 的方式表达槽位
6. action 通过字符串编码参数再在 orchestrator 中解析

## 落地策略

本次重构采用彻底替换方案：

1. 重写 action schema、planner、event/command、state guard。
2. 重写 `ActionComponent.kt` 和全部 panel。
3. 删除旧的 `mergeOptions`、`quickReplyOptions`、`parseActionEvent` 风格协议。
4. 统一 ViewModel 和 orchestrator 的 action 入口为强类型 command。
5. 所有 UI 只消费新 schema，不保留兼容分支。

## 验收标准

重构完成后必须满足：

1. 仓库中不再存在 `mergeOptions`。
2. 仓库中不再存在从多个 component 聚合 action 的逻辑。
3. 任意 panel 不再对 action 做主次分组推断。
4. `ActionPlanner` 输出的 component 可直接渲染。
5. `StateGuard` 和 `CaptureOrchestrator` 使用强类型 `ActionCommand`。
6. 所有 action panel 编译通过，行为与现有流程一致。

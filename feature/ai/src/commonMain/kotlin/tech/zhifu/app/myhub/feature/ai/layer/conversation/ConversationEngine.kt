package tech.zhifu.app.myhub.feature.ai.layer.conversation

import tech.zhifu.app.myhub.feature.ai.AIMsg
import tech.zhifu.app.myhub.feature.ai.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.CaptureState

class ConversationEngine(
    private val stateMachine: ConversationStateMachine,
    private val slotManager: SlotManager,
    private val actionPlanner: ActionPlanner,
    private val contextManager: ContextManager,
) {
    fun refreshActionComponents(context: ConversationContext): ConversationContext {
        return context.with(
            actionComponents = actionPlanner.actionsFor(
                state = context.state,
                draft = context.draft,
                missingFields = context.missingFields,
            )
        )
    }

    fun bootstrap(): ConversationContext {
        val context = contextManager.newContext()
        return context.with(
            messages = listOf(
                AIMsg(
                    id = contextManager.nextMessageId(),
                    role = AIMsg.Role.AI,
                    text = "请输入你要捕获的内容，我会在本地为你生成草稿并引导发布。",
                )
            )
        )
    }

    fun onDraftCreated(
        context: ConversationContext,
        userInput: String,
        intent: String,
        draft: CaptureDraft,
    ): ConversationContext {
        val missing = slotManager.missingFields(draft)
        val nextState = if (missing.isEmpty()) {
            stateMachine.transition(context.state, CaptureSignal.DraftReady)
        } else {
            stateMachine.transition(context.state, CaptureSignal.NeedMoreInfo)
        }
        val prompt = if (missing.isEmpty()) {
            "草稿已完成，是否进入发布确认？"
        } else {
            "我已完成草稿。还缺少：${missing.joinToString("、")}。请补充标签，或点击“跳过标签”。"
        }
        return context.with(
            state = nextState,
            sessionId = contextManager.nextSessionId(),
            draft = draft,
            missingFields = missing,
            messages = context.messages +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.USER, userInput) +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.AI, "意图识别：$intent") +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.AI, prompt),
            actionComponents = actionPlanner.actionsFor(nextState, draft = draft, missingFields = missing)
        )
    }

    fun onTagUpdated(
        context: ConversationContext,
        tag: String,
        draft: CaptureDraft,
    ): ConversationContext {
        val missing = slotManager.missingFields(draft)
        val nextState = if (missing.isEmpty()) {
            stateMachine.transition(context.state, CaptureSignal.DraftReady)
        } else {
            stateMachine.transition(context.state, CaptureSignal.NeedMoreInfo)
        }
        return context.with(
            state = nextState,
            draft = draft,
            missingFields = missing,
            messages = context.messages +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.USER, tag) +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.AI, "已添加标签：$tag"),
            actionComponents = actionPlanner.actionsFor(nextState, draft = draft, missingFields = missing)
        )
    }

    fun onMoveToReview(context: ConversationContext): ConversationContext {
        val nextState = stateMachine.transition(context.state, CaptureSignal.MoveToReview)
        return context.with(
            state = nextState,
            missingFields = emptyList(),
            messages = context.messages + AIMsg(
                id = contextManager.nextMessageId(),
                role = AIMsg.Role.AI,
                text = "已进入发布确认阶段。你可以直接发布，或先编辑标题。",
            ),
            actionComponents = actionPlanner.actionsFor(nextState, draft = context.draft, missingFields = emptyList()),
        )
    }

    fun onManualEdit(context: ConversationContext): ConversationContext {
        val nextState = CaptureState.MANUAL_EDIT
        return context.with(
            state = nextState,
            messages = context.messages + AIMsg(
                id = contextManager.nextMessageId(),
                role = AIMsg.Role.AI,
                text = "请输入你希望的标题。",
            ),
            actionComponents = actionPlanner.actionsFor(
                nextState,
                draft = context.draft,
                missingFields = context.missingFields
            )
        )
    }

    fun onAiUnavailable(
        context: ConversationContext,
        userInput: String,
        reason: String,
        draft: CaptureDraft,
    ): ConversationContext {
        val nextState = CaptureState.MANUAL_EDIT
        return context.with(
            state = nextState,
            sessionId = contextManager.nextSessionId(),
            draft = draft,
            missingFields = slotManager.missingFields(draft),
            messages = context.messages +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.USER, userInput) +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.SYSTEM, "AI 不可用：$reason") +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.AI, "已切换到手工编辑，请先输入标题后发布。"),
            actionComponents = actionPlanner.actionsFor(
                nextState,
                draft = draft,
                missingFields = slotManager.missingFields(draft)
            )
        )
    }

    fun onTitleUpdated(context: ConversationContext, title: String, draft: CaptureDraft): ConversationContext {
        val nextState = CaptureState.CARD_REVIEW
        return context.with(
            state = nextState,
            draft = draft,
            messages = context.messages +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.USER, title) +
                AIMsg(contextManager.nextMessageId(), AIMsg.Role.AI, "标题已更新，可以发布。"),
            actionComponents = actionPlanner.actionsFor(nextState, draft = draft, missingFields = context.missingFields)
        )
    }

    fun onPublishing(context: ConversationContext): ConversationContext {
        val nextState = stateMachine.transition(context.state, CaptureSignal.PublishRequested)
        return context.with(
            state = nextState,
            actionComponents = actionPlanner.actionsFor(
                nextState,
                draft = context.draft,
                missingFields = context.missingFields
            ),
        )
    }

    fun onPublished(context: ConversationContext, title: String): ConversationContext {
        val nextState = stateMachine.transition(context.state, CaptureSignal.PublishSucceeded)
        return context.with(
            state = nextState,
            messages = context.messages + AIMsg(
                id = contextManager.nextMessageId(),
                role = AIMsg.Role.AI,
                text = "发布完成（本地提交）：$title",
            ),
            actionComponents = actionPlanner.actionsFor(nextState, draft = context.draft, missingFields = emptyList()),
        )
    }

    fun onReset(): ConversationContext {
        val next = contextManager.newContext()
        return next.with(
            messages = listOf(
                AIMsg(
                    id = contextManager.nextMessageId(),
                    role = AIMsg.Role.AI,
                    text = "新会话已开始，请输入你要捕获的内容。",
                )
            ),
            actionComponents = actionPlanner.actionsFor(next.state),
        )
    }

    fun onBlockedInput(context: ConversationContext): ConversationContext {
        return context.with(
            messages = context.messages + AIMsg(
                id = contextManager.nextMessageId(),
                role = AIMsg.Role.SYSTEM,
                text = "当前阶段不接受自由输入，请使用下方动作。",
            )
        )
    }

    fun onBlockedAction(context: ConversationContext, action: String): ConversationContext {
        return context.with(
            messages = context.messages + AIMsg(
                id = contextManager.nextMessageId(),
                role = AIMsg.Role.SYSTEM,
                text = "动作不可用：$action（当前状态 ${context.state.name}）",
            )
        )
    }
}

data class ConversationContext(
    val sessionId: String? = null,
    val state: CaptureState = CaptureState.IDLE,
    val messages: List<AIMsg> = emptyList(),
    val draft: CaptureDraft? = null,
    val missingFields: List<String> = emptyList(),
    val actionComponents: List<ActionComponentSchema> = emptyList(),
)

private fun ConversationContext.with(
    sessionId: String? = this.sessionId,
    state: CaptureState = this.state,
    messages: List<AIMsg> = this.messages,
    draft: CaptureDraft? = this.draft,
    missingFields: List<String> = this.missingFields,
    actionComponents: List<ActionComponentSchema> = this.actionComponents,
): ConversationContext = copy(
    sessionId = sessionId,
    state = state,
    messages = messages,
    draft = draft,
    missingFields = missingFields,
    actionComponents = actionComponents,
)

class ContextManager {
    private var sessionSeq = 0L
    private var messageSeq = 0L

    fun newContext(): ConversationContext = ConversationContext(
        sessionId = nextSessionId(),
        state = CaptureState.IDLE,
        actionComponents = emptyList(),
    )

    fun nextSessionId(): String {
        sessionSeq += 1
        return "capture_session_$sessionSeq"
    }

    fun nextMessageId(): String {
        messageSeq += 1
        return "m$messageSeq"
    }
}

package tech.zhifu.app.myhub.feature.ai.layer.conversation

import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPlanner
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ConversationContext
import tech.zhifu.app.myhub.feature.ai.layer.conversation.slot.SlotManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.Signal
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.StateMachine
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Message

class ConversationEngine(
    private val stateMachine: StateMachine,
    private val slotManager: SlotManager,
    private val actionPlanner: ActionPlanner,
    private val contextManager: ContextManager,
) {
    fun refreshActionComponents(
        context: ConversationContext
    ): ConversationContext = context.copy(
        actionComponents = actionPlanner.actionsFor(
            state = context.state,
            draft = context.draft,
            missingFields = context.missingFields,
        )
    )

    fun bootstrap(): ConversationContext {
        val context = contextManager.newContext()
        return context.copy(
            messages = listOf(
                Message(
                    id = contextManager.nextMessageId(),
                    role = Message.Role.AI,
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
        reasoning: String? = null,
    ): ConversationContext {
        val missing = slotManager.missingFields(draft)
        val nextState = if (missing.isEmpty()) {
            stateMachine.transition(
                current = context.state,
                signal = Signal.DraftReady
            )
        } else {
            stateMachine.transition(
                current = context.state,
                signal = Signal.NeedMoreInfo
            )
        }
        return context.copy(
            state = nextState,
            sessionId = contextManager.nextSessionId(),
            draft = draft,
            missingFields = missing,
            messages = context.messages + buildList {
                add(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.USER,
                        text = userInput
                    )
                )
                if (reasoning.isNullOrBlank().not()) {
                    add(
                        Message(
                            id = contextManager.nextMessageId(),
                            role = Message.Role.SYSTEM,
                            text = "AI 思考过程：\n${reasoning.trim()}",
                        )
                    )
                }
                add(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = "意图识别：$intent"
                    )
                )
                add(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = if (missing.isEmpty()) {
                            "草稿已完成，是否进入发布确认？"
                        } else {
                            "我已完成草稿。还缺少：${missing.joinToString("、")}。请补充标签，或点击“跳过标签”。"
                        }
                    )
                )
            },
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = draft,
                missingFields = missing
            )
        )
    }

    fun onTagUpdated(
        context: ConversationContext,
        tag: String,
        draft: CaptureDraft,
    ): ConversationContext {
        val missing = slotManager.missingFields(draft)
        val nextState = if (missing.isEmpty()) {
            stateMachine.transition(
                current = context.state,
                signal = Signal.DraftReady
            )
        } else {
            stateMachine.transition(
                current = context.state,
                signal = Signal.NeedMoreInfo
            )
        }
        return context.copy(
            state = nextState,
            draft = draft,
            missingFields = missing,
            messages = context.messages +
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.USER,
                        text = tag
                    ),
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = "已添加标签：$tag"
                    )
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = draft,
                missingFields = missing
            )
        )
    }

    fun onMoveToReview(
        context: ConversationContext
    ): ConversationContext {
        val nextState = stateMachine.transition(
            current = context.state,
            signal = Signal.MoveToReview
        )
        return context.copy(
            state = nextState,
            missingFields = emptyList(),
            messages = context.messages +
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = "已进入发布确认阶段。你可以直接发布，或先编辑标题。",
                    ),
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = context.draft,
                missingFields = emptyList()
            ),
        )
    }

    fun onManualEdit(context: ConversationContext): ConversationContext {
        val nextState = ConversationState.MANUAL_EDIT
        return context.copy(
            state = nextState,
            messages = context.messages +
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = "请输入你希望的标题。",
                    )
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
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
        val nextState = ConversationState.MANUAL_EDIT
        return context.copy(
            state = nextState,
            sessionId = contextManager.nextSessionId(),
            draft = draft,
            missingFields = slotManager.missingFields(draft),
            messages = context.messages +
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.USER,
                        text = userInput
                    ),
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.SYSTEM,
                        text = "AI 不可用：$reason"
                    ),
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = "已切换到手工编辑，请先输入标题后发布。"
                    ),
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = draft,
                missingFields = slotManager.missingFields(draft)
            )
        )
    }

    fun onTitleUpdated(
        context: ConversationContext,
        title: String,
        draft: CaptureDraft
    ): ConversationContext {
        val nextState = ConversationState.CARD_REVIEW
        return context.copy(
            state = nextState,
            draft = draft,
            messages = context.messages +
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.USER,
                        text = title
                    ),
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = "标题已更新，可以发布。"
                    ),
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = draft,
                missingFields = context.missingFields
            )
        )
    }

    fun onPublishing(
        context: ConversationContext
    ): ConversationContext {
        val nextState = stateMachine.transition(
            current = context.state,
            signal = Signal.PublishRequested
        )
        return context.copy(
            state = nextState,
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = context.draft,
                missingFields = context.missingFields
            ),
        )
    }

    fun onPublished(
        context: ConversationContext,
        title: String
    ): ConversationContext {
        val nextState = stateMachine.transition(
            current = context.state,
            signal = Signal.PublishSucceeded
        )
        return context.copy(
            state = nextState,
            messages = context.messages +
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = "发布完成（本地提交）：$title",
                    )
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = context.draft,
                missingFields = emptyList()
            ),
        )
    }

    fun onReset(): ConversationContext {
        val next = contextManager.newContext()
        return next.copy(
            messages =
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        text = "新会话已开始，请输入你要捕获的内容。",
                    )
                ),
            actionComponents = actionPlanner.actionsFor(
                state = next.state
            ),
        )
    }

    fun onBlockedInput(
        context: ConversationContext
    ): ConversationContext = context.copy(
        messages = context.messages +
            listOf(
                Message(
                    id = contextManager.nextMessageId(),
                    role = Message.Role.SYSTEM,
                    text = "当前阶段不接受自由输入，请使用下方动作。",
                )
            )
    )

    fun onBlockedAction(
        context: ConversationContext,
        action: String
    ): ConversationContext {
        return context.copy(
            messages = context.messages +
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.SYSTEM,
                        text = "动作不可用：$action（当前状态 ${context.state.name}）",
                    )
                )
        )
    }
}

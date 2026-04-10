package tech.zhifu.app.myhub.feature.ai.layer.conversation

import org.jetbrains.compose.resources.StringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPlanner
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ConversationContext
import tech.zhifu.app.myhub.feature.ai.layer.conversation.slot.SlotManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.Signal
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.StateMachine
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_ai_unavailable
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_blocked_action
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_blocked_input
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_bootstrap
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_draft_complete
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_draft_need_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_draft_need_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_draft_need_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_intent
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_manual_edit
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_media_attached
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_media_not_selected
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_move_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_published
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_reasoning
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_reset
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_field
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_switch_manual
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_tag_added_complete
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_tag_added_need_more
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_title_updated

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
                    textRes = Res.string.feature_ai_msg_bootstrap,
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
                            textRes = Res.string.feature_ai_msg_reasoning,
                            textArgs = listOf(reasoning.trim()),
                        )
                    )
                }
                add(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        textRes = Res.string.feature_ai_msg_intent,
                        textArgs = listOf(intent),
                    )
                )
                add(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        textRes = if (missing.isEmpty()) {
                            Res.string.feature_ai_msg_draft_complete
                        } else {
                            missingMessageRes(missing)
                        },
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
                        textRes = if (missing.isEmpty()) {
                            Res.string.feature_ai_msg_tag_added_complete
                        } else {
                            Res.string.feature_ai_msg_tag_added_need_more
                        },
                        textArgs = listOf(tag),
                    )
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = draft,
                missingFields = missing
            )
        )
    }

    fun onMediaUpdated(
        context: ConversationContext,
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
                        role = Message.Role.AI,
                        textRes = if (draft.mediaAssets.isEmpty()) {
                            Res.string.feature_ai_msg_media_not_selected
                        } else {
                            Res.string.feature_ai_msg_media_attached
                        },
                        textArgs = if (draft.mediaAssets.isEmpty()) {
                            emptyList()
                        } else {
                            listOf(draft.mediaAssets.size)
                        },
                    )
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = draft,
                missingFields = missing
            )
        )
    }

    fun onFieldSkipped(
        context: ConversationContext,
        field: Field,
    ): ConversationContext {
        val remainedMissing = context.missingFields.filterNot { it == field }
        val nextState = if (remainedMissing.isEmpty()) {
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
            missingFields = remainedMissing,
            messages = context.messages +
                listOf(
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        textRes = when (field) {
                            Field.MEDIA -> Res.string.feature_ai_msg_skip_media
                            Field.TAGS -> Res.string.feature_ai_msg_skip_tags
                            else -> Res.string.feature_ai_msg_skip_field
                        },
                        textArgs = if (field == Field.MEDIA || field == Field.TAGS) {
                            emptyList()
                        } else {
                            listOf(field.name)
                        },
                    )
                ),
            actionComponents = actionPlanner.actionsFor(
                state = nextState,
                draft = context.draft,
                missingFields = remainedMissing
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
                        textRes = Res.string.feature_ai_msg_move_review,
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
                        textRes = Res.string.feature_ai_msg_manual_edit,
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
                        textRes = Res.string.feature_ai_msg_ai_unavailable,
                        textArgs = listOf(reason),
                    ),
                    Message(
                        id = contextManager.nextMessageId(),
                        role = Message.Role.AI,
                        textRes = Res.string.feature_ai_msg_switch_manual,
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
                        textRes = Res.string.feature_ai_msg_title_updated,
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
                        textRes = Res.string.feature_ai_msg_published,
                        textArgs = listOf(title),
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
                        textRes = Res.string.feature_ai_msg_reset,
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
                    textRes = Res.string.feature_ai_msg_blocked_input,
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
                        textRes = Res.string.feature_ai_msg_blocked_action,
                        textArgs = listOf(action, context.state.name),
                    )
                )
        )
    }

    private fun missingMessageRes(
        missing: List<Field>
    ): StringResource = when (missing.firstOrNull()) {
        Field.MEDIA -> Res.string.feature_ai_msg_draft_need_media
        Field.TAGS -> Res.string.feature_ai_msg_draft_need_tags
        Field.TITLE -> Res.string.feature_ai_msg_draft_need_title
        else -> Res.string.feature_ai_msg_draft_need_tags
    }
}

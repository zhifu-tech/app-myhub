package tech.zhifu.app.myhub.feature.ai.layer.conversation

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPlanner
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextChangeCallback
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ConversationContext
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.appendMessages
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ofMessage
import tech.zhifu.app.myhub.feature.ai.layer.conversation.slot.SlotManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.Signal
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.StateMachine
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredDraftSession
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
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_draft_saved
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_intent
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_location_cleared
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_location_editing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_location_updated
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_manual_edit
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_media_attached
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_published
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_restore_session
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_field
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_summary_editing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_summary_updated
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_tag_added_need_more
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_tag_removed
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_tags_editing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_title_updated
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_type_updated
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

/**
 * ConversationEngine 只负责「会话上下文编排」：
 * 1. 追加消息
 * 2. 触发状态机转移
 * 3. 基于状态+草稿重算 action components
 *
 * 与 CaptureOrchestrator 的边界：
 * - Orchestrator 负责外部交互（Provider/Tool/Storage）与错误分类。
 * - Engine 负责把结果落到 UI 可消费的 ConversationContext。
 *
 * 新增 action 时遵循固定路径：
 * 1) 在 Orchestrator 解析 action 并拿到业务结果（通常是 draft 或失败原因）。
 * 2) 调用 Engine 的三类入口之一：
 *    - 消息型: emitMessage(...)
 *    - 状态型：transitionWithPlan(...)
 *    - 复合型：append message 后 transitionWithPlan(...)
 * 3) 不直接在 Orchestrator 手动改 state/actionComponents。
 */
class ConversationEngine(
    private val stateMachine: StateMachine,
    private val slotManager: SlotManager,
    private val actionPlanner: ActionPlanner,
    private val contextManager: ContextManager,
) {
    private val commandMutex = Mutex()

    fun addContextChangeCallback(callback: ContextChangeCallback) {
        contextManager.addContextChangeCallback(callback)
    }

    fun removeContextChangeCallback(callback: ContextChangeCallback) {
        contextManager.removeContextChangeCallback(callback)
    }

    fun currentDraft(): CaptureDraft? = contextManager.context.draft

    fun currentMissingFields(): List<Field> = contextManager.context.missingFields

    fun currentState(): ConversationState = contextManager.context.state

    fun currentSessionId(): String? = contextManager.context.sessionId

    fun currentFocusField(): Field? = contextManager.context.focusField

    suspend fun applyCaptureAnalysisResult(
        draft: CaptureDraft,
        intent: String? = null,
        reasoning: String? = null,
    ) = serialize {
        with(contextManager) {
            val missingFields = slotManager.missingFields(draft)
            val messages = buildList {
                if (reasoning.isNullOrBlank().not()) {
                    add(
                        ofMessage(
                            role = Message.Role.THINKING,
                            text = reasoning.trim(),
                        )
                    )
                }
                if (intent.isNullOrBlank().not()) {
                    add(
                        ofMessage(
                            role = Message.Role.AI,
                            textRes = Res.string.feature_ai_msg_intent,
                            textArgs = listOf(intent),
                        )
                    )
                }
            }
            transitionWithPlan(
                signal = if (missingFields.isEmpty()) Signal.DRAFT_COMPLETE else Signal.DRAFT_INCOMPLETE,
                draft = draft,
                missingFields = missingFields,
                focusField = missingFields.firstOrNull(),
                messages = messages,
                reasoningStatus = false,
                reasoningText = "",
            )
        }
    }

    suspend fun applyCaptureTypeUpdated(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_type_updated,
                    textArgs = listOf(draft.captureType?.value.orEmpty()),
                )
            )
        }
    }

    suspend fun applyLocationCleared(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_location_cleared,
                )
            )
        }
    }

    suspend fun applyLocationUpdated(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_location_updated,
                    textArgs = listOf(draft.location?.name.orEmpty()),
                )
            )
        }
    }

    suspend fun applyMediaUpdated(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_media_attached,
                    textArgs = listOf(draft.mediaAssets.size),
                )
            )
        }
    }

    suspend fun applyPublished(
        title: String
    ) = serialize {
        with(contextManager) {
            transitionWithPlan(
                signal = Signal.PUBLISH_SUCCEEDED,
                focusField = context.focusField,
                missingFields = context.missingFields,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_published,
                    textArgs = listOf(title),
                )
            )
        }
    }

    suspend fun applyReasoningProgress(
        reasoning: String = "",
    ) = serialize {
        with(contextManager) {
            notifyContextChange(
                updated = context.copy(
                    reasoningStatus = true,
                    reasoningText = reasoning,
                )
            )
        }
    }

    suspend fun applyRestoredSession(
        restored: StoredDraftSession?,
    ): Boolean {
        val restoredSession = restored ?: return false
        val draft = restoredSession.draft ?: return false
        // 检验状态能否恢复
        restoredSession.state.takeIf {
            it in setOf(
                ConversationState.INFO_COLLECT,
                ConversationState.CARD_REVIEW,
                ConversationState.MANUAL_EDIT,
            )
        } ?: return false
        serialize {
            with(contextManager) {
                val messages = appendMessages {
                    add(
                        ofMessage(
                            role = Message.Role.SYSTEM,
                            textRes = Res.string.feature_ai_msg_restore_session,
                        )
                    )
                }
                notifyContextChange(
                    updated = context.copy(
                        sessionId = restoredSession.sessionId,
                        state = restoredSession.state,
                        draft = restoredSession.draft,
                        messages = messages,
                    )
                )
                when (restoredSession.state) {
                    // 信息收集，focusField为空，则通过 transitionToNextState 来恢复
                    ConversationState.INFO_COLLECT -> transitionToNextState(draft = draft)
                    // 手动编辑，放弃编辑，进入review
                    ConversationState.MANUAL_EDIT -> transitionToNextState(draft = draft)
                    // 如果草稿是卡片审核的，则恢复审核状态，通过 Signal.DRAFT_COMPLETE 恢复
                    ConversationState.CARD_REVIEW -> transitionWithPlan(
                        signal = Signal.DRAFT_COMPLETE,
                        draft = draft,
                        message = contextManager.ofMessage(
                            role = Message.Role.AI,
                            text = "已进入发布确认阶段，你可以直接发布！"
                        )
                    )

                    else -> Unit
                }
            }
        }
        return true
    }

    suspend fun applySummaryUpdated(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_summary_updated,
                )
            )
        }
    }

    suspend fun applyTagRemoved(
        tag: String,
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_tag_removed,
                    textArgs = listOf(tag),
                )
            )
        }
    }

    suspend fun applyTagUpdated(
        tag: String,
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_tag_added_need_more,
                    textArgs = listOf(tag),
                )
            )
        }
    }

    suspend fun applyTitleUpdated(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_title_updated,
                )
            )
        }
    }

    suspend fun appendUserInputMessage(
        text: String,
    ) = serialize {
        with(contextManager) {
            emitMessage(
                ofMessage(
                    role = Message.Role.USER,
                    text = text,
                )
            )
        }
    }

    suspend fun commandCaptureAnalysisStarted(
    ) = serialize {
        transitionWithPlan(
            signal = Signal.START_CAPTURE,
        )
    }

    suspend fun commandEnterManualEdit(
        field: Field,
    ) = serialize {
        with(contextManager) {
            transitionWithPlan(
                signal = Signal.REQUEST_MANUAL_EDIT,
                focusField = field,
                message = when (field) {
                    Field.MEDIA -> ofMessage(
                        role = Message.Role.USER,
                        text = "请选择你想要的图片"
                    )

                    Field.TAGS -> ofMessage(
                        role = Message.Role.SYSTEM,
                        textRes = Res.string.feature_ai_msg_tags_editing
                    )

                    Field.SUMMARY -> ofMessage(
                        role = Message.Role.SYSTEM,
                        textRes = Res.string.feature_ai_msg_summary_editing
                    )

                    Field.LOCATION -> ofMessage(
                        role = Message.Role.SYSTEM,
                        textRes = Res.string.feature_ai_msg_location_editing
                    )

                    else -> ofMessage(
                        role = Message.Role.SYSTEM,
                        textRes = Res.string.feature_ai_msg_manual_edit
                    )
                },
            )
        }
    }

    suspend fun commandPublishRequested(
    ) = serialize {
        with(contextManager) {
            transitionWithPlan(
                signal = Signal.REQUEST_PUBLISH,
                focusField = null,
                message = ofMessage(
                    role = Message.Role.AI,
                    text = "发布中..."
                ),
            )
        }
    }

    suspend fun commandResetSession(
    ) = serialize {
        with(contextManager) {
            notifyContextChange(updated = newContext())
            emitMessage(
                ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_bootstrap,
                )
            )
        }
    }

    suspend fun commandReview(
    ) = serialize {
        with(contextManager) {
            transitionWithPlan(
                signal = Signal.REQUEST_REVIEW,
                focusField = null,
                message = ofMessage(
                    role = Message.Role.AI,
                    text = "已进入发布确认阶段，你可以直接发布！"
                ),
            )
        }
    }

    suspend fun commandSkipField(
        field: Field,
    ) = serialize {
        with(contextManager) {
            transitionToNextState(
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_skip_field,
                    textArgs = listOf(field.name),
                )
            )
        }
    }

    suspend fun emitAiUnavailable(
        reason: String,
    ) = serialize {
        emitMessages(
            message = contextManager.ofMessage(
                role = Message.Role.SYSTEM,
                textRes = Res.string.feature_ai_msg_ai_unavailable,
                textArgs = listOf(reason),
            ),
            message2 = contextManager.ofMessage(
                role = Message.Role.AI,
                text = "已切换到手工编辑"// TODO: 国际化
            )
        )
    }

    suspend fun emitBlockedAction(
        action: String
    ) = serialize {
        with(contextManager) {
            emitMessage(
                ofMessage(
                    role = Message.Role.SYSTEM,
                    textRes = Res.string.feature_ai_msg_blocked_action,
                    textArgs = listOf(action, contextManager.context.state.name),
                )
            )
        }
    }

    suspend fun emitBlockedInput(
    ) = serialize {
        with(contextManager) {
            emitMessage(
                ofMessage(
                    role = Message.Role.SYSTEM,
                    textRes = Res.string.feature_ai_msg_blocked_input,
                )
            )
        }
    }

    suspend fun emitBootstrap(
    ) = serialize {
        with(contextManager) {
            emitMessage(
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_bootstrap,
                )
            )
        }
    }

    suspend fun emitDraftSaved(
    ) = serialize {
        with(contextManager) {
            emitMessage(
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_draft_saved,
                )
            )
        }
    }

    private suspend fun <T> serialize(
        block: suspend () -> T,
    ): T = commandMutex.withLock { block() }

    /**
     * 消息型入口：只追加消息，不触发状态变更。
     */
    private suspend fun emitMessage(
        message: Message,
    ) = with(contextManager) {
        logger.debug { "emitMessage: $message" }
        notifyContextChange(
            updated = context.copy(
                messages = appendMessages {
                    add(message)
                }
            )
        )
    }

    private suspend fun emitMessages(
        message: Message,
        message2: Message,
    ) = with(contextManager) {
        notifyContextChange(
            updated = context.copy(
                messages = appendMessages {
                    add(message)
                    add(message2)
                }
            )
        )
    }

    /**
     * 状态型入口：通过状态机转移，并统一重算 action components。
     * 可选携带 messages，形成"消息+状态"的复合更新。
     */
    private suspend fun transitionWithPlan(
        signal: Signal,
        draft: CaptureDraft? = null,
        focusField: Field? = null,
        missingFields: List<Field>? = null,
        message: Message? = null,
        messages: List<Message>? = null,
        reasoningStatus: Boolean? = null,
        reasoningText: String? = null,
    ) = with(contextManager) {
        val current = context
        val nextState = stateMachine.transition(
            current = current.state,
            signal = signal,
        )
        val nextDraft = draft ?: current.draft
        val nextMissing = missingFields ?: current.missingFields
        val nextFocus = focusField

        val nextActions = actionPlanner.actionsFor(
            state = nextState,
            draft = nextDraft,
            missingFields = nextMissing,
            focusField = nextFocus,
        )

        val nextMessages = message?.let { current.messages + it }
            ?: messages?.let { current.messages + it }
            ?: current.messages

        notifyContextChange(
            updated = current.copy(
                state = nextState,
                draft = nextDraft,
                focusField = nextFocus,
                missingFields = nextMissing,
                actionComponents = nextActions,
                messages = nextMessages,
                reasoningStatus = reasoningStatus ?: current.reasoningStatus,
                reasoningText = reasoningText ?: current.reasoningText,
            )
        )
        // 状态变更之后，追加状态变化消息
        onPostContextChanged(
            cur = context,
            pre = current
        )
    }

    suspend fun transitionToNextState(
        draft: CaptureDraft? = currentDraft(),
        messages: List<Message>? = null,
        message: Message? = null,
    ) = with(contextManager) {
        when (context.state) {
            ConversationState.IDLE -> {
                transitionWithPlan(
                    signal = Signal.START_CAPTURE,
                    draft = draft,
                    message = message,
                    messages = messages
                )
            }

            ConversationState.INFO_COLLECT -> {
                val nextMissionFields = context.missingFields.filterNot { it == context.focusField }
                logger.debug { "transitionToNextState: $nextMissionFields" }
                val nextFocusField = nextMissionFields.firstOrNull()
                val signal = if (nextMissionFields.isEmpty()) {
                    Signal.DRAFT_COMPLETE
                } else {
                    Signal.DRAFT_INCOMPLETE
                }
                transitionWithPlan(
                    signal = signal,
                    draft = draft,
                    focusField = nextFocusField,
                    missingFields = nextMissionFields,
                    message = message,
                    messages = messages,
                )
            }

            ConversationState.MANUAL_EDIT -> {
                transitionWithPlan(
                    signal = Signal.REQUEST_REVIEW,
                    draft = draft,
                    focusField = null,
                    message = message,
                    messages = messages,
                )
            }

            ConversationState.CARD_REVIEW -> {
                transitionWithPlan(
                    signal = Signal.REQUEST_PUBLISH,
                    draft = draft,
                    focusField = null,
                    message = message,
                    messages = messages,
                )
            }

            else -> Unit
        }
    }

    private suspend fun onPostContextChanged(
        cur: ConversationContext,
        pre: ConversationContext?
    ) {
        // 在进入某个状态/或者某个状态发生变化时，需要追加信息
        when (cur.state) {
            ConversationState.INFO_COLLECT -> {
                logger.debug { "onPostContextChanged: ${cur.focusField} , ${pre?.focusField}" }
                if (pre?.focusField != cur.focusField) {
                    logger.debug { "onPostContextChanged: context = ${cur.focusField}, ${cur.focusField}" }
                    emitMessage(
                        contextManager.ofMessage(
                            role = Message.Role.AI,
                            textRes = when (cur.focusField) {
                                Field.MEDIA -> Res.string.feature_ai_msg_draft_need_media
                                Field.TAGS -> Res.string.feature_ai_msg_draft_need_tags
                                Field.SUMMARY -> Res.string.feature_ai_msg_summary_editing
                                Field.TITLE -> Res.string.feature_ai_msg_draft_need_title
                                else -> Res.string.feature_ai_msg_draft_complete
                            }
                        )
                    )
                }
            }

            ConversationState.CARD_REVIEW -> {
                if (pre?.state != ConversationState.CARD_REVIEW) {
                    emitMessage(
                        contextManager.ofMessage(
                            role = Message.Role.AI,
                            text = "已进入发布确认阶段，你可以直接发布！"
                        )
                    )
                }
            }

            else -> Unit
        }
    }
}

package tech.zhifu.app.myhub.feature.ai.layer.conversation

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPlanner
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextChangeCallback
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ContextManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ConversationContext
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.appendMessages
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.bindActionComponents
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.canOwnActionComponents
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ofMessage
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.replaceMessage
import tech.zhifu.app.myhub.feature.ai.layer.conversation.slot.SlotManager
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.Signal
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.StateMachine
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredDraftSession
import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
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
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_location_cleared
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_location_editing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_location_updated
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_manual_edit
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_media_editing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_move_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_published
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_publishing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_restore_session
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_review_input_help
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_current_step
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_skip_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_summary_editing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_summary_updated
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_tag_added_need_more
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_tag_removed
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_tags_editing
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_msg_title_editing
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

    fun currentDraft(): CaptureDraft = contextManager.context.draft

    fun currentState(): ConversationState = contextManager.context.state

    fun currentSessionId(): String? = contextManager.context.sessionId

    fun currentFocusField(): Field? = contextManager.context.focusField

    suspend fun applyCaptureAnalysisResult(
        draft: CaptureDraft,
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
            applyDraftMutation(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_location_cleared,
                ),
                manualEditField = Field.LOCATION,
            )
        }
    }

    suspend fun applyLocationUpdated(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            applyDraftMutation(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_location_updated,
                    textArgs = listOf(draft.location?.name.orEmpty()),
                ),
                manualEditField = Field.LOCATION,
            )
        }
    }

    suspend fun applyMediaUpdated(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            when (context.state) {
                ConversationState.MANUAL_EDIT -> {
                    updateActionOwnerMessage(
                        draft = draft,
                        focusField = Field.MEDIA,
                        message = ofMessage(
                            role = Message.Role.AI,
                            textRes = Res.string.feature_ai_msg_media_editing,
                            editingField = Field.MEDIA,
                        ),
                    )
                }

                ConversationState.INFO_COLLECT -> {
                    transitionToNextState(
                        draft = draft,
                    )
                }

                else -> {
                    updateActionOwnerMessage(
                        draft = draft,
                        focusField = Field.MEDIA,
                        message = ofMessage(
                            role = Message.Role.AI,
                            textRes = Res.string.feature_ai_msg_media_editing,
                            editingField = Field.MEDIA,
                        ),
                    )
                }
            }
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
                            textRes = Res.string.feature_ai_msg_move_review,
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
            applyDraftMutation(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_summary_updated,
                ),
                manualEditField = Field.SUMMARY,
            )
        }
    }

    suspend fun applyTagRemoved(
        tag: String,
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            applyDraftMutation(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_tag_removed,
                    textArgs = listOf(tag),
                ),
                manualEditField = Field.TAGS,
            )
        }
    }

    suspend fun applyTagUpdated(
        tag: String,
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            applyDraftMutation(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_tag_added_need_more,
                    textArgs = listOf(tag),
                ),
                manualEditField = Field.TAGS,
            )
        }
    }

    suspend fun applyTitleUpdated(
        draft: CaptureDraft,
    ) = serialize {
        with(contextManager) {
            applyDraftMutation(
                draft = draft,
                message = ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_title_updated,
                ),
                manualEditField = Field.TITLE,
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

    suspend fun appendUserMediaMessage(
        mediaAssets: List<CaptureMediaAsset>,
        text: String = "",
    ) = serialize {
        if (mediaAssets.isEmpty()) return@serialize
        with(contextManager) {
            emitMessage(
                ofMessage(
                    role = Message.Role.USER,
                    text = text,
                    mediaAssets = mediaAssets,
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
                        role = Message.Role.AI,
                        textRes = if (context.draft.mediaAssets.isEmpty()) {
                            Res.string.feature_ai_msg_draft_need_media
                        } else {
                            Res.string.feature_ai_msg_media_editing
                        },
                        editingField = field,
                    )

                    Field.TITLE -> ofMessage(
                        role = Message.Role.AI,
                        textRes = Res.string.feature_ai_msg_title_editing,
                        editingField = field,
                    )

                    Field.TAGS -> ofMessage(
                        role = Message.Role.AI,
                        textRes = Res.string.feature_ai_msg_tags_editing,
                        editingField = field,
                    )

                    Field.SUMMARY -> ofMessage(
                        role = Message.Role.AI,
                        textRes = Res.string.feature_ai_msg_summary_editing,
                        editingField = field,
                    )

                    Field.LOCATION -> ofMessage(
                        role = Message.Role.AI,
                        textRes = Res.string.feature_ai_msg_location_editing,
                        editingField = field,
                    )

                    else -> ofMessage(
                        role = Message.Role.AI,
                        textRes = Res.string.feature_ai_msg_manual_edit,
                        editingField = field,
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
                    textRes = Res.string.feature_ai_msg_publishing,
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
                    textRes = when (field) {
                        Field.MEDIA -> Res.string.feature_ai_msg_skip_media
                        Field.TAGS -> Res.string.feature_ai_msg_skip_tags
                        Field.TITLE -> Res.string.feature_ai_msg_skip_title
                        Field.SUMMARY -> Res.string.feature_ai_msg_skip_summary
                        Field.LOCATION -> Res.string.feature_ai_msg_skip_location
                        Field.UNKNOWN -> Res.string.feature_ai_msg_skip_current_step
                    },
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
                textRes = Res.string.feature_ai_msg_manual_edit,
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

    suspend fun emitReviewInputHelp(
    ) = serialize {
        with(contextManager) {
            emitMessage(
                ofMessage(
                    role = Message.Role.AI,
                    textRes = Res.string.feature_ai_msg_review_input_help,
                ),
                bindCurrentActions = true,
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
        bindCurrentActions: Boolean = false,
    ) = with(contextManager) {
        logger.debug { "emitMessage: $message" }
        val nextOwnerMessageId = if (bindCurrentActions && message.canOwnActionComponents()) {
            message.id
        } else {
            context.actionOwnerMessageId
        }
        val nextMessages = appendMessages {
            add(message)
        }.let { messages ->
            if (bindCurrentActions && message.canOwnActionComponents()) {
                messages.bindActionComponents(
                    components = context.actionComponents,
                    ownerMessageId = nextOwnerMessageId,
                )
            } else {
                messages
            }
        }
        notifyContextChange(
            updated = context.copy(
                messages = nextMessages,
                actionOwnerMessageId = nextOwnerMessageId,
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
            focusField = nextFocus,
        )
        val actionOwnerMessageId = when {
            message?.canOwnActionComponents() == true -> message.id
            messages != null -> messages.lastOrNull(Message::canOwnActionComponents)?.id
            else -> current.actionOwnerMessageId
        }

        val nextMessages = (message?.let { current.messages + it }
            ?: messages?.let { current.messages + it }
            ?: current.messages)
            .bindActionComponents(
                components = nextActions,
                ownerMessageId = actionOwnerMessageId,
            )

        notifyContextChange(
            updated = current.copy(
                state = nextState,
                draft = nextDraft,
                focusField = nextFocus,
                missingFields = nextMissing,
                actionComponents = nextActions,
                messages = nextMessages,
                actionOwnerMessageId = actionOwnerMessageId,
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
                if (pre?.focusField != cur.focusField) {
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
                        ),
                        bindCurrentActions = true,
                    )
                }
            }

            ConversationState.CARD_REVIEW -> {
                if (pre?.state != ConversationState.CARD_REVIEW) {
                    emitMessage(
                        contextManager.ofMessage(
                            role = Message.Role.AI,
                            textRes = Res.string.feature_ai_msg_move_review,
                        ),
                        bindCurrentActions = true,
                    )
                }
            }

            else -> Unit
        }
    }

    private suspend fun ContextManager.applyDraftMutation(
        draft: CaptureDraft,
        message: Message,
        manualEditField: Field,
    ) {
        if (context.state == ConversationState.MANUAL_EDIT) {
            updateActionOwnerMessage(
                draft = draft,
                focusField = manualEditField,
                message = message,
            )
        } else {
            transitionToNextState(
                draft = draft,
                message = message,
            )
        }
    }

    private suspend fun ContextManager.refreshCurrentState(
        draft: CaptureDraft,
        focusField: Field? = context.focusField,
        message: Message? = null,
    ) {
        val current = context
        val nextMissing = slotManager.missingFields(draft)
        val nextActions = actionPlanner.actionsFor(
            state = current.state,
            draft = draft,
            focusField = focusField,
        )
        val actionOwnerMessageId = when {
            message?.canOwnActionComponents() == true -> message.id
            current.actionOwnerMessageId != null -> current.actionOwnerMessageId
            else -> current.messages.lastOrNull { existing ->
                existing.canOwnActionComponents() && existing.actionComponents.isNotEmpty()
            }?.id
        }
        val nextMessages = (current.messages + listOfNotNull(message))
            .bindActionComponents(
                components = nextActions,
                ownerMessageId = actionOwnerMessageId,
            )
        notifyContextChange(
            updated = current.copy(
                draft = draft,
                focusField = focusField,
                missingFields = nextMissing,
                actionComponents = nextActions,
                messages = nextMessages,
                actionOwnerMessageId = actionOwnerMessageId,
            )
        )
    }

    private suspend fun ContextManager.updateActionOwnerMessage(
        draft: CaptureDraft,
        focusField: Field? = context.focusField,
        message: Message,
    ) {
        val current = context
        val nextMissing = slotManager.missingFields(draft)
        val nextActions = actionPlanner.actionsFor(
            state = current.state,
            draft = draft,
            focusField = focusField,
        )
        val ownerMessageId = current.actionOwnerMessageId
            ?: current.messages.lastOrNull { existing ->
                existing.canOwnActionComponents() && existing.actionComponents.isNotEmpty()
            }?.id

        if (ownerMessageId == null) {
            refreshCurrentState(
                draft = draft,
                focusField = focusField,
                message = message,
            )
            return
        }

        val nextMessages = current.messages
            .replaceMessage(ownerMessageId) { existing ->
                existing.copy(
                    text = message.text,
                    textRes = message.textRes,
                    textArgs = message.textArgs,
                    mediaAssets = message.mediaAssets,
                    editingField = message.editingField ?: existing.editingField,
                )
            }
            .bindActionComponents(
                components = nextActions,
                ownerMessageId = ownerMessageId,
            )
        notifyContextChange(
            updated = current.copy(
                draft = draft,
                focusField = focusField,
                missingFields = nextMissing,
                actionComponents = nextActions,
                messages = nextMessages,
                actionOwnerMessageId = ownerMessageId,
            )
        )
    }
}

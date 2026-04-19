package tech.zhifu.app.myhub.feature.ai.orchestrator

import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.model.util.generateUUId
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisError
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisResult
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.MutableProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouter
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.ProviderTelemetry
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ConversationEngine
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionEvent
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.parseActionEvent
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.StateGuard
import tech.zhifu.app.myhub.feature.ai.layer.storage.StorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredAiJob
import tech.zhifu.app.myhub.feature.ai.layer.storage.job.failed
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandDispatcher
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult
import tech.zhifu.app.myhub.feature.ai.model.CaptureType
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.orchestrator.patch.PatchApplier
import tech.zhifu.app.myhub.feature.ai.orchestrator.storage.autoSaveDraftSession
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * CaptureOrchestrator 只负责「外部系统编排」：
 * 1. 校验输入/动作是否可执行（StateGuard）
 * 2. 调用 Provider / Tool / Storage
 * 3. 将结果交给 ConversationEngine 落到会话上下文
 *
 * 新增 action 的标准流程：
 * 1) 在 onAction 中解析 ActionEvent 并分发到 handleXxx。
 * 2) handleXxx 内只做外部调用与错误分类，不直接改 state。
 * 3) 成功后调用 ConversationEngine 的标准入口（消息型/状态型/复合型）。
 */
class CaptureOrchestrator(
    private val providerRouter: ProviderRouter,
    val conversationEngine: ConversationEngine,
    private val stateGuard: StateGuard,
    private val providerAnalysisExecutor: ProviderAnalysisExecutor,
    private val providerConfigSource: MutableProviderConfigSource,
    private val providerTelemetry: ProviderTelemetry,
    private val toolCommandDispatcher: ToolCommandDispatcher,
    private val patchApplier: PatchApplier,
    val storageGateway: StorageGateway,
) {
    init {
        autoSaveDraftSession()
    }

    fun providerMode(): ProviderMode = providerRouter.mode

    suspend fun updateProviderConfig(
        config: ProviderRoutingConfig
    ) = providerConfigSource.update(config)

    suspend fun bootstrap() {
        val restored = storageGateway.loadLatestDraftSession()
        if (false && conversationEngine.applyRestoredSession(restored = restored)) {
            logger.debug { "bootstrap: restored session ${restored?.sessionId}" }
        } else {
            conversationEngine.emitBootstrap()
        }
    }

    suspend fun onInput(input: String) {
        val text = input.trim()
        if (text.isBlank()) return
        val state = conversationEngine.currentState()
        if (!stateGuard.canInput(state)) {
            conversationEngine.emitBlockedInput()
            logger.debug { "onInput: blocked input $state" }
            return
        }
        when (state) {
            ConversationState.IDLE,
            ConversationState.COMPLETE -> handleCaptureAnalysis(text)

            ConversationState.INFO_COLLECT,
            ConversationState.MANUAL_EDIT -> handleDraftFieldInput(text)

            else -> {
                logger.debug { "onInput: blocked input $state" }
                conversationEngine.emitBlockedInput()
            }
        }
    }

    suspend fun onAction(action: String) {
        // 这是新增 action 的首个入口：所有 action 必须先经过 guard，再解析为 ActionEvent。
        val state = conversationEngine.currentState()
        if (!stateGuard.canAction(state = state, action = action)) {
            conversationEngine.emitBlockedAction(action = action)
            return
        }
        when (val event = parseActionEvent(action)) {
            is ActionEvent.Option -> when (event.type) {
                ActionOptionType.CLEAR_LOCATION -> handleClearLocation()
                ActionOptionType.DELETE_CARD -> handleDeleteCard()
                ActionOptionType.EDIT_LOCATION -> conversationEngine.commandEnterManualEdit(Field.LOCATION)
                ActionOptionType.EDIT_MEDIA -> conversationEngine.commandEnterManualEdit(Field.MEDIA)
                ActionOptionType.EDIT_SUMMARY -> conversationEngine.commandEnterManualEdit(Field.SUMMARY)
                ActionOptionType.EDIT_TAGS -> conversationEngine.commandEnterManualEdit(Field.TAGS)
                ActionOptionType.EDIT_TITLE -> conversationEngine.commandEnterManualEdit(Field.TITLE)
                ActionOptionType.NEW_CAPTURE -> {
                    conversationEngine.commandResetSession()
                }

                ActionOptionType.PUBLISH -> handlePublish()
                ActionOptionType.REMOVE_MEDIA -> handleRemoveAllMedia()
                ActionOptionType.REPLACE_MEDIA -> handleAttachMedia(replaceExisting = true)
                ActionOptionType.REVIEW -> conversationEngine.commandReview()
                ActionOptionType.SAVE_DRAFT -> conversationEngine.emitDraftSaved()
                ActionOptionType.SKIP_MEDIA -> conversationEngine.commandSkipField(Field.MEDIA)
                ActionOptionType.SKIP_TAGS -> conversationEngine.commandSkipField(Field.TAGS)
                ActionOptionType.UPLOAD_MEDIA -> handleAttachMedia(replaceExisting = false)
                else -> Unit
            }

            is ActionEvent.AddTag -> handleTagSelected(event.tag)
            is ActionEvent.RemoveTag -> handleTagRemoved(event.tag)
            is ActionEvent.RemoveMediaAt -> handleMediaRemovedAt(event.index)
            is ActionEvent.SetCaptureType -> handleCaptureTypeSelected(event.type, action)
            is ActionEvent.SetLocation -> handleLocationSelected(event.location)
            else -> Unit
        }
    }

    private suspend fun handleDraftFieldInput(text: String) {
        val draft = conversationEngine.currentDraft()
        conversationEngine.appendUserInputMessage(text = text)
        when (conversationEngine.currentFocusField()) {
            Field.TITLE -> {
                when (val result = toolCommandDispatcher.execute(
                    command = ToolCommand.UpdateTitle(draft = draft, title = text)
                )) {
                    is ToolCommandResult.DraftUpdated -> {
                        delay(100.milliseconds)
                        conversationEngine.applyTitleUpdated(result.draft)
                    }

                    is ToolCommandResult.Failed -> {
                        conversationEngine.emitBlockedAction(
                            action = "update_title:${result.code}:${result.message}"
                        )
                    }

                    else -> Unit
                }
            }

            Field.TAGS -> {
                when (val result = toolCommandDispatcher.execute(
                    command = ToolCommand.AddTag(draft = draft, tag = text)
                )) {
                    is ToolCommandResult.DraftUpdated -> {
                        conversationEngine.applyTagUpdated(
                            tag = text,
                            draft = result.draft,
                        )
                    }

                    is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                        action = "add_tag:${result.code}:${result.message}"
                    )

                    else -> Unit
                }
            }

            Field.SUMMARY -> {
                when (val result = toolCommandDispatcher.execute(
                    command = ToolCommand.UpdateSummary(draft = draft, summary = text)
                )) {
                    is ToolCommandResult.DraftUpdated -> {
                        conversationEngine.applySummaryUpdated(
                            draft = result.draft,
                        )
                    }

                    is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                        action = "update_summary:${result.code}:${result.message}"
                    )

                    else -> Unit
                }
            }

            Field.LOCATION -> {
                when (val result = toolCommandDispatcher.execute(
                    command = ToolCommand.UpdateLocation(draft = draft, location = text)
                )) {
                    is ToolCommandResult.DraftUpdated -> {
                        conversationEngine.applyLocationUpdated(
                            draft = result.draft
                        )
                    }

                    is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                        action = "update_location:${result.code}:${result.message}"
                    )

                    else -> Unit
                }
            }

            Field.MEDIA -> conversationEngine.emitBlockedInput()
            else -> conversationEngine.emitBlockedInput()
        }
    }

    private suspend fun handlePublish() {
        val draft = conversationEngine.currentDraft()
        conversationEngine.commandPublishRequested()
        when (val result = toolCommandDispatcher.execute(
            command = ToolCommand.PublishCard(draft = draft)
        )) {
            is ToolCommandResult.Published -> {
                storageGateway.clearDraftSession(
                    sessionId = conversationEngine.currentSessionId().orEmpty()
                )
                conversationEngine.applyPublished(title = result.title)
            }

            is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                action = "${ActionOptionType.PUBLISH.value}:${result.code}:${result.message}"
            )

            else -> Unit
        }
    }

    private suspend fun handleDeleteCard() {
        storageGateway.clearDraftSession(
            sessionId = conversationEngine.currentSessionId().orEmpty()
        )
        conversationEngine.commandResetSession()
    }

    private suspend fun handleClearLocation() {
        val draft = conversationEngine.currentDraft()
        when (val result = toolCommandDispatcher.execute(
            command = ToolCommand.ClearLocation(draft = draft)
        )) {
            is ToolCommandResult.DraftUpdated -> {
                conversationEngine.applyLocationCleared(result.draft)
            }

            is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                action = "clear_location:${result.code}:${result.message}"
            )

            else -> Unit
        }
    }

    private suspend fun handleAttachMedia(replaceExisting: Boolean) {
        val draft = conversationEngine.currentDraft()
        val sourceDraft = if (replaceExisting) draft.copy(mediaAssets = emptyList()) else draft
        when (val result = toolCommandDispatcher.execute(
            command = ToolCommand.AttachPickedMedia(draft = sourceDraft)
        )) {
            is ToolCommandResult.MediaAttached -> {
                conversationEngine.applyMediaUpdated(result.draft)
            }

            is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                action = "attach_media:${result.code}:${result.message}"
            )

            else -> Unit
        }
    }

    private suspend fun handleRemoveAllMedia() {
        val draft = conversationEngine.currentDraft()
        conversationEngine.applyMediaUpdated(
            draft = draft.copy(mediaAssets = emptyList())
        )
    }

    private suspend fun handleMediaRemovedAt(index: Int) {
        val draft = conversationEngine.currentDraft()
        if (index !in draft.mediaAssets.indices) {
            conversationEngine.emitBlockedAction(action = ActionOptionType.encodeRemoveMediaAt(index))
            return
        }
        conversationEngine.applyMediaUpdated(
            draft = draft.copy(
                mediaAssets = draft.mediaAssets.filterIndexed { i, _ -> i != index }
            )
        )
    }

    private suspend fun handleTagSelected(tag: String) {
        val draft = conversationEngine.currentDraft()
        when (val result = toolCommandDispatcher.execute(
            command = ToolCommand.AddTag(draft = draft, tag = tag)
        )) {
            is ToolCommandResult.DraftUpdated -> {
                conversationEngine.applyTagUpdated(
                    tag = tag,
                    draft = result.draft,
                )
            }

            is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                action = "add_tag:${result.code}:${result.message}"
            )

            else -> Unit
        }
    }

    private suspend fun handleTagRemoved(tag: String) {
        val draft = conversationEngine.currentDraft()
        when (val result = toolCommandDispatcher.execute(
            command = ToolCommand.RemoveTag(draft = draft, tag = tag)
        )) {
            is ToolCommandResult.DraftUpdated -> {
                conversationEngine.applyTagRemoved(
                    tag = tag,
                    draft = result.draft,
                )
            }

            is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                action = "remove_tag:${result.code}:${result.message}"
            )

            else -> Unit
        }
    }

    private suspend fun handleCaptureTypeSelected(type: String, rawAction: String) {
        val draft = conversationEngine.currentDraft()
        val parsedType = CaptureType.fromValue(type) ?: run {
            conversationEngine.emitBlockedAction(rawAction)
            return
        }
        when (val result = toolCommandDispatcher.execute(
            command = ToolCommand.UpdateType(draft = draft, type = parsedType)
        )) {
            is ToolCommandResult.DraftUpdated -> {
                conversationEngine.applyCaptureTypeUpdated(
                    draft = result.draft,
                )
            }

            is ToolCommandResult.Failed -> {
                conversationEngine.emitBlockedAction(
                    action = "set_type:${result.code}:${result.message}"
                )
            }

            else -> Unit
        }
    }

    private suspend fun handleLocationSelected(location: String) {
        val draft = conversationEngine.currentDraft()
        when (val result = toolCommandDispatcher.execute(
            command = ToolCommand.UpdateLocation(draft = draft, location = location)
        )) {
            is ToolCommandResult.DraftUpdated -> {
                conversationEngine.applyLocationUpdated(
                    draft = result.draft,
                )
            }

            is ToolCommandResult.Failed -> conversationEngine.emitBlockedAction(
                action = "set_location:${result.code}:${result.message}"
            )

            else -> Unit
        }
    }

    private suspend fun handleCaptureAnalysis(text: String) {
        conversationEngine.appendUserInputMessage(text = text)
        conversationEngine.commandCaptureAnalysisStarted()

        val route = providerRouter.resolveRoute()
        val request = ProviderAnalysisRequest(
            language = "zh-CN",
            inputText = text,
        )
        val aiJob = StoredAiJob(
            id = "job_${generateUUId()}",
            provider = route.mode.name.lowercase(),
            requestJson = Json.encodeToString(request),
            status = "running",
        )
        storageGateway.saveAiJob(snapshot = aiJob)

        if (!route.available) {
            providerTelemetry.recordFailure(
                mode = route.mode,
                latencyMs = 0,
                category = ProviderAnalysisError.UNAVAILABLE,
            )
            storageGateway.saveAiJob(
                snapshot = aiJob.failed(
                    reason = route.reason ?: "AI_UNAVAILABLE"
                )
            )
            conversationEngine.emitAiUnavailable(
                reason = route.reason ?: "AI_UNAVAILABLE",
            )
            delay(300.milliseconds)
            conversationEngine.applyCaptureAnalysisResult(
                draft = conversationEngine.currentDraft(),
                reasoning = "",
            )
            return
        }

        val startedAt = Clock.System.now().toEpochMilliseconds()
        when (val result = providerAnalysisExecutor.analyze(
            route = route,
            request = request,
            onReasoning = { reasoning ->
                conversationEngine.applyReasoningProgress(reasoning = reasoning)
            },
        )) {
            is ProviderAnalysisResult.Success -> {
                storageGateway.saveAiJob(
                    snapshot = aiJob.copy(
                        responseJson = "",
                        status = "succeeded",
                    )
                )

                val applied = patchApplier.apply(
                    draft = conversationEngine.currentDraft(),
                    ops = result.data.patches,
                )
                conversationEngine.applyCaptureAnalysisResult(
                    draft = applied.draft,
                    reasoning = result.data.reasoning,
                )
            }

            is ProviderAnalysisResult.Failed -> {
                providerTelemetry.recordFailure(
                    mode = route.mode,
                    latencyMs = Clock.System.now().toEpochMilliseconds() - startedAt,
                    category = result.category,
                )
                storageGateway.saveAiJob(
                    snapshot = aiJob.failed(
                        reason = result.reason,
                        category = result.category.name.lowercase()
                    )
                )
                conversationEngine.emitAiUnavailable(reason = "AI不可用")
                delay(100.milliseconds)
                conversationEngine.applyCaptureAnalysisResult(
                    draft = conversationEngine.currentDraft(),
                )
            }
        }
    }
}

package tech.zhifu.app.myhub.feature.ai

import tech.zhifu.app.myhub.feature.ai.layer.agent.CaptureAgent
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.ProviderTelemetry
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisContext
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisInput
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisResult
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderErrorCategory
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.MutableProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouter
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ConversationContext
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ConversationEngine
import tech.zhifu.app.myhub.feature.ai.layer.conversation.StateGuard
import tech.zhifu.app.myhub.feature.ai.layer.storage.CaptureStorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredAiJob
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolDispatcher
import tech.zhifu.app.myhub.feature.ai.layer.tool.ToolResult
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig
import kotlin.time.Clock

class CaptureOrchestrator(
    private val providerRouter: ProviderRouter,
    private val conversationEngine: ConversationEngine,
    private val stateGuard: StateGuard,
    private val captureAgent: CaptureAgent,
    private val providerAnalysisExecutor: ProviderAnalysisExecutor,
    private val providerConfigSource: MutableProviderConfigSource,
    private val providerTelemetry: ProviderTelemetry,
    private val toolDispatcher: ToolDispatcher,
    private val storageGateway: CaptureStorageGateway,
) {
    suspend fun bootstrap(): ConversationContext {
        val restored = storageGateway.loadLatestDraftSession()
        if (restored != null) {
            val base = conversationEngine.bootstrap()
            val restoredMessages = base.messages + AIMsg(
                id = "m_restore",
                role = AIMsg.Role.SYSTEM,
                text = "已恢复上次会话草稿。",
            )
            val restoredContext = base.copy(
                sessionId = restored.sessionId,
                state = restored.state,
                draft = restored.draft,
                missingFields = restored.missingFields,
                messages = restoredMessages,
            )
            return conversationEngine.refreshActionComponents(restoredContext)
        }
        return conversationEngine.bootstrap()
    }

    suspend fun onInput(context: ConversationContext, input: String): ConversationContext {
        val text = input.trim()
        if (text.isBlank()) return context
        if (!stateGuard.canInput(context.state)) {
            return conversationEngine.onBlockedInput(context)
        }
        val updated = when (context.state) {
            CaptureState.IDLE,
            CaptureState.COMPLETE -> {
                val route = providerRouter.resolveRoute()
                val jobId = "job_${Clock.System.now().toEpochMilliseconds()}"
                storageGateway.saveAiJob(
                    StoredAiJob(
                        id = jobId,
                        provider = route.mode.name.lowercase(),
                        requestJson = """{"task":"capture_analysis","input":"${text.escapeJson()}"}""",
                        status = "running",
                    )
                )
                if (!route.available) {
                    providerTelemetry.recordFailure(
                        mode = route.mode,
                        latencyMs = 0,
                        category = ProviderErrorCategory.UNAVAILABLE,
                    )
                    storageGateway.saveAiJob(
                        StoredAiJob(
                            id = jobId,
                            provider = route.mode.name.lowercase(),
                            requestJson = """{"task":"capture_analysis","input":"${text.escapeJson()}"}""",
                            responseJson = """{"error":"${(route.reason ?: "AI_UNAVAILABLE").escapeJson()}"}""",
                            status = "failed",
                        )
                    )
                    val fallbackDraft = fallbackDraftFromInput(text)
                    conversationEngine.onAiUnavailable(
                        context = context,
                        userInput = text,
                        reason = route.reason ?: "AI_UNAVAILABLE",
                        draft = fallbackDraft,
                    )
                } else {
                    val startedAt = Clock.System.now().toEpochMilliseconds()
                    when (
                        val analysis = providerAnalysisExecutor.analyze(
                            route = route,
                            request = ProviderAnalysisRequest(
                                input = ProviderAnalysisInput(text = text),
                                context = ProviderAnalysisContext(
                                    state = context.state.name,
                                    missing_fields = context.missingFields,
                                ),
                            ),
                        )
                    ) {
                        is ProviderAnalysisResult.Success -> {
                            providerTelemetry.recordSuccess(
                                mode = route.mode,
                                latencyMs = Clock.System.now().toEpochMilliseconds() - startedAt,
                            )
                            val fallbackSuggestion = captureAgent.analyzeToDraft(text)
                            val suggestion = fallbackSuggestion.copy(
                                intent = analysis.output.intent,
                                draft = fallbackSuggestion.draft.copy(
                                    title = analysis.output.title.ifBlank { fallbackSuggestion.draft.title },
                                    summary = analysis.output.summary.ifBlank { fallbackSuggestion.draft.summary },
                                    tags = analysis.output.tags.ifEmpty { fallbackSuggestion.draft.tags },
                                ),
                            )
                            val next = conversationEngine.onDraftCreated(
                                context = context.copy(state = CaptureState.INTENT_DETECT),
                                userInput = text,
                                intent = suggestion.intent,
                                draft = suggestion.draft,
                            )
                            storageGateway.saveAiJob(
                                StoredAiJob(
                                    id = jobId,
                                    provider = route.mode.name.lowercase(),
                                    requestJson = """{"task":"capture_analysis","input":"${text.escapeJson()}"}""",
                                    responseJson = analysis.rawResponseJson,
                                    status = "succeeded",
                                )
                            )
                            next
                        }

                        is ProviderAnalysisResult.Failed -> {
                            providerTelemetry.recordFailure(
                                mode = route.mode,
                                latencyMs = Clock.System.now().toEpochMilliseconds() - startedAt,
                                category = analysis.category,
                            )
                            storageGateway.saveAiJob(
                                StoredAiJob(
                                    id = jobId,
                                    provider = route.mode.name.lowercase(),
                                    requestJson = """{"task":"capture_analysis","input":"${text.escapeJson()}"}""",
                                    responseJson = """{"error":"${analysis.reason.escapeJson()}","category":"${analysis.category.name.lowercase()}"}""",
                                    status = "failed",
                                )
                            )
                            val fallbackDraft = fallbackDraftFromInput(text)
                            conversationEngine.onAiUnavailable(
                                context = context,
                                userInput = text,
                                reason = analysis.reason,
                                draft = fallbackDraft,
                            )
                        }
                    }
                }
            }

            CaptureState.INFO_COLLECT -> {
                val draft = context.draft ?: return context
                when (val result = toolDispatcher.execute(ToolCommand.AddTag(draft, text))) {
                    is ToolResult.DraftUpdated -> conversationEngine.onTagUpdated(context, text, result.draft)
                    is ToolResult.Failed -> conversationEngine.onBlockedAction(
                        context,
                        "add_tag:${result.code}:${result.message}"
                    )

                    else -> context
                }
            }

            CaptureState.CARD_REVIEW,
            CaptureState.MANUAL_EDIT -> {
                val draft = context.draft ?: return context
                when (val result = toolDispatcher.execute(ToolCommand.UpdateTitle(draft, text))) {
                    is ToolResult.DraftUpdated -> conversationEngine.onTitleUpdated(context, text, result.draft)
                    is ToolResult.Failed -> conversationEngine.onBlockedAction(
                        context,
                        "update_title:${result.code}:${result.message}"
                    )

                    else -> context
                }
            }

            else -> conversationEngine.onBlockedInput(context)
        }
        persistDraftSession(updated)
        return updated
    }

    suspend fun onAction(context: ConversationContext, action: String): ConversationContext {
        if (!stateGuard.canAction(context.state, action)) {
            return conversationEngine.onBlockedAction(context, action)
        }
        val updated = when (action) {
            "upload_media" -> {
                val draft = context.draft ?: return context
                when (val result = toolDispatcher.execute(ToolCommand.AttachPickedMedia(draft))) {
                    is ToolResult.MediaAttached -> {
                        context.copy(
                            draft = result.draft,
                            messages = context.messages + AIMsg(
                                id = "m_upload_${Clock.System.now().toEpochMilliseconds()}",
                                role = AIMsg.Role.AI,
                                text = if (result.draft.mediaAssets.isEmpty()) {
                                    "未选择媒体文件。"
                                } else {
                                    "已附加媒体 ${result.draft.mediaAssets.size} 个。"
                                },
                            )
                        ).let { conversationEngine.refreshActionComponents(it) }
                    }

                    is ToolResult.Failed -> conversationEngine.onBlockedAction(
                        context,
                        "attach_media:${result.code}:${result.message}"
                    )

                    else -> context
                }
            }

            in listOf("skip_tags", "review") -> conversationEngine.onMoveToReview(context)
            "edit_title" -> conversationEngine.onManualEdit(context)
            "publish" -> {
                val draft = context.draft ?: return context
                val publishing = conversationEngine.onPublishing(context)
                when (val result = toolDispatcher.execute(ToolCommand.PublishCard(draft))) {
                    is ToolResult.Published -> {
                        storageGateway.clearDraftSession(publishing.sessionId.orEmpty())
                        conversationEngine.onPublished(publishing, result.title)
                    }

                    is ToolResult.Failed -> conversationEngine.onBlockedAction(
                        context,
                        "publish:${result.code}:${result.message}"
                    )

                    else -> context
                }
            }

            "new_capture" -> conversationEngine.onReset()
            else -> {
                if (action.startsWith("tag:")) {
                    val tag = action.removePrefix("tag:").trim()
                    if (tag.isBlank()) {
                        conversationEngine.onBlockedAction(context, action)
                    } else {
                        val draft = context.draft ?: return context
                        when (val result = toolDispatcher.execute(ToolCommand.AddTag(draft, tag))) {
                            is ToolResult.DraftUpdated -> conversationEngine.onTagUpdated(context, tag, result.draft)
                            is ToolResult.Failed -> conversationEngine.onBlockedAction(
                                context,
                                "add_tag:${result.code}:${result.message}"
                            )

                            else -> context
                        }
                    }
                } else {
                    context
                }
            }
        }
        if (action != "publish") persistDraftSession(updated)
        return updated
    }

    fun providerMode(): ProviderMode = providerRouter.mode

    suspend fun updateProviderConfig(config: ProviderRoutingConfig) {
        providerConfigSource.update(config)
    }

    private suspend fun persistDraftSession(context: ConversationContext) {
        val sessionId = context.sessionId ?: return
        storageGateway.saveDraftSession(
            sessionId = sessionId,
            state = context.state,
            draft = context.draft,
            missingFields = context.missingFields,
        )
    }
}

private fun fallbackDraftFromInput(input: String): CaptureDraft {
    val now = Clock.System.now().toEpochMilliseconds()
    return CaptureDraft(
        id = "draft_fallback_$now",
        title = "",
        summary = input.trim(),
        tags = listOf("手工"),
        sourceText = input.trim(),
        mediaAssets = emptyList(),
    )
}

private fun String.escapeJson(): String = this
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")

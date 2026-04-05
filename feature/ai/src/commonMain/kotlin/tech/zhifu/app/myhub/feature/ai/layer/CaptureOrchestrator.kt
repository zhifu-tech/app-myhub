package tech.zhifu.app.myhub.feature.ai.layer

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Message
import tech.zhifu.app.myhub.feature.ai.layer.agent.CaptureAgent
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisContext
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisExecutor
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisInput
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisRequest
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderAnalysisResult
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.analysis.ProviderErrorCategory
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.config.MutableProviderConfigSource
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.router.ProviderRouter
import tech.zhifu.app.myhub.feature.ai.layer.agent.provider.telemetry.ProviderTelemetry
import tech.zhifu.app.myhub.feature.ai.layer.conversation.ConversationEngine
import tech.zhifu.app.myhub.feature.ai.layer.conversation.context.ConversationContext
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.StateGuard
import tech.zhifu.app.myhub.feature.ai.layer.storage.StorageGateway
import tech.zhifu.app.myhub.feature.ai.layer.storage.StoredAiJob
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommand
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandDispatcher
import tech.zhifu.app.myhub.feature.ai.layer.tool.command.ToolCommandResult
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
    private val toolCommandDispatcher: ToolCommandDispatcher,
    private val storageGateway: StorageGateway,
) {
    fun providerMode(): ProviderMode = providerRouter.mode

    suspend fun updateProviderConfig(
        config: ProviderRoutingConfig
    ) = providerConfigSource.update(config)

    suspend fun bootstrap(): ConversationContext {
        val restored = storageGateway.loadLatestDraftSession()
        if (restored != null) {
            val base = conversationEngine.bootstrap()
            val restoredMessages = base.messages +
                listOf(
                    Message(
                        id = "m_restore",
                        role = Message.Role.SYSTEM,
                        text = "已恢复上次会话草稿。",
                    )
                )
            val restoredContext = base.copy(
                sessionId = restored.sessionId,
                state = restored.state,
                draft = restored.draft,
                missingFields = restored.missingFields,
                messages = restoredMessages,
            )
            return conversationEngine.refreshActionComponents(restoredContext)
        } else {
            return conversationEngine.bootstrap()
        }
    }

    suspend fun onInput(
        context: ConversationContext,
        input: String
    ): ConversationContext {
        val text = input.trim()
        if (text.isBlank()) return context
        if (!stateGuard.canInput(context.state)) {
            return conversationEngine.onBlockedInput(context)
        }
        val updated = when (context.state) {
            ConversationState.IDLE,
            ConversationState.COMPLETE -> {
                val route = providerRouter.resolveRoute()
                val jobId = "job_${Clock.System.now().toEpochMilliseconds()}"
                storageGateway.saveAiJob(
                    snapshot = StoredAiJob(
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
                        snapshot = StoredAiJob(
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
                                input = ProviderAnalysisInput(
                                    text = text
                                ),
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
                            val fallbackSuggestion = captureAgent.analyzeToDraft(input = text)
                            val suggestion = fallbackSuggestion.copy(
                                intent = analysis.output.intent,
                                draft = fallbackSuggestion.draft.copy(
                                    title = analysis.output.title.ifBlank {
                                        fallbackSuggestion.draft.title
                                    },
                                    summary = analysis.output.summary.ifBlank {
                                        fallbackSuggestion.draft.summary
                                    },
                                    tags = analysis.output.tags.ifEmpty {
                                        fallbackSuggestion.draft.tags
                                    },
                                ),
                            )
                            val next = conversationEngine.onDraftCreated(
                                context = context.copy(
                                    state = ConversationState.INTENT_DETECT
                                ),
                                userInput = text,
                                intent = suggestion.intent,
                                draft = suggestion.draft,
                            )
                            storageGateway.saveAiJob(
                                snapshot = StoredAiJob(
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
                                snapshot = StoredAiJob(
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

            ConversationState.INFO_COLLECT -> {
                // fixme: 为什么直接进入 增加 tag ？？
                val draft = context.draft ?: return context
                val result = toolCommandDispatcher.execute(
                    command = ToolCommand.AddTag(
                        draft = draft,
                        tag = text
                    )
                )
                when (result) {
                    is ToolCommandResult.DraftUpdated -> {
                        conversationEngine.onTagUpdated(
                            context = context,
                            tag = text,
                            draft = result.draft
                        )
                    }

                    is ToolCommandResult.Failed -> {
                        conversationEngine.onBlockedAction(
                            context = context,
                            action = "add_tag:${result.code}:${result.message}"
                        )
                    }

                    else -> context
                }
            }

            ConversationState.CARD_REVIEW,
            ConversationState.MANUAL_EDIT -> {
                // fixme: 为什么直接进入 更新 title ？？
                val draft = context.draft ?: return context
                val result = toolCommandDispatcher.execute(
                    command = ToolCommand.UpdateTitle(
                        draft = draft,
                        title = text
                    )
                )
                when (result) {
                    is ToolCommandResult.DraftUpdated -> {
                        conversationEngine.onTitleUpdated(
                            context = context,
                            title = text,
                            draft = result.draft
                        )
                    }

                    is ToolCommandResult.Failed -> {
                        conversationEngine.onBlockedAction(
                            context = context,
                            action = "update_title:${result.code}:${result.message}"
                        )
                    }

                    else -> context
                }
            }

            else -> {
                conversationEngine.onBlockedInput(context)
            }
        }
        persistDraftSession(updated)
        return updated
    }

    suspend fun onAction(
        context: ConversationContext,
        action: String
    ): ConversationContext {
        if (!stateGuard.canAction(
                state = context.state,
                action = action
            )
        ) {
            return conversationEngine.onBlockedAction(
                context = context,
                action = action
            )
        }
        val updated = when (action) {
            "upload_media" -> {
                val draft = context.draft ?: return context
                val result = toolCommandDispatcher.execute(
                    command = ToolCommand.AttachPickedMedia(
                        draft = draft
                    )
                )
                when (result) {
                    is ToolCommandResult.MediaAttached -> {
                        context
                            .copy(
                                draft = result.draft,
                                messages = context.messages +
                                    listOf(
                                        Message(
                                            id = "m_upload_${Clock.System.now().toEpochMilliseconds()}",
                                            role = Message.Role.AI,
                                            text = if (result.draft.mediaAssets.isEmpty()) {
                                                "未选择媒体文件。"
                                            } else {
                                                "已附加媒体 ${result.draft.mediaAssets.size} 个。"
                                            },
                                        )
                                    )
                            )
                            .let {
                                conversationEngine.refreshActionComponents(context = it)
                            }
                    }

                    is ToolCommandResult.Failed -> {
                        conversationEngine.onBlockedAction(
                            context = context,
                            action = "attach_media:${result.code}:${result.message}"
                        )
                    }

                    else -> context
                }
            }

            in listOf("skip_tags", "review") -> {
                conversationEngine.onMoveToReview(context)
            }

            "edit_title" -> {
                conversationEngine.onManualEdit(context)
            }

            "publish" -> {
                val draft = context.draft ?: return context
                val publishing = conversationEngine.onPublishing(context)
                val result = toolCommandDispatcher.execute(
                    command = ToolCommand.PublishCard(
                        draft = draft
                    )
                )
                when (result) {
                    is ToolCommandResult.Published -> {
                        storageGateway.clearDraftSession(
                            sessionId = publishing.sessionId.orEmpty()
                        )
                        conversationEngine.onPublished(
                            context = publishing,
                            title = result.title
                        )
                    }

                    is ToolCommandResult.Failed -> {
                        conversationEngine.onBlockedAction(
                            context = context,
                            action = "publish:${result.code}:${result.message}"
                        )
                    }

                    else -> context
                }
            }

            "new_capture" -> {
                conversationEngine.onReset()
            }

            else -> {
                if (action.startsWith("tag:")) {
                    val tag = action.removePrefix("tag:").trim()
                    if (tag.isBlank()) {
                        conversationEngine.onBlockedAction(
                            context = context,
                            action = action
                        )
                    } else {
                        val draft = context.draft ?: return context
                        val result = toolCommandDispatcher.execute(
                            command = ToolCommand.AddTag(
                                draft = draft,
                                tag = tag
                            )
                        )
                        when (result) {
                            is ToolCommandResult.DraftUpdated -> {
                                conversationEngine.onTagUpdated(
                                    context = context,
                                    tag = tag,
                                    draft = result.draft
                                )
                            }

                            is ToolCommandResult.Failed -> {
                                conversationEngine.onBlockedAction(
                                    context = context,
                                    action = "add_tag:${result.code}:${result.message}"
                                )
                            }

                            else -> context
                        }
                    }
                } else {
                    context
                }
            }
        }
        if (action != "publish") {
            persistDraftSession(context = updated)
        }
        return updated
    }

    private suspend fun persistDraftSession(
        context: ConversationContext
    ) {
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

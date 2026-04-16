package tech.zhifu.app.myhub.feature.ai.orchestrator.storage

import tech.zhifu.app.myhub.feature.ai.orchestrator.CaptureOrchestrator

fun CaptureOrchestrator.autoSaveDraftSession() {
    conversationEngine.addContextChangeCallback { context, pre ->
        if (pre?.sessionId != context.sessionId ||
            pre?.draft != context.draft ||
            pre?.missingFields != context.missingFields ||
            pre.state != context.state
        ) {
            val sessionId = context.sessionId ?: return@addContextChangeCallback
            storageGateway.saveDraftSession(
                sessionId = sessionId,
                state = context.state,
                draft = context.draft,
                missingFields = context.missingFields,
            )
        }
    }
}

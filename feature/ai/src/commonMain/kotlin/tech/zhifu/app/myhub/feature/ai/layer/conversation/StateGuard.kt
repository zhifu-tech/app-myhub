package tech.zhifu.app.myhub.feature.ai.layer.conversation

import tech.zhifu.app.myhub.feature.ai.CaptureState

class StateGuard {
    fun canInput(state: CaptureState): Boolean {
        return state in setOf(
            CaptureState.IDLE,
            CaptureState.COMPLETE,
            CaptureState.INFO_COLLECT,
            CaptureState.CARD_REVIEW,
            CaptureState.MANUAL_EDIT,
        )
    }

    fun canAction(state: CaptureState, action: String): Boolean {
        val allowed = when (state) {
            CaptureState.INFO_COLLECT -> setOf("skip_tags", "review", "upload_media")
            CaptureState.CARD_REVIEW,
            CaptureState.MANUAL_EDIT -> setOf("edit_title", "publish")
            CaptureState.COMPLETE -> setOf("new_capture")
            else -> emptySet()
        }
        if (action.startsWith("tag:") && state == CaptureState.INFO_COLLECT) return true
        return action in allowed
    }
}

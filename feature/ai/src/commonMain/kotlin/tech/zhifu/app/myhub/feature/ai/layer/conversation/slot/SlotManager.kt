package tech.zhifu.app.myhub.feature.ai.layer.conversation.slot

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft

class SlotManager {
    fun missingFields(draft: CaptureDraft): List<String> {
        val missing = mutableListOf<String>()
        if (draft.title.isBlank()) missing += "title"
        if (draft.tags.isEmpty()) missing += "tags"
        return missing
    }
}

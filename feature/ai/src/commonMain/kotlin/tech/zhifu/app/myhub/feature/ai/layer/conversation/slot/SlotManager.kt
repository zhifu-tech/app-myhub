package tech.zhifu.app.myhub.feature.ai.layer.conversation.slot

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.model.Field

class SlotManager {
    fun missingFields(draft: CaptureDraft): List<Field> {
        val missing = mutableListOf<Field>()
        if (draft.mediaAssets.isEmpty()) missing += Field.MEDIA
        if (draft.tags.isEmpty()) missing += Field.TAGS
        if (draft.title.isBlank()) missing += Field.TITLE
        return missing
    }
}

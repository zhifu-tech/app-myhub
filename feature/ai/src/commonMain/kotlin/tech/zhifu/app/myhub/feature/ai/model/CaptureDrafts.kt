package tech.zhifu.app.myhub.feature.ai.model

import tech.zhifu.app.myhub.datastore.model.util.generateUUId

fun newCaptureDraft() = CaptureDraft(
    id = "draft-${generateUUId()}"
)

fun CaptureDraft.hasVisibleContent(): Boolean {
    return title.isNotBlank() ||
        summary.isNotBlank() ||
        tags.isNotEmpty() ||
        sourceText.isNotBlank() ||
        mediaAssets.isNotEmpty() ||
        captureType != null ||
        location != null
}

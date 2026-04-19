package tech.zhifu.app.myhub.feature.ai.model

import tech.zhifu.app.myhub.datastore.model.util.generateUUId

fun newCaptureDraft() = CaptureDraft(
    id = "draft-${generateUUId()}"
)

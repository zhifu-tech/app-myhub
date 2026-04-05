package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import tech.zhifu.app.myhub.feature.ai.model.CaptureDraft
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState

class ActionPlanner {
    fun actionsFor(
        state: ConversationState,
        draft: CaptureDraft? = null,
        missingFields: List<String> = emptyList(),
    ): List<ActionComponentSchema> = when (state) {
        ConversationState.INFO_COLLECT -> listOf(
            ActionComponentSchema(
                type = "upload",
                field = "media",
                options = listOf(
                    ActionOptionSchema(
                        label = "上传图片",
                        value = "upload_media"
                    ),
                )
            ),
            ActionComponentSchema(
                type = "tag_selector",
                field = "tags",
                options = buildTagOptions(draft)
            ),
            ActionComponentSchema(
                type = "quick_reply",
                field = if ("tags" in missingFields) "tags" else null,
                options = listOf(
                    ActionOptionSchema(
                        label = "跳过标签",
                        value = "skip_tags"
                    ),
                    ActionOptionSchema(
                        label = "进入复核",
                        value = "review"
                    )
                )
            )
        )

        ConversationState.CARD_REVIEW,
        ConversationState.MANUAL_EDIT -> listOf(
            ActionComponentSchema(
                type = "input",
                field = "title",
                options = listOf(
                    ActionOptionSchema(
                        label = "输入新标题后发送",
                        value = "input_title_hint"
                    ),
                )
            ),
            ActionComponentSchema(
                type = "card_actions",
                options = listOf(
                    ActionOptionSchema(
                        label = "编辑标题",
                        value = "edit_title"
                    ),
                    ActionOptionSchema(
                        label = "发布卡片",
                        value = "publish"
                    ),
                )
            )
        )

        ConversationState.COMPLETE -> listOf(
            ActionComponentSchema(
                type = "quick_reply",
                options = listOf(
                    ActionOptionSchema(
                        label = "新建捕获",
                        value = "new_capture"
                    ),
                )
            )
        )

        else -> emptyList()
    }
}

private fun buildTagOptions(
    draft: CaptureDraft?
): List<ActionOptionSchema> {
    val options = draft?.tags
        ?.take(5)
        ?.map { ActionOptionSchema(label = it, value = "tag:$it") }
        .orEmpty()
    if (options.isNotEmpty()) return options
    return listOf(
        ActionOptionSchema(label = "美食", value = "tag:美食"),
        ActionOptionSchema(label = "想法", value = "tag:想法"),
        ActionOptionSchema(label = "待办", value = "tag:待办"),
    )
}

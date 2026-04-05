package tech.zhifu.app.myhub.feature.ai.layer.conversation.state

enum class ConversationState {
    IDLE,
    INTENT_DETECT,
    DRAFT_CREATE,
    INFO_COLLECT,
    CARD_REVIEW,
    PUBLISH_CONFIRM,
    COMPLETE,
    MANUAL_EDIT
}

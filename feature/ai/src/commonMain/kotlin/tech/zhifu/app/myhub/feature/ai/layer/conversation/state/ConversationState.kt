package tech.zhifu.app.myhub.feature.ai.layer.conversation.state

enum class ConversationState {
    IDLE,
    INTENT_DETECT,
    INFO_COLLECT,
    CARD_REVIEW,
    PUBLISH,
    COMPLETE,
    MANUAL_EDIT
}

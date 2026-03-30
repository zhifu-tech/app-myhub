package tech.zhifu.app.myhub.feature.ai

sealed interface AIMessage {
    val id: String

    data class AiText(
        override val id: String,
        val text: String,
    ) : AIMessage

    data class UserText(
        override val id: String,
        val text: String,
    ) : AIMessage

    data class AiAskImage(
        override val id: String,
        val text: String,
    ) : AIMessage

    data class AiTagEditor(
        override val id: String,
        val text: String,
    ) : AIMessage

    data class AiTitleInput(
        override val id: String,
        val text: String,
        val contentTitle: String,
    ) : AIMessage

    data class AiPublishPrompt(
        override val id: String,
        val text: String,
    ) : AIMessage
}

internal val aiMockMessages = listOf(
    AIMessage.AiText(
        id = "welcome",
        text = "您好！有什么我可以帮您捕获的吗？您可以直接告诉我，我会为您整理。",
    ),
    AIMessage.UserText(
        id = "user-1",
        text = "昨天在上海吃了一家很好吃的一兰拉面。",
    ),
    AIMessage.AiAskImage(
        id = "image-prompt",
        text = "听起来很棒！需要添加一张图片吗？",
    ),
    AIMessage.AiTagEditor(
        id = "tag-prompt",
        text = "这些是我识别出的关键词，要不要加个标签？",
    ),
    AIMessage.AiTitleInput(
        id = "title-prompt",
        text = "看起来不错，起个标题吧",
        contentTitle = "上海的一兰拉面美食体验",
    ),
    AIMessage.AiPublishPrompt(
        id = "publish-prompt",
        text = "完美的捕获，卡片准备好了，是否发布？",
    ),
)

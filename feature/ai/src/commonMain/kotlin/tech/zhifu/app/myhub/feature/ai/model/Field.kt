package tech.zhifu.app.myhub.feature.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Field(val value: String) {
    @SerialName("unknown")
    UNKNOWN(value = "unknown"),

    @SerialName("media")
    MEDIA(value = "media"),

    @SerialName("tags")
    TAGS(value = "tags"),

    @SerialName("summary")
    SUMMARY(value = "summary"),

    @SerialName("title")
    TITLE(value = "title"),

    @SerialName("location")
    LOCATION(value = "location");
}

package tech.zhifu.app.myhub.feature.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CaptureType(
    val value: String,
) {
    @SerialName("place")
    PLACE("place"),

    @SerialName("idea")
    IDEA("idea"),

    @SerialName("article")
    ARTICLE("article"),

    @SerialName("person")
    PERSON("person");

    companion object {
        fun fromValue(value: String): CaptureType? = entries.firstOrNull {
            it.value.equals(other = value.trim(), ignoreCase = true)
        }
    }
}

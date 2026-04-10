package tech.zhifu.app.myhub.feature.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Field {
    @SerialName("media")
    MEDIA,

    @SerialName("tags")
    TAGS,

    @SerialName("title")
    TITLE,
}

fun Field.asRawValue(): String = when (this) {
    Field.MEDIA -> "media"
    Field.TAGS -> "tags"
    Field.TITLE -> "title"
}

fun String.toFieldOrNull(): Field? = when (lowercase()) {
    "media" -> Field.MEDIA
    "tags" -> Field.TAGS
    "title" -> Field.TITLE
    else -> null
}

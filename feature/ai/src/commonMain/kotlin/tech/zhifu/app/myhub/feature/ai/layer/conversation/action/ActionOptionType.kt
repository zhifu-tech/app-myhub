package tech.zhifu.app.myhub.feature.ai.layer.conversation.action

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ActionOptionType(
    val value: String,
) {
    @SerialName("upload_media")
    UPLOAD_MEDIA("upload_media"),

    @SerialName("replace_media")
    REPLACE_MEDIA("replace_media"),

    @SerialName("remove_media")
    REMOVE_MEDIA("remove_media"),

    @SerialName("skip_media")
    SKIP_MEDIA("skip_media"),

    @SerialName("skip_tags")
    SKIP_TAGS("skip_tags"),

    @SerialName("review")
    REVIEW("review"),

    @SerialName("edit_media")
    EDIT_MEDIA("edit_media"),

    @SerialName("edit_title")
    EDIT_TITLE("edit_title"),

    @SerialName("edit_tags")
    EDIT_TAGS("edit_tags"),

    @SerialName("edit_summary")
    EDIT_SUMMARY("edit_summary"),

    @SerialName("edit_location")
    EDIT_LOCATION("edit_location"),

    @SerialName("remove_tag")
    REMOVE_TAG("remove_tag"),

    @SerialName("publish")
    PUBLISH("publish"),

    @SerialName("save_draft")
    SAVE_DRAFT("save_draft"),

    @SerialName("delete_card")
    DELETE_CARD("delete_card"),

    @SerialName("new_capture")
    NEW_CAPTURE("new_capture"),

    @SerialName("input_title_hint")
    INPUT_TITLE_HINT("input_title_hint"),

    @SerialName("set_capture_type")
    SET_CAPTURE_TYPE("set_capture_type"),

    @SerialName("set_location")
    SET_LOCATION("set_location"),

    @SerialName("clear_location")
    CLEAR_LOCATION("clear_location"),

    @SerialName("tag")
    TAG("tag");

    companion object {
        private const val SEPARATOR = ":"

        private fun decodePayload(
            action: String,
            option: ActionOptionType,
        ): String? {
            val prefix = "${option.value}$SEPARATOR"
            if (!action.startsWith(prefix)) return null
            return action.removePrefix(prefix).trim().takeIf { it.isNotBlank() }
        }

        private fun encodePayload(
            option: ActionOptionType,
            value: String,
        ): String = "${option.value}$SEPARATOR${value.trim()}"

        fun encodeTagAdd(tag: String): String = encodePayload(TAG, tag)

        fun isTagAddAction(action: String): Boolean = decodeTagAdd(action) != null

        fun decodeTagAdd(action: String): String? = decodePayload(action, TAG)

        fun encodeTagRemove(tag: String): String = encodePayload(REMOVE_TAG, tag)

        fun decodeTagRemove(action: String): String? = decodePayload(action, REMOVE_TAG)

        fun encodeSetCaptureType(type: String): String = encodePayload(SET_CAPTURE_TYPE, type)

        fun decodeSetCaptureType(action: String): String? = decodePayload(action, SET_CAPTURE_TYPE)

        fun encodeSetLocation(location: String): String = encodePayload(SET_LOCATION, location)

        fun decodeSetLocation(action: String): String? = decodePayload(action, SET_LOCATION)

        fun encodeRemoveMediaAt(index: Int): String = encodePayload(REMOVE_MEDIA, index.toString())

        fun decodeRemoveMediaAt(action: String): Int? = decodePayload(action, REMOVE_MEDIA)?.toIntOrNull()
    }
}

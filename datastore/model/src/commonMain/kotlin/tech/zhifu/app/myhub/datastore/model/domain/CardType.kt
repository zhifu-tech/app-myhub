package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
enum class CardType(val value: String) {
    NOTE("note");

    companion object {
        fun fromWire(value: String?): CardType =
            entries.firstOrNull { it.value == value?.lowercase() }
                ?: NOTE
    }
}

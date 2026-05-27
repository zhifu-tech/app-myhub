package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
enum class CardStatus(val wire: String) {
    DRAFT("draft"),
    PUBLISHED("published"),
    ARCHIVED("archived");

    companion object {
        fun fromWire(value: String?): CardStatus {
            return entries.firstOrNull { it.wire == value?.lowercase() } ?: DRAFT
        }
    }
}

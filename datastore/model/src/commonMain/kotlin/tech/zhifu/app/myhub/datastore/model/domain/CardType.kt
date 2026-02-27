package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
enum class CardType(val wire: String) {
    Review("review"),
    Do("do"),
    Material("material");

    companion object {
        fun fromWire(value: String?): CardType {
            return entries.firstOrNull { it.wire == value?.lowercase() } ?: Review
        }
    }
}

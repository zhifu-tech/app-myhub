package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
enum class CardSource(val wire: String) {
    Link("link"),
    Extract("extract"),
    Own("own");

    companion object {
        fun fromWire(value: String?): CardSource {
            return entries.firstOrNull { it.wire == value?.lowercase() } ?: Extract
        }
    }
}

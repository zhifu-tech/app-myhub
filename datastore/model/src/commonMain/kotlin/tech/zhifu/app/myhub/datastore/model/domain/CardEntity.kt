package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonObject

@Serializable
data class CardEntity(
    val type: String,
    val name: String,
    val meta: JsonObject? = null,
)

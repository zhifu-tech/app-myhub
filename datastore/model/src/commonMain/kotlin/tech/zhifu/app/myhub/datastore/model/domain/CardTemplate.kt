package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CardTemplate(
    val id: String,
    val type: String,
    val title: String? = null,
    val content: String? = null,
    val description: String? = null,
    val createdAt: Instant
)

package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CardTag(
    val cardId: String,
    val tagId: String,
    val createdAt: Instant
)

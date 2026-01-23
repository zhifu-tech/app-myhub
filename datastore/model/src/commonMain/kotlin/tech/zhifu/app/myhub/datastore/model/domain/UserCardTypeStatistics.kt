package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserCardTypeStatistics(
    val cardType: String,
    val count: Int = 0,
    val userId: String
)

package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val userId: String,
    val theme: String = "dark",
    val language: String = "zh-CN",
    val defaultCardType: String? = null,
    val autoSync: Boolean = true,
    val syncInterval: Long = 3600000L
)

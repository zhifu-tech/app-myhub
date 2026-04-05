package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val userId: String,
    val theme: String? = null,
    val language: String? = null,
    val aiProvider: String? = null,
    val layoutAsList: Boolean = true,
    val sortAsDate: Boolean = true,
    val sortAsName: Boolean = true,
    val autoSync: Boolean = true,
    val syncInterval: Long = 3600000L
)

package tech.zhifu.app.myhub.settings

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val language: String? = null,
    val isDarkMode: Boolean = true
)

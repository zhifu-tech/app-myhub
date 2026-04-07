package tech.zhifu.app.myhub.ui.state.theme

import kotlinx.coroutines.flow.StateFlow
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

interface ThemeState {
    val userRepository: UserRepository
    val theme: StateFlow<Theme>
}

enum class Theme(val value: String) {
    Light("light"),
    Dark("dark"),
    System("system");

    companion object {
        fun fromWire(value: String?): Theme? {
            return entries
                .firstOrNull {
                    it.value == value?.trim()?.lowercase()
                }
        }
    }
}

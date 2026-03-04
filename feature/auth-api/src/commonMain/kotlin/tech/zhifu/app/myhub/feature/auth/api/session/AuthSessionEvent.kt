package tech.zhifu.app.myhub.feature.auth.api.session

sealed interface AuthSessionEvent {
    data class Expired(
        val source: String
    ) : AuthSessionEvent
}

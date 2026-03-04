package tech.zhifu.app.myhub.feature.auth.api.session

import kotlinx.coroutines.flow.Flow

interface AuthSessionCoordinator {
    val events: Flow<AuthSessionEvent>

    fun onUnauthorized(source: String)

    fun onAuthorized()
}

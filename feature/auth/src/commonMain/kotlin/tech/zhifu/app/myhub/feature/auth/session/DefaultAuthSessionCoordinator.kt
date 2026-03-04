package tech.zhifu.app.myhub.feature.auth.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import tech.zhifu.app.myhub.feature.auth.api.session.AuthSessionCoordinator
import tech.zhifu.app.myhub.feature.auth.api.session.AuthSessionEvent
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger

class DefaultAuthSessionCoordinator : AuthSessionCoordinator {
    private val logger = logger("AuthSessionCoordinator")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val expiredGate = Mutex()
    private val _events = MutableSharedFlow<AuthSessionEvent>(extraBufferCapacity = 1)

    override val events: Flow<AuthSessionEvent> = _events

    override fun onUnauthorized(source: String) {
        if (!expiredGate.tryLock()) return
        logger.info { "Session expired, source=$source" }
        scope.launch {
            _events.emit(AuthSessionEvent.Expired(source))
        }
    }

    override fun onAuthorized() {
        if (expiredGate.isLocked) {
            expiredGate.unlock()
        }
    }
}

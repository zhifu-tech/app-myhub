package tech.zhifu.app.myhub.datastore.repository.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.sync.SyncCoordinator
import tech.zhifu.app.myhub.sync.SyncMode
import tech.zhifu.app.myhub.sync.SyncRequestFactory
import tech.zhifu.app.myhub.sync.SyncScheduleConfig
import tech.zhifu.app.myhub.sync.SyncScheduler
import tech.zhifu.app.myhub.sync.SyncTrigger

class SyncForegroundScheduler(
    private val scope: CoroutineScope,
    private val coordinator: Lazy<SyncCoordinator>,
) : SyncScheduler {
    private var job: Job? = null

    override fun start(config: SyncScheduleConfig, requestFactory: SyncRequestFactory) {
        if (config.mode == SyncMode.DISABLED) {
            stop()
            return
        }
        job?.cancel()
        job = scope.launch {
            coordinator.value.requestSync(requestFactory(SyncTrigger.APP_START))
            while (isActive) {
                delay(config.interval)
                coordinator.value.requestSync(requestFactory(SyncTrigger.TIMER))
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }
}

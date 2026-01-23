package tech.zhifu.app.myhub.sync

interface SyncCoordinator {
    suspend fun requestSync(request: SyncRequest)
}

interface SyncScheduler {
    fun start(config: SyncScheduleConfig, requestFactory: SyncRequestFactory)
    fun stop()
}

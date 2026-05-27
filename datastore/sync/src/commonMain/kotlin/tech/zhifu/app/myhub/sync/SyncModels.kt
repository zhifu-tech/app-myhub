package tech.zhifu.app.myhub.sync

import kotlin.time.Duration

enum class SyncMode { ENABLED, DISABLED }

enum class SyncTrigger {
    APP_START,
    FOREGROUND,
    NETWORK_RESTORED,
    MANUAL,
    TIMER
}

data class SyncScheduleConfig(
    val interval: Duration,
    val mode: SyncMode
)

data class SyncRequest(
    val userId: String,
    val trigger: SyncTrigger
)

typealias SyncRequestFactory = (SyncTrigger) -> SyncRequest

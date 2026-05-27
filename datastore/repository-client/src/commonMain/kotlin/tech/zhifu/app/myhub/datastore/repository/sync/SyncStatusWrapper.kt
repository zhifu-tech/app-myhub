package tech.zhifu.app.myhub.datastore.repository.sync

import kotlinx.coroutines.flow.MutableStateFlow

class SyncStatusWrapper {
    private val statusMap: MutableMap<String, MutableStateFlow<SyncStatus>> = mutableMapOf()

    fun statusFlow(userId: String): MutableStateFlow<SyncStatus> {
        return statusMap.getOrPut(userId) {
            MutableStateFlow(SyncStatus.IDLE)
        }
    }
}

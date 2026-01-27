package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Bookkeeper
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage

@OptIn(ExperimentalStoreApi::class)
fun createTemplateStoreBookkeeper(
    bookkeeperStorage: BookkeeperStorage
): TemplateStoreBookkeeper = Bookkeeper.by(
    getLastFailedSync = { key ->
        val keyString = when (key) {
            is TemplateStoreKey.ById -> "template:${key.id}"
            is TemplateStoreKey.All -> "templates:all"
        }
        bookkeeperStorage.getLastFailedSync(keyString)
    },
    setLastFailedSync = { key, timestamp ->
        val keyString = when (key) {
            is TemplateStoreKey.ById -> "template:${key.id}"
            is TemplateStoreKey.All -> "templates:all"
        }
        bookkeeperStorage.setLastFailedSync(keyString, timestamp)
    },
    clear = { key ->
        val keyString = when (key) {
            is TemplateStoreKey.ById -> "template:${key.id}"
            is TemplateStoreKey.All -> "templates:all"
        }
        bookkeeperStorage.clearFailedSync(keyString)
    },
    clearAll = {
        bookkeeperStorage.clearAllFailedSyncs()
    }
)

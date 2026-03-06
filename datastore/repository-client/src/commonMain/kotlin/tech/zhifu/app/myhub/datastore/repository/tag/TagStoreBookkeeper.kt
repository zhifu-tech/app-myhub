package tech.zhifu.app.myhub.datastore.repository.tag

import org.mobilenativefoundation.store.store5.Bookkeeper
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage
import tech.zhifu.app.myhub.logger.Logger

fun createTagStoreBookkeeper(
    bookkeeperStorage: BookkeeperStorage,
    logger: Logger,
): TagStoreBookkeeper = Bookkeeper.by(
    getLastFailedSync = { key ->
        val keyString = when (key) {
            is TagStoreKey.ById -> "tag:${key.id}"
            is TagStoreKey.ByUser -> "tags:${key.userId}"
        }
        bookkeeperStorage.getLastFailedSync(keyString)
    },
    setLastFailedSync = { key, timestamp ->
        val keyString = when (key) {
            is TagStoreKey.ById -> "tag:${key.id}"
            is TagStoreKey.ByUser -> "tags:${key.userId}"
        }
        bookkeeperStorage.setLastFailedSync(keyString, timestamp)
    },
    clear = { key ->
        val keyString = when (key) {
            is TagStoreKey.ById -> "tag:${key.id}"
            is TagStoreKey.ByUser -> "tags:${key.userId}"
        }
        bookkeeperStorage.clearFailedSync(keyString)
    },
    clearAll = {
        bookkeeperStorage.clearAllFailedSyncs()
    }
)

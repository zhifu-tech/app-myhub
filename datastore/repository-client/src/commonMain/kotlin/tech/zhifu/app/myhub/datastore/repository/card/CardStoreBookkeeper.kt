package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.Bookkeeper
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage

internal fun createCardStoreBookkeeper(
    bookkeeperStorage: BookkeeperStorage
): CardStoreBookkeeper = Bookkeeper.by(
    getLastFailedSync = { key ->
        val keyString = when (key) {
            is CardStoreKey.ById -> "card:${key.id}"
            is CardStoreKey.ByIds -> "cards:ids:${key.ids.sorted().joinToString(",")}"
            is CardStoreKey.ByUser -> "cards:${key.userId}"
        }
        bookkeeperStorage.getLastFailedSync(keyString)
    },
    setLastFailedSync = { key, timestamp ->
        val keyString = when (key) {
            is CardStoreKey.ById -> "card:${key.id}"
            is CardStoreKey.ByIds -> "cards:ids:${key.ids.sorted().joinToString(",")}"
            is CardStoreKey.ByUser -> "cards:${key.userId}"
        }
        bookkeeperStorage.setLastFailedSync(keyString, timestamp)
    },
    clear = { key ->
        val keyString = when (key) {
            is CardStoreKey.ById -> "card:${key.id}"
            is CardStoreKey.ByIds -> "cards:ids:${key.ids.sorted().joinToString(",")}"
            is CardStoreKey.ByUser -> "cards:${key.userId}"
        }
        bookkeeperStorage.clearFailedSync(keyString)
    },
    clearAll = {
        bookkeeperStorage.clearAllFailedSyncs()
    }
)

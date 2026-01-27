package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Bookkeeper
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreBookkeeper(
    bookkeeperStorage: BookkeeperStorage
): CollectionStoreBookkeeper = Bookkeeper.by(
    getLastFailedSync = { key ->
        val keyString = when (key) {
            is CollectionStoreKey.ById -> "collection:${key.id}"
            is CollectionStoreKey.ByUser -> "collections:${key.userId}"
        }
        bookkeeperStorage.getLastFailedSync(keyString)
    },
    setLastFailedSync = { key, timestamp ->
        val keyString = when (key) {
            is CollectionStoreKey.ById -> "collection:${key.id}"
            is CollectionStoreKey.ByUser -> "collections:${key.userId}"
        }
        bookkeeperStorage.setLastFailedSync(keyString, timestamp)
    },
    clear = { key ->
        val keyString = when (key) {
            is CollectionStoreKey.ById -> "collection:${key.id}"
            is CollectionStoreKey.ByUser -> "collections:${key.userId}"
        }
        bookkeeperStorage.clearFailedSync(keyString)
    },
    clearAll = {
        bookkeeperStorage.clearAllFailedSyncs()
    }
)

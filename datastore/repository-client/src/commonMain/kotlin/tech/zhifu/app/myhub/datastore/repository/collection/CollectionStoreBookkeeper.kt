package tech.zhifu.app.myhub.datastore.repository.collection

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Bookkeeper
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage

@OptIn(ExperimentalStoreApi::class)
fun createCollectionStoreBookkeeper(
    bookkeeperStorage: BookkeeperStorage
): CollectionStoreBookkeeper = Bookkeeper.by(
    getLastFailedSync = { key ->
        val keyString = key.toBookkeeperKey()
        bookkeeperStorage.getLastFailedSync(keyString)
    },
    setLastFailedSync = { key, timestamp ->
        val keyString = key.toBookkeeperKey()
        bookkeeperStorage.setLastFailedSync(keyString, timestamp)
    },
    clear = { key ->
        val keyString = key.toBookkeeperKey()
        bookkeeperStorage.clearFailedSync(keyString)
    },
    clearAll = {
        bookkeeperStorage.clearAllFailedSyncs()
    }
)

@OptIn(ExperimentalStoreApi::class)
private fun CollectionStoreKey<String>.toBookkeeperKey(): String = when (this) {
    is CollectionStoreKey.ById -> "collection:$id"
    is CollectionStoreKey.ByUser ->
        "collections:$userId:$page:$size:sort=${sort?.name}:filters=${filters.hashToken()}"
}

private fun List<*>?.hashToken(): Int = this?.hashCode() ?: 0

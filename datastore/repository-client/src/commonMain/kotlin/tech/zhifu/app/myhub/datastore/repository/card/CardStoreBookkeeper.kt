package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.Bookkeeper
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage

internal fun createCardStoreBookkeeper(
    bookkeeperStorage: BookkeeperStorage
): CardStoreBookkeeper = Bookkeeper.by(
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

private fun CardStoreKey<String>.toBookkeeperKey(): String =
    when (this) {
        is CardStoreKey.ById -> "card:$id"
        is CardStoreKey.ByUserCursor -> "cards:$userId:$cursor:$size:$orderByUpdated:$orderByTitle:${query.orEmpty()}"
        else -> ""
    }

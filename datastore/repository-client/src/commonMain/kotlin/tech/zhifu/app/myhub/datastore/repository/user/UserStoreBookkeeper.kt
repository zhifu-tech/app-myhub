package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Bookkeeper
import tech.zhifu.app.myhub.datastore.repository.store.BookkeeperStorage

@OptIn(ExperimentalStoreApi::class)
fun createUserStoreBookkeeper(
    bookkeeperStorage: BookkeeperStorage
): UserStoreBookkeeper = Bookkeeper.by(
    getLastFailedSync = { key ->
        val keyString = when (key) {
            is UserStoreKey.ById -> "user:${key.id}"
            is UserStoreKey.PreferencesById -> "user_preferences:${key.id}"
        }
        bookkeeperStorage.getLastFailedSync(keyString)
    },
    setLastFailedSync = { key, timestamp ->
        val keyString = when (key) {
            is UserStoreKey.ById -> "user:${key.id}"
            is UserStoreKey.PreferencesById -> "user_preferences:${key.id}"
        }
        bookkeeperStorage.setLastFailedSync(keyString, timestamp)
    },
    clear = { key ->
        val keyString = when (key) {
            is UserStoreKey.ById -> "user:${key.id}"
            is UserStoreKey.PreferencesById -> "user_preferences:${key.id}"
        }
        bookkeeperStorage.clearFailedSync(keyString)
    },
    clearAll = {
        bookkeeperStorage.clearAllFailedSyncs()
    }
)

package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

@OptIn(ExperimentalStoreApi::class)
fun createUserStoreFetcher(
    remoteUserDataSource: RemoteUserDataSource
): UserStoreFetcher = Fetcher.of { key ->
    when (key) {
        is UserStoreKey.ById -> {
            val user = remoteUserDataSource.getUser(key.id)
            UserStoreData.UserData(user)
        }

        is UserStoreKey.PreferencesById -> {
            val preferences = remoteUserDataSource.getUserPreferences(key.id)
                ?: UserPreferences(userId = key.id)
            UserStoreData.PreferencesData(preferences)
        }
    }
}

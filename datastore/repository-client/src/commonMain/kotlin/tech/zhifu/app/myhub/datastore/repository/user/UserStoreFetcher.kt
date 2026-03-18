package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import tech.zhifu.app.myhub.datastore.datasource.user.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

fun createUserStoreFetcher(
    remoteUserDataSource: RemoteUserDataSource
): UserStoreFetcher = Fetcher.ofResult { key ->
    when (key) {
        is UserStoreKey.ById -> {
            val user = remoteUserDataSource.getUser(key.id)
            if (user == null) FetcherResult.Error.Message("User not found: ${key.id}")
            else FetcherResult.Data(UserStoreData.UserData(user))
        }

        is UserStoreKey.PreferencesById -> {
            val preferences = remoteUserDataSource.getUserPreferences(key.id)
                ?: UserPreferences(userId = key.id)
            FetcherResult.Data(UserStoreData.PreferencesData(preferences))
        }
    }
}

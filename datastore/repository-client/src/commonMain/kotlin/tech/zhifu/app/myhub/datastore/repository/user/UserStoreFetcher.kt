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
            val user = remoteUserDataSource.fetchUser(key.id)
                ?: throw NoSuchElementException("User not found: ${key.id}")
            UserStoreData.UserData(user)
        }

        is UserStoreKey.PreferencesById -> {
            // RemoteUserDataSource 没有 fetchUserPreferences
            // 返回默认值，实际偏好会从本地存储读取
            UserStoreData.PreferencesData(UserPreferences(userId = key.id))
        }
    }
}

package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.user.LocalUserDataSource

fun createUserStoreSourceOfTruth(
    localUserDataSource: LocalUserDataSource,
): UserStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is UserStoreKey.ById -> {
                if (key.id.isEmpty()) {
                    localUserDataSource.flowUser()
                        .map { UserStoreData.UserData(it) }
                } else {
                    localUserDataSource.flowUser(key.id)
                        .map { UserStoreData.UserData(it) }
                }
            }

            is UserStoreKey.PreferencesById -> {
                localUserDataSource.flowUserPreferences(userId = key.id)
                    .map { UserStoreData.PreferencesData(it) }
            }
        }
    },
    writer = { key, data ->
        when (key) {
            is UserStoreKey.ById if data is UserStoreData.UserData -> {
                val user = data.user ?: return@of
                localUserDataSource.insertUser(user)
            }

            is UserStoreKey.PreferencesById if data is UserStoreData.PreferencesData -> {
                localUserDataSource.insertUserPreferences(data.preferences)
            }

            else -> {}
        }
    },
    delete = { key ->
        when (key) {
            is UserStoreKey.ById -> {
                localUserDataSource.deleteUser(userId = key.id)
            }

            else -> {}
        }
    },
)

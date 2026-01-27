package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource

@OptIn(ExperimentalStoreApi::class)
fun createUserStoreSourceOfTruth(
    localUserDataSource: LocalUserDataSource
): UserStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is UserStoreKey.ById -> flow {
                try {
                    // LocalUserDataSource 没有 observeUserById，使用 getUser
                    val user = localUserDataSource.getUser(key.id)
                    if (user != null) {
                        emit(UserStoreData.UserData(user))
                    } else {
                        emit(null)
                    }
                } catch (_: Exception) {
                    emit(null)
                }
            }

            is UserStoreKey.PreferencesById -> {
                localUserDataSource.observeUserPreferences(key.id).map { preferences ->
                    UserStoreData.PreferencesData(preferences)
                }
            }
        }
    },
    writer = { key, data ->
        when {
            key is UserStoreKey.ById && data is UserStoreData.UserData -> {
                localUserDataSource.insertUser(data.user)
            }

            key is UserStoreKey.PreferencesById && data is UserStoreData.PreferencesData -> {
                localUserDataSource.insertUserPreferences(data.preferences)
            }

            else -> {
                // Store5 框架应该保证类型匹配，这里主要是防御性编程
            }
        }
    },
    delete = { key ->
        when (key) {
            is UserStoreKey.ById -> localUserDataSource.deleteUser(key.id)
            is UserStoreKey.PreferencesById -> {
                // LocalUserDataSource 没有 deleteUserPreferences
                // 暂时不执行操作
            }
        }
    },
    deleteAll = { }
)

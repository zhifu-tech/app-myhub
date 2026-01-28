package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.flow
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug

@OptIn(ExperimentalStoreApi::class)
fun createUserStoreSourceOfTruth(
    localUserDataSource: LocalUserDataSource,
    logger: Logger,
): UserStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        logger.debug {
            "reader called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
        when (key) {
            is UserStoreKey.ById -> flow {
                val user = localUserDataSource.getUser(key.id)
                if (user != null) {
                    emit(UserStoreData.UserData(user))
                } else {
                    emit(null)
                }
            }

            is UserStoreKey.PreferencesById -> flow {
                val userPreferences = localUserDataSource.getUserPreferences(userId = key.id)
                if (userPreferences != null) {
                    emit(UserStoreData.PreferencesData(userPreferences))
                } else {
                    emit(null)
                }
            }
        }
    },
    writer = { key, data ->
        logger.debug {
            "writer called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
        when (key) {
            is UserStoreKey.ById if data is UserStoreData.UserData -> {
                localUserDataSource.insertUser(data.user)
            }

            is UserStoreKey.PreferencesById if data is UserStoreData.PreferencesData -> {
                localUserDataSource.insertUserPreferences(data.preferences)
            }

            else -> {}
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
)

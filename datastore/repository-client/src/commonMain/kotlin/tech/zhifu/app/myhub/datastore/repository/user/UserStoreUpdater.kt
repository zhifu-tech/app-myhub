package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error

internal fun createUserStoreUpdater(
    remoteUserDataSource: RemoteUserDataSource,
    logger: Logger,
): UserStoreUpdater = Updater.by(
    post = { key, data ->
        logger.debug("updater") {
            "post is called with key: $key (type=${key::class.qualifiedName}, " +
                "data=$data, instance=${System.identityHashCode(key)}"
        }
        when (key) {
            is UserStoreKey.ById if data is UserStoreData.UserData -> remoteUserDataSource
                .updateUser(
                    id = key.id,
                    user = data.user,
                )
                .let { UserStoreData.UserData(it) }
                .let { StoreWriteResponse.Success.Typed(it) }
                .let { UpdaterResult.Success.Typed(it) }

            is UserStoreKey.PreferencesById if data is UserStoreData.PreferencesData -> remoteUserDataSource
                .updateUserPreferences(
                    userId = key.id,
                    preferences = data.preferences
                )
                .let { UserStoreData.PreferencesData(it) }
                .let { StoreWriteResponse.Success.Typed(it) }
                .let { UpdaterResult.Success.Typed(it) }

            else -> {
                logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
            }
        }
    }
)

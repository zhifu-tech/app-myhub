package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.network.NetworkException

internal fun createUserStoreUpdater(
    remoteUserDataSource: RemoteUserDataSource,
    logger: Logger = logger("UserStoreUpdater")
): UserStoreUpdater = Updater.by(
    post = { key, data ->
        try {
            when {
                key is UserStoreKey.ById && data is UserStoreData.UserData -> {
                    val updatedUser = remoteUserDataSource.updateUser(key.id, data.user)
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            UserStoreData.UserData(updatedUser)
                        )
                    )
                }

                key is UserStoreKey.PreferencesById && data is UserStoreData.PreferencesData -> {
                    val updatedPreferences = remoteUserDataSource.updateUserPreferences(
                        userId = key.id,
                        preferences = data.preferences
                    )
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            UserStoreData.PreferencesData(updatedPreferences)
                        )
                    )
                }

                else -> {
                    logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                    UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
                }
            }
        } catch (e: NetworkException) {
            logger.error(e) { "Network error during user update: key=$key" }
            UpdaterResult.Error.Exception(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during user update: key=$key" }
            UpdaterResult.Error.Exception(e)
        }
    }
)

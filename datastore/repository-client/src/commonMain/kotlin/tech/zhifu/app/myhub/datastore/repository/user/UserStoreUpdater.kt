package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.user.RemoteUserDataSource
import tech.zhifu.app.myhub.logger.Logger

internal fun createUserStoreUpdater(
    remoteUserDataSource: RemoteUserDataSource,
    logger: Logger,
): UserStoreUpdater = Updater.by(
    post = { _, data ->
        UpdaterResult.Success.Untyped(StoreWriteResponse.Success.Untyped(data))
    }
)

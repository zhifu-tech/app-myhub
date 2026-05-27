package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

internal fun createCardStoreUpdater(): CardStoreUpdater = Updater.by(
    post = { _, _ ->
        UpdaterResult.Error.Message("NOT SUPPORT CLOUD STORE FOR NOW")
    }
)

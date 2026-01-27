package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger

internal fun createTemplateStoreUpdater(
    logger: Logger = logger("TemplateStoreUpdater")
): TemplateStoreUpdater = Updater.by(
    post = { key, data ->
        try {
            when {
                key is TemplateStoreKey.ById && data is TemplateStoreData.Single -> {
                    // TODO: 实现单模板写入 API（需要 RemoteTemplateDataSource）
                    // 当前没有远程数据源，直接返回成功
                    UpdaterResult.Success.Typed(StoreWriteResponse.Success.Typed(data))
                }

                key is TemplateStoreKey.All && data is TemplateStoreData.Collection -> {
                    // TODO: 实现批量写入 API（需要 RemoteTemplateDataSource）
                    // 当前没有远程数据源，直接返回成功
                    UpdaterResult.Success.Typed(StoreWriteResponse.Success.Typed(data))
                }

                else -> {
                    logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                    UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during template update: key=$key" }
            UpdaterResult.Error.Exception(e)
        }
    }
)

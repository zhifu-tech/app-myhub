package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardTemplateDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.network.NetworkException

internal fun createTemplateStoreUpdater(
    remoteCardTemplateDataSource: RemoteCardTemplateDataSource,
    logger: Logger = logger("TemplateStoreUpdater")
): TemplateStoreUpdater = Updater.by(
    post = { key, data ->
        try {
            when {
                key is TemplateStoreKey.ById && data is TemplateStoreData.Single -> {
                    val updatedTemplate = remoteCardTemplateDataSource.updateTemplate(
                        id = key.id,
                        template = data.template
                    )
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            TemplateStoreData.Single(updatedTemplate)
                        )
                    )
                }

                key is TemplateStoreKey.All && data is TemplateStoreData.Collection -> {
                    // ⚠️ 批量更新：当前实现逐个更新，未来可以优化为批量 API
                    val updatedTemplates = data.templates.map { template ->
                        remoteCardTemplateDataSource.updateTemplate(
                            id = template.id,
                            template = template
                        )
                    }
                    UpdaterResult.Success.Typed(
                        StoreWriteResponse.Success.Typed(
                            TemplateStoreData.Collection.fromTemplates(updatedTemplates)
                        )
                    )
                }

                else -> {
                    logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                    UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
                }
            }
        } catch (e: NetworkException) {
            logger.error(e) { "Network error during template update: key=$key" }
            UpdaterResult.Error.Exception(e)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during template update: key=$key" }
            UpdaterResult.Error.Exception(e)
        }
    }
)

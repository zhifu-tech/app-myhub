package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import tech.zhifu.app.myhub.datastore.datasource.card.RemoteCardTemplateDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error

internal fun createTemplateStoreUpdater(
    remoteCardTemplateDataSource: RemoteCardTemplateDataSource,
    logger: Logger,
): TemplateStoreUpdater = Updater.by(
    post = { key, data ->
        logger.debug("updater") {
            "post is called with key: $key (type=${key::class.qualifiedName}, " +
                "data=$data, instance=${key.hashCode()}"
        }
        when (key) {
            is TemplateStoreKey.ById if data is TemplateStoreData.Single -> {
                val updatedTemplate = remoteCardTemplateDataSource.updateTemplate(
                    id = key.id,
                    template = data.template
                )
                UpdaterResult.Success.Typed(
                    value = StoreWriteResponse.Success.Typed(
                        value = TemplateStoreData.Single(updatedTemplate)
                    )
                )
            }

            is TemplateStoreKey.All if data is TemplateStoreData.Collection -> {
                val updatedTemplates = data.templates.map { template ->
                    remoteCardTemplateDataSource.updateTemplate(
                        id = template.id,
                        template = template
                    )
                }
                UpdaterResult.Success.Typed(
                    value = StoreWriteResponse.Success.Typed(
                        value = TemplateStoreData.Collection.fromTemplates(updatedTemplates)
                    )
                )
            }

            else -> {
                logger.error { "Unsupported key/data combination: key=${key::class}, data=${data::class}" }
                UpdaterResult.Error.Message("Unsupported operation: key and data type mismatch")
            }
        }
    }
)

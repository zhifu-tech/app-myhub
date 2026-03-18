package tech.zhifu.app.myhub.datastore.repository.template

import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.card.LocalCardTemplateDataSource
import tech.zhifu.app.myhub.logger.Logger

fun createTemplateStoreSourceOfTruth(
    localCardTemplateDataSource: LocalCardTemplateDataSource,
    logger: Logger,
): TemplateStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is TemplateStoreKey.ById -> localCardTemplateDataSource.observeTemplates()
                .map { templates ->
                    templates.find { it.id == key.id }?.let { TemplateStoreData.Single(it) }
                }

            is TemplateStoreKey.All -> localCardTemplateDataSource.observeTemplates()
                .map { templates ->
                    if (templates.isEmpty()) null
                    else TemplateStoreData.Collection.fromTemplates(templates)
                }
        }
    },
    writer = { key, data ->
        when (key) {
            is TemplateStoreKey.ById if data is TemplateStoreData.Single -> {
                localCardTemplateDataSource.insertTemplate(data.template)
            }

            is TemplateStoreKey.All if data is TemplateStoreData.Collection -> {
                data.templates.forEach { template ->
                    localCardTemplateDataSource.insertTemplate(template)
                }
            }

            else -> {}
        }
    },
)

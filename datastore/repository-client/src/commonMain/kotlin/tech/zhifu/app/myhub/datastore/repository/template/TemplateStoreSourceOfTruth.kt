package tech.zhifu.app.myhub.datastore.repository.template

import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalCardTemplateDataSource
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug

@OptIn(ExperimentalStoreApi::class)
fun createTemplateStoreSourceOfTruth(
    localCardTemplateDataSource: LocalCardTemplateDataSource,
    logger: Logger,
): TemplateStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        logger.debug {
            "reader called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
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
        logger.debug {
            "writer called with key: $key (type=${key::class.qualifiedName}, " +
                "instance=${System.identityHashCode(key)}"
        }
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

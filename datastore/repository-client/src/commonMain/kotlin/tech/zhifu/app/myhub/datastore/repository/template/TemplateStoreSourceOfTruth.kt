package tech.zhifu.app.myhub.datastore.repository.template

import kotlinx.coroutines.flow.flow
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalCardTemplateDataSource

@OptIn(ExperimentalStoreApi::class)
fun createTemplateStoreSourceOfTruth(
    localCardTemplateDataSource: LocalCardTemplateDataSource
): TemplateStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        flow {
            try {
                when (key) {
                    is TemplateStoreKey.ById -> {
                        val templates = localCardTemplateDataSource.getTemplates()
                        val template = templates.find { it.id == key.id }
                        if (template != null) {
                            emit(TemplateStoreData.Single(template))
                        } else {
                            emit(null)
                        }
                    }

                    is TemplateStoreKey.All -> {
                        val templates = localCardTemplateDataSource.getTemplates()
                        if (templates.isEmpty()) {
                            emit(null)
                        } else {
                            emit(TemplateStoreData.Collection.fromTemplates(templates))
                        }
                    }
                }
            } catch (_: Exception) {
                emit(null)
            }
        }
    },
    writer = { key, data ->
        when {
            key is TemplateStoreKey.ById && data is TemplateStoreData.Single -> {
                localCardTemplateDataSource.insertTemplate(data.template)
            }

            key is TemplateStoreKey.All && data is TemplateStoreData.Collection -> {
                data.templates.forEach { template ->
                    localCardTemplateDataSource.insertTemplate(template)
                }
            }

            else -> {
                // Store5 框架应该保证类型匹配，这里主要是防御性编程
            }
        }
    },
    delete = { _ ->
        // LocalCardTemplateDataSource 没有 delete 方法
    },
    deleteAll = { }
)

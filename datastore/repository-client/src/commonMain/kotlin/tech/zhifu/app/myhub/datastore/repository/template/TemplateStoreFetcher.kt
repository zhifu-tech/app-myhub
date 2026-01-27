package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import tech.zhifu.app.myhub.datastore.datasource.LocalCardTemplateDataSource

@OptIn(ExperimentalStoreApi::class)
fun createTemplateStoreFetcher(
    localCardTemplateDataSource: LocalCardTemplateDataSource
): TemplateStoreFetcher = Fetcher.of { key ->
    // 模板当前没有远程数据源，从本地数据源获取
    when (key) {
        is TemplateStoreKey.ById -> {
            val templates = localCardTemplateDataSource.getTemplates()
            val template = templates.find { it.id == key.id }
                ?: throw NoSuchElementException("Template not found: ${key.id}")
            TemplateStoreData.Single(template)
        }

        is TemplateStoreKey.All -> {
            val templates = localCardTemplateDataSource.getTemplates()
            TemplateStoreData.Collection.fromTemplates(templates)
        }
    }
}

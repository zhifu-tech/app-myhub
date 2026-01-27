package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardTemplateDataSource

@OptIn(ExperimentalStoreApi::class)
fun createTemplateStoreFetcher(
    remoteCardTemplateDataSource: RemoteCardTemplateDataSource
): TemplateStoreFetcher = Fetcher.of { key ->
    when (key) {
        is TemplateStoreKey.ById -> {
            val template = remoteCardTemplateDataSource.getTemplateById(key.id)
                ?: throw NoSuchElementException("Template not found: ${key.id}")
            TemplateStoreData.Single(template)
        }

        is TemplateStoreKey.All -> {
            val templates = remoteCardTemplateDataSource.getTemplates()
            TemplateStoreData.Collection.fromTemplates(templates)
        }
    }
}

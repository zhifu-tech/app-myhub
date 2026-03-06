package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardTemplateDataSource

fun createTemplateStoreFetcher(
    remoteCardTemplateDataSource: RemoteCardTemplateDataSource
): TemplateStoreFetcher = Fetcher.ofResult { key ->
    when (key) {
        is TemplateStoreKey.ById -> {
            val template = remoteCardTemplateDataSource.getTemplateById(key.id)
            if (template == null) FetcherResult.Error.Message("Template not found: ${key.id}")
            else FetcherResult.Data(TemplateStoreData.Single(template))
        }

        is TemplateStoreKey.All -> {
            val templates = remoteCardTemplateDataSource.getTemplates()
            FetcherResult.Data(TemplateStoreData.Collection.fromTemplates(templates))
        }
    }
}

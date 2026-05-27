package tech.zhifu.app.myhub.datastore.repository.card

import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.FetcherResult

internal fun createCardStoreFetcher(): CardStoreFetcher = Fetcher.ofResult { _ ->
    FetcherResult.Error.Message("NOT SUPPORT CLOUD STORE FOR NOW")
}

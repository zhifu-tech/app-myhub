package tech.zhifu.app.myhub.datastore.repository.template

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.InsertionStrategy
import org.mobilenativefoundation.store.core5.StoreKey

@OptIn(ExperimentalStoreApi::class)
sealed class TemplateStoreKey : StoreKey<String> {

    data class ById(
        override val id: String,
    ) : TemplateStoreKey(), StoreKey.Single<String>

    data class All(
        override val insertionStrategy: InsertionStrategy = InsertionStrategy.REPLACE
    ) : TemplateStoreKey(), StoreKey.Collection<String>
}

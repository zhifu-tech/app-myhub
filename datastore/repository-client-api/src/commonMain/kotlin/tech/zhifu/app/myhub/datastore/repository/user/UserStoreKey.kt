package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.core5.StoreKey

@OptIn(ExperimentalStoreApi::class)
sealed class UserStoreKey : StoreKey<String> {

    data class ById(
        override val id: String,
    ) : UserStoreKey(), StoreKey.Single<String>

    data class PreferencesById(
        override val id: String,
    ) : UserStoreKey(), StoreKey.Single<String>
}

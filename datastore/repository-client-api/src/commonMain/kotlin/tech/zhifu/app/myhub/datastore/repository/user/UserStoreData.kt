package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.core5.StoreData
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

sealed class UserStoreData : StoreData<String> {

    data class UserData(
        val user: User?,
        override val id: String = user?.id.orEmpty()
    ) : UserStoreData(), StoreData.Single<String>

    data class PreferencesData(
        val preferences: UserPreferences,
        override val id: String = preferences.userId
    ) : UserStoreData(), StoreData.Single<String>
}

val UserStoreData.user: User?
    get() = (this as? UserStoreData.UserData)?.user

val UserStoreData.preferences: UserPreferences?
    get() = (this as? UserStoreData.PreferencesData)?.preferences

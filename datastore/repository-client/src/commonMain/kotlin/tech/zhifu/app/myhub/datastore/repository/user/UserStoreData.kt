package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.core5.StoreData
import org.mobilenativefoundation.store.store5.Validator
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

sealed class UserStoreData : StoreData<String> {

    data class UserData(
        val user: User?,
        override val id: String = user?.id.orEmpty()
    ) : UserStoreData(), StoreData.Single<String>

    data class PreferencesData(
        val preferences: UserPreferences? = null,
        val themeToWrite: String? = null,
        val languageToWrite: String? = null,
        val aiProviderToWrite: String? = null,
        val sortAsNameToWrite: Boolean? = null,
        val sortAsDateToWrite: Boolean? = null,
        val layoutAsListToWrite: Boolean? = null,
        override val id: String,
    ) : UserStoreData(), StoreData.Single<String>
}

val UserStoreData.user: User?
    get() = (this as? UserStoreData.UserData)?.user

val UserStoreData.preferences: UserPreferences?
    get() = (this as? UserStoreData.PreferencesData)?.preferences

fun createUserStoreDataValidator() =
    object : Validator<UserStoreData> {
        override suspend fun isValid(
            item: UserStoreData
        ): Boolean = when (item) {
            is UserStoreData.UserData -> item.user != null
            is UserStoreData.PreferencesData -> item.preferences != null
        }
    }

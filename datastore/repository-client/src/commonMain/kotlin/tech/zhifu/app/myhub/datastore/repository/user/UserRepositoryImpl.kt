package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

class UserRepositoryImpl(
    private val store: UserStore,
    private var currentUserId: String = "",
) : UserRepository {

    override fun userFlow(): Flow<User?> =
        userFlow(userId = currentUserId)

    override fun userFlow(
        userId: String
    ): Flow<User?> =
        store
            .stream<StoreReadResponse<User>>(
                request = StoreReadRequest.localOnly(
                    // 约定：空 id 表示当前登录用户
                    key = UserStoreKey.ById(id = userId),
                )
            )
            .map { it.dataOrNull()?.user }
            .onEach { user ->
                if (user != null && currentUserId.isEmpty()) {
                    currentUserId = user.id
                }
            }

    override suspend fun upsertUser(
        user: User
    ): Long =
        if (user.id.isBlank()) {
            insertUser(user)
        } else {
            updateUser(user)
        }

    override suspend fun insertUser(
        user: User,
    ): Long =
        store
            .write(
                request = StoreWriteRequest.of(
                    key = UserStoreKey.ById(id = user.id),
                    value = UserStoreData.UserData(user = user)
                )
            )
            .let { res ->
                if (res is StoreWriteResponse.Success) 1L else 0L
            }

    override suspend fun updateUser(
        user: User,
    ): Long =
        store
            .write(
                request = StoreWriteRequest.of(
                    key = UserStoreKey.ById(id = user.id),
                    value = UserStoreData.UserData(user = user)
                )
            )
            .let { res ->
                if (res is StoreWriteResponse.Success) 1L else 0L
            }

    override suspend fun hasUser(): Boolean {
        return currentUserId.isNotEmpty() || getUser() != null
    }

    override suspend fun getUser(): User? =
        getUser(userId = currentUserId)

    override suspend fun requireUser(): User =
        getUser()
            ?: run {
                throw IllegalStateException("At least one user is needed!")
            }

    override suspend fun getUser(
        userId: String
    ): User? =
        userFlow(userId = userId)
            .firstOrNull()

    override fun userPreferencesFlow(
        userId: String,
    ): Flow<UserPreferences?> =
        store
            .stream<StoreWriteResponse>(
                request = StoreReadRequest.localOnly(
                    key = UserStoreKey.PreferencesById(id = userId),
                )
            )
            .map { it.dataOrNull()?.preferences }

    override suspend fun getUserPreferences(
        userId: String,
    ): UserPreferences? =
        store
            .stream<StoreWriteResponse>(
                request = StoreReadRequest.localOnly(
                    key = UserStoreKey.PreferencesById(id = userId),
                )
            )
            .first()
            .dataOrNull()?.preferences

    override suspend fun upsertUserPreferences(
        userId: String,
        preferences: UserPreferences,
    ): Long =
        upsertUserPreferences(
            userId = userId,
            preferences = preferences,
        )

    override suspend fun updateUserPreferencesTheme(
        userId: String,
        theme: String,
    ): Long =
        upsertUserPreferencesInternal(
            userId = userId,
            theme = theme,
        )

    override suspend fun updateUserPreferencesLanguage(
        userId: String,
        language: String,
    ): Long =
        upsertUserPreferencesInternal(
            userId = userId,
            language = language,
        )

    override suspend fun updateUserPreferencesSort(
        userId: String,
        sortAsDate: Boolean,
        sortAsName: Boolean,
    ): Long =
        upsertUserPreferencesInternal(
            userId = userId,
            sortAsDate = sortAsDate,
            sortAsName = sortAsName,
        )

    override suspend fun updateUserPreferencesLayout(
        userId: String,
        layoutAsList: Boolean,
    ): Long =
        upsertUserPreferencesInternal(
            userId = userId,
            layoutAsList = layoutAsList,
        )

    private suspend fun upsertUserPreferencesInternal(
        userId: String,
        preferences: UserPreferences? = null,
        theme: String? = null,
        language: String? = null,
        sortAsDate: Boolean? = null,
        sortAsName: Boolean? = null,
        layoutAsList: Boolean? = null,
    ): Long =
        store
            .write(
                request = StoreWriteRequest.of(
                    key = UserStoreKey.PreferencesById(id = userId),
                    value = UserStoreData.PreferencesData(
                        id = userId,
                        preferences = preferences,
                        themeToWrite = theme,
                        languageToWrite = language,
                        sortAsDateToWrite = sortAsDate,
                        sortAsNameToWrite = sortAsName,
                        layoutAsListToWrite = layoutAsList,
                    )
                )
            )
            .let { res ->
                if (res is StoreWriteResponse.Success) 1L else 0L
            }
}

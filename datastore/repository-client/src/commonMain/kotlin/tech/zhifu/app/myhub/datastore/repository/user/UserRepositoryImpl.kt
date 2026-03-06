package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.impl.recordInsertOperation
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.sync.SyncEntityType

class UserRepositoryImpl(
    private val logger: Logger,
    private val syncRepository: SyncRepository,
    private val store: UserStore,
) : UserRepository {

    override suspend fun insertUser(user: User, needSync: Boolean) {
        store.write(
            StoreWriteRequest.of(
                key = UserStoreKey.ById(user.id),
                value = UserStoreData.UserData(user)
            )
        )

        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = user.id,
                entityType = SyncEntityType.User,
                entityId = user.id,
                payload = user,
            )
        }
    }

    override suspend fun getUser(): User = getUserOrNull() ?: run {
        logger.error { "Get User before bootstrap finished" }
        throw IllegalStateException("At least one user is needed!")
    }

    override suspend fun getUserOrNull(): User? =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.localOnly(
                key = UserStoreKey.ById(""/*current user*/)
            )
        ).first()
            .let { it as? StoreReadResponse.Data }
            ?.let { it.value as? UserStoreData.UserData }
            ?.user

    override suspend fun getUser(userId: String): UserStoreData =
        store.get<UserStoreKey, UserStoreData, StoreWriteResponse>(
            key = UserStoreKey.ById(userId)
        )

    override fun streamUser(userId: String, refresh: Boolean): Flow<StoreReadResponse<UserStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = UserStoreKey.ById(userId),
                refresh = refresh
            )
        )

    override fun streamUser(): Flow<User?> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.localOnly(
                key = UserStoreKey.ById(""), // 约定：空 id 表示当前登录用户
            )
        ).map {
            (it as? StoreReadResponse.Data)?.value?.user
        }

    override suspend fun insertUserPreferences(preferences: UserPreferences, needSync: Boolean) {
        store.write(
            StoreWriteRequest.of(
                key = UserStoreKey.PreferencesById(preferences.userId),
                value = UserStoreData.PreferencesData(preferences)
            )
        )

        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = preferences.userId,
                entityType = SyncEntityType.UserPreferences,
                entityId = preferences.userId,
                payload = preferences,
            )
        }
    }

    override suspend fun getUserPreferences(): UserPreferences {
        val user = getUser()
        return getUserPreferences(user.id).preferences ?: run {
            logger.error { "Get UserPreferences before bootstrap finished. " }
            throw IllegalStateException("UserPreferences not found")
        }
    }

    override suspend fun getUserPreferences(userId: String): UserStoreData =
        store.get<UserStoreKey, UserStoreData, StoreWriteResponse>(
            key = UserStoreKey.PreferencesById(userId)
        )

    override fun streamUserPreferences(userId: String, refresh: Boolean): Flow<StoreReadResponse<UserStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = UserStoreKey.PreferencesById(userId),
                refresh = refresh
            )
        )

    override suspend fun updateUserPreferencesTheme(userId: String, theme: String) {
        val current = getUserPreferences(userId).preferences ?: UserPreferences(userId = userId)
        if (current.theme == theme) return
        val updated = current.copy(theme = theme)
        insertUserPreferences(updated, needSync = true)
    }

    override suspend fun updateUserPreferencesLanguage(userId: String, language: String) {
        val current = getUserPreferences(userId).preferences ?: UserPreferences(userId = userId)
        if (current.language == language) return
        val updated = current.copy(language = language)
        insertUserPreferences(updated, needSync = true)
    }
}

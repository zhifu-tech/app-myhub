package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.impl.recordInsertOperation
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
class UserRepositoryImpl(
    private val store: UserStore,
    private val localUserDataSource: LocalUserDataSource,
    private val syncRepository: SyncRepository,
    private val logger: Logger = logger("UserRepo")
) : UserRepository {

    // ==================== User 操作 ====================

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

    override suspend fun hasUser(): Boolean {
        return localUserDataSource.getUserOrNull() != null
    }

    override suspend fun getUser(): User {
        val localUser = localUserDataSource.getUserOrNull()
        if (localUser != null) {
            return localUser
        }
        throw IllegalStateException("At least one user is needed!")
    }

    override suspend fun getUser(userId: String): UserStoreData? =
        runCatching {
            store.get<UserStoreKey, UserStoreData, StoreWriteResponse>(
                key = UserStoreKey.ById(userId)
            )
        }.onFailure {
            logger.error(it) { "get user for {user:$userId} from store failed" }
        }.getOrNull()

    override fun streamUser(userId: String, refresh: Boolean): Flow<StoreReadResponse<UserStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = UserStoreKey.ById(userId),
                refresh = refresh
            )
        )

    // ==================== UserPreferences 操作 ====================

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
        return getUserPreferences(user.id)?.preferences
            ?: throw IllegalStateException("UserPreferences not found")
    }

    override suspend fun getUserPreferences(userId: String): UserStoreData? =
        runCatching {
            store.get<UserStoreKey, UserStoreData, StoreWriteResponse>(
                key = UserStoreKey.PreferencesById(userId)
            )
        }.onFailure {
            logger.error(it) { "get user preferences for {user:$userId} from store failed" }
        }.getOrNull()

    override fun streamUserPreferences(userId: String, refresh: Boolean): Flow<StoreReadResponse<UserStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = UserStoreKey.PreferencesById(userId),
                refresh = refresh
            )
        )

    override suspend fun updateUserPreferencesTheme(userId: String, theme: String) {
        val current = getUserPreferences(userId)?.preferences ?: UserPreferences(userId = userId)
        if (current.theme == theme) return
        val updated = current.copy(theme = theme)
        insertUserPreferences(updated, needSync = true)
    }

    override suspend fun updateUserPreferencesLanguage(userId: String, language: String) {
        val current = getUserPreferences(userId)?.preferences ?: UserPreferences(userId = userId)
        if (current.language == language) return
        val updated = current.copy(language = language)
        insertUserPreferences(updated, needSync = true)
    }
}

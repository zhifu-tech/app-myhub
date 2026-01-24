package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import tech.zhifu.app.myhub.cache.Cache
import tech.zhifu.app.myhub.cache.CacheConfig
import tech.zhifu.app.myhub.cache.cache
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange

class UserRepositoryImpl(
    private val localUserDataSource: LocalUserDataSource,
    private val remoteUserDataSource: RemoteUserDataSource,
    private val syncRepository: SyncRepository,
    private val userCache: Cache<String, User> = cache(config = CacheConfig(maximumSize = 1)),
    private val currentUserKey: String = "currentUser",
) : UserRepository {

    override val syncUserChangeApplier: SyncChangeApplier = object : SyncChangeApplier {

        override suspend fun applyChanges(
            entity: SyncEntityType,
            operations: SyncOperations,
            change: SyncPullChange
        ) {
            when (operations) {
                SyncOperations.Insert -> syncRepository.applyChange(
                    deserializer = User.serializer(),
                    payload = change.payload
                ) {
                    insertUser(this, false)
                }

                else -> {}
            }
        }
    }

    override val syncUserPreferencesChangeApplier: SyncChangeApplier = object : SyncChangeApplier {
        override suspend fun applyChanges(
            entity: SyncEntityType,
            operations: SyncOperations,
            change: SyncPullChange
        ) {
            when (operations) {
                SyncOperations.Insert -> syncRepository.applyChange(
                    deserializer = UserPreferences.serializer(),
                    payload = change.payload
                ) {
                    insertUserPreferences(this, false)
                }

                else -> {}
            }
        }

    }

    override suspend fun insertUser(user: User, needSync: Boolean) {
        localUserDataSource.insertUser(user)
        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = user.id,
                entityType = SyncEntityType.User,
                entityId = user.id,
                payload = user
            )
        }
    }

    override suspend fun hasUser(): Boolean {
        return localUserDataSource.getUserOrNull() != null
    }

    override suspend fun getUser(): User {
        val cached = userCache.get(currentUserKey)
        if (cached != null) {
            logger.info { "get user from cache" }
            return cached
        }
        val localUserNullable = localUserDataSource.getUserOrNull()
        if (localUserNullable != null) {
            userCache.put(currentUserKey, localUserNullable)
            return localUserNullable
        }
        throw IllegalStateException("At least one user is needed!")
    }

    override fun observeUser(): Flow<User> {
        return localUserDataSource.observeUser().onEach {
            userCache.put(currentUserKey, it)
        }
    }

    override suspend fun getUserById(userId: String): User? {
        val localUser = localUserDataSource.getUser(userId)
        if (localUser != null) {
            return localUser
        }
        // 从网络拉取数据，不满足离线优先的原则，需要通过同步获取
        // 这里保留，防止出错。
        return remoteUserDataSource.fetchUser(userId)?.also {
            localUserDataSource.insertUser(it)
        }
    }

    override suspend fun insertUserPreferences(preferences: UserPreferences, needSync: Boolean) {
        localUserDataSource.insertUserPreferences(preferences)
        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = preferences.userId,
                entityType = SyncEntityType.UserPreferences,
                entityId = preferences.userId,
                payload = preferences
            )
        }
    }

    override suspend fun getUserPreferences(): UserPreferences {
        return localUserDataSource.getUserPreferences(getUser().id)!!
    }

    override suspend fun getUserPreferences(userId: String): UserPreferences? {
        return localUserDataSource.getUserPreferences(userId)
    }

    override fun observeUserPreferences(userId: String): Flow<UserPreferences> {
        return localUserDataSource.observeUserPreferences(userId)
    }

    override suspend fun updateUserPreferencesTheme(userId: String, theme: String) {
        val current = localUserDataSource.getUserPreferences(userId) ?: UserPreferences(userId = userId)
        if (current.theme == theme) return
        val updated = current.copy(theme = theme)
        localUserDataSource.insertUserPreferences(updated)
        syncRepository.recordInsertOperation(
            userId = userId,
            entityType = SyncEntityType.UserPreferences,
            entityId = userId,
            payload = updated
        )
    }

    override suspend fun updateUserPreferencesLanguage(userId: String, language: String) {
        val current = localUserDataSource.getUserPreferences(userId) ?: UserPreferences(userId = userId)
        if (current.language == language) return
        val updated = current.copy(language = language)
        localUserDataSource.insertUserPreferences(updated)
        syncRepository.recordInsertOperation(
            userId = userId,
            entityType = SyncEntityType.UserPreferences,
            entityId = userId,
            payload = updated
        )
    }
}


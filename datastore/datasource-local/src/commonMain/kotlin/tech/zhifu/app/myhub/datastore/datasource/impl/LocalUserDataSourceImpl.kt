package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

class LocalUserDataSourceImpl(
    private val database: MyHubDatabase
) : LocalUserDataSource {

    override suspend fun insertUser(user: User) {
        database.userQueries.insertUser(
            id = user.id,
            username = user.username,
            display_name = user.displayName,
            avatar_url = user.avatarUrl,
            avatar_text = user.avatarText,
            created_at = user.createdAt.toString(),
            updated_at = user.updatedAt.toString(),
            status = user.status,
            last_login_at = user.lastLoginAt.toString()
        )
    }

    override suspend fun updateUser(user: User) {
        database.userQueries.updateUser(
            username = user.username,
            display_name = user.displayName,
            avatar_url = user.avatarUrl,
            avatar_text = user.avatarText,
            updated_at = user.updatedAt.toString(),
            status = user.status,
            last_login_at = user.lastLoginAt.toString(),
            id = user.id
        )
    }

    override suspend fun getUser(): User {
        return database.userQueries.selectCurrentUser()
            .executeAsOne()
            .toDomain()
    }

    override suspend fun getUserOrNull(): User? {
        return database.userQueries.selectCurrentUser()
            .executeAsOneOrNull()
            ?.toDomain()
    }

    override suspend fun getUser(userId: String): User? {
        return database.userQueries
            .selectUserById(userId)
            .executeAsOneOrNull()
            ?.toDomain()
    }

    override fun observeUser(): Flow<User?> {
        return database.userQueries
            .selectCurrentUser()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }
    }

    override suspend fun deleteUser(userId: String) {
        database.userQueries.deleteUser(userId)
    }

    override suspend fun insertUserPreferences(preferences: UserPreferences) {
        database.user_preferencesQueries.insertUserPreferences(
            user_id = preferences.userId,
            theme = preferences.theme,
            language = preferences.language,
            default_card_type = preferences.defaultCardType,
            auto_sync = if (preferences.autoSync) 1L else 0L,
            sync_interval = preferences.syncInterval
        )
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences) {
        database.user_preferencesQueries.updateUserPreferences(
            theme = preferences.theme,
            language = preferences.language,
            default_card_type = preferences.defaultCardType,
            auto_sync = if (preferences.autoSync) 1L else 0L,
            sync_interval = preferences.syncInterval,
            user_id = preferences.userId
        )
    }

    override suspend fun getUserPreferences(userId: String): UserPreferences? {
        return database.user_preferencesQueries
            .selectUserPreferencesByUserId(userId)
            .executeAsOneOrNull()
            ?.toDomain()
    }

    override fun observeUserPreferences(userId: String): Flow<UserPreferences> {
        return database.user_preferencesQueries
            .selectUserPreferencesByUserId(userId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() ?: UserPreferences(userId = userId) }
    }
}

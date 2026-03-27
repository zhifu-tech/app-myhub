package tech.zhifu.app.myhub.datastore.datasource.user

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
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

    override fun flowUser(): Flow<User?> =
        database.userQueries
            .selectCurrentUser()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }

    override fun flowUser(userId: String): Flow<User?> =
        database.userQueries
            .selectUserById(userId)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }

    override suspend fun deleteUser(userId: String) {
        database.userQueries.deleteUser(userId)
    }

    override suspend fun insertUserPreferences(preferences: UserPreferences) {
        database.user_preferencesQueries.insertUserPreferences(
            user_id = preferences.userId,
            layout_as_list = if (preferences.layoutAsList) 1L else 0L,
            sort_as_date = if (preferences.sortAsDate) 1L else 0L,
            sort_as_name = if (preferences.sortAsName) 1L else 0L,
            auto_sync = if (preferences.autoSync) 1L else 0L,
            sync_interval = preferences.syncInterval
        )
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences) {
        database.user_preferencesQueries.updateUserPreferences(
            layout_as_list = if (preferences.layoutAsList) 1L else 0L,
            sort_as_date = if (preferences.sortAsDate) 1L else 0L,
            sort_as_name = if (preferences.sortAsName) 1L else 0L,
            auto_sync = if (preferences.autoSync) 1L else 0L,
            sync_interval = preferences.syncInterval,
            user_id = preferences.userId
        )
    }

    override suspend fun getUserPreferences(
        userId: String
    ): UserPreferences? = database.user_preferencesQueries
        .selectUserPreferencesByUserId(userId)
        .executeAsOneOrNull()
        ?.toDomain()

    override fun flowUserPreferences(
        userId: String,
    ): Flow<UserPreferences> = database.user_preferencesQueries
        .selectUserPreferencesByUserId(userId)
        .asFlow()
        .mapToOneOrNull(Dispatchers.Default)
        .map { it?.toDomain() ?: UserPreferences(userId = userId) }
}

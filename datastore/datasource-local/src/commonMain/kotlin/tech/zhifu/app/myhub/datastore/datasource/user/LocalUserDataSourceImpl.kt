package tech.zhifu.app.myhub.datastore.datasource.user

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

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

    override fun userPreferencesFlow(
        userId: String,
    ): Flow<UserPreferences?> =
        database.user_preferencesQueries
            .selectByUserId(user_id = userId)
            .asFlow()
            .mapToOneOrNull(context = Dispatchers.Default)
            .map { it?.toDomain() }

    override suspend fun getUserPreferences(
        userId: String
    ): UserPreferences? = database.user_preferencesQueries
        .selectByUserId(userId)
        .executeAsOneOrNull()
        ?.toDomain()

    override suspend fun upsertUserPreferences(
        preferences: UserPreferences,
    ) = database.user_preferencesQueries.upsert(
        user_id = preferences.userId,
        theme = preferences.theme.orEmpty(),
        language = preferences.language.orEmpty(),
        layout_as_list = if (preferences.layoutAsList) 1L else 0L,
        sort_as_date = if (preferences.sortAsDate) 1L else 0L,
        sort_as_name = if (preferences.sortAsName) 1L else 0L,
        auto_sync = if (preferences.autoSync) 1L else 0L,
        sync_interval = preferences.syncInterval
    ).also {
        logger.debug { "upsertUserPreferences: $it" }
    }

    override suspend fun updateUserPreferencesTheme(
        userId: String,
        theme: String,
    ) = database.user_preferencesQueries.updateTheme(
        theme = theme,
        user_id = userId
    ).also {
        logger.debug { "updateUserPreferencesTheme: $it, $userId, $theme" }
    }

    override suspend fun updateUserPreferencesSort(
        userId: String,
        sortAsDate: Boolean,
        sortAsName: Boolean
    ) = database.user_preferencesQueries
        .updateSort(
            sort_as_date = if (sortAsDate) 1L else 0L,
            sort_as_name = if (sortAsName) 1L else 0L,
            user_id = userId
        )

    override suspend fun updateUserPreferencesLayout(
        userId: String,
        layoutAsList: Boolean
    ): Long = database.user_preferencesQueries
        .updateLayout(
            layout_as_list = if (layoutAsList) 1L else 0L,
            user_id = userId
        )
}

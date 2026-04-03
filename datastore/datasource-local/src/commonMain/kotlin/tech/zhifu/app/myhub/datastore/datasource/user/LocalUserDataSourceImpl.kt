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
    private val database: MyHubDatabase,
) : LocalUserDataSource {

    override fun userFlow(
        userId: String
    ): Flow<User?> =
        if (userId.isEmpty()) {
            database.userQueries
                .selectCurrentUser()
                .asFlow()
                .mapToOneOrNull(Dispatchers.Default)
                .map { it?.toDomain() }
        } else {
            database.userQueries
                .selectUserById(userId)
                .asFlow()
                .mapToOneOrNull(Dispatchers.Default)
                .map { it?.toDomain() }
        }

    override suspend fun insertUser(
        user: User
    ): Long =
        database.userQueries
            .insertUser(
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

    override suspend fun updateUser(
        user: User
    ): Long =
        database.userQueries
            .updateUser(
                username = user.username,
                display_name = user.displayName,
                avatar_url = user.avatarUrl,
                avatar_text = user.avatarText,
                updated_at = user.updatedAt.toString(),
                status = user.status,
                last_login_at = user.lastLoginAt.toString(),
                id = user.id
            )

    override suspend fun getUser(
        userId: String
    ): User? =
        if (userId.isEmpty()) {
            database.userQueries
                .selectCurrentUser()
                .executeAsOneOrNull()
                ?.toDomain()
        } else {
            database.userQueries
                .selectUserById(userId)
                .executeAsOneOrNull()
                ?.toDomain()
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
    ): UserPreferences? =
        database.user_preferencesQueries
            .selectByUserId(userId)
            .executeAsOneOrNull()
            ?.toDomain()

    override suspend fun upsertUserPreferences(
        userId: String,
        preferences: UserPreferences,
    ) =
        database.user_preferencesQueries
            .upsert(
                user_id = userId,
                theme = preferences.theme.orEmpty(),
                language = preferences.language.orEmpty(),
                layout_as_list = if (preferences.layoutAsList) 1L else 0L,
                sort_as_date = if (preferences.sortAsDate) 1L else 0L,
                sort_as_name = if (preferences.sortAsName) 1L else 0L,
                auto_sync = if (preferences.autoSync) 1L else 0L,
                sync_interval = preferences.syncInterval
            )

    override suspend fun updateUserPreferencesTheme(
        userId: String,
        theme: String,
    ) =
        database.user_preferencesQueries
            .updateTheme(
                theme = theme,
                user_id = userId
            )

    override suspend fun updateUserPreferencesLanguage(
        userId: String,
        language: String,
    ) =
        database.user_preferencesQueries
            .updateLanguage(
                language = language,
                user_id = userId
            )

    override suspend fun updateUserPreferencesSort(
        userId: String,
        sortAsDate: Boolean,
        sortAsName: Boolean
    ): Long =
        database.user_preferencesQueries
            .updateSort(
                sort_as_date = if (sortAsDate) 1L else 0L,
                sort_as_name = if (sortAsName) 1L else 0L,
                user_id = userId
            )

    override suspend fun updateUserPreferencesLayout(
        userId: String,
        layoutAsList: Boolean
    ): Long =
        database.user_preferencesQueries
            .updateLayout(
                layout_as_list = if (layoutAsList) 1L else 0L,
                user_id = userId
            )
}

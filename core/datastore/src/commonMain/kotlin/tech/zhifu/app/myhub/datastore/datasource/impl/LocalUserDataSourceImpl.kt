package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import kotlin.time.Instant

/**
 * 本地用户数据源实现（使用SQLDelight）
 */
class LocalUserDataSourceImpl(
    private val database: MyHubDatabase
) : LocalUserDataSource {

    override suspend fun getCurrentUser(): User? {
        val userRow = database.userQueries.selectCurrentUser().awaitAsOneOrNull() ?: return null
        val preferencesRow = userRow.id.let { userId ->
            database.userQueries.selectUserPreferences(userId).awaitAsOneOrNull()
        }

        return userRow.toUser(preferencesRow)
    }

    override suspend fun saveUser(user: User) {
        database.transaction {
            // 保存用户信息
            database.userQueries.insertUser(
                id = user.id,
                username = user.username,
                email = user.email,
                display_name = user.displayName,
                avatar_url = user.avatarUrl,
                created_at = user.createdAt.toString()
            )

            // 保存用户偏好设置
            user.preferences?.let { prefs ->
                database.userQueries.insertOrUpdatePreferences(
                    user_id = user.id,
                    theme = prefs.theme,
                    language = prefs.language,
                    default_card_type = prefs.defaultCardType?.name,
                    auto_sync = if (prefs.autoSync) 1L else 0L,
                    sync_interval = prefs.syncInterval
                )
            }
        }
    }

    override suspend fun clearUser() {
        // 删除用户会自动删除关联的偏好设置（外键约束）
        val user = database.userQueries.selectCurrentUser().awaitAsOneOrNull()
        user?.let {
            database.userQueries.deleteUser(it.id)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUser(): Flow<User?> {
        return database.userQueries.selectCurrentUser()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .flatMapLatest { userRow ->
                if (userRow == null) return@flatMapLatest flowOf(null)

                database.userQueries.selectUserPreferences(userRow.id)
                    .asFlow()
                    .mapToOneOrNull(Dispatchers.Default)
                    .map { prefsRow ->
                        userRow.toUser(prefsRow)
                    }
            }
    }

    /**
     * 将数据库行转换为User实体
     */
    private fun tech.zhifu.app.myhub.datastore.database.User.toUser(
        preferencesRow: tech.zhifu.app.myhub.datastore.database.User_preferences?
    ): User {
        val preferences = preferencesRow?.let {
            UserPreferences(
                theme = it.theme,
                language = it.language,
                defaultCardType = it.default_card_type?.let { type -> CardType.valueOf(type) },
                autoSync = it.auto_sync == 1L,
                syncInterval = it.sync_interval
            )
        }

        return User(
            id = id,
            username = username,
            email = email,
            displayName = display_name,
            avatarUrl = avatar_url,
            createdAt = Instant.parse(created_at),
            preferences = preferences
        )
    }
}

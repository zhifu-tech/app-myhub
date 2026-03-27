package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface UserRepository {

    // ==================== User 操作 ====================

    suspend fun insertUser(
        user: User,
        needSync: Boolean = true
    )

    suspend fun hasUser(): Boolean = getUserOrNull() != null

    suspend fun getUser(): User

    suspend fun getUserOrNull(): User?

    fun streamUser(): Flow<User?>

    // ==================== UserPreferences 操作 ====================

    fun streamUserPreferences(
        userId: String,
    ): Flow<UserPreferences?>

    suspend fun insertUserPreferences(
        preferences: UserPreferences,
        needSync: Boolean = true
    )
}

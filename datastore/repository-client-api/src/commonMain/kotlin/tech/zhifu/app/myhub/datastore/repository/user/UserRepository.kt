package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.operations.user.UserPreferencesOperations

interface UserRepository :
    UserPreferencesOperations {

    // ==================== User 操作 ====================

    suspend fun insertUser(
        user: User,
        needSync: Boolean = true
    )

    suspend fun hasUser(): Boolean = getUserOrNull() != null

    suspend fun getUser(): User

    suspend fun getUserOrNull(): User?

    fun userFlow(): Flow<User?>
}

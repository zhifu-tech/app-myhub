package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface LocalUserDataSource {
    suspend fun insertUser(user: User)

    suspend fun updateUser(user: User)

    suspend fun getUserOrNull(): User?

    suspend fun getUser(userId: String): User?

    fun observeUser(): Flow<User?>

    suspend fun deleteUser(userId: String)

    suspend fun insertUserPreferences(preferences: UserPreferences)

    suspend fun updateUserPreferences(preferences: UserPreferences)

    suspend fun getUserPreferences(userId: String): UserPreferences?

    fun observeUserPreferences(userId: String): Flow<UserPreferences>
}

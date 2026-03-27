package tech.zhifu.app.myhub.datastore.datasource.user

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface LocalUserDataSource {
    suspend fun insertUser(user: User)

    suspend fun updateUser(user: User)

    suspend fun getUserOrNull(): User?

    suspend fun getUser(userId: String): User?

    fun flowUser(): Flow<User?>

    fun flowUser(userId: String): Flow<User?>

    suspend fun deleteUser(userId: String)

    suspend fun insertUserPreferences(preferences: UserPreferences)

    suspend fun updateUserPreferences(preferences: UserPreferences)

    suspend fun getUserPreferences(userId: String): UserPreferences?

    fun flowUserPreferences(userId: String): Flow<UserPreferences>
}

package tech.zhifu.app.myhub.datastore.datasource.user

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.operations.user.UserPreferencesOperations

interface LocalUserDataSource :
    UserPreferencesOperations {
    suspend fun insertUser(user: User)

    suspend fun updateUser(user: User)

    suspend fun getUserOrNull(): User?

    suspend fun getUser(userId: String): User?

    fun flowUser(): Flow<User?>

    fun flowUser(userId: String): Flow<User?>

    suspend fun deleteUser(userId: String)
}

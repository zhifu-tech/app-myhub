package tech.zhifu.app.myhub.datastore.operations.user

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.User

interface UserOperations {
    fun userFlow(userId: String): Flow<User?>

    suspend fun insertUser(user: User): Long

    suspend fun updateUser(user: User): Long

    suspend fun getUser(userId: String): User?
}

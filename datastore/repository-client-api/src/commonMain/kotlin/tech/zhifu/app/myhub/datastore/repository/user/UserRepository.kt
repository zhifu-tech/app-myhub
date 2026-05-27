package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.operations.user.UserOperations
import tech.zhifu.app.myhub.datastore.operations.user.UserPreferencesOperations

interface UserRepository :
    UserOperations,
    UserPreferencesOperations {

    fun userFlow(): Flow<User?>

    suspend fun upsertUser(
        user: User
    ): Long

    suspend fun hasUser(): Boolean
    suspend fun getUser(): User?
    suspend fun requireUser(): User

}

package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.UserRepository

/**
 * 用户仓库实现
 */
class UserRepositoryImpl(
    private val localDataSource: LocalUserDataSource
) : UserRepository {

    override fun getCurrentUser(): Flow<User?> {
        return localDataSource.observeUser()
    }

    override suspend fun updateUser(user: User): Result<User> {
        return try {
            localDataSource.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences> {
        return try {
            val currentUser = localDataSource.getCurrentUser()
                ?: return Result.failure(IllegalStateException("No user found"))

            val updatedUser = currentUser.copy(preferences = preferences)
            localDataSource.saveUser(updatedUser)
            Result.success(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPreferences(): UserPreferences? {
        return localDataSource.getCurrentUser()?.preferences
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            localDataSource.clearUser()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


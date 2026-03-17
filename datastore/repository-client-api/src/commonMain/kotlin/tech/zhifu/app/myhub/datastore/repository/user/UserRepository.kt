package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.store5.StoreReadResponse
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface UserRepository {

    // ==================== User 操作 ====================

    suspend fun insertUser(user: User, needSync: Boolean = true)

    suspend fun hasUser(): Boolean = getUserOrNull() != null

    suspend fun getUser(): User

    suspend fun getUserOrNull(): User?

    suspend fun getUser(userId: String): UserStoreData?

    fun streamUser(): Flow<User?>

    fun streamUser(userId: String, refresh: Boolean = false): Flow<StoreReadResponse<UserStoreData>>

    // ==================== UserPreferences 操作 ====================

    fun streamUserPreferences(
        userId: String,
        refresh: Boolean = false,
    ): Flow<StoreReadResponse<UserStoreData>>

    suspend fun insertUserPreferences(
        preferences: UserPreferences,
        needSync: Boolean = true
    )
}

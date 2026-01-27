package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface RemoteUserDataSource {
    suspend fun fetchUser(userId: String): User?
    
    suspend fun updateUser(user: User): User
    
    suspend fun updateUserPreferences(userId: String, preferences: UserPreferences): UserPreferences
}

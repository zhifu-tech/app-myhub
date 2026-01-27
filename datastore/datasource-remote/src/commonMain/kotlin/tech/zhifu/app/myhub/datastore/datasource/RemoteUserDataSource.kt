package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface RemoteUserDataSource {
    suspend fun getUser(id: String): User?
    
    suspend fun createUser(user: User): User
    
    suspend fun updateUser(id: String, user: User): User
    
    suspend fun deleteUser(id: String)
    
    suspend fun getUserPreferences(userId: String): UserPreferences?
    
    suspend fun updateUserPreferences(userId: String, preferences: UserPreferences): UserPreferences
}

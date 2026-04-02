package tech.zhifu.app.myhub.datastore.operations.user

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

interface UserPreferencesOperations {

    fun userPreferencesFlow(
        userId: String,
    ): Flow<UserPreferences?>

    suspend fun getUserPreferences(
        userId: String,
    ): UserPreferences?

    suspend fun upsertUserPreferences(
        preferences: UserPreferences,
    ): Long

    suspend fun updateUserPreferencesTheme(
        userId: String,
        theme: String
    ): Long

    suspend fun updateUserPreferencesSort(
        userId: String,
        sortAsDate: Boolean,
        sortAsName: Boolean
    ): Long

    suspend fun updateUserPreferencesLayout(
        userId: String,
        layoutAsList: Boolean
    ): Long
}

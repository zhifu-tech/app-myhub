package tech.zhifu.app.myhub.datastore.datasource.user

import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import kotlin.time.Instant

class RemoteUserDataSourceNoop : RemoteUserDataSource {
    override suspend fun getUser(id: String): User = User(
        id = id,
        username = "dev-$id",
        displayName = "Dev User",
        avatarUrl = "",
        avatarText = "DU",
        createdAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(0),
        status = "active",
        lastLoginAt = Instant.fromEpochMilliseconds(0),
    )

    override suspend fun createUser(user: User): User = user

    override suspend fun updateUser(id: String, user: User): User = user

    override suspend fun deleteUser(id: String) = Unit

    override suspend fun getUserPreferences(userId: String): UserPreferences =
        UserPreferences(userId = userId)

    override suspend fun updateUserPreferences(
        userId: String,
        preferences: UserPreferences,
    ): UserPreferences = preferences
}

package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.sync.recordInsertOperation
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.sync.SyncEntityType

class UserRepositoryImpl(
    private val logger: Logger,
    private val syncRepository: SyncRepository,
    private val store: UserStore,
) : UserRepository {

    override suspend fun insertUser(
        user: User,
        needSync: Boolean
    ) {
        store.write(
            request = StoreWriteRequest.of(
                key = UserStoreKey.ById(id = user.id),
                value = UserStoreData.UserData(user = user)
            )
        )

        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = user.id,
                entityType = SyncEntityType.User,
                entityId = user.id,
                payload = user,
            )
        }
    }

    override suspend fun getUser(): User =
        getUserOrNull() ?: run {
            logger.error { "Get User before bootstrap finished" }
            throw IllegalStateException("At least one user is needed!")
        }

    override suspend fun getUserOrNull(): User? =
        store
            .stream<StoreReadRequest<UserStoreData>>(
                request = StoreReadRequest.localOnly(
                    // 约定：空 id 表示当前登录用户
                    key = UserStoreKey.ById(""),
                )
            )
            .filterNot { it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData }
            .first()
            .dataOrNull()
            ?.user

    override fun userFlow(): Flow<User?> =
        store
            .stream<StoreReadResponse<User>>(
                request = StoreReadRequest.localOnly(
                    // 约定：空 id 表示当前登录用户
                    key = UserStoreKey.ById(id = ""),
                )
            )
            .map { it.dataOrNull()?.user }

    override fun userPreferencesFlow(
        userId: String,
    ): Flow<UserPreferences?> =
        store
            .stream<StoreWriteResponse>(
                request = StoreReadRequest.localOnly(
                    key = UserStoreKey.PreferencesById(id = userId),
                )
            )
            .map { it.dataOrNull()?.preferences }

    override suspend fun getUserPreferences(
        userId: String,
    ): UserPreferences? =
        store
            .stream<StoreWriteResponse>(
                request = StoreReadRequest.localOnly(
                    key = UserStoreKey.PreferencesById(id = userId),
                )
            )
            .first()
            .dataOrNull()?.preferences

    override suspend fun upsertUserPreferences(
        preferences: UserPreferences,
    ): Long {
        val userId = preferences.userId
        val res = store
            .write(
                request = StoreWriteRequest.of(
                    key = UserStoreKey.PreferencesById(id = userId),
                    value = UserStoreData.PreferencesData(
                        id = userId,
                        preferences = preferences,
                    )
                )
            )
        return if (res is StoreWriteResponse.Success) 1L else 0L
    }

    override suspend fun updateUserPreferencesTheme(
        userId: String,
        theme: String,
    ): Long {
        val res = store
            .write(
                request = StoreWriteRequest.of(
                    key = UserStoreKey.PreferencesById(id = userId),
                    value = UserStoreData.PreferencesData(
                        id = userId,
                        themeToWrite = theme,
                    )
                )
            )
        return if (res is StoreWriteResponse.Success) 1L else 0L
    }


    override suspend fun updateUserPreferencesSort(
        userId: String,
        sortAsDate: Boolean,
        sortAsName: Boolean,
    ): Long {
        val res = store
            .write(
                request = StoreWriteRequest.of(
                    key = UserStoreKey.PreferencesById(id = userId),
                    value = UserStoreData.PreferencesData(
                        id = userId,
                        sortAsDateToWrite = sortAsDate,
                        sortAsNameToWrite = sortAsName,
                    )
                )
            )
        return if (res is StoreWriteResponse.Success) 1L else 0L
    }

    override suspend fun updateUserPreferencesLayout(
        userId: String,
        layoutAsList: Boolean,
    ): Long {
        val res = store
            .write(
                request = StoreWriteRequest.of(
                    key = UserStoreKey.PreferencesById(id = userId),
                    value = UserStoreData.PreferencesData(
                        id = userId,
                        layoutAsListToWrite = layoutAsList,
                    )
                )
            )
        return if (res is StoreWriteResponse.Success) 1L else 0L
    }
}

package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.User

interface RemoteUserDataSource {
    suspend fun fetchUser(userId: String): User?
}

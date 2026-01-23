package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.model.domain.User

/**
 * 用户仓库接口（基础接口，同步风格）
 */
interface UserRepository {
    suspend fun getUserById(userId: String): User? {
        throw UnsupportedOperationException("Not supported in this repository")
    }
}

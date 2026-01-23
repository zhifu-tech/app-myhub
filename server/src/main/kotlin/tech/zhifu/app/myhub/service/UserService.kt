package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.domain.User

/**
 * 用户服务
 */
class UserService(
    private val userRepository: UserRepository
) {
    suspend fun fetchUser(userId: String): User? {
        return userRepository.getUserById(userId = userId)
    }
}

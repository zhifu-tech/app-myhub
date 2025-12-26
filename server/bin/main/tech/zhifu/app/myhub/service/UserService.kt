package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.exception.ValidationException
import tech.zhifu.app.myhub.datastore.repository.UserRepository

/**
 * 用户服务
 */
class UserService(
    private val userRepository: UserRepository
) {
    suspend fun getCurrentUser(): User {
        return userRepository.getCurrentUser()
            ?: throw tech.zhifu.app.myhub.exception.NotFoundException("User", "current")
    }

    suspend fun updateUser(user: User): User {
        if (user.username.isBlank()) {
            throw ValidationException("Username cannot be empty")
        }

        return userRepository.updateUser(user)
    }
}


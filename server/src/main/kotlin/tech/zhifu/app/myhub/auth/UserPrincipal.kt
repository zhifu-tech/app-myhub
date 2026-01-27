package tech.zhifu.app.myhub.auth

import io.ktor.server.auth.Principal
import tech.zhifu.app.myhub.datastore.model.domain.User

/**
 * 用户主体（Principal）
 * 用于存储认证后的用户信息
 */
data class UserPrincipal(
    val user: User
) : Principal {
    val userId: String
        get() = user.id
}

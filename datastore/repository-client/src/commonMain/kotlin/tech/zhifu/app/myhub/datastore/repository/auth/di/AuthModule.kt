package tech.zhifu.app.myhub.datastore.repository.auth.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.auth.AuthService
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

/**
 * 认证模块
 * 提供 AuthService
 */
val authModule = module {
    single<AuthService> {
        AuthService(
            remoteAuthDataSource = get(),
            tokenStorage = get(),
            userRepository = get()
        )
    }
}

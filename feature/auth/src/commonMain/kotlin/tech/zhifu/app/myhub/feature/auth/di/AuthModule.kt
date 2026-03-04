package tech.zhifu.app.myhub.feature.auth.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.auth.api.session.AuthSessionCoordinator
import tech.zhifu.app.myhub.feature.auth.session.DefaultAuthSessionCoordinator

fun authModule() = module {
    single<AuthSessionCoordinator> { DefaultAuthSessionCoordinator() }
}

package tech.zhifu.app.myhub.network.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.network.auth.TokenStorage
import tech.zhifu.app.myhub.network.auth.impl.Crypto
import tech.zhifu.app.myhub.network.auth.impl.CryptoImpl
import tech.zhifu.app.myhub.network.auth.impl.SecureTokenStorage
import tech.zhifu.app.myhub.settings.di.coreSettingsModule

val networkModule = module {
    // 包含 core/settings 模块
    includes(coreSettingsModule)

    // 加密库
    factory<Crypto> {
        CryptoImpl()
    }

    // Token 存储（加密存储）
    single<TokenStorage> {
        SecureTokenStorage(
            localStore = get(),
            crypto = get()
        )
    }
}

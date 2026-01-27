package tech.zhifu.app.myhub.network.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.network.auth.TokenStorage
import tech.zhifu.app.myhub.network.auth.impl.Crypto
import tech.zhifu.app.myhub.network.auth.impl.CryptoImpl
import tech.zhifu.app.myhub.network.auth.impl.SecureTokenStorage
import tech.zhifu.app.myhub.settings.di.coreSettingsModule

/**
 * 网络模块
 * 提供 TokenStorage、Crypto 等基础组件
 *
 * 注意：HttpClient 在 RemoteDataSourceModule 中创建
 * Ktor 会自动根据平台选择引擎（Android/OkHttp, iOS/Darwin, JVM/CIO, JS/Js）
 */
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

    // 注意：带认证的 HttpClient 在 RemoteDataSourceModule 中创建
    // 如果需要不带认证的 HttpClient，可以使用 createHttpClient() 直接创建
}

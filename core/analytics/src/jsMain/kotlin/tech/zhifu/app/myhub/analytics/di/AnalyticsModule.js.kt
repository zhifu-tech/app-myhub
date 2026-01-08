package tech.zhifu.app.myhub.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module
import tech.zhifu.app.myhub.analytics.AnalyticsConfig
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.analytics.Region
import tech.zhifu.app.myhub.config.AppBuildConfig

internal actual fun analyticsPlatformModule(): Module = module {
    factory<AnalyticsConfig> {
        AnalyticsConfig(
            region = if (AppBuildConfig.isGooglePlay()) Region.OVERSEAS else Region.DOMESTIC,
            enabled = true,
            debugMode = AppBuildConfig.enableDebugFeatures,
            providers = buildList {
                // Console Provider（所有平台都支持，用于调试）
                add(ProviderConfig(type = ProviderType.CONSOLE))
                if (AppBuildConfig.isGooglePlay()) {
                    add(
                        ProviderConfig(
                            type = ProviderType.FIREBASE,
                            customParams = getProviderConfigFirebaseOptions(),
                        )
                    )
                } else {
                    // FIXME: todo
                    // add(ProviderConfig(type = ProviderType.UMENG))
                }
            },
        )
    }
}

// 注意：这些配置值应该从 Firebase Console 获取：
// Firebase Console > Project Settings > Your apps > Web app 获取
// 打开 Firebase Console: https://console.firebase.google.com/
private fun getProviderConfigFirebaseOptions() = mapOf(
    "apiKey" to "AIzaSyDwhlwvWxk4VQAqa5WI9uAsgbkKwB53iyI",
    "authDomain" to "myhub-2a6db.firebaseapp.com",
    "projectId" to "myhub-2a6db",
    "storageBucket" to "myhub-2a6db.firebasestorage.app",
    "appId" to "1:557805960070:web:e3a7c4b4719b7c232dca07",
    "measurementId" to "G-GBSVG75ZE6",
)

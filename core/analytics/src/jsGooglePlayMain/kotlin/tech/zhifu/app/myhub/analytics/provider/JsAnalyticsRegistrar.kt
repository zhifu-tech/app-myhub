package tech.zhifu.app.myhub.analytics.provider

import dev.gitlive.firebase.FirebaseOptions
import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.Platform
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.analytics.Region
import tech.zhifu.app.myhub.config.AppBuildConfig

/**
 * Js 平台 Provider 注册器
 */
class JsAnalyticsRegistrar : AnalyticsProviderRegistrar {

    override fun register(factory: AnalyticsProviderFactory) {
        factory.register(ProviderType.FIREBASE) {
            FirebaseProvider(
                name = "Firebase",
                supportedPlatforms = setOf(Platform.JS),
                supportedRegions = setOf(Region.OVERSEAS),
            )
        }
    }
}

internal fun ProviderConfig.toFirebaseOptions(): FirebaseOptions? = FirebaseOptions(
    apiKey = customParams["apiKey"]!!,
    authDomain = customParams["authDomain"],
    projectId = customParams["projectId"],
    storageBucket = customParams["storageBucket"]!!,
    applicationId = customParams["appId"]!!,
)


internal fun MutableList<ProviderConfig>.addChannelList() {
    add(
        ProviderConfig(
            type = ProviderType.FIREBASE,
            // 注意：这些配置值应该从 Firebase Console 获取：
            // Firebase Console > Project Settings > Your apps > Web app 获取
            // 打开 Firebase Console: https://console.firebase.google.com/
            customParams = mapOf(
                "apiKey" to "AIzaSyDwhlwvWxk4VQAqa5WI9uAsgbkKwB53iyI",
                "authDomain" to "myhub-2a6db.firebaseapp.com",
                "projectId" to "myhub-2a6db",
                "storageBucket" to "myhub-2a6db.firebasestorage.app",
                "appId" to "1:557805960070:web:e3a7c4b4719b7c232dca07",
                "measurementId" to "G-GBSVG75ZE6",
            )
        )
    )
}

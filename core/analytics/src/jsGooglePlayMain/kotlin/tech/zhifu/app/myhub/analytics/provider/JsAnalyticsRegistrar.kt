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

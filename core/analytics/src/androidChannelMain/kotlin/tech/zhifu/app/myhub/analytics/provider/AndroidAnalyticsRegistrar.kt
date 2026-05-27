package tech.zhifu.app.myhub.analytics.provider

import android.content.Context
import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.Platform
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.Region

/**
 * Android 平台 Provider 注册器
 */
class AndroidAnalyticsRegistrar : AnalyticsProviderRegistrar {

    override fun register(factory: AnalyticsProviderFactory) {
    }
}

internal fun MutableList<ProviderConfig>.addChannelList() {
    // Keep it empty
}

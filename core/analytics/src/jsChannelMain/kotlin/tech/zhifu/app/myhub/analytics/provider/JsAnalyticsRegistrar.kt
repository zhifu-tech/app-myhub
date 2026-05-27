package tech.zhifu.app.myhub.analytics.provider

import tech.zhifu.app.myhub.analytics.AnalyticsProviderFactory
import tech.zhifu.app.myhub.analytics.AnalyticsProviderRegistrar
import tech.zhifu.app.myhub.analytics.ProviderType
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.analytics.ProviderConfig

/**
 * Js 平台 Provider 注册器
 */
class JsAnalyticsRegistrar : AnalyticsProviderRegistrar {

    override fun register(factory: AnalyticsProviderFactory) {
        logger.info { "register JsAnalyticsRegistrar for defualt" }
    }
}


internal fun MutableList<ProviderConfig>.addChannelList() {

}

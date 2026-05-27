package tech.zhifu.app.myhub.feature.settings.content.ai

import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode_direct_api
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode_disabled
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode_server_gateway
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode

fun ProviderMode.labelToken() = when (this) {
    ProviderMode.DISABLED -> Res.string.feature_settings_ai_mode_disabled
    ProviderMode.SERVER_GATEWAY -> Res.string.feature_settings_ai_mode_server_gateway
    ProviderMode.DIRECT_API -> Res.string.feature_settings_ai_mode_direct_api
}

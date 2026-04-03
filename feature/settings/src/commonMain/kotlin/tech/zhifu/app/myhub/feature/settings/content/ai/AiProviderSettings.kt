package tech.zhifu.app.myhub.feature.settings.content.ai

import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode_direct_api
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode_disabled
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode_server_gateway
import tech.zhifu.app.myhub.ui.state.ai.AIProviderMode

fun AIProviderMode.labelToken() = when (this) {
    AIProviderMode.Disabled -> Res.string.feature_settings_ai_mode_disabled
    AIProviderMode.ServerGateway -> Res.string.feature_settings_ai_mode_server_gateway
    AIProviderMode.DirectApi -> Res.string.feature_settings_ai_mode_direct_api
}

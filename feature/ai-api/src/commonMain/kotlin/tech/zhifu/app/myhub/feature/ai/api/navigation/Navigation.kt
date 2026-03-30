package tech.zhifu.app.myhub.feature.ai.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.navigation.AppNavigator

@Serializable
object AiNavKey : NavKey

fun AppNavigator.navigateToAiCapture() {
    navigate(AiNavKey)
}

package tech.zhifu.app.myhub.feature.ai.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.navigation.AppNavigator

@Serializable
object AINavKey : NavKey

fun AppNavigator.navigateToAICapture() {
    navigate(AINavKey)
}

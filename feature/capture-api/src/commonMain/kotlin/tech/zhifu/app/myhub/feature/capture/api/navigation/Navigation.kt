package tech.zhifu.app.myhub.feature.capture.api.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.navigation.AppNavigator

@Serializable
object CaptureNavKey : NavKey

fun AppNavigator.navigateToCapture() {
    navigate(CaptureNavKey)
}

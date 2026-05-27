package tech.zhifu.app.myhub.feature.mixed.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.navigation.AppNavigator

@Serializable
object OpenSourceLicensesNavKey : NavKey

@Serializable
object SupportNavKey : NavKey

fun AppNavigator.navigateToOpenSourceLicenses() {
    navigate(OpenSourceLicensesNavKey)
}

fun AppNavigator.navigateToSupport() {
    navigate(SupportNavKey)
}

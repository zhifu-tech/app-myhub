package tech.zhifu.app.myhub.feature.mixed.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.feature.mixed.api.OpenSourceLicensesNavKey
import tech.zhifu.app.myhub.feature.mixed.api.SupportNavKey
import tech.zhifu.app.myhub.feature.mixed.license.OpenSourceLicensesRoute
import tech.zhifu.app.myhub.feature.mixed.support.SupportRoute
import tech.zhifu.app.myhub.navigation.AppNavigator

fun EntryProviderScope<NavKey>.mixedEntry(
    navigator: AppNavigator,
) {
    entry<OpenSourceLicensesNavKey> {
        OpenSourceLicensesRoute(
            navigator = navigator,
        )
    }
    entry<SupportNavKey> {
        SupportRoute(
            navigator = navigator,
        )
    }
}

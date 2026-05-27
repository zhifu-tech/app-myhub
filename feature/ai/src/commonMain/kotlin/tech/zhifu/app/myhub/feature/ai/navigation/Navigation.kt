package tech.zhifu.app.myhub.feature.ai.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.feature.ai.AIScreen
import tech.zhifu.app.myhub.feature.ai.api.navigation.AINavKey
import tech.zhifu.app.myhub.navigation.AppNavigator

fun EntryProviderScope<NavKey>.aiEntry(
    navigator: AppNavigator,
) {
    entry<AINavKey> {
        AIScreen(navigator = navigator)
    }
}

package tech.zhifu.app.myhub.feature.capture.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.feature.capture.CaptureScreen
import tech.zhifu.app.myhub.feature.capture.api.navigation.CaptureNavKey
import tech.zhifu.app.myhub.navigation.AppNavigator

fun EntryProviderScope<NavKey>.captureEntry(navigator: AppNavigator) {
    entry<CaptureNavKey> {
        CaptureScreen(
            onClose = navigator::goBack
        )
    }
}

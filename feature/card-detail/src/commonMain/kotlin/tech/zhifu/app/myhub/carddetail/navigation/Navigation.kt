package tech.zhifu.app.myhub.carddetail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.carddetail.CardDetailScreen
import tech.zhifu.app.myhub.core.navigation.AppNavigator
import tech.zhifu.app.myhub.feature.carddetail.api.CardNavKey

fun EntryProviderScope<NavKey>.cardEntry(navigator: AppNavigator) {
    entry<CardNavKey> { key ->
        CardDetailScreen(
            cardId = key.id,
            onNavigateBack = { navigator.goBack() }
        )
    }
}

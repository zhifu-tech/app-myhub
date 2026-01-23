package tech.zhifu.app.myhub.feature.card.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import tech.zhifu.app.myhub.feature.card.CardDetailScreen
import tech.zhifu.app.myhub.feature.card.api.CardNavKey
import tech.zhifu.app.myhub.navigation.AppNavigator

fun EntryProviderScope<NavKey>.cardEntry(navigator: AppNavigator) {
    entry<CardNavKey> { key ->
        CardDetailScreen(
            cardId = key.cardId,
            onNavigateBack = { navigator.goBack() }
        )
    }
}

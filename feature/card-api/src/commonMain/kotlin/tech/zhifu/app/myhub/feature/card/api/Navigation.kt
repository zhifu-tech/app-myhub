package tech.zhifu.app.myhub.feature.card.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.navigation.AppNavigator

@Serializable
data class CardNavKey(val cardId: String) : NavKey

fun AppNavigator.navigateToCardDetail(cardId: String) {
    navigate(CardNavKey(cardId))
}

package tech.zhifu.app.myhub.feature.carddetail.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.core.navigation.AppNavigator

@Serializable
data class CardNavKey(val id: String) : NavKey

fun AppNavigator.navigateToCardDetail(id: String) {
    navigate(CardNavKey(id))
}

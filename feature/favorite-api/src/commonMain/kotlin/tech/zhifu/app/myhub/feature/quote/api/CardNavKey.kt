package tech.zhifu.app.myhub.feature.quote.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import tech.zhifu.app.myhub.core.navigation.AppNavigator

@Serializable
data class QuoteNavKey(val id: String) : NavKey

fun AppNavigator.navigateToQuote(id: String) {
    navigate(QuoteNavKey(id))
}

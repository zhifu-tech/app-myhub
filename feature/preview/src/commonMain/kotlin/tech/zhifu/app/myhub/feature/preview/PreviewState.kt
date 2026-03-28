package tech.zhifu.app.myhub.feature.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import tech.zhifu.app.myhub.ui.model.ContentCard

@Composable
fun rememberPreviewState(): PreviewState {
    return remember { PreviewState() }
}

@Stable
class PreviewState internal constructor(
    initialCard: ContentCard? = null,
) {
    var card by mutableStateOf(initialCard)
        private set

    fun show(card: ContentCard) {
        if (this.card == card) return
        this.card = card
    }

    fun hide() {
        this.card = null
    }
}

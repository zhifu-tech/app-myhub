package tech.zhifu.app.myhub.feature.preview

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import tech.zhifu.app.myhub.ui.model.ContentCard

@Stable
class PreviewState {
    var pined by mutableStateOf(false)
    val card = mutableStateOf<ContentCard?>(null)

    fun show(card: ContentCard) {
        this.card.value = card
    }

    fun hide() {
        this.card.value = null
    }

    fun isPreviewing() = this.card.value != null
}

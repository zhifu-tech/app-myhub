package tech.zhifu.app.myhub.feature.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Composable
fun rememberPreviewState(): PreviewState {
    return remember { PreviewState() }
}

@Stable
class PreviewState internal constructor(
    initialVisible: Boolean = false,
    initialVisiblePayload: PreviewPayload? = null,
) {
    var visible by mutableStateOf(initialVisible)
        private set
    var payload by mutableStateOf(initialVisiblePayload)
        private set

    fun show(payload: PreviewPayload) {
        if (this.payload == payload && visible) return
        this.payload = payload
        this.visible = true
    }

    fun hide() {
        this.visible = false
    }

    companion object {
        const val SHARED_BOUNDS_DURATION_MS = 360
    }
}

@Stable
@Serializable
data class PreviewPayload(
    val contentId: String,
    val title: String? = null,
    val summary: String? = null,
    val coverUrl: String? = null,
    val isVideo: Boolean = false,
    val dateText: String? = null,
    val location: String? = null,
    val tags: List<String> = emptyList(),
    val body: String? = null,
)

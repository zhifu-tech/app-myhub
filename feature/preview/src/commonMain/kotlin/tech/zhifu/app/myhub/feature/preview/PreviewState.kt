package tech.zhifu.app.myhub.feature.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun rememberPreviewState(): PreviewState {
    return remember { PreviewState() }
}

@Stable
class PreviewState internal constructor(
    initialVisiblePayload: PreviewPayload? = null,
) {
    var payload by mutableStateOf(initialVisiblePayload)
        private set

    fun show(payload: PreviewPayload) {
        if (this.payload == payload) return
        this.payload = payload
    }

    fun hide() {
        this.payload = null
    }
}

@Stable
data class PreviewPayload(
    val id: String,
    val title: String?,
    val note: String?,
    //
    val coverIcon: ImageVector?,
    val coverBackground: Color,
    val coverTint: Color?,
    val coverUrl: String?,
    //
    val isVideo: Boolean,
    val location: String?,
    val tags: List<String>,
    //
    val actionLabel: String,
    val actionIcon: ImageVector,
    val actionColor: Color,
    //
    val updatedTimeMs: Long,
)

package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MessageBubbleItem(
    placeLeft: Boolean,
    backgroundColor: Color,
    content: @Composable (BoxScope.() -> Unit)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement =
            if (placeLeft) Arrangement.Start
            else Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .shadow(
                    elevation = 2.dp,
                    spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.05f),
                    ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.05f),
                )
                .background(
                    color = backgroundColor,
                    shape =
                        if (placeLeft) {
                            RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp,
                            )
                        } else {
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 0.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp,
                            )
                        }
                )
                .padding(all = 16.dp),
            content = content,
        )
    }
}

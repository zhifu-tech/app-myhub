package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                .background(
                    color = backgroundColor,
                    shape =
                        if (placeLeft) {
                            RoundedCornerShape(
                                topStart = 6.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp,
                            )
                        } else {
                            RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 6.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp,
                            )
                        }
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            content = content,
        )
    }
}

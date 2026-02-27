package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun CardSkeleton(
    modifier: Modifier = Modifier.Companion
) {
    Surface(
        shape = RoundedCornerShape(size = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        shadowElevation = 24.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        val shape = RoundedCornerShape(size = 8.dp)
        val skeletonColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
        Column(
            modifier = Modifier.padding(all = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(fraction = 0.4f)
                    .height(12.dp)
                    .clip(shape)
                    .background(skeletonColor)
            )
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(18.dp)
                    .clip(shape)
                    .background(skeletonColor)
            )
            Box(
                modifier = Modifier.fillMaxWidth(fraction = 0.92f)
                    .height(18.dp)
                    .clip(shape)
                    .background(skeletonColor)
            )
            Box(
                modifier = Modifier.fillMaxWidth(fraction = 0.65f)
                    .height(18.dp)
                    .clip(shape)
                    .background(skeletonColor)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(54.dp)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(skeletonColor)
            )
        }
    }
}

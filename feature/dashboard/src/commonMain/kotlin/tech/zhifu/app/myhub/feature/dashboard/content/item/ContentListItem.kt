package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.sharedElement
import tech.zhifu.app.myhub.ui.model.ContentCard
import tech.zhifu.app.myhub.ui.model.toImageVector

@Composable
fun ContentListItem(
    item: ContentCard,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContentItemLeading(
            item = item,
            modifier = Modifier
                .sharedElement(
                    key = "content-image-${item.id}",
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .size(64.dp)
                .clip(shape = MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier
                    .sharedElement(
                        key = "content-title-${item.id}",
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                text = item.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.action.let { action ->
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    action.iconKey.toImageVector()?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = action.color,
                            modifier = Modifier
                                .sharedElement(
                                    key = "content-action-icon-${item.id}",
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                                .size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = action.label,
                        color = action.color,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .sharedElement(
                                key = "content-action-label-${item.id}",
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                    )
                }
            }
        }
    }
}

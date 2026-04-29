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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.component.card.mixed.CardStatusAction
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.feature.preview.sharedElement

@Composable
fun ContentListItem(
    item: ContentCard,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onOpenMediaPreview: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ContentItemLeading(
            item = item,
            onOpenMediaPreview = onOpenMediaPreview,
            modifier = Modifier
                .sharedElement(
                    key = "content-image-${item.card.id}",
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .size(76.dp)
                .clip(shape = MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier
                    .sharedElement(
                        key = "content-title-${item.card.id}",
                        animatedVisibilityScope = animatedVisibilityScope,
                    ),
                text = item.card.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CardStatusAction(
                status = item.card.status,
                updatedAt = item.card.updatedAt,
                modifier = Modifier
                    .sharedElement(
                        key = "content-status-${item.card.id}",
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .padding(top = 4.dp),
            )
        }
    }
}

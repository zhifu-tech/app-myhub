package tech.zhifu.app.myhub.feature.dashboard.content.item

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.component.card.mixed.CardStatusAction
import tech.zhifu.app.myhub.datastore.model.domain.ContentCard
import tech.zhifu.app.myhub.feature.preview.sharedElement

@Composable
fun ContentGridItem(
    item: ContentCard,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onOpenMediaPreview: (Int) -> Unit,
) {
    ContentItemLeading(
        item = item,
        onOpenMediaPreview = onOpenMediaPreview,
        modifier = Modifier
            .sharedElement(
                key = "content-image-${item.card.id}",
                animatedVisibilityScope = animatedVisibilityScope,
            )
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
    )

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = item.card.title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        CardStatusAction(
            status = item.card.status,
            updatedAt = item.card.updatedAt,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

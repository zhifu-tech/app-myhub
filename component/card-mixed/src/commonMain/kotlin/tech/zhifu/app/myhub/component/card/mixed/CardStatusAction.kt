package tech.zhifu.app.myhub.component.card.mixed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.card.mixed.resources.Res
import tech.zhifu.app.myhub.component.card.mixed.resources.card_status_archived
import tech.zhifu.app.myhub.component.card.mixed.resources.card_status_draft_continue_inputting
import tech.zhifu.app.myhub.component.card.mixed.resources.card_status_published_review_now
import tech.zhifu.app.myhub.datastore.model.domain.CardStatus

@Composable
fun CardStatusAction(
    status: CardStatus,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
    textAlpha: Float = 1f,
) {
    val config = statusConfig(status)
    Surface(
        modifier = modifier,
        color = config.color.copy(alpha = 0.08f),
        contentColor = config.color,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                tint = config.color.copy(alpha = textAlpha),
                modifier = Modifier.size(iconSize),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(config.labelRes),
                color = config.color.copy(alpha = textAlpha),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun statusConfig(
    status: CardStatus
): CardStatusConfig = when (status) {
    CardStatus.DRAFT -> CardStatusConfig(
        labelRes = Res.string.card_status_draft_continue_inputting,
        icon = Icons.Outlined.Edit,
        color = Color(0xFF2563EB),
    )

    CardStatus.PUBLISHED -> CardStatusConfig(
        labelRes = Res.string.card_status_published_review_now,
        icon = Icons.Outlined.Visibility,
        color = Color(0xFF137FEC),
    )

    CardStatus.ARCHIVED -> CardStatusConfig(
        labelRes = Res.string.card_status_archived,
        icon = Icons.Outlined.EditNote,
        color = Color(0xFF64748B),
    )
}

@Immutable
private data class CardStatusConfig(
    val labelRes: StringResource,
    val icon: ImageVector,
    val color: Color,
)

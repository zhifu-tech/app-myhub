package tech.zhifu.app.myhub.feature.ai.content.item.action

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionCommand
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionPayload
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.text
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_check_copy
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_check_cover
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_check_meta
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_status
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_support_floating
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_support_pinned
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_title_floating
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_panel_preview_title_pinned

@Composable
fun ReviewActionPanel(
    component: ActionComponentSchema,
    onAction: (ActionCommand) -> Unit,
) {
    val payload = component.payload as? ActionPayload.Review ?: return
    val previewPinned = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    ActionSupportPanel(
        eyebrow = stringResource(Res.string.feature_ai_action_panel_preview_status),
        title = if (previewPinned) {
            stringResource(Res.string.feature_ai_action_panel_preview_title_pinned)
        } else {
            stringResource(Res.string.feature_ai_action_panel_preview_title_floating)
        },
        supporting = if (previewPinned) {
            stringResource(Res.string.feature_ai_action_panel_preview_support_pinned)
        } else {
            stringResource(Res.string.feature_ai_action_panel_preview_support_floating)
        },
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DraftMetaPill(text = stringResource(Res.string.feature_ai_action_panel_preview_check_cover))
            DraftMetaPill(text = stringResource(Res.string.feature_ai_action_panel_preview_check_copy))
            DraftMetaPill(text = stringResource(Res.string.feature_ai_action_panel_preview_check_meta))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            payload.fields.forEach { field ->
                Surface(
                    onClick = { onAction(field.action.command) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 11.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = field.action.label.text(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = field.action.label.text(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

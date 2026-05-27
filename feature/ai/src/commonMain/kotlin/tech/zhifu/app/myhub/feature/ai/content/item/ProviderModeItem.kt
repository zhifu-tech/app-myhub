package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_close
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_hidden_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_mode_direct_desc
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_mode_direct_label
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_mode_disabled_desc
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_mode_disabled_label
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_mode_server_desc
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_mode_server_label
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_provider_title
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.updateAIProviderMode
import tech.zhifu.app.myhub.ui.state.ai.updateAIProviderShortcutVisible
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ProviderModeItem(viewModel: AIViewModel) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { content ->
            ProviderModeItemState(
                mode = content.providerMode,
                shortcutVisible = content.shortcutVisible
            )
        }
    }
    val safState = state ?: return
    var showHiddenHint by remember { mutableStateOf(false) }

    ProviderModeItemContent(
        onProviderModeChange = viewModel::updateAIProviderMode,
        onClose = {
            showHiddenHint = true
            viewModel.updateAIProviderShortcutVisible(false)
        },
        providerMode = safState.mode,
        shortcutVisible = safState.shortcutVisible,
        showHiddenHint = showHiddenHint,
    )
}

@Composable
fun ProviderModeItemContent(
    onProviderModeChange: (ProviderMode) -> Unit,
    onClose: () -> Unit,
    providerMode: ProviderMode?,
    shortcutVisible: Boolean,
    showHiddenHint: Boolean,
) {
    if (providerMode == null) return
    if (!shortcutVisible) {
        if (!showHiddenHint) return
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
            ),
        ) {
            Text(
                text = stringResource(Res.string.feature_ai_provider_hidden_hint),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
        ),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.feature_ai_provider_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = providerMode.description(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(Res.string.feature_ai_provider_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(Res.string.feature_ai_provider_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProviderMode.entries.forEach { mode ->
                    val selected = mode == providerMode
                    Surface(
                        onClick = { onProviderModeChange(mode) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
                            },
                        ),
                    ) {
                        Text(
                            text = mode.label(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

private data class ProviderModeItemState(
    val mode: ProviderMode,
    val shortcutVisible: Boolean,
)

@Composable
private fun ProviderMode.label(): String = when (this) {
    ProviderMode.DISABLED -> stringResource(Res.string.feature_ai_provider_mode_disabled_label)
    ProviderMode.SERVER_GATEWAY -> stringResource(Res.string.feature_ai_provider_mode_server_label)
    ProviderMode.DIRECT_API -> stringResource(Res.string.feature_ai_provider_mode_direct_label)
}

@Composable
private fun ProviderMode.description(): String = when (this) {
    ProviderMode.DISABLED -> stringResource(Res.string.feature_ai_provider_mode_disabled_desc)
    ProviderMode.SERVER_GATEWAY -> stringResource(Res.string.feature_ai_provider_mode_server_desc)
    ProviderMode.DIRECT_API -> stringResource(Res.string.feature_ai_provider_mode_direct_desc)
}

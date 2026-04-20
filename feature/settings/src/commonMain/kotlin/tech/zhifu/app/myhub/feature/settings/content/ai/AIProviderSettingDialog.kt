package tech.zhifu.app.myhub.feature.settings.content.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_config
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_api_key
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_endpoint
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_hint
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_model
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_title
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode_hint
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_retries
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_runtime_hint
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_runtime_title
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_save
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_shortcut_hint
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_shortcut_title
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_timeout_ms
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_close
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_off
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_on
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig

@Composable
fun AIProviderSettingDialog(
    providerRoutingConfig: ProviderRoutingConfig,
    timeoutInput: String,
    maxRetriesInput: String,
    validationMessageRes: StringResource?,
    visible: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onModeChanged: (ProviderMode) -> Unit,
    onEndpointChanged: (String) -> Unit,
    onModelChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onTimeoutChanged: (String) -> Unit,
    onRetriesChanged: (String) -> Unit,

    providerShortcutVisible: Boolean,
    onProviderShortcutVisibleChanged: (Boolean) -> Unit,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.feature_settings_ai_config)) },
        text = {
            val scrollState = rememberScrollState()
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.verticalScroll(scrollState)
            ) {
                SettingsSectionCard {
                    Text(
                        text = stringResource(Res.string.feature_settings_ai_shortcut_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(Res.string.feature_settings_ai_shortcut_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                if (providerShortcutVisible) {
                                    Res.string.feature_settings_on
                                } else {
                                    Res.string.feature_settings_off
                                }
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Switch(
                            checked = providerShortcutVisible,
                            onCheckedChange = onProviderShortcutVisibleChanged,
                        )
                    }
                }

                SettingsSectionCard {
                    Text(
                        text = stringResource(Res.string.feature_settings_ai_mode),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(Res.string.feature_settings_ai_mode_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ProviderMode.entries.forEach { mode ->
                            val isSelected = mode == providerRoutingConfig.mode
                            FilterChip(
                                onClick = { onModeChanged(mode) },
                                selected = isSelected,
                                label = {
                                    Text(stringResource(mode.labelToken()))
                                },
                            )
                        }
                    }

                    if (providerRoutingConfig.mode == ProviderMode.DIRECT_API) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        )
                        Text(
                            text = stringResource(Res.string.feature_settings_ai_direct_title),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = stringResource(Res.string.feature_settings_ai_direct_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = providerRoutingConfig.directEndpoint,
                            onValueChange = onEndpointChanged,
                            singleLine = true,
                            label = { Text(stringResource(Res.string.feature_settings_ai_direct_endpoint)) },
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = providerRoutingConfig.directModel,
                            onValueChange = onModelChanged,
                            singleLine = true,
                            label = { Text(stringResource(Res.string.feature_settings_ai_direct_model)) },
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = providerRoutingConfig.directApiKey,
                            onValueChange = onApiKeyChanged,
                            singleLine = true,
                            label = { Text(stringResource(Res.string.feature_settings_ai_direct_api_key)) },
                        )
                    }

                }

                SettingsSectionCard {
                    Text(
                        text = stringResource(Res.string.feature_settings_ai_runtime_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(Res.string.feature_settings_ai_runtime_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = timeoutInput,
                        onValueChange = onTimeoutChanged,
                        singleLine = true,
                        label = { Text(stringResource(Res.string.feature_settings_ai_timeout_ms)) },
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = maxRetriesInput,
                        onValueChange = onRetriesChanged,
                        singleLine = true,
                        label = { Text(stringResource(Res.string.feature_settings_ai_retries)) },
                    )
                }

                if (validationMessageRes != null) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    ) {
                        Text(
                            text = stringResource(validationMessageRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(text = stringResource(Res.string.feature_settings_ai_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.feature_settings_close))
            }
        }
    )
}

@Composable
private fun SettingsSectionCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

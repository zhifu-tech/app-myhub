package tech.zhifu.app.myhub.feature.settings.content.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_model
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_retries
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_save
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_timeout_ms
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_close
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
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.feature_settings_ai_config)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.feature_settings_ai_mode),
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ProviderMode.entries.forEach { mode ->
                        val isSelected = mode == providerRoutingConfig.mode
                        AssistChip(
                            onClick = { onModeChanged(mode) },
                            label = {
                                val prefix = if (isSelected) "✓ " else ""
                                Text("$prefix${stringResource(mode.labelToken())}")
                            },
                        )
                    }
                }

                if (providerRoutingConfig.mode == ProviderMode.DIRECT_API) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = providerRoutingConfig.directEndpoint,
                        onValueChange = onEndpointChanged,
                        label = { Text(stringResource(Res.string.feature_settings_ai_direct_endpoint)) },
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = providerRoutingConfig.directModel,
                        onValueChange = onModelChanged,
                        label = { Text(stringResource(Res.string.feature_settings_ai_direct_model)) },
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = providerRoutingConfig.directApiKey,
                        onValueChange = onApiKeyChanged,
                        label = { Text(stringResource(Res.string.feature_settings_ai_direct_api_key)) },
                    )
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = timeoutInput,
                    onValueChange = onTimeoutChanged,
                    label = { Text(stringResource(Res.string.feature_settings_ai_timeout_ms)) },
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = maxRetriesInput,
                    onValueChange = onRetriesChanged,
                    label = { Text(stringResource(Res.string.feature_settings_ai_retries)) },
                )

                if (validationMessageRes != null) {
                    Text(
                        text = stringResource(validationMessageRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
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

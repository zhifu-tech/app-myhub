//package tech.zhifu.app.myhub.feature.settings.content.ai
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material3.AssistChip
//import androidx.compose.material3.Button
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.ListItemDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import org.jetbrains.compose.resources.stringResource
//import tech.zhifu.app.myhub.feature.settings.SettingsUiState
//import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
//import tech.zhifu.app.myhub.feature.settings.resources.Res
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_config
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_api_key
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_endpoint
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_direct_model
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_mode
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_retries
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_save
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_timeout_ms
//
//@Composable
//fun AiProviderSettingItemRoute(
//    viewModel: SettingsViewModel,
//) {
//    val state = viewModel.collectFieldAsState { uiState ->
//        (uiState as? SettingsUiState.Content)?.aiProviderSettingState
//    }.value ?: return
//
//    AiProviderSettingItem(
//        state = state,
//        onModeChanged = viewModel::updateAiMode,
//        onEndpointChanged = viewModel::updateAiDirectEndpoint,
//        onModelChanged = viewModel::updateAiDirectModel,
//        onApiKeyChanged = viewModel::updateAiDirectApiKey,
//        onTimeoutChanged = viewModel::updateAiTimeoutMs,
//        onRetriesChanged = viewModel::updateAiMaxRetries,
//        onSave = viewModel::saveAiProviderSettings,
//    )
//}
//
//@Composable
//private fun AiProviderSettingItem(
//    state: AiProviderSettingState,
//    onModeChanged: (String) -> Unit,
//    onEndpointChanged: (String) -> Unit,
//    onModelChanged: (String) -> Unit,
//    onApiKeyChanged: (String) -> Unit,
//    onTimeoutChanged: (String) -> Unit,
//    onRetriesChanged: (String) -> Unit,
//    onSave: () -> Unit,
//) {
//    Surface(
//        shape = MaterialTheme.shapes.medium,
//        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//    ) {
//        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            ListItem(
//                headlineContent = { Text(stringResource(Res.string.feature_settings_ai_config)) },
//                supportingContent = { Text(stringResource(Res.string.feature_settings_ai_mode)) },
//                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
//            )
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//            ) {
//                listOf("DISABLED", "SERVER_GATEWAY", "DIRECT_API").forEach { mode ->
//                    AssistChip(
//                        onClick = { onModeChanged(mode) },
//                        label = {
//                            val active = if (state.mode.uppercase() == mode) "✓" else ""
//                            Text("$active$mode")
//                        },
//                    )
//                }
//            }
//
//            OutlinedTextField(
//                modifier = Modifier.fillMaxWidth(),
//                value = state.directEndpoint,
//                onValueChange = onEndpointChanged,
//                label = { Text(stringResource(Res.string.feature_settings_ai_direct_endpoint)) },
//                enabled = !state.isSubmitting,
//            )
//            OutlinedTextField(
//                modifier = Modifier.fillMaxWidth(),
//                value = state.directModel,
//                onValueChange = onModelChanged,
//                label = { Text(stringResource(Res.string.feature_settings_ai_direct_model)) },
//                enabled = !state.isSubmitting,
//            )
//            OutlinedTextField(
//                modifier = Modifier.fillMaxWidth(),
//                value = state.directApiKey,
//                onValueChange = onApiKeyChanged,
//                label = { Text(stringResource(Res.string.feature_settings_ai_direct_api_key)) },
//                enabled = !state.isSubmitting,
//            )
//            OutlinedTextField(
//                modifier = Modifier.fillMaxWidth(),
//                value = state.timeoutMs,
//                onValueChange = onTimeoutChanged,
//                label = { Text(stringResource(Res.string.feature_settings_ai_timeout_ms)) },
//                enabled = !state.isSubmitting,
//            )
//            OutlinedTextField(
//                modifier = Modifier.fillMaxWidth(),
//                value = state.maxRetries,
//                onValueChange = onRetriesChanged,
//                label = { Text(stringResource(Res.string.feature_settings_ai_retries)) },
//                enabled = !state.isSubmitting,
//            )
//            if (state.validationMessage.isNotBlank()) {
//                Text(
//                    text = state.validationMessage,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.error,
//                )
//            }
//            Button(
//                onClick = onSave,
//                enabled = !state.isSubmitting,
//            ) {
//                Text(stringResource(Res.string.feature_settings_ai_save))
//            }
//        }
//    }
//}

package tech.zhifu.app.myhub.feature.settings.content.ai

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_config
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_direct_api_key_required
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_direct_endpoint_required
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_direct_model_required
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_max_retries_invalid
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_timeout_invalid
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.collectAIProviderState
import tech.zhifu.app.myhub.ui.state.ai.updateAIProvider

@Composable
fun AiProviderSettingItem(
    viewModel: SettingsViewModel,
) {
    val provider by viewModel.collectAIProviderState()
    val showDialog = remember { mutableStateOf(false) }
    var editProvider by remember(provider, showDialog.value) { mutableStateOf(provider.copy()) }
    var timeoutInput by remember(provider, showDialog.value) { mutableStateOf(provider.timeoutMs.toString()) }
    var maxRetriesInput by remember(provider, showDialog.value) { mutableStateOf(provider.maxRetries.toString()) }
    var validationMessageRes by remember(provider, showDialog.value) { mutableStateOf<StringResource?>(null) }

    AiProviderSettingItemContent(
        providerRoutingConfig = provider,
        onClick = {
            editProvider = provider.copy()
            timeoutInput = provider.timeoutMs.toString()
            maxRetriesInput = provider.maxRetries.toString()
            validationMessageRes = null
            showDialog.value = true
        }
    )

    AiProviderSettingDialog(
        providerRoutingConfig = editProvider,
        timeoutInput = timeoutInput,
        maxRetriesInput = maxRetriesInput,
        validationMessageRes = validationMessageRes,
        visible = showDialog.value,
        onDismiss = {
            showDialog.value = false
        },
        onSave = {
            val validation = validateAiProviderState(
                providerRoutingConfig = editProvider,
                timeoutInput = timeoutInput,
                maxRetriesInput = maxRetriesInput,
            )
            if (validation != null) {
                validationMessageRes = validation
                return@AiProviderSettingDialog
            }
            val timeout = timeoutInput.toLongOrNull() ?: 15_000L
            val retries = maxRetriesInput.toIntOrNull() ?: 1
            viewModel.updateAIProvider(
                editProvider.copy(
                    timeoutMs = timeout,
                    maxRetries = retries,
                )
            )
            showDialog.value = false
        },
        onModeChanged = { mode ->
            editProvider = editProvider.copy(mode = mode)
            validationMessageRes = null
        },
        onEndpointChanged = { value ->
            editProvider = editProvider.copy(directEndpoint = value)
            validationMessageRes = null
        },
        onModelChanged = { value ->
            editProvider = editProvider.copy(directModel = value)
            validationMessageRes = null
        },
        onApiKeyChanged = { value ->
            editProvider = editProvider.copy(directApiKey = value)
            validationMessageRes = null
        },
        onTimeoutChanged = { value ->
            timeoutInput = value
            validationMessageRes = null
        },
        onRetriesChanged = { value ->
            maxRetriesInput = value
            validationMessageRes = null
        },
    )
}

@Composable
private fun AiProviderSettingItemContent(
    providerRoutingConfig: ProviderRoutingConfig,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        ListItem(
            headlineContent = {
                Text(text = stringResource(Res.string.feature_settings_ai_config))
            },
            supportingContent = {
                Text(text = stringResource(providerRoutingConfig.mode.labelToken()))
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

private fun validateAiProviderState(
    providerRoutingConfig: ProviderRoutingConfig,
    timeoutInput: String,
    maxRetriesInput: String,
): StringResource? {
    val timeout = timeoutInput.toLongOrNull()
    if (timeout == null || timeout <= 0L) {
        return Res.string.feature_settings_ai_validation_timeout_invalid
    }

    val retries = maxRetriesInput.toIntOrNull()
    if (retries == null || retries < 0) {
        return Res.string.feature_settings_ai_validation_max_retries_invalid
    }

    if (providerRoutingConfig.mode == ProviderMode.DIRECT_API) {
        if (providerRoutingConfig.directEndpoint.isBlank()) {
            return Res.string.feature_settings_ai_validation_direct_endpoint_required
        }
        if (providerRoutingConfig.directModel.isBlank()) {
            return Res.string.feature_settings_ai_validation_direct_model_required
        }
        if (providerRoutingConfig.directApiKey.isBlank()) {
            return Res.string.feature_settings_ai_validation_direct_api_key_required
        }
    }

    return null
}

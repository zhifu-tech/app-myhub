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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_config
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_direct_api_key_required
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_direct_endpoint_required
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_direct_image_model_required
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_direct_model_required
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_direct_vision_model_required
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_max_retries_invalid
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai_validation_timeout_invalid
import tech.zhifu.app.myhub.ui.state.ai.ProviderMode
import tech.zhifu.app.myhub.ui.state.ai.ProviderRoutingConfig
import tech.zhifu.app.myhub.ui.state.ai.updateAIProvider

@Composable
fun AIProviderSettingItem(
    viewModel: SettingsViewModel,
) {
    val showDialog = remember { mutableStateOf(false) }
    val providerRoutingConfig by viewModel.providerRoutingConfig.collectAsStateWithLifecycle()
    var editProvider by remember(
        providerRoutingConfig,
        showDialog.value
    ) {
        mutableStateOf(providerRoutingConfig.copy())
    }
    val timeoutInput = remember(
        providerRoutingConfig,
        showDialog.value
    ) {
        mutableStateOf(providerRoutingConfig.timeoutMs.toString())
    }
    var maxRetriesInput by remember(
        providerRoutingConfig,
        showDialog.value
    ) {
        mutableStateOf(providerRoutingConfig.maxRetries.toString())
    }
    val validationMessageRes = remember(
        providerRoutingConfig,
        showDialog.value
    ) {
        mutableStateOf<StringResource?>(null)
    }
    var providerShortcutVisible by remember(
        providerRoutingConfig,
        showDialog.value
    ) {
        mutableStateOf(providerRoutingConfig.shortcutVisible)
    }

    AiProviderSettingItemContent(
        providerRoutingConfig = providerRoutingConfig,
        onClick = {
            editProvider = providerRoutingConfig.copy()
            timeoutInput.value = providerRoutingConfig.timeoutMs.toString()
            maxRetriesInput = providerRoutingConfig.maxRetries.toString()
            validationMessageRes.value = null
            showDialog.value = true
        }
    )

    AIProviderSettingDialog(
        providerRoutingConfig = editProvider,
        timeoutInput = timeoutInput.value,
        maxRetriesInput = maxRetriesInput,
        validationMessageRes = validationMessageRes.value,
        visible = showDialog.value,
        onDismiss = {
            showDialog.value = false
        },
        onSave = {
            val validation = validateAiProviderState(
                providerRoutingConfig = editProvider,
                timeoutInput = timeoutInput.value,
                maxRetriesInput = maxRetriesInput,
            )
            if (validation != null) {
                validationMessageRes.value = validation
                return@AIProviderSettingDialog
            }
            val timeout = timeoutInput.value.toLongOrNull() ?: 15_000L
            val retries = maxRetriesInput.toIntOrNull() ?: 1
            viewModel.updateAIProvider(
                editProvider.copy(
                    shortcutVisible = providerShortcutVisible,
                    timeoutMs = timeout,
                    maxRetries = retries,
                )
            )
            showDialog.value = false
        },
        onModeChanged = { mode ->
            editProvider = editProvider.copy(mode = mode)
            validationMessageRes.value = null
        },
        onEndpointChanged = { value ->
            editProvider = editProvider.copy(directEndpoint = value)
            validationMessageRes.value = null
        },
        onModelChanged = { value ->
            editProvider = editProvider.copy(directModel = value)
            validationMessageRes.value = null
        },
        onVisionModelChanged = { value ->
            editProvider = editProvider.copy(directVisionModel = value)
            validationMessageRes.value = null
        },
        onImageModelChanged = { value ->
            editProvider = editProvider.copy(directImageModel = value)
            validationMessageRes.value = null
        },
        onApiKeyChanged = { value ->
            editProvider = editProvider.copy(directApiKey = value)
            validationMessageRes.value = null
        },
        onTimeoutChanged = { value ->
            timeoutInput.value = value
            validationMessageRes.value = null
        },
        onRetriesChanged = { value ->
            maxRetriesInput = value
            validationMessageRes.value = null
        },
        providerShortcutVisible = providerShortcutVisible,
        onProviderShortcutVisibleChanged = { visible ->
            providerShortcutVisible = visible
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
        if (providerRoutingConfig.directVisionModel.isBlank()) {
            return Res.string.feature_settings_ai_validation_direct_vision_model_required
        }
        if (providerRoutingConfig.directImageModel.isBlank()) {
            return Res.string.feature_settings_ai_validation_direct_image_model_required
        }
        if (requiresDirectApiKey(providerRoutingConfig.directEndpoint) && providerRoutingConfig.directApiKey.isBlank()) {
            return Res.string.feature_settings_ai_validation_direct_api_key_required
        }
    }

    return null
}

private fun requiresDirectApiKey(
    endpoint: String,
): Boolean {
    val normalized = endpoint.trim().lowercase()
    return normalized.isNotBlank() &&
        !normalized.contains("localhost") &&
        !normalized.contains("127.0.0.1") &&
        !normalized.contains(":11434") &&
        !normalized.contains("ollama")
}

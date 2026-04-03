package tech.zhifu.app.myhub.feature.settings.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.content.ai.AiProviderSettingItem
import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSettingItem
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSettingItem
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_ai
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_appearance_language
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_license
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_logout
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_more
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_support
import tech.zhifu.app.myhub.feature.settings.viewmodel.navigateToOpenSourceLicenses
import tech.zhifu.app.myhub.feature.settings.viewmodel.navigateToSupport
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

@Composable
fun Content(
    viewModel: SettingsViewModel,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.feature_settings_appearance_language),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        ThemeSettingItem(viewModel = viewModel)
        LanguageSettingItem(viewModel = viewModel)
        Text(
            text = stringResource(Res.string.feature_settings_ai),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        AiProviderSettingItem(viewModel = viewModel)
        Text(
            text = stringResource(Res.string.feature_settings_more),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        ActionSettingItem(
            title = stringResource(Res.string.feature_settings_license),
            onClick = viewModel::navigateToOpenSourceLicenses,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null
                )
            }
        )
        ActionSettingItem(
            title = stringResource(Res.string.feature_settings_support),
            onClick = viewModel::navigateToSupport,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ContactSupport,
                    contentDescription = null
                )
            }
        )
        ActionSettingItem(
            title = stringResource(Res.string.feature_settings_logout),
            onClick = {
                logger.debug { "Settings logout click" }
            },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
private fun ActionSettingItem(
    title: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        ListItem(
            headlineContent = { Text(text = title) },
            leadingContent = icon,
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

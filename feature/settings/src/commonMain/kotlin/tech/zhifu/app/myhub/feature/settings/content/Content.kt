package tech.zhifu.app.myhub.feature.settings.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    }
}

package tech.zhifu.app.myhub.feature.settings.content.language

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_close
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_select_language
import tech.zhifu.app.myhub.ui.state.language.Language

@Composable
internal fun LanguageSelectionDialog(
    language: Language,
    onSelected: (Language) -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(Res.string.feature_settings_select_language))
        },
        text = {
            Column {
                Language.entries.forEach { entry ->
                    LanguageItem(
                        entry = entry,
                        language = language,
                        onClick = { onSelected(entry) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(Res.string.feature_settings_close))
            }
        }
    )
}

@Composable
private fun LanguageItem(
    entry: Language,
    language: Language,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = (entry == language),
            onClick = null,
        )
        Text(
            text = stringResource(entry.labelToken()),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

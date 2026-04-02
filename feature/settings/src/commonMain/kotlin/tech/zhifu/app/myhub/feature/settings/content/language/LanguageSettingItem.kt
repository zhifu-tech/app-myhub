//package tech.zhifu.app.myhub.feature.settings.content.language
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Language
//import androidx.compose.material3.Icon
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.ListItemDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.graphics.Color
//import org.jetbrains.compose.resources.stringResource
//import tech.zhifu.app.myhub.feature.settings.SettingsUiState
//import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
//import tech.zhifu.app.myhub.feature.settings.resources.Res
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_display_language
//
//@Composable
//fun LanguageSettingItemRoute(
//    viewModel: SettingsViewModel,
//) {
//    val state = viewModel.collectFieldAsState { uiState ->
//        (uiState as? SettingsUiState.Content)?.languageSettingState
//    }.value ?: return
//
//    LanguageSettingItem(
////        language = state.language,
//        enabled = state.isSubmitting.not(),
//        onLanguageClick = viewModel::showLanguageDialog
//    )
//
//    if (state.showLanguageDialog) {
//        LanguageSelectionDialog(
//            language = state.language,
//            isSubmitting = state.isSubmitting,
//            onSelected = viewModel::update,
//            onDismiss = viewModel::hideLanguageDialog
//        )
//    }
//}
//
//@Composable
//internal fun LanguageSettingItem(
////    language: Language,
//    enabled: Boolean = true,
//    onLanguageClick: () -> Unit
//) {
//    Surface(
//        onClick = onLanguageClick,
//        enabled = enabled,
//        shape = MaterialTheme.shapes.medium,
//        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//    ) {
//        ListItem(
//            headlineContent = {
//                Text(
//                    stringResource(Res.string.feature_settings_display_language)
//                )
//            },
//            supportingContent = {
//                // FIXME:
////                Text(
////                    language.getLocalizedLabel()
////                )
//            },
//            leadingContent = {
//                Icon(
//                    imageVector = Icons.Default.Language,
//                    contentDescription = null
//                )
//            },
//            colors = ListItemDefaults.colors(
//                containerColor = Color.Transparent
//            )
//        )
//    }
//}

//package tech.zhifu.app.myhub.feature.settings.content.language
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.RadioButton
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import org.jetbrains.compose.resources.stringResource
//import tech.zhifu.app.myhub.feature.settings.resources.Res
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_close
//import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_select_language
//import tech.zhifu.app.myhub.language.Language
//import tech.zhifu.app.myhub.language.getLocalizedLabel
//
//@Composable
//internal fun LanguageSelectionDialog(
////    language: Language,
//    isSubmitting: Boolean,
////    onSelected: (Language) -> Unit,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(stringResource(Res.string.feature_settings_select_language)) },
//        text = {
//            Column {
//                Language.entries.forEach { entry ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        RadioButton(
//                            selected = (entry == language),
//                            enabled = !isSubmitting,
//                            onClick = {
//                                if (!isSubmitting) {
//                                    onSelected(entry)
//                                }
//                            }
//                        )
//                        Text(
//                            text = entry.getLocalizedLabel(),
//                            modifier = Modifier.padding(start = 16.dp)
//                        )
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(
//                enabled = !isSubmitting,
//                onClick = onDismiss
//            ) {
//                Text(stringResource(Res.string.feature_settings_close))
//            }
//        }
//    )
//}

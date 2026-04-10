package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionSchema
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_input_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_input_title_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_new_capture
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_publish
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_skip_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_skip_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_draft_actions
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_next_step
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_recommended_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_upload_media
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ActionSectionItem(
    viewModel: AIViewModel
) {
    val components by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.actionComponents.orEmpty()
    }
    ActionSectionItemContent(
        components = components,
        onAction = viewModel::performQuickAction,
    )
}

@Composable
fun ActionSectionItemContent(
    components: List<ActionComponentSchema>,
    onAction: (String) -> Unit,
) {
    if (components.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        components.forEach { component ->
            when (component.type) {
                ActionComponentType.QUICK_REPLY -> ActionCard(
                    title = when (component.field) {
                        Field.MEDIA -> stringResource(Res.string.feature_ai_action_title_media)
                        Field.TAGS -> stringResource(Res.string.feature_ai_action_title_tags)
                        else -> stringResource(Res.string.feature_ai_action_title_next_step)
                    },
                    options = component.options,
                    onAction = onAction,
                    primaryActions = setOf("review"),
                )

                ActionComponentType.TAG_SELECTOR -> ActionCard(
                    title = stringResource(Res.string.feature_ai_action_title_recommended_tags),
                    options = component.options,
                    onAction = onAction,
                    chipOnly = true,
                )

                ActionComponentType.UPLOAD -> ActionCard(
                    title = stringResource(Res.string.feature_ai_action_title_media),
                    options = component.options,
                    onAction = onAction,
                    primaryActions = setOf("upload_media"),
                )

                ActionComponentType.CARD_ACTIONS -> ActionCard(
                    title = stringResource(Res.string.feature_ai_action_title_draft_actions),
                    options = component.options,
                    onAction = onAction,
                    primaryActions = setOf("publish"),
                )

                ActionComponentType.INPUT -> InputHintCard(
                    hint = component.options.firstOrNull()
                        ?.let { option -> displayLabel(option) }
                        ?.ifBlank {
                            stringResource(Res.string.feature_ai_action_input_hint)
                        }
                        ?: stringResource(Res.string.feature_ai_action_input_hint)
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    options: List<ActionOptionSchema>,
    onAction: (String) -> Unit,
    chipOnly: Boolean = false,
    primaryActions: Set<String> = emptySet(),
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                options.forEach { option ->
                    if (chipOnly) {
                        AssistChip(
                            onClick = { onAction(option.value) },
                            label = { Text(displayLabel(option)) },
                        )
                    } else if (option.value in primaryActions) {
                        Button(
                            onClick = { onAction(option.value) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(displayLabel(option))
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onAction(option.value) },
                        ) {
                            Text(displayLabel(option))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputHintCard(
    hint: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            text = hint,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun displayLabel(option: ActionOptionSchema): String = when (option.value) {
    "upload_media" -> stringResource(Res.string.feature_ai_action_upload_media)
    "skip_media" -> stringResource(Res.string.feature_ai_action_skip_media)
    "skip_tags" -> stringResource(Res.string.feature_ai_action_skip_tags)
    "review" -> stringResource(Res.string.feature_ai_action_review)
    "edit_title" -> stringResource(Res.string.feature_ai_action_edit_title)
    "publish" -> stringResource(Res.string.feature_ai_action_publish)
    "new_capture" -> stringResource(Res.string.feature_ai_action_new_capture)
    "input_title_hint" -> stringResource(Res.string.feature_ai_action_input_title_hint)
    else -> option.label
}

package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
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
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentStatus
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionComponentType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionSchema
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionType
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_clear_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_delete_card
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_edit_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_input_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_input_title_hint
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_new_capture
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_publish
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_remove_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_replace_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_save_draft
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_skip_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_skip_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_draft_actions
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_next_step
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_recommended_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_title_type
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_upload_media
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ActionSectionItem(
    viewModel: AIViewModel
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { content ->
            ActionSectionState(
                components = content.context.actionComponents,
                selectedTags = content.context.draft.tags,
            )
        }
    }
    val safeState = state ?: return
    ActionSectionItemContent(
        components = safeState.components,
        selectedTags = safeState.selectedTags,
        onAction = viewModel::performQuickAction,
    )
}

@Composable
fun ActionSectionItemContent(
    components: List<ActionComponentSchema>,
    selectedTags: List<String>,
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
//                        Field.MEDIA -> stringResource(Res.string.feature_ai_action_title_media)
//                        Field.TAGS -> stringResource(Res.string.feature_ai_action_title_tags)
                        else -> stringResource(Res.string.feature_ai_action_title_next_step)
                    },
                    options = component.options,
                    selectedTags = selectedTags,
                    onAction = onAction,
                    primaryActions = setOf(ActionOptionType.REVIEW),
                )

                ActionComponentType.TAG_SELECTOR -> ActionCard(
                    title = if (component.options.all { it.type == ActionOptionType.REMOVE_TAG }) {
                        stringResource(Res.string.feature_ai_action_title_tags)
                    } else {
                        stringResource(Res.string.feature_ai_action_title_recommended_tags)
                    },
                    options = component.options,
                    selectedTags = selectedTags,
                    onAction = onAction,
                    chipOnly = true,
                )

                ActionComponentType.UPLOAD -> ActionCard(
                    title = stringResource(Res.string.feature_ai_action_title_media),
                    options = component.options,
                    selectedTags = selectedTags,
                    onAction = onAction,
                    primaryActions = setOf(ActionOptionType.UPLOAD_MEDIA),
                )

                ActionComponentType.CARD_ACTIONS -> ActionCard(
                    title = stringResource(Res.string.feature_ai_action_title_draft_actions),
                    options = component.options,
                    selectedTags = selectedTags,
                    onAction = onAction,
                    primaryActions = setOf(ActionOptionType.PUBLISH),
                    status = component.status,
                )

                ActionComponentType.INPUT -> InputHintCard(
                    hint = component.options.firstOrNull()
                        ?.let { option -> displayLabel(option) }
                        ?.ifBlank {
                            stringResource(Res.string.feature_ai_action_input_hint)
                        }
                        ?: stringResource(Res.string.feature_ai_action_input_hint)
                )

                ActionComponentType.OPTION_GRID -> ActionCard(
                    title = stringResource(Res.string.feature_ai_action_title_type),
                    options = component.options,
                    selectedTags = selectedTags,
                    onAction = onAction,
                    chipOnly = true,
                    status = component.status,
                )

                ActionComponentType.LOCATION_PICKER -> ActionCard(
                    title = stringResource(Res.string.feature_ai_action_title_location),
                    options = component.options,
                    selectedTags = selectedTags,
                    onAction = onAction,
                    chipOnly = true,
                    status = component.status,
                )

                ActionComponentType.CARD_PREVIEW -> InputHintCard(
                    hint = "Card preview",
                )

                ActionComponentType.FIELD_EDITOR -> ActionCard(
                    title = "Field editor",
                    options = component.options,
                    selectedTags = selectedTags,
                    onAction = onAction,
                    status = component.status,
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    options: List<ActionOptionSchema>,
    selectedTags: List<String>,
    onAction: (String) -> Unit,
    chipOnly: Boolean = false,
    primaryActions: Set<ActionOptionType> = emptySet(),
    status: ActionComponentStatus = ActionComponentStatus.ACTIVE,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (status == ActionComponentStatus.COMPLETED) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
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
                        when (option.type) {
                            ActionOptionType.REMOVE_TAG -> {
                                InputChip(
                                    selected = true,
                                    onClick = { onAction(option.value) },
                                    label = { Text(displayLabel(option)) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = null,
                                        )
                                    },
                                )
                            }

                            ActionOptionType.TAG -> {
                                val selected = option.label in selectedTags
                                AssistChip(
                                    onClick = {
                                        if (selected) {
                                            onAction(ActionOptionType.encodeTagRemove(option.label))
                                        } else {
                                            onAction(option.value)
                                        }
                                    },
                                    label = { Text(displayLabel(option)) },
                                )
                            }

                            else -> {
                                if (option.selected) {
                                    InputChip(
                                        selected = true,
                                        onClick = { onAction(option.value) },
                                        label = { Text(displayLabel(option)) },
                                    )
                                } else {
                                    AssistChip(
                                        onClick = { onAction(option.value) },
                                        label = { Text(displayLabel(option)) },
                                    )
                                }
                            }
                        }
                    } else if (option.type in primaryActions || option.selected) {
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
private fun displayLabel(option: ActionOptionSchema): String = when (option.type) {
    ActionOptionType.UPLOAD_MEDIA -> stringResource(Res.string.feature_ai_action_upload_media)
    ActionOptionType.REPLACE_MEDIA -> stringResource(Res.string.feature_ai_action_replace_media)
    ActionOptionType.REMOVE_MEDIA -> stringResource(Res.string.feature_ai_action_remove_media)
    ActionOptionType.SKIP_MEDIA -> stringResource(Res.string.feature_ai_action_skip_media)
    ActionOptionType.SKIP_TAGS -> stringResource(Res.string.feature_ai_action_skip_tags)
    ActionOptionType.REVIEW -> stringResource(Res.string.feature_ai_action_review)
    ActionOptionType.EDIT_MEDIA -> stringResource(Res.string.feature_ai_action_edit_media)
    ActionOptionType.EDIT_LOCATION -> stringResource(Res.string.feature_ai_action_edit_location)
    ActionOptionType.EDIT_TITLE -> stringResource(Res.string.feature_ai_action_edit_title)
    ActionOptionType.EDIT_TAGS -> stringResource(Res.string.feature_ai_action_edit_tags)
    ActionOptionType.EDIT_SUMMARY -> stringResource(Res.string.feature_ai_action_edit_summary)
    ActionOptionType.PUBLISH -> stringResource(Res.string.feature_ai_action_publish)
    ActionOptionType.SAVE_DRAFT -> stringResource(Res.string.feature_ai_action_save_draft)
    ActionOptionType.DELETE_CARD -> stringResource(Res.string.feature_ai_action_delete_card)
    ActionOptionType.NEW_CAPTURE -> stringResource(Res.string.feature_ai_action_new_capture)
    ActionOptionType.INPUT_TITLE_HINT -> stringResource(Res.string.feature_ai_action_input_title_hint)
    ActionOptionType.CLEAR_LOCATION -> stringResource(Res.string.feature_ai_action_clear_location)
    ActionOptionType.REMOVE_TAG -> option.label
    else -> option.label
}

private data class ActionSectionState(
    val components: List<ActionComponentSchema>,
    val selectedTags: List<String>,
)

package tech.zhifu.app.myhub.feature.ai.content.input

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionCommand
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.Field
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_complete
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_hint_apply
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_hint_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_context_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_default
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_location
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_review
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_summary
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_tags
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_placeholder_title
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_quick_capture
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_quick_upload
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_input_support_capture
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun ChatInputBar(
    modifier: Modifier,
    viewModel: AIViewModel,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { uiState ->
            ChatInputBarState(
                input = uiState.input,
                conversationState = uiState.context.state,
                inputField = uiState.context.focusField,
                missingFields = uiState.context.missingFields,
                inputLocked = uiState.context.state == ConversationState.CARD_REVIEW &&
                    uiState.context.focusField == null,
            )
        }
    }
    val safeState = state ?: return
    ChatInputBarContent(
        modifier = modifier,
        onInputChange = { text ->
            viewModel.updateInput(text)
        },
        input = safeState.input,
        placeHolder = safeState.placeHolderText(),
        contextLabel = safeState.contextLabel(),
        supportingText = safeState.supportingText(),
        inputEnabled = safeState.inputLocked.not(),
        topContent = {
            if (safeState.showCaptureShortcuts()) {
                QuickCaptureActions(
                    onCapture = {
                        viewModel.doAction(ActionCommand.CaptureMedia)
                    },
                    onUpload = {
                        viewModel.doAction(ActionCommand.UploadMedia)
                    },
                )
            }
        },
        onSend = {
            viewModel.submitInput()
        }
    )
}

@Immutable
data class ChatInputBarState(
    val input: String,
    val conversationState: ConversationState,
    val inputField: Field?,
    val missingFields: List<Field>,
    val inputLocked: Boolean,
)

@Composable
fun ChatInputBarContent(
    modifier: Modifier = Modifier,
    input: String,
    placeHolder: String,
    contextLabel: String? = null,
    supportingText: String? = null,
    inputEnabled: Boolean = true,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    topContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable ((Boolean) -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    val canSend = inputEnabled && input.isNotBlank()
    val heightState = rememberStickyInputHeight()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            topContent?.invoke()
            contextLabel
                ?.takeIf { it.isNotBlank() }
                ?.let { label ->
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.92f),
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                leadingContent?.invoke()
                StickyInputCore(
                    input = input,
                    placeHolder = placeHolder,
                    onInputChange = onInputChange,
                    heightState = heightState,
                    enabled = inputEnabled,
                    onSend = {
                        if (canSend) {
                            onSend(input)
                            onInputChange("")
                            heightState.reset()
                            focusManager.clearFocus()
                        }
                    }
                )
                if (trailingContent != null) {
                    trailingContent(canSend)
                } else {
                    SendButton(
                        enabled = canSend,
                        onClick = {
                            if (canSend) {
                                onSend(input)
                                onInputChange("")
                                heightState.reset()
                                focusManager.clearFocus()
                            }
                        }
                    )
                }
            }
            supportingText
                ?.takeIf { it.isNotBlank() }
                ?.let { text ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
        }
    }
}

@Composable
private fun ChatInputBarState.supportingText(): String? =
    input.takeIf { it.isBlank() }?.let {
        when (conversationState) {
            ConversationState.IDLE,
            ConversationState.COMPLETE -> stringResource(Res.string.feature_ai_input_support_capture)

            ConversationState.INFO_COLLECT,
            ConversationState.MANUAL_EDIT ->
                when (inputField ?: missingFields.firstOrNull()) {
                    Field.MEDIA -> stringResource(Res.string.feature_ai_input_support_capture)
                    Field.TITLE,
                    Field.SUMMARY,
                    Field.LOCATION,
                    Field.TAGS -> stringResource(Res.string.feature_ai_input_context_hint_apply)

                    else -> null
                }

            ConversationState.CARD_REVIEW ->
                if (inputLocked) {
                    stringResource(Res.string.feature_ai_input_context_hint_review)
                } else {
                    stringResource(Res.string.feature_ai_input_context_hint_apply)
                }

            else -> null
        }
    }

@Composable
private fun ChatInputBarState.contextLabel(): String? =
    when (conversationState) {
        ConversationState.INFO_COLLECT if missingFields.firstOrNull() == Field.MEDIA ->
            stringResource(Res.string.feature_ai_input_context_media)

        ConversationState.INFO_COLLECT if missingFields.firstOrNull() == Field.TAGS ->
            stringResource(Res.string.feature_ai_input_context_tags)

        ConversationState.INFO_COLLECT if missingFields.firstOrNull() == Field.TITLE ->
            stringResource(Res.string.feature_ai_input_context_title)

        ConversationState.CARD_REVIEW,
        ConversationState.MANUAL_EDIT -> when (inputField) {
            Field.TAGS -> stringResource(Res.string.feature_ai_input_context_tags)
            Field.SUMMARY -> stringResource(Res.string.feature_ai_input_context_summary)
            Field.TITLE -> stringResource(Res.string.feature_ai_input_context_title)
            Field.LOCATION -> stringResource(Res.string.feature_ai_input_context_location)
            Field.MEDIA -> stringResource(Res.string.feature_ai_input_context_media)
            else -> stringResource(Res.string.feature_ai_input_context_review)
        }

        ConversationState.COMPLETE -> stringResource(Res.string.feature_ai_input_context_complete)
        else -> null
    }

private fun ChatInputBarState.showCaptureShortcuts(): Boolean {
    return when (conversationState) {
        ConversationState.IDLE,
        ConversationState.COMPLETE -> true

        ConversationState.INFO_COLLECT ->
            (inputField ?: missingFields.firstOrNull()) == Field.MEDIA

        else -> false
    }
}

@Composable
private fun ChatInputBarState.placeHolderText(): String =
    when (conversationState) {
        ConversationState.INFO_COLLECT if missingFields.firstOrNull() == Field.MEDIA ->
            stringResource(Res.string.feature_ai_input_placeholder_media)

        ConversationState.INFO_COLLECT if missingFields.firstOrNull() == Field.TAGS ->
            stringResource(Res.string.feature_ai_input_placeholder_tags)

        ConversationState.INFO_COLLECT if missingFields.firstOrNull() == Field.TITLE ->
            stringResource(Res.string.feature_ai_input_placeholder_title)

        ConversationState.CARD_REVIEW, ConversationState.MANUAL_EDIT ->
            when (inputField) {
                Field.TAGS -> stringResource(Res.string.feature_ai_input_placeholder_tags)
                Field.SUMMARY -> stringResource(Res.string.feature_ai_input_placeholder_summary)
                Field.TITLE -> stringResource(Res.string.feature_ai_input_placeholder_title)
                Field.LOCATION -> stringResource(Res.string.feature_ai_input_placeholder_location)
                Field.MEDIA -> stringResource(Res.string.feature_ai_input_placeholder_media)
                else -> stringResource(Res.string.feature_ai_input_placeholder_review)
            }

        else -> stringResource(Res.string.feature_ai_input_placeholder_default)
    }

@Composable
private fun QuickCaptureActions(
    onCapture: () -> Unit,
    onUpload: () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = onCapture,
            label = {
                Text(text = stringResource(Res.string.feature_ai_input_quick_capture))
            },
        )
        AssistChip(
            onClick = onUpload,
            label = {
                Text(text = stringResource(Res.string.feature_ai_input_quick_upload))
            },
        )
    }
}

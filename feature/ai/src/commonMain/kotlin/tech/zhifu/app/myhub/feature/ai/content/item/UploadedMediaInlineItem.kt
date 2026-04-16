package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.AIUiState
import tech.zhifu.app.myhub.feature.ai.AIViewModel
import tech.zhifu.app.myhub.feature.ai.layer.conversation.action.ActionOptionType
import tech.zhifu.app.myhub.feature.ai.layer.conversation.state.ConversationState
import tech.zhifu.app.myhub.feature.ai.model.CaptureMediaAsset
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_remove_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_replace_media
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_action_uploaded_media
import tech.zhifu.app.myhub.ui.viewmodel.collectAsSelectedStateWithLifecycle
import tech.zhifu.app.myhub.ui.viewmodel.uiState

@Composable
fun UploadedMediaInlineItem(
    viewModel: AIViewModel,
) {
    val state by viewModel.uiState.collectAsSelectedStateWithLifecycle {
        (it as? AIUiState.Content)?.let { state ->
            when (state.conversationState) {
                ConversationState.INFO_COLLECT,
                ConversationState.CARD_REVIEW,
                ConversationState.MANUAL_EDIT -> {
                    state.draft?.mediaAssets
                }

                else -> null
            }
        }
    }
    val safeState = state ?: return
    UploadedMediaInlineItemContent(
        mediaAssets = safeState,
        onRemove = { index ->
            viewModel.performQuickAction(
                action = ActionOptionType.encodeRemoveMediaAt(index)
            )
        },
        onReplace = {
            viewModel.performQuickAction(
                action = ActionOptionType.REPLACE_MEDIA.value
            )
        }
    )
}

@Composable
fun UploadedMediaInlineItemContent(
    mediaAssets: List<CaptureMediaAsset>,
    onRemove: (Int) -> Unit,
    onReplace: () -> Unit,
) {
    if (mediaAssets.isEmpty()) return
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
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
                text = stringResource(Res.string.feature_ai_action_uploaded_media),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(
                    items = mediaAssets,
                    key = { index, asset -> "${asset.localUri}#$index" },
                ) { index, asset ->
                    Box {
                        SubcomposeAsyncImage(
                            model = asset.localUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .padding(top = 2.dp, end = 2.dp),
                        )
                        IconButton(
                            onClick = { onRemove(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(Res.string.feature_ai_action_remove_media),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(999.dp),
                                    )
                                    .padding(2.dp),
                            )
                        }
                    }
                }
            }
            OutlinedButton(
                onClick = onReplace,
                modifier = Modifier.height(36.dp),
            ) {
                Text(stringResource(Res.string.feature_ai_action_replace_media))
            }
        }
    }
}

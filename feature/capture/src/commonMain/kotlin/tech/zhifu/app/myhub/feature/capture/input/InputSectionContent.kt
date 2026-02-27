package tech.zhifu.app.myhub.feature.capture.input

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.component.media.MediaItem
import tech.zhifu.app.myhub.component.media.component.MediaThumbnail
import tech.zhifu.app.myhub.feature.capture.resources.Res
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_add_media
import tech.zhifu.app.myhub.feature.capture.resources.feature_capture_input_placeholder
import tech.zhifu.app.myhub.theme.AppMotionTokens
import tech.zhifu.app.myhub.theme.motionTween

@Composable
fun InputContent(
    inputText: String,
    onInputChange: (String) -> Unit,
    onInputFocusChanged: (Boolean) -> Unit,
    mediaItems: List<MediaItem>,
    onRemoveMedia: (String) -> Unit,
    onAddMedia: () -> Unit,
    onPreviewMedia: (MediaItem) -> Unit,
    horizontalPadding: Dp,
    isProcessing: Boolean
) {
    val inputInteractionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var textFieldBounds by remember { mutableStateOf<Rect?>(null) }
    var inputFocused by remember { mutableStateOf(false) }
    val underlineAnim = remember { Animatable(0f) }
    val underlineProgress = underlineAnim.value
    LaunchedEffect(key1 = inputFocused) {
        underlineAnim.animateTo(
            targetValue = if (inputFocused) 1f else 0f,
            animationSpec = motionTween(
                spec = if (inputFocused) AppMotionTokens.UnderlineExpand else AppMotionTokens.UnderlineCollapse
            )
        )
    }
    Column(
        modifier = Modifier.widthIn(max = 768.dp)
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(bottom = 80.dp)
            .pointerInput(key1 = textFieldBounds) {
                detectTapGestures { offset ->
                    val bounds = textFieldBounds
                    if (bounds != null && !bounds.contains(offset)) {
                        focusManager.clearFocus()
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        InputField(
            inputText = inputText,
            onInputChange = onInputChange,
            isProcessing = isProcessing,
            underlineProgress = underlineProgress,
            inputInteractionSource = inputInteractionSource,
            focusRequester = focusRequester,
            onFocused = { focused -> inputFocused = focused },
            onFocusStateChange = onInputFocusChanged,
            onBoundsChanged = { bounds -> textFieldBounds = bounds }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            mediaItems.forEach { item ->
                MediaThumbnail(
                    item = item,
                    onRemove = { onRemoveMedia(item.id) },
                    onPreview = { onPreviewMedia(item) },
                    enabled = !isProcessing
                )
            }
            AddMediaButton(
                onClick = onAddMedia,
                enabled = !isProcessing
            )
        }
    }
}

@Composable
private fun InputField(
    inputText: String,
    onInputChange: (String) -> Unit,
    isProcessing: Boolean,
    underlineProgress: Float,
    inputInteractionSource: MutableInteractionSource,
    focusRequester: FocusRequester,
    onFocused: (Boolean) -> Unit,
    onFocusStateChange: (Boolean) -> Unit,
    onBoundsChanged: (Rect) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        BasicTextField(
            value = inputText,
            onValueChange = onInputChange,
            enabled = !isProcessing,
            interactionSource = inputInteractionSource,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Light,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontFamily = FontFamily.Serif
            ),
            cursorBrush = SolidColor(value = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 16.dp)
                .focusRequester(focusRequester)
                .focusable(
                    enabled = !isProcessing,
                    interactionSource = inputInteractionSource
                )
                .pointerInput(key1 = isProcessing) {
                    if (!isProcessing) {
                        detectTapGestures { focusRequester.requestFocus() }
                    }
                }
                .onFocusChanged { focusState ->
                    onFocused(focusState.isFocused)
                    onFocusStateChange(focusState.isFocused)
                }
                .onGloballyPositioned { coordinates ->
                    onBoundsChanged(coordinates.boundsInParent())
                },
            singleLine = false,
            maxLines = 6,
            decorationBox = { inner ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.feature_capture_input_placeholder),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Light,
                                fontSize = 20.sp,
                                lineHeight = 26.sp,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }
                    inner()
                }
            }
        )
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(2.dp)
        ) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth().height(1.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline
                    )
            )
            if (underlineProgress > 0.01f) {
                BoxWithConstraints(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                ) {
                    val fullWidth = maxWidth
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart)
                            .width(fullWidth * underlineProgress)
                            .height(2.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}


@Composable
private fun AddMediaButton(
    onClick: () -> Unit, enabled: Boolean
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(size = 16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 24.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = stringResource(Res.string.feature_capture_add_media),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

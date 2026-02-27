package tech.zhifu.app.myhub.feature.capture.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import tech.zhifu.app.myhub.feature.capture.ReviewContentType
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

@Composable
internal fun ReviewContentContainer(
    contentType: ReviewContentType,
    selectedContentType: ReviewContentType?,
    onSelect: (ReviewContentType) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isSelected = selectedContentType == contentType
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isHovered -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val shadowElevation = if (isSelected) 12.dp else 0.dp
    Surface(
        shape = RoundedCornerShape(size = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = shadowElevation,
        modifier = modifier
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onSelect(contentType) }
    ) {
        content()
    }
}

@Composable

internal fun ReviewText(
    textState: RichTextState,
    onTextChange: (String) -> Unit,
    enabled: Boolean,
    selectedContentType: ReviewContentType?,
    onSelectContent: (ReviewContentType) -> Unit
) {
    var lastSelection by remember { mutableStateOf(TextRange.Zero) }
    val onTextChangeState by rememberUpdatedState(onTextChange)
    LaunchedEffect(key1 = textState) {
        snapshotFlow { textState.annotatedString }
            .map { textState.toHtml() }
            .onEach {
                logger.debug {
                    "new rich text state= $it"
                }
            }
            .distinctUntilChanged()
            .onEach { markdown ->
                logger.debug {
                    "new rich text state222= $markdown"
                }
                onTextChangeState(markdown)
            }
            .collect { }
    }
    LaunchedEffect(key1 = textState) {
        snapshotFlow { textState.selection }
            .distinctUntilChanged()
            .onEach { lastSelection = it }
            .collect { }
    }
    val applySpanStyle: (SpanStyle) -> Unit = { style ->
        if (textState.selection != lastSelection) {
            textState.selection = lastSelection
        }
        textState.toggleSpanStyle(style)
    }
    val applyParagraphStyle: (ParagraphStyle) -> Unit = { style ->
        if (textState.selection != lastSelection) {
            textState.selection = lastSelection
        }
        textState.toggleParagraphStyle(style)
    }
    ReviewContentContainer(
        contentType = ReviewContentType.Text,
        selectedContentType = selectedContentType,
        onSelect = onSelectContent
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReviewToolbarIcon(icon = Icons.Default.FormatBold) {
                        applySpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    }
                    ReviewToolbarIcon(icon = Icons.Default.FormatItalic) {
                        applySpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    }
                    ReviewToolbarIcon(icon = Icons.Default.FormatUnderlined) {
                        applySpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    }
                    VerticalDivider(
                        modifier = Modifier.width(1.dp)
                            .height(18.dp)
                            .background(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )
                    ReviewToolbarIcon(icon = Icons.AutoMirrored.Filled.FormatAlignLeft) {
                        applyParagraphStyle(ParagraphStyle(textAlign = TextAlign.Left))
                    }
                    ReviewToolbarIcon(icon = Icons.Filled.FormatAlignCenter) {
                        applyParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))
                    }
                    ReviewToolbarIcon(icon = Icons.AutoMirrored.Filled.FormatAlignRight) {
                        applyParagraphStyle(ParagraphStyle(textAlign = TextAlign.Right))
                    }
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            BasicRichTextEditor(
                state = textState,
                enabled = enabled,
                textStyle = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 6,
                cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
                decorationBox = @Composable { innerTextField ->
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(20.dp)
            )
        }
    }
}

internal fun isLikelyHtml(content: String): Boolean {
    val trimmed = content.trim()
    if (trimmed.isEmpty()) return false
    return trimmed.startsWith("<") && trimmed.contains(">")
}


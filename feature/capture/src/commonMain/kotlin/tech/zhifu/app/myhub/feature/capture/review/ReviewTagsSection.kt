package tech.zhifu.app.myhub.feature.capture.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ReviewTagsSection(
    tags: List<String>,
    tagQuery: String,
    onTagQueryChange: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit
) {
    val normalizedTags = tags.map { it.trim().trimStart('#') }
    val isDuplicate = tagQuery.isNotBlank() &&
        normalizedTags.any { it.equals(tagQuery, ignoreCase = true) }
    val suggestionPool = remember(normalizedTags) {
        (normalizedTags + listOf(
            "Design",
            "Inspiration",
            "Architecture",
            "Product",
            "AI",
            "Video",
            "Image",
            "Code"
        )).distinct()
    }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showTagInput by remember { mutableStateOf(false) }
    val updateQuery: (String) -> Unit = { value ->
        showTagInput = true
        dropdownExpanded = true
        onTagQueryChange(value)
    }
    LaunchedEffect(tagQuery, dropdownExpanded) {
        if (tagQuery.isBlank() && !dropdownExpanded) {
            showTagInput = false
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "TAGS",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.4.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            normalizedTags.forEach { tag ->
                ReviewTagChip(
                    text = "#${tag}",
                    onRemove = { onRemoveTag(tag) }
                )
            }
            if (showTagInput || tagQuery.isNotBlank()) {
                ReviewTagInputChip(
                    value = tagQuery,
                    isDuplicate = isDuplicate,
                    isActive = true,
                    onValueChange = updateQuery
                )
            } else {
                ReviewTagAddButton(onClick = {
                    showTagInput = true
                    dropdownExpanded = true
                })
            }
        }
        ReviewTagDropdown(
            query = tagQuery,
            expanded = dropdownExpanded,
            suggestions = suggestionPool,
            onSelectTag = {
                onAddTag(it)
                dropdownExpanded = false
                showTagInput = false
            },
            onCreateTag = {
                onAddTag(it)
                dropdownExpanded = false
                showTagInput = false
            },
            onDismiss = { dropdownExpanded = false }
        )
    }
}

@Composable
private fun ReviewTagChip(
    text: String,
    onRemove: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val closeInteractionSource = remember { MutableInteractionSource() }
    val isCloseHovered by closeInteractionSource.collectIsHoveredAsState()
    Surface(
        shape = RoundedCornerShape(size = 12.dp),
        color = Color(0xFF2B2930),
        border = BorderStroke(
            1.dp,
            if (isHovered) MaterialTheme.colorScheme.primary
            else Color(0xFF49454F)
        ),
        modifier = Modifier.hoverable(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) { }
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                color = Color(0xFFE6E1E5)
            )
            Box(
                modifier = Modifier.size(20.dp)
                    .clip(CircleShape)
                    .hoverable(closeInteractionSource)
                    .background(
                        if (isCloseHovered) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFCAC4D0)
                )
            }
        }
    }
}

@Composable
private fun ReviewTagInputChip(
    value: String,
    isDuplicate: Boolean,
    isActive: Boolean,
    onValueChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(size = 12.dp),
        color = Color(0xFF2B2930),
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF49454F)
        )
    ) {
        Row(
            modifier = Modifier.widthIn(min = 160.dp)
                .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "#",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE6E1E5)
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            )
            if (isDuplicate) {
                Surface(
                    shape = RoundedCornerShape(size = 8.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                text = "!",
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Text(
                            text = "DUPLICATE",
                            style = TextStyle(
                                fontSize = 9.sp,
                                letterSpacing = 0.6.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewTagAddButton(onClick: () -> Unit) {
    val borderColor = Color(0xFF938F99)
    Row(
        modifier = Modifier.drawBehind {
            val strokeWidth = 1.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            drawRoundRect(
                color = borderColor,
                size = size,
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = strokeWidth, pathEffect = dashEffect)
            )
        }.clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFFCAC4D0)
        )
        Text(
            text = "Add Tag",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            color = Color(0xFFCAC4D0)
        )
    }
}

@Composable
private fun ReviewTagDropdown(
    query: String,
    expanded: Boolean,
    suggestions: List<String>,
    onSelectTag: (String) -> Unit,
    onCreateTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!expanded && query.isBlank()) return
    val normalizedQuery = query.trim()
    val filteredSuggestions = suggestions.filter {
        if (normalizedQuery.isBlank()) true else isSubsequenceMatch(it, normalizedQuery)
    }
    Surface(
        shape = RoundedCornerShape(size = 12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        shadowElevation = 16.dp,
        modifier = Modifier.widthIn(max = 360.dp)
    ) {
        Column {
            filteredSuggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable {
                            onSelectTag(suggestion)
                            onDismiss()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = buildHighlightedTagText(
                            suggestion,
                            normalizedQuery,
                            MaterialTheme.colorScheme.primary
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable {
                        if (normalizedQuery.isNotBlank()) {
                            onCreateTag(normalizedQuery)
                            onDismiss()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (normalizedQuery.isNotBlank()) {
                        "Create \"#${normalizedQuery}\""
                    } else {
                        "Create new tag"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun isSubsequenceMatch(text: String, query: String): Boolean {
    if (query.isBlank()) return true
    var matchIndex = 0
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    lowerText.forEach { char ->
        if (matchIndex < lowerQuery.length && char == lowerQuery[matchIndex]) {
            matchIndex += 1
        }
    }
    return matchIndex == lowerQuery.length
}

private fun buildHighlightedTagText(
    text: String,
    query: String,
    highlightColor: Color
) = buildAnnotatedString {
    if (query.isBlank()) {
        append("#${text}")
        return@buildAnnotatedString
    }
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    var queryIndex = 0
    append("#")
    text.forEachIndexed { index, char ->
        val shouldHighlight = queryIndex < lowerQuery.length &&
            lowerText[index] == lowerQuery[queryIndex]
        if (shouldHighlight) {
            withStyle(SpanStyle(color = highlightColor)) {
                append(char)
            }
            queryIndex += 1
        } else {
            append(char)
        }
    }
}


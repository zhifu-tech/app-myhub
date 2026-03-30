package tech.zhifu.app.myhub.feature.ai

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import tech.zhifu.app.myhub.navigation.AppNavigator
import kotlin.math.min

@Composable
fun AiScreen(
    navigator: AppNavigator,
    viewModel: AIViewModel = koinViewModel<AIViewModel>(),
) {
    val state by viewModel.collectFieldAsState { it }
    val contentState = state as? AIUiState.Content

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AiCaptureColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AiCaptureTopBar(onBackClick = navigator::goBack)
            AiCaptureTimeline(
                messages = contentState?.messages ?: aiMockMessages,
                tags = contentState?.tags ?: emptyList(),
                tagInputMode = contentState?.isTagInputMode ?: false,
                newTag = contentState?.newTag ?: "",
                onRemoveTag = viewModel::removeTag,
                onShowTagInput = viewModel::showTagInput,
                onTagValueChange = viewModel::updateNewTag,
                onAddTag = viewModel::addTag,
            )
        }
        AiCaptureInputBar(
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AiCaptureTopBar(
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AiCaptureColors.HeaderBackground)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = AiCaptureColors.TitleText,
                )
            }
            Column {
                Text(
                    text = "AI 捕获",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = AiCaptureColors.TitleText,
                        fontWeight = FontWeight.Bold,
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AiCaptureColors.OnlineDot),
                    )
                    Text(
                        text = "在线状态",
                        style = MaterialTheme.typography.labelSmall.copy(color = AiCaptureColors.SecondaryText),
                    )
                }
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
private fun AiCaptureTimeline(
    messages: List<AIMessage>,
    tags: List<String>,
    tagInputMode: Boolean,
    newTag: String,
    onRemoveTag: (String) -> Unit,
    onShowTagInput: () -> Unit,
    onTagValueChange: (String) -> Unit,
    onAddTag: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 196.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(messages, key = { it.id }) { message ->
            when (message) {
                is AIMessage.AiText -> AiTextBubble(text = message.text)
                is AIMessage.UserText -> UserTextBubble(text = message.text)
                is AIMessage.AiAskImage -> AiImagePrompt(title = message.text)
                is AIMessage.AiTagEditor -> {
                    AiTagPrompt(
                        title = message.text,
                        tags = tags,
                        tagInputMode = tagInputMode,
                        newTag = newTag,
                        onRemoveTag = onRemoveTag,
                        onShowTagInput = onShowTagInput,
                        onTagValueChange = onTagValueChange,
                        onAddTag = onAddTag,
                    )
                }

                is AIMessage.AiTitleInput -> {
                    AiTitlePrompt(
                        title = message.text,
                        contentTitle = message.contentTitle,
                    )
                }

                is AIMessage.AiPublishPrompt -> {
                    AiPublishPrompt(title = message.text)
                }
            }
        }
    }
}

@Composable
private fun AiTextBubble(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AiAvatar()
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(AiCaptureColors.AiBubble)
                .border(
                    width = 1.dp,
                    color = AiCaptureColors.AiBorder,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AiCaptureColors.PrimaryText,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Medium,
                )
            )
        }
    }
}

@Composable
private fun UserTextBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.84f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(AiCaptureColors.UserBubble)
                .padding(horizontal = 14.dp, vertical = 13.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            )
        }
    }
}

@Composable
private fun AiImagePrompt(title: String) {
    AiCardFrame {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AiCaptureColors.PrimaryText,
                fontWeight = FontWeight.SemiBold,
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, AiCaptureColors.ButtonBorder, RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    tint = AiCaptureColors.PrimaryText,
                )
                Text(
                    text = "上传图片",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AiCaptureColors.PrimaryText,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        DisabledActionButton(text = "跳过")
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AiTagPrompt(
    title: String,
    tags: List<String>,
    tagInputMode: Boolean,
    newTag: String,
    onRemoveTag: (String) -> Unit,
    onShowTagInput: () -> Unit,
    onTagValueChange: (String) -> Unit,
    onAddTag: () -> Unit,
) {
    AiCardFrame {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AiCaptureColors.PrimaryText,
                fontWeight = FontWeight.SemiBold,
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEach { tag ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AiCaptureColors.TagBackground)
                        .border(1.dp, AiCaptureColors.TagBorder, RoundedCornerShape(10.dp))
                        .padding(start = 10.dp, top = 6.dp, end = 4.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AiCaptureColors.TagText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    )
                    IconButton(
                        onClick = { onRemoveTag(tag) },
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            tint = AiCaptureColors.TagText,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (tagInputMode) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, AiCaptureColors.ButtonBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = newTag,
                        onValueChange = onTagValueChange,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = AiCaptureColors.PrimaryText),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (newTag.isEmpty()) {
                                Text(
                                    text = "输入新标签",
                                    style = MaterialTheme.typography.bodySmall.copy(color = AiCaptureColors.SecondaryText)
                                )
                            }
                            innerTextField()
                        },
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AiCaptureColors.UserBubble)
                        .clickable(onClick = onAddTag)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "添加",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .clickable(onClick = onShowTagInput)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = AiCaptureColors.TagText,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "新增标签",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AiCaptureColors.TagText,
                        fontWeight = FontWeight.SemiBold,
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        DisabledActionButton(text = "下一步")
    }
}

@Composable
private fun AiTitlePrompt(
    title: String,
    contentTitle: String,
) {
    AiCardFrame {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AiCaptureColors.PrimaryText,
                fontWeight = FontWeight.SemiBold,
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, AiCaptureColors.ButtonBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Column {
                Text(
                    text = "内容标题",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AiCaptureColors.SecondaryText,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contentTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = AiCaptureColors.PrimaryText,
                        fontWeight = FontWeight.Medium,
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        PrimaryActionButton(text = "下一步")
    }
}

@Composable
private fun AiPublishPrompt(
    title: String,
) {
    AiCardFrame {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AiCaptureColors.PrimaryText,
                fontWeight = FontWeight.SemiBold,
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        PrimaryActionButton(text = "发布")
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, AiCaptureColors.ButtonBorder, RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "继续编辑",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AiCaptureColors.PrimaryText,
                    fontWeight = FontWeight.SemiBold,
                )
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AiCaptureColors.UserBubble)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        )
    }
}

@Composable
private fun DisabledActionButton(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AiCaptureColors.DisabledButton)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AiCaptureColors.SecondaryText,
                fontWeight = FontWeight.SemiBold,
            )
        )
    }
}

@Composable
private fun AiCardFrame(
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AiAvatar()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(AiCaptureColors.AiBubble)
                .border(
                    width = 1.dp,
                    color = AiCaptureColors.AiBorder,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .padding(14.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun AiAvatar() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        KineticLogo(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun KineticLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gradient = Brush.linearGradient(
            colors = listOf(Color(0xFFB4A3FF), Color(0xFF4F46E5)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
        )
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.8f)
            cubicTo(
                size.width * 0.2f,
                size.height * 0.8f,
                size.width * 0.2f,
                size.height * 0.3f,
                size.width * 0.35f,
                size.height * 0.3f
            )
            cubicTo(
                size.width * 0.5f,
                size.height * 0.3f,
                size.width * 0.5f,
                size.height * 0.7f,
                size.width * 0.65f,
                size.height * 0.7f
            )
            cubicTo(
                size.width * 0.8f,
                size.height * 0.7f,
                size.width * 0.8f,
                size.height * 0.2f,
                size.width * 0.8f,
                size.height * 0.2f
            )
        }
        drawPath(
            path = path,
            brush = gradient,
            style = Stroke(
                width = min(size.width, size.height) * 0.16f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
        drawCircle(
            color = Color(0xFF131316),
            radius = min(size.width, size.height) * 0.08f,
            center = Offset(size.width * 0.8f, size.height * 0.2f),
        )
    }
}

@Composable
private fun AiCaptureInputBar(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "capture-card-heartbeat")
    val heartbeatOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                0f at 0 using LinearEasing
                -6f at 750 using LinearEasing
                0f at 1500 using LinearEasing
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "capture-card-heartbeat-offset",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        AiCaptureColors.Background.copy(alpha = 0.95f),
                        AiCaptureColors.Background,
                    )
                )
            )
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = heartbeatOffset.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.78f))
                        .border(1.dp, Color.White.copy(alpha = 0.56f), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF2FF))
                            .border(1.dp, Color(0xFFE0E5FF), RoundedCornerShape(10.dp))
                            .padding(horizontal = 13.dp, vertical = 10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Article,
                            contentDescription = null,
                            tint = AiCaptureColors.TagText,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.72f))
                        .border(1.dp, Color.White.copy(alpha = 0.62f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "补充更多信息...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9CA3B4)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

private object AiCaptureColors {
    val Background = Color(0xFFF6F6F8)
    val HeaderBackground = Color(0xFFF3F4F6)
    val TitleText = Color(0xFF111827)
    val PrimaryText = Color(0xFF374151)
    val SecondaryText = Color(0xFF94A3B8)
    val OnlineDot = Color(0xFF10B981)

    val AiBubble = Color(0xFFF1F5F9)
    val AiBorder = Color(0xFFE8EDF3)
    val UserBubble = Color(0xFF6366F1)
    val DisabledButton = Color(0xFFE7EDF4)
    val ButtonBorder = Color(0xFFE2E8F0)

    val TagBackground = Color(0xFFEFF2FF)
    val TagBorder = Color(0xFFDDE3FF)
    val TagText = Color(0xFF6366F1)
}

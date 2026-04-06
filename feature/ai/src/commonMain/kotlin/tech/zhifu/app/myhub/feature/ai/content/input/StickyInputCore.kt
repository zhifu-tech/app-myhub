package tech.zhifu.app.myhub.feature.ai.content.input

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * ```md
 * 1. 输入体验:
 *  + 单行 → 多行平滑增长
 *  + 删除不会瞬间塌
 *  + 超过6行内部滚动
 * 2. 行为逻辑
 *  + Send / IME 一致
 *  + 发送后清空 + 收缩
 *  + 失焦收缩
 * 3. 动效
 *  + 高度 spring（iOS感）
 *  + SendButton scale + alpha
 * ```
 * */
@Composable
internal fun RowScope.StickyInputCore(
    input: String,
    placeHolder: String,
    onInputChange: (String) -> Unit,
    heightState: StickyInputHeightState,
    onSend: () -> Unit
) {
    val minHeight = 40.dp
    val maxHeight = 140.dp

    val density = LocalDensity.current

    @Suppress("VariableNeverRead")
    var isFocused by remember { mutableStateOf(false) }

    val animatedHeight by animateDpAsState(
        targetValue = with(density) {
            heightState
                .getDisplayHeightPx()
                .toDp()
                .coerceIn(minHeight, maxHeight)
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = 0.85f // 👈 iOS手感关键
        ),
        label = "input_height"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 6.dp)
            .height(animatedHeight)
    ) {
        BasicTextField(
            value = input,
            onValueChange = onInputChange,
            maxLines = 6,

            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),

            cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),

            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Send
            ),

            keyboardActions = KeyboardActions(
                onSend = { onSend() }
            ),

            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { keyEvent ->
                    when {
                        keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter -> {
                            onSend()
                            return@onKeyEvent true
                        }
                    }
                    false
                }
                .padding(vertical = 8.dp)
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (!it.isFocused) {
                        heightState.reset() // 👈 失焦收缩
                    }
                },

            // 捕获真实高度
            onTextLayout = { result ->
                heightState.update(result.size.height)
            }
        )

        if (input.isEmpty()) {
            Text(
                text = placeHolder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.BottomStart)
                    .padding(bottom = 12.dp)
            )
        }
    }
}


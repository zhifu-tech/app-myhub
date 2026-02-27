package tech.zhifu.app.myhub.feature.card.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.component.card.CardPreview
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.content

/**
 * 卡片内容展示区
 * 复用现有的 CardComponent 渲染卡片
 *
 * 根据截图设计：
 * - 移除错误的灰色背景
 * - 卡片的圆角背景就是详情页的卡片背景（CardComponent 本身有圆角）
 * - 顶部显示标题，标题在圆角背景之上
 * - 标题有过渡动画：从 card 标题过渡到详情页标题
 */
@Composable
fun CardDetailContent(
    card: Card,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 标题显示在圆角背景之上，带过渡动画
        val cardTitle = card.metadata.content?.title ?: card.type.wire
        AnimatedContent(
            targetState = cardTitle,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) + slideInVertically(
                    initialOffsetY = { -it / 3 },
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ) togetherWith fadeOut(
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            label = "card_title_transition"
        ) { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 使用统一的 CardComponent 渲染卡片
        // CardComponent 本身有圆角背景，不需要额外的背景容器
        CardPreview(
            card = card,
            modifier = Modifier.fillMaxWidth(),
            // 详情页模式下，可以禁用某些交互
            onClick = {}  // 详情页中点击卡片不跳转
        )
    }
}

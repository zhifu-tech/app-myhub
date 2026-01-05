package tech.zhifu.app.myhub.component.card

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers

/**
 * ArticleCardComponent 单元测试
 */
class ArticleCardComponentTest {

    private val component = ArticleCardComponent()

    @Test
    fun `test getTypeIconColor returns blue`() {
        // When
        val color = component.getTypeIconColor()

        // Then
        assertEquals(Color(0xFF3B82F6), color)
    }

    @Test
    fun `test getTypeIconText returns A`() {
        // When
        val text = component.getTypeIconText()

        // Then
        assertEquals("A", text)
    }

    // 注意：getDisplayTitle 是 @Composable 函数，需要在 Compose 测试环境中测试
}


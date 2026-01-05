package tech.zhifu.app.myhub.component.card

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers

/**
 * IdeaCardComponent 单元测试
 */
class IdeaCardComponentTest {

    private val component = IdeaCardComponent()

    @Test
    fun `test getTypeIconColor returns amber`() {
        // When
        val color = component.getTypeIconColor()

        // Then
        assertEquals(Color(0xFFF59E0B), color)
    }

    @Test
    fun `test getTypeIconText returns I`() {
        // When
        val text = component.getTypeIconText()

        // Then
        assertEquals("I", text)
    }

    // 注意：getDisplayTitle 是 @Composable 函数，需要在 Compose 测试环境中测试
}


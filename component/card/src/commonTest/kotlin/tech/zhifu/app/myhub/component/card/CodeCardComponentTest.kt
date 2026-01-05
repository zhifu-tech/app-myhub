package tech.zhifu.app.myhub.component.card

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers

/**
 * CodeCardComponent 单元测试
 */
class CodeCardComponentTest {

    private val component = CodeCardComponent()

    @Test
    fun `test getTypeIconColor returns green`() {
        // When
        val color = component.getTypeIconColor()

        // Then
        assertEquals(Color(0xFF10B981), color)
    }

    @Test
    fun `test getTypeIconText returns C`() {
        // When
        val text = component.getTypeIconText()

        // Then
        assertEquals("C", text)
    }

    // 注意：getDisplayTitle 是 @Composable 函数，需要在 Compose 测试环境中测试
}


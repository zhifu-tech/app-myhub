package tech.zhifu.app.myhub.component.card

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers

/**
 * ChecklistCardComponent 单元测试
 */
class ChecklistCardComponentTest {

    private val component = ChecklistCardComponent()

    @Test
    fun `test getTypeIconColor returns red`() {
        // When
        val color = component.getTypeIconColor()

        // Then
        assertEquals(Color(0xFFEF4444), color)
    }

    @Test
    fun `test getTypeIconText returns checkmark`() {
        // When
        val text = component.getTypeIconText()

        // Then
        assertEquals("✓", text)
    }

    // 注意：getDisplayTitle 是 @Composable 函数，需要在 Compose 测试环境中测试
}


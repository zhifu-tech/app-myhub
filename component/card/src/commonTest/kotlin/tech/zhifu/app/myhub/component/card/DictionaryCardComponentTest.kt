package tech.zhifu.app.myhub.component.card

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers

/**
 * DictionaryCardComponent 单元测试
 */
class DictionaryCardComponentTest {

    private val component = DictionaryCardComponent()

    @Test
    fun `test getTypeIconColor returns cyan`() {
        // When
        val color = component.getTypeIconColor()

        // Then
        assertEquals(Color(0xFF06B6D4), color)
    }

    @Test
    fun `test getTypeIconText returns D`() {
        // When
        val text = component.getTypeIconText()

        // Then
        assertEquals("D", text)
    }

    // 注意：getDisplayTitle 是 @Composable 函数，需要在 Compose 测试环境中测试
}


package tech.zhifu.app.myhub.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * WindowSize 单元测试
 */
class WindowSizeTest {

    @Test
    fun `test calculateWindowSizeClass with compact size`() {
        // Given
        val size = DpSize(400.dp, 800.dp)

        // When
        val result = calculateWindowSizeClass(size)

        // Then
        assertEquals(WindowSizeClass.Compact, result)
    }

    @Test
    fun `test calculateWindowSizeClass with medium size`() {
        // Given
        val size = DpSize(700.dp, 1000.dp)

        // When
        val result = calculateWindowSizeClass(size)

        // Then
        assertEquals(WindowSizeClass.Medium, result)
    }

    @Test
    fun `test calculateWindowSizeClass with expanded size`() {
        // Given
        val size = DpSize(1000.dp, 1200.dp)

        // When
        val result = calculateWindowSizeClass(size)

        // Then
        assertEquals(WindowSizeClass.Expanded, result)
    }

    @Test
    fun `test calculateWindowSizeClass at boundary 600dp`() {
        // Given - 刚好小于 600dp
        val size1 = DpSize(599.dp, 800.dp)
        // Given - 刚好等于 600dp
        val size2 = DpSize(600.dp, 800.dp)

        // When
        val result1 = calculateWindowSizeClass(size1)
        val result2 = calculateWindowSizeClass(size2)

        // Then
        assertEquals(WindowSizeClass.Compact, result1)
        assertEquals(WindowSizeClass.Medium, result2)
    }

    @Test
    fun `test calculateWindowSizeClass at boundary 840dp`() {
        // Given - 刚好小于 840dp
        val size1 = DpSize(839.dp, 1000.dp)
        // Given - 刚好等于 840dp
        val size2 = DpSize(840.dp, 1000.dp)

        // When
        val result1 = calculateWindowSizeClass(size1)
        val result2 = calculateWindowSizeClass(size2)

        // Then
        assertEquals(WindowSizeClass.Medium, result1)
        assertEquals(WindowSizeClass.Expanded, result2)
    }

    @Test
    fun `test WindowSizeClass isCompact property`() {
        // Then
        assertTrue(WindowSizeClass.Compact.isCompact)
        assertFalse(WindowSizeClass.Medium.isCompact)
        assertFalse(WindowSizeClass.Expanded.isCompact)
    }

    @Test
    fun `test WindowSizeClass isMedium property`() {
        // Then
        assertFalse(WindowSizeClass.Compact.isMedium)
        assertTrue(WindowSizeClass.Medium.isMedium)
        assertFalse(WindowSizeClass.Expanded.isMedium)
    }

    @Test
    fun `test WindowSizeClass isExpanded property`() {
        // Then
        assertFalse(WindowSizeClass.Compact.isExpanded)
        assertFalse(WindowSizeClass.Medium.isExpanded)
        assertTrue(WindowSizeClass.Expanded.isExpanded)
    }

    @Test
    fun `test WindowSizeClass enum values`() {
        // Then
        assertEquals(3, WindowSizeClass.entries.size)
        assertEquals(WindowSizeClass.Compact, WindowSizeClass.entries[0])
        assertEquals(WindowSizeClass.Medium, WindowSizeClass.entries[1])
        assertEquals(WindowSizeClass.Expanded, WindowSizeClass.entries[2])
    }
}


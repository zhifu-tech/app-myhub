package tech.zhifu.app.myhub.datastore.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * SearchFilter 数据模型单元测试
 */
class SearchFilterTest {

    @Test
    fun `test SearchFilter creation with default values`() {
        // When
        val filter = SearchFilter()

        // Then
        assertNull(filter.query)
        assertTrue(filter.cardTypes.isEmpty())
        assertTrue(filter.tags.isEmpty())
        assertNull(filter.isFavorite)
        assertNull(filter.isTemplate)
        assertNull(filter.dateRange)
        assertEquals(SortBy.UPDATED_AT_DESC, filter.sortBy)
    }

    @Test
    fun `test SearchFilter creation with all fields`() {
        // Given
        val dateRange = DateRange(start = 1000L, end = 2000L)

        // When
        val filter = SearchFilter(
            query = "test query",
            cardTypes = listOf(CardType.QUOTE, CardType.CODE),
            tags = listOf("tag1", "tag2"),
            isFavorite = true,
            isTemplate = false,
            dateRange = dateRange,
            sortBy = SortBy.CREATED_AT_ASC
        )

        // Then
        assertEquals("test query", filter.query)
        assertEquals(2, filter.cardTypes.size)
        assertEquals(CardType.QUOTE, filter.cardTypes[0])
        assertEquals(CardType.CODE, filter.cardTypes[1])
        assertEquals(2, filter.tags.size)
        assertEquals(true, filter.isFavorite)
        assertEquals(false, filter.isTemplate)
        assertNotNull(filter.dateRange)
        assertEquals(1000L, filter.dateRange.start)
        assertEquals(2000L, filter.dateRange.end)
        assertEquals(SortBy.CREATED_AT_ASC, filter.sortBy)
    }

    @Test
    fun `test DateRange creation`() {
        // When
        val dateRange = DateRange(start = 1000L, end = 2000L)

        // Then
        assertEquals(1000L, dateRange.start)
        assertEquals(2000L, dateRange.end)
    }

    @Test
    fun `test DateRange with null values`() {
        // When
        val dateRange = DateRange()

        // Then
        assertNull(dateRange.start)
        assertNull(dateRange.end)
    }

    @Test
    fun `test SortBy enum values`() {
        // Then
        assertEquals(8, SortBy.entries.size)
        assertEquals(SortBy.CREATED_AT_ASC, SortBy.entries[0])
        assertEquals(SortBy.CREATED_AT_DESC, SortBy.entries[1])
        assertEquals(SortBy.UPDATED_AT_ASC, SortBy.entries[2])
        assertEquals(SortBy.UPDATED_AT_DESC, SortBy.entries[3])
        assertEquals(SortBy.TITLE_ASC, SortBy.entries[4])
        assertEquals(SortBy.TITLE_DESC, SortBy.entries[5])
        assertEquals(SortBy.LAST_REVIEWED_AT_ASC, SortBy.entries[6])
        assertEquals(SortBy.LAST_REVIEWED_AT_DESC, SortBy.entries[7])
    }
}


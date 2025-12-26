package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTagDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.Tag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * LocalTagDataSource 测试
 */
class LocalTagDataSourceTest {

    @Test
    fun `test insert and get tag`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTagDataSourceImpl(database)
        val tag = createTestTag("1", "tag1")

        // When
        dataSource.insertTag(tag)
        val result = dataSource.getTagById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("tag1", result.name)
    }

    @Test
    fun `test get all tags`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTagDataSourceImpl(database)
        val tag1 = createTestTag("1", "tag1")
        val tag2 = createTestTag("2", "tag2")
        val tag3 = createTestTag("3", "tag3")

        // When
        dataSource.insertTag(tag1)
        dataSource.insertTag(tag2)
        dataSource.insertTag(tag3)
        val result = dataSource.getAllTags()

        // Then
        assertEquals(3, result.size)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "2" })
        assertTrue(result.any { it.id == "3" })
    }

    @Test
    fun `test get tag by name`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTagDataSourceImpl(database)
        val tag = createTestTag("1", "unique-tag")

        // When
        dataSource.insertTag(tag)
        val result = dataSource.getTagByName("unique-tag")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("unique-tag", result.name)
    }

    @Test
    fun `test update tag`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTagDataSourceImpl(database)
        val tag = createTestTag("1", "tag1")
        dataSource.insertTag(tag)

        // When
        val updatedTag = tag.copy(name = "updated-tag", color = "#FF0000")
        dataSource.updateTag(updatedTag)
        val result = dataSource.getTagById("1")

        // Then
        assertNotNull(result)
        assertEquals("updated-tag", result.name)
        assertEquals("#FF0000", result.color)
    }

    @Test
    fun `test delete tag`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTagDataSourceImpl(database)
        val tag = createTestTag("1", "tag1")
        dataSource.insertTag(tag)

        // When
        dataSource.deleteTag("1")
        val result = dataSource.getTagById("1")

        // Then
        assertNull(result)
    }

    @Test
    fun `test observe tags flow`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTagDataSourceImpl(database)
        val tag1 = createTestTag("1", "tag1")
        val tag2 = createTestTag("2", "tag2")

        // When
        val flow = dataSource.observeTags()
        dataSource.insertTag(tag1)
        val firstResult = flow.first()

        dataSource.insertTag(tag2)
        val secondResult = flow.first()

        // Then
        assertTrue(firstResult.size >= 1)
        assertTrue(secondResult.size >= 2)
    }

    @Test
    fun `test tag with color and description`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTagDataSourceImpl(database)
        val tag = createTestTag("1", "tag1", color = "#FF5733", description = "Test description")

        // When
        dataSource.insertTag(tag)
        val result = dataSource.getTagById("1")

        // Then
        assertNotNull(result)
        assertEquals("#FF5733", result.color)
        assertEquals("Test description", result.description)
    }

    /**
     * 创建测试用的标签
     */
    private fun createTestTag(
        id: String,
        name: String,
        color: String? = null,
        description: String? = null,
        cardCount: Int = 0
    ): Tag {
        return Tag(
            id = id,
            name = name,
            color = color,
            description = description,
            cardCount = cardCount,
            createdAt = Clock.System.now()
        )
    }
}


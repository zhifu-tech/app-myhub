package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTagDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.Tag
import tech.zhifu.app.myhub.datastore.repository.impl.TagRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * TagRepository 测试（服务端）
 */
class TagRepositoryTest {

    @Test
    fun `test create tag`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTagDataSourceImpl(database)
        val repository = TagRepositoryImpl(localDataSource)
        val tag = createTestTag("1", "tag1")

        // When
        val createdTag = repository.createTag(tag)

        // Then
        assertNotNull(createdTag)
        assertEquals("1", createdTag.id)
        assertEquals("tag1", createdTag.name)
    }

    @Test
    fun `test create tag with duplicate name fails`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTagDataSourceImpl(database)
        val repository = TagRepositoryImpl(localDataSource)
        val tag1 = createTestTag("1", "tag1")
        val tag2 = createTestTag("2", "tag1") // 同名标签

        // When
        repository.createTag(tag1)

        // Then - 应该抛出异常
        var exceptionThrown = false
        try {
            repository.createTag(tag2)
        } catch (e: IllegalArgumentException) {
            exceptionThrown = true
            assertTrue(e.message?.contains("already exists") == true || e.message?.contains("tag1") == true)
        } catch (e: Exception) {
            // 也接受其他类型的异常（例如数据库约束违反）
            exceptionThrown = true
        }
        assertTrue(exceptionThrown, "Expected exception when creating tag with duplicate name")
    }

    @Test
    fun `test get all tags`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTagDataSourceImpl(database)
        val repository = TagRepositoryImpl(localDataSource)
        val tag1 = createTestTag("1", "tag1")
        val tag2 = createTestTag("2", "tag2")
        repository.createTag(tag1)
        repository.createTag(tag2)

        // When
        val result = repository.getAllTags()

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "2" })
    }

    @Test
    fun `test get tag by id`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTagDataSourceImpl(database)
        val repository = TagRepositoryImpl(localDataSource)
        val tag = createTestTag("1", "tag1")
        repository.createTag(tag)

        // When
        val result = repository.getTagById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("tag1", result.name)
    }

    @Test
    fun `test get tag by name`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTagDataSourceImpl(database)
        val repository = TagRepositoryImpl(localDataSource)
        val tag = createTestTag("1", "unique-tag")
        repository.createTag(tag)

        // When
        val result = repository.getTagByName("unique-tag")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("unique-tag", result.name)
    }

    @Test
    fun `test update tag`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTagDataSourceImpl(database)
        val repository = TagRepositoryImpl(localDataSource)
        val tag = createTestTag("1", "tag1")
        repository.createTag(tag)

        // When
        val updatedTag = tag.copy(name = "updated-tag", color = "#FF0000")
        val result = repository.updateTag(updatedTag)

        // Then
        assertNotNull(result)
        val retrievedTag = repository.getTagById("1")
        assertNotNull(retrievedTag)
        assertEquals("updated-tag", retrievedTag.name)
        assertEquals("#FF0000", retrievedTag.color)
    }

    @Test
    fun `test delete tag`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTagDataSourceImpl(database)
        val repository = TagRepositoryImpl(localDataSource)
        val tag = createTestTag("1", "tag1")
        repository.createTag(tag)

        // When
        val result = repository.deleteTag("1")
        val retrievedTag = repository.getTagById("1")

        // Then
        assertTrue(result)
        assertNull(retrievedTag)
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


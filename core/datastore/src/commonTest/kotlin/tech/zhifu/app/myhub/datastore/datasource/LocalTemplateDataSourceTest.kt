package tech.zhifu.app.myhub.datastore.datasource

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTemplateDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * LocalTemplateDataSource 测试
 */
class LocalTemplateDataSourceTest {

    @Test
    fun `test insert and get template`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTemplateDataSourceImpl(database)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)

        // When
        dataSource.insertTemplate(template)
        val result = dataSource.getTemplateById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("Template 1", result.name)
        assertEquals(CardType.QUOTE, result.cardType)
    }

    @Test
    fun `test get all templates`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTemplateDataSourceImpl(database)
        val template1 = createTestTemplate("1", "Template 1", CardType.QUOTE)
        val template2 = createTestTemplate("2", "Template 2", CardType.CODE)
        val template3 = createTestTemplate("3", "Template 3", CardType.IDEA)

        // When
        dataSource.insertTemplate(template1)
        dataSource.insertTemplate(template2)
        dataSource.insertTemplate(template3)
        val result = dataSource.getAllTemplates()

        // Then
        assertEquals(3, result.size)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "2" })
        assertTrue(result.any { it.id == "3" })
    }

    @Test
    fun `test update template`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTemplateDataSourceImpl(database)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)
        dataSource.insertTemplate(template)

        // When
        val updatedTemplate = template.copy(
            name = "Updated Template",
            description = "Updated description"
        )
        dataSource.updateTemplate(updatedTemplate)
        val result = dataSource.getTemplateById("1")

        // Then
        assertNotNull(result)
        assertEquals("Updated Template", result.name)
        assertEquals("Updated description", result.description)
    }

    @Test
    fun `test delete template`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTemplateDataSourceImpl(database)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)
        dataSource.insertTemplate(template)

        // When
        dataSource.deleteTemplate("1")
        val result = dataSource.getTemplateById("1")

        // Then
        assertNull(result)
    }

    @Test
    fun `test observe templates flow`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTemplateDataSourceImpl(database)
        val template1 = createTestTemplate("1", "Template 1", CardType.QUOTE)
        val template2 = createTestTemplate("2", "Template 2", CardType.CODE)

        // When
        val flow = dataSource.observeTemplates()
        dataSource.insertTemplate(template1)
        val firstResult = flow.first()

        dataSource.insertTemplate(template2)
        val secondResult = flow.first()

        // Then
        assertTrue(firstResult.size >= 1)
        assertTrue(secondResult.size >= 2)
    }

    @Test
    fun `test template with default tags`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTemplateDataSourceImpl(database)
        val template = createTestTemplate(
            "1",
            "Template 1",
            CardType.QUOTE,
            defaultTags = listOf("tag1", "tag2", "tag3")
        )

        // When
        dataSource.insertTemplate(template)
        val result = dataSource.getTemplateById("1")

        // Then
        assertNotNull(result)
        assertEquals(3, result.defaultTags.size)
        assertTrue(result.defaultTags.contains("tag1"))
        assertTrue(result.defaultTags.contains("tag2"))
        assertTrue(result.defaultTags.contains("tag3"))
    }

    @Test
    fun `test system template vs user template`() = runDatabaseTest { database ->
        // Given
        val dataSource = LocalTemplateDataSourceImpl(database)
        val systemTemplate = createTestTemplate("1", "System Template", CardType.QUOTE, isSystemTemplate = true)
        val userTemplate = createTestTemplate("2", "User Template", CardType.CODE, isSystemTemplate = false)

        // When
        dataSource.insertTemplate(systemTemplate)
        dataSource.insertTemplate(userTemplate)
        val systemResult = dataSource.getTemplateById("1")
        val userResult = dataSource.getTemplateById("2")

        // Then
        assertNotNull(systemResult)
        assertTrue(systemResult.isSystemTemplate)
        assertNotNull(userResult)
        assertTrue(!userResult.isSystemTemplate)
    }

    /**
     * 创建测试用的模板
     */
    private fun createTestTemplate(
        id: String,
        name: String,
        cardType: CardType,
        description: String? = null,
        defaultContent: String? = null,
        defaultTags: List<String> = emptyList(),
        isSystemTemplate: Boolean = false
    ): Template {
        val now = Clock.System.now()
        return Template(
            id = id,
            name = name,
            description = description,
            cardType = cardType,
            previewImageUrl = null,
            defaultContent = defaultContent,
            defaultMetadata = null,
            defaultTags = defaultTags,
            usageCount = 0,
            isSystemTemplate = isSystemTemplate,
            createdAt = now,
            updatedAt = now
        )
    }
}


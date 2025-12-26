package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTemplateDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.repository.impl.TemplateRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * TemplateRepository 测试（服务端）
 */
class TemplateRepositoryTest {

    @Test
    fun `test create template`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val repository = TemplateRepositoryImpl(localDataSource)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)

        // When
        val createdTemplate = repository.createTemplate(template)

        // Then
        assertNotNull(createdTemplate)
        assertEquals("1", createdTemplate.id)
        assertEquals("Template 1", createdTemplate.name)
        assertEquals(CardType.QUOTE, createdTemplate.cardType)
    }

    @Test
    fun `test get all templates`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val repository = TemplateRepositoryImpl(localDataSource)
        val template1 = createTestTemplate("1", "Template 1", CardType.QUOTE)
        val template2 = createTestTemplate("2", "Template 2", CardType.CODE)
        repository.createTemplate(template1)
        repository.createTemplate(template2)

        // When
        val result = repository.getAllTemplates()

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "2" })
    }

    @Test
    fun `test get template by id`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val repository = TemplateRepositoryImpl(localDataSource)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)
        repository.createTemplate(template)

        // When
        val result = repository.getTemplateById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("Template 1", result.name)
        assertEquals(CardType.QUOTE, result.cardType)
    }

    @Test
    fun `test update template`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val repository = TemplateRepositoryImpl(localDataSource)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)
        repository.createTemplate(template)

        // When
        val updatedTemplate = template.copy(
            name = "Updated Template",
            description = "Updated description"
        )
        val result = repository.updateTemplate(updatedTemplate)

        // Then
        assertNotNull(result)
        val retrievedTemplate = repository.getTemplateById("1")
        assertNotNull(retrievedTemplate)
        assertEquals("Updated Template", retrievedTemplate.name)
        assertEquals("Updated description", retrievedTemplate.description)
    }

    @Test
    fun `test delete template`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val repository = TemplateRepositoryImpl(localDataSource)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)
        repository.createTemplate(template)

        // When
        val result = repository.deleteTemplate("1")
        val retrievedTemplate = repository.getTemplateById("1")

        // Then
        assertTrue(result)
        assertNull(retrievedTemplate)
    }

    @Test
    fun `test template with default tags`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val repository = TemplateRepositoryImpl(localDataSource)
        val template = createTestTemplate(
            "1",
            "Template 1",
            CardType.QUOTE,
            defaultTags = listOf("tag1", "tag2")
        )

        // When
        repository.createTemplate(template)
        val result = repository.getTemplateById("1")

        // Then
        assertNotNull(result)
        assertEquals(2, result.defaultTags.size)
        assertTrue(result.defaultTags.contains("tag1"))
        assertTrue(result.defaultTags.contains("tag2"))
    }

    @Test
    fun `test template with system and user templates`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val repository = TemplateRepositoryImpl(localDataSource)
        val systemTemplate = createTestTemplate("1", "System Template", CardType.QUOTE, isSystemTemplate = true)
        val userTemplate = createTestTemplate("2", "User Template", CardType.CODE, isSystemTemplate = false)
        repository.createTemplate(systemTemplate)
        repository.createTemplate(userTemplate)

        // When
        val allTemplates = repository.getAllTemplates()
        val systemTemplates = allTemplates.filter { it.isSystemTemplate }
        val userTemplates = allTemplates.filter { !it.isSystemTemplate }

        // Then
        assertTrue(systemTemplates.isNotEmpty())
        assertTrue(userTemplates.isNotEmpty())
        assertTrue(systemTemplates.all { it.isSystemTemplate })
        assertTrue(userTemplates.all { !it.isSystemTemplate })
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


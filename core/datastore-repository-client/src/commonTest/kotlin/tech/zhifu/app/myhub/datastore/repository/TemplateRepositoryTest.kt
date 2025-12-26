package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalTemplateDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteTemplateDataSourceStub
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
 * TemplateRepository 测试
 */
class TemplateRepositoryTest {

    @Test
    fun `test create template`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)

        // When
        val createdTemplate = repository.createTemplate(template)

        // Then
        assertNotNull(createdTemplate)
        assertEquals("1", createdTemplate.id)
        assertEquals("Template 1", createdTemplate.name)
    }

    @Test
    fun `test get all templates`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
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
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)
        repository.createTemplate(template)

        // When
        val result = repository.getTemplateById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("Template 1", result.name)
    }

    @Test
    fun `test get templates by type`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
        val template1 = createTestTemplate("1", "Template 1", CardType.QUOTE)
        val template2 = createTestTemplate("2", "Template 2", CardType.CODE)
        val template3 = createTestTemplate("3", "Template 3", CardType.QUOTE)
        repository.createTemplate(template1)
        repository.createTemplate(template2)
        repository.createTemplate(template3)

        // When
        val flow = repository.observeTemplatesByType(CardType.QUOTE)
        val result = flow.first()

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.all { it.cardType == CardType.QUOTE })
    }

    @Test
    fun `test get system templates`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
        val systemTemplate = createTestTemplate("1", "System Template", CardType.QUOTE, isSystemTemplate = true)
        val userTemplate = createTestTemplate("2", "User Template", CardType.CODE, isSystemTemplate = false)
        repository.createTemplate(systemTemplate)
        repository.createTemplate(userTemplate)

        // When - getSystemTemplates 方法已不存在，使用 observeAllTemplates 代替
        val flow = repository.observeAllTemplates()
        val result = flow.first().filter { it.isSystemTemplate }

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.isSystemTemplate })
    }

    @Test
    fun `test get user templates`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
        val systemTemplate = createTestTemplate("1", "System Template", CardType.QUOTE, isSystemTemplate = true)
        val userTemplate = createTestTemplate("2", "User Template", CardType.CODE, isSystemTemplate = false)
        repository.createTemplate(systemTemplate)
        repository.createTemplate(userTemplate)

        // When - getUserTemplates 方法已不存在，使用 observeAllTemplates 代替
        val flow = repository.observeAllTemplates()
        val result = flow.first().filter { !it.isSystemTemplate }

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { !it.isSystemTemplate })
    }

    @Test
    fun `test update template`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
        val template = createTestTemplate("1", "Template 1", CardType.QUOTE)
        repository.createTemplate(template)

        // When
        val updatedTemplate = template.copy(name = "Updated Template")
        val result = repository.updateTemplate(updatedTemplate)

        // Then
        assertNotNull(result)
        val retrievedTemplate = repository.getTemplateById("1")
        assertNotNull(retrievedTemplate)
        assertEquals("Updated Template", retrievedTemplate.name)
    }

    @Test
    fun `test delete template`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
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
    fun `test create card from template`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalTemplateDataSourceImpl(database)
        val remoteDataSource = RemoteTemplateDataSourceStub()
        val repository = TemplateRepositoryImpl(localDataSource, remoteDataSource)
        val template = createTestTemplate(
            "1",
            "Template 1",
            CardType.QUOTE,
            defaultContent = "Default content",
            defaultTags = listOf("tag1", "tag2")
        )
        repository.createTemplate(template)

        // When
        val card = repository.createCardFromTemplate("1")

        // Then
        assertNotNull(card)
        assertEquals(CardType.QUOTE, card.type)
        assertEquals("Default content", card.content)
        assertEquals(2, card.tags.size)
        // 验证模板使用计数增加
        val updatedTemplate = repository.getTemplateById("1")
        assertNotNull(updatedTemplate)
        assertEquals(1, updatedTemplate.usageCount)
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


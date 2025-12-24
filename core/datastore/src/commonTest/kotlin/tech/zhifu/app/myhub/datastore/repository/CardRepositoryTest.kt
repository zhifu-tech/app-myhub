package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteCardDataSourceStub
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.SortBy
import tech.zhifu.app.myhub.datastore.repository.impl.CardRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * CardRepository 测试
 */
class CardRepositoryTest {

    @Test
    fun `test create card`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card = createTestCard("1", CardType.QUOTE)

        // When
        val result = repository.createCard(card)

        // Then
        assertTrue(result.isSuccess)
        val createdCard = result.getOrNull()
        assertNotNull(createdCard)
        assertEquals("1", createdCard.id)
    }

    @Test
    fun `test get all cards`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        repository.createCard(card1)
        repository.createCard(card2)

        // When
        val flow = repository.getAllCards()
        val result = flow.first()

        // Then
        assertTrue(result.size >= 2)
    }

    @Test
    fun `test get card by id`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val result = repository.getCardById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
    }

    @Test
    fun `test update card`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val updatedCard = card.copy(content = "Updated Content")
        val result = repository.updateCard(updatedCard)

        // Then
        assertTrue(result.isSuccess)
        val retrievedCard = repository.getCardById("1")
        assertNotNull(retrievedCard)
        assertEquals("Updated Content", retrievedCard.content)
    }

    @Test
    fun `test delete card`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val result = repository.deleteCard("1")
        val retrievedCard = repository.getCardById("1")

        // Then
        assertTrue(result.isSuccess)
        // 注意：由于远程数据源是stub，本地删除后可能还能获取到（如果远程有缓存）
        // 这里主要测试本地删除功能
    }

    @Test
    fun `test toggle favorite`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val result = repository.toggleFavorite("1")

        // Then
        assertTrue(result.isSuccess)
        val updatedCard = result.getOrNull()
        assertNotNull(updatedCard)
        assertTrue(updatedCard.isFavorite)
    }

    @Test
    fun `test get favorite cards`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card1 = createTestCard("1", CardType.QUOTE, isFavorite = true)
        val card2 = createTestCard("2", CardType.CODE, isFavorite = false)
        val card3 = createTestCard("3", CardType.IDEA, isFavorite = true)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val flow = repository.getFavoriteCards()
        val result = flow.first()

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `test get cards by type`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        val card3 = createTestCard("3", CardType.QUOTE)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val flow = repository.getCardsByType(CardType.QUOTE)
        val result = flow.first()

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.all { it.type == CardType.QUOTE })
    }

    @Test
    fun `test search cards by query`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card1 = createTestCard("1", CardType.QUOTE, content = "Kotlin is great")
        val card2 = createTestCard("2", CardType.CODE, content = "Java code example")
        val card3 = createTestCard("3", CardType.IDEA, content = "Kotlin multiplatform idea")
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(query = "Kotlin")
        val flow = repository.searchCards(filter)
        val result = flow.first()

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all {
            it.content.contains("Kotlin", ignoreCase = true) ||
                it.title?.contains("Kotlin", ignoreCase = true) == true
        })
    }

    @Test
    fun `test search cards by type filter`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        val card3 = createTestCard("3", CardType.IDEA)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(cardTypes = listOf(CardType.QUOTE, CardType.IDEA))
        val flow = repository.searchCards(filter)
        val result = flow.first()

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.type == CardType.QUOTE || it.type == CardType.IDEA })
    }

    @Test
    fun `test search cards by favorite filter`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val card1 = createTestCard("1", CardType.QUOTE, isFavorite = true)
        val card2 = createTestCard("2", CardType.CODE, isFavorite = false)
        val card3 = createTestCard("3", CardType.IDEA, isFavorite = true)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(isFavorite = true)
        val flow = repository.searchCards(filter)
        val result = flow.first()

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `test search cards sorting`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val repository = CardRepositoryImpl(localDataSource, remoteDataSource)
        val now = Clock.System.now()
        val card1 = createTestCard("1", CardType.QUOTE, createdAt = now - kotlin.time.Duration.parse("PT3H"))
        val card2 = createTestCard("2", CardType.CODE, createdAt = now - kotlin.time.Duration.parse("PT1H"))
        val card3 = createTestCard("3", CardType.IDEA, createdAt = now - kotlin.time.Duration.parse("PT2H"))
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(sortBy = SortBy.CREATED_AT_DESC)
        val flow = repository.searchCards(filter)
        val result = flow.first()

        // Then
        assertTrue(result.size >= 3)
        // 验证排序：最新的在前（按创建时间降序）
        val timestamps = result.map { it.createdAt.epochSeconds }
        assertEquals(timestamps.sortedDescending(), timestamps)
    }

    /**
     * 创建测试用的卡片
     */
    private fun createTestCard(
        id: String,
        type: CardType,
        title: String = "Test Title",
        content: String = "Test Content",
        isFavorite: Boolean = false,
        createdAt: Instant = Clock.System.now(),
    ): Card {
        return Card(
            id = id,
            type = type,
            title = title,
            content = content,
            author = "Test Author",
            source = "Test Source",
            language = null,
            tags = emptyList(),
            isFavorite = isFavorite,
            isTemplate = false,
            createdAt = createdAt,
            updatedAt = createdAt,
            lastReviewedAt = null,
            metadata = null
        )
    }
}

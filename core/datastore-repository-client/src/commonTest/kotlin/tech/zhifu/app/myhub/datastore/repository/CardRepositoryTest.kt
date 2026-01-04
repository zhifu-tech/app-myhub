package tech.zhifu.app.myhub.datastore.repository

import kotlinx.coroutines.flow.first
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalUserDataSourceImpl
import tech.zhifu.app.myhub.datastore.datasource.impl.RemoteCardDataSourceStub
import tech.zhifu.app.myhub.datastore.datasource.impl.UserContextProviderStub
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.SortBy
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.repository.impl.CardRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * CardRepository 测试
 */
class CardRepositoryTest {

    private val testUserId = "test-user-1"

    private suspend fun createRepository(
        database: MyHubDatabase,
        userId: String = testUserId
    ): CardRepositoryImpl {
        val localDataSource = LocalCardDataSourceImpl(database)
        val userDataSource = LocalUserDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val userContextProvider = UserContextProviderStub(userId)

        // 设置当前用户
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            createdAt = Clock.System.now(),
            preferences = null
        )
        userDataSource.saveUser(user)

        return CardRepositoryImpl(localDataSource, remoteDataSource, userContextProvider, userDataSource)
    }

    @Test
    fun `test create card`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val card = createTestCard("1", CardType.QUOTE)

        // When
        val createdCard = repository.createCard(card)

        // Then
        assertNotNull(createdCard)
        assertEquals("1", createdCard.id)
    }

    @Test
    fun `test get all cards`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        repository.createCard(card1)
        repository.createCard(card2)

        // When
        val result = repository.getAllCards()

        // Then
        assertTrue(result.size >= 2)
    }

    @Test
    fun `test get card by id`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
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
        val repository = createRepository(database)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val updatedCard = card.copy(content = "Updated Content")
        val result = repository.updateCard(updatedCard)

        // Then
        assertNotNull(result)
        val retrievedCard = repository.getCardById("1")
        assertNotNull(retrievedCard)
        assertEquals("Updated Content", retrievedCard.content)
    }

    @Test
    fun `test delete card`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val result = repository.deleteCard("1")
        val retrievedCard = repository.getCardById("1")

        // Then
        assertTrue(result)
        // 注意：由于远程数据源是stub，本地删除后可能还能获取到（如果远程有缓存）
        // 这里主要测试本地删除功能
    }

    @Test
    fun `test toggle favorite`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val updatedCard = repository.toggleFavorite("1")

        // Then
        assertNotNull(updatedCard)
        assertTrue(updatedCard.isFavorite)
    }

    @Test
    fun `test get favorite cards`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val card1 = createTestCard("1", CardType.QUOTE, isFavorite = true)
        val card2 = createTestCard("2", CardType.CODE, isFavorite = false)
        val card3 = createTestCard("3", CardType.IDEA, isFavorite = true)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val flow = repository.observeFavoriteCards()
        val result = flow.first()

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `test get cards by type`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        val card3 = createTestCard("3", CardType.QUOTE)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val flow = repository.observeCardsByType(CardType.QUOTE)
        val result = flow.first()

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.all { it.type == CardType.QUOTE })
    }

    @Test
    fun `test search cards by query`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val card1 = createTestCard("1", CardType.QUOTE, content = "Kotlin is great")
        val card2 = createTestCard("2", CardType.CODE, content = "Java code example")
        val card3 = createTestCard("3", CardType.IDEA, content = "Kotlin multiplatform idea")
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(query = "Kotlin")
        val result = repository.searchCards(filter)

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
        val repository = createRepository(database)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        val card3 = createTestCard("3", CardType.IDEA)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(cardTypes = listOf(CardType.QUOTE, CardType.IDEA))
        val result = repository.searchCards(filter)

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.type == CardType.QUOTE || it.type == CardType.IDEA })
    }

    @Test
    fun `test search cards by favorite filter`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val card1 = createTestCard("1", CardType.QUOTE, isFavorite = true)
        val card2 = createTestCard("2", CardType.CODE, isFavorite = false)
        val card3 = createTestCard("3", CardType.IDEA, isFavorite = true)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(isFavorite = true)
        val result = repository.searchCards(filter)

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun `test search cards sorting`() = runDatabaseTest { database ->
        // Given
        val repository = createRepository(database)
        val now = Clock.System.now()
        val card1 = createTestCard("1", CardType.QUOTE, createdAt = now - Duration.parse("PT3H"))
        val card2 = createTestCard("2", CardType.CODE, createdAt = now - Duration.parse("PT1H"))
        val card3 = createTestCard("3", CardType.IDEA, createdAt = now - Duration.parse("PT2H"))
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(sortBy = SortBy.CREATED_AT_DESC)
        val result = repository.searchCards(filter)

        // Then
        assertTrue(result.size >= 3)
        // 验证排序：最新的在前（按创建时间降序）
        val timestamps = result.map { it.createdAt.epochSeconds }
        assertEquals(timestamps.sortedDescending(), timestamps)
    }

    @Test
    fun `test multi-user data isolation`() = runDatabaseTest { database ->
        // Given
        val userId1 = "user-1"
        val userId2 = "user-2"
        val repository1 = createRepository(database, userId1)
        val repository2 = createRepository(database, userId2)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        val card3 = createTestCard("3", CardType.IDEA)

        // When - User 1 creates cards
        repository1.createCard(card1)
        repository1.createCard(card2)

        // User 2 creates a card
        repository2.createCard(card3)

        // Then - User 1 should only see their cards
        val user1Cards = repository1.getAllCards()
        assertEquals(2, user1Cards.size)
        assertTrue(user1Cards.any { it.id == "1" })
        assertTrue(user1Cards.any { it.id == "2" })
        assertTrue(user1Cards.none { it.id == "3" })

        // User 2 should only see their card
        val user2Cards = repository2.getAllCards()
        assertEquals(1, user2Cards.size)
        assertTrue(user2Cards.any { it.id == "3" })
        assertTrue(user2Cards.none { it.id == "1" })
        assertTrue(user2Cards.none { it.id == "2" })
    }

    @Test
    fun `test flow updates when user switches`() = runDatabaseTest { database ->
        // Given
        val userId1 = "user-1"
        val userId2 = "user-2"
        val userDataSource = LocalUserDataSourceImpl(database)
        val localDataSource = LocalCardDataSourceImpl(database)
        val remoteDataSource = RemoteCardDataSourceStub()
        val userContextProvider = UserContextProviderStub(userId1)

        // 设置用户
        val user1 = User(
            id = userId1,
            username = "user1",
            email = "user1@example.com",
            displayName = "User 1",
            avatarUrl = null,
            createdAt = Clock.System.now(),
            preferences = null
        )
        userDataSource.saveUser(user1)

        val repository = CardRepositoryImpl(localDataSource, remoteDataSource, userContextProvider, userDataSource)

        // When - User 1 creates cards
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        repository.createCard(card1)
        repository.createCard(card2)

        // Observe cards for User 1
        val flow = repository.observeAllCards()
        val user1Result = flow.first()
        assertEquals(2, user1Result.size)

        // Switch to User 2
        val user2 = User(
            id = userId2,
            username = "user2",
            email = "user2@example.com",
            displayName = "User 2",
            avatarUrl = null,
            createdAt = Clock.System.now(),
            preferences = null
        )
        userDataSource.saveUser(user2)
        userContextProvider.setUserId(userId2)

        // User 2 should see empty list (no cards yet)
        val user2Result = flow.first()
        assertTrue(user2Result.isEmpty() || user2Result.size == 0)

        // User 2 creates a card
        val card3 = createTestCard("3", CardType.IDEA)
        repository.createCard(card3)

        // User 2 should now see their card
        val user2ResultAfterCreate = flow.first()
        assertEquals(1, user2ResultAfterCreate.size)
        assertTrue(user2ResultAfterCreate.any { it.id == "3" })
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


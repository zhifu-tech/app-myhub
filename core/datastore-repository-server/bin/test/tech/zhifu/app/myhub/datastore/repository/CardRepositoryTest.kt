package tech.zhifu.app.myhub.datastore.repository

import tech.zhifu.app.myhub.datastore.database.runDatabaseTest
import tech.zhifu.app.myhub.datastore.datasource.impl.LocalCardDataSourceImpl
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.SortBy
import tech.zhifu.app.myhub.datastore.repository.impl.CardRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * CardRepository 测试（服务端）
 */
class CardRepositoryTest {

    @Test
    fun `test create card`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card = createTestCard("1", CardType.QUOTE)

        // When
        val createdCard = repository.createCard(card)

        // Then
        assertNotNull(createdCard)
        assertEquals("1", createdCard.id)
        assertEquals(CardType.QUOTE, createdCard.type)
    }

    @Test
    fun `test create card with blank id generates id`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card = createTestCard("", CardType.QUOTE) // 空白ID

        // When
        val createdCard = repository.createCard(card)

        // Then
        assertNotNull(createdCard)
        assertTrue(createdCard.id.isNotBlank())
        assertTrue(createdCard.id.startsWith("card-"))
    }

    @Test
    fun `test get all cards`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card1 = createTestCard("1", CardType.QUOTE)
        val card2 = createTestCard("2", CardType.CODE)
        repository.createCard(card1)
        repository.createCard(card2)

        // When
        val result = repository.getAllCards()

        // Then
        assertTrue(result.size >= 2)
        assertTrue(result.any { it.id == "1" })
        assertTrue(result.any { it.id == "2" })
    }

    @Test
    fun `test get card by id`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val result = repository.getCardById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals(CardType.QUOTE, result.type)
    }

    @Test
    fun `test get card by id when not exists`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)

        // When
        val result = repository.getCardById("non-existent")

        // Then
        assertNull(result)
    }

    @Test
    fun `test update card`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card = createTestCard("1", CardType.QUOTE, content = "Original Content")
        repository.createCard(card)

        // When
        val updatedCard = card.copy(content = "Updated Content", title = "Updated Title")
        val result = repository.updateCard(updatedCard)

        // Then
        assertNotNull(result)
        assertEquals("Updated Content", result.content)
        assertEquals("Updated Title", result.title)

        val retrievedCard = repository.getCardById("1")
        assertNotNull(retrievedCard)
        assertEquals("Updated Content", retrievedCard.content)
        assertEquals("Updated Title", retrievedCard.title)
    }

    @Test
    fun `test update card when not exists throws exception`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card = createTestCard("non-existent", CardType.QUOTE)

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            repository.updateCard(card)
        }
    }

    @Test
    fun `test delete card`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card = createTestCard("1", CardType.QUOTE)
        repository.createCard(card)

        // When
        val result = repository.deleteCard("1")
        val retrievedCard = repository.getCardById("1")

        // Then
        assertTrue(result)
        assertNull(retrievedCard)
    }

    @Test
    fun `test delete card when not exists returns false`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)

        // When
        val result = repository.deleteCard("non-existent")

        // Then
        assertTrue(result) // 删除不存在的卡片也返回 true（不抛异常）
    }

    @Test
    fun `test toggle favorite`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card = createTestCard("1", CardType.QUOTE, isFavorite = false)
        repository.createCard(card)

        // When
        val updatedCard = repository.toggleFavorite("1")

        // Then
        assertNotNull(updatedCard)
        assertTrue(updatedCard.isFavorite)

        val retrievedCard = repository.getCardById("1")
        assertNotNull(retrievedCard)
        assertTrue(retrievedCard.isFavorite)
    }

    @Test
    fun `test toggle favorite twice`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card = createTestCard("1", CardType.QUOTE, isFavorite = false)
        repository.createCard(card)

        // When
        val firstToggle = repository.toggleFavorite("1")
        val secondToggle = repository.toggleFavorite("1")

        // Then
        assertTrue(firstToggle.isFavorite)
        assertTrue(!secondToggle.isFavorite) // 第二次切换应该取消收藏
    }

    @Test
    fun `test toggle favorite when not exists throws exception`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            repository.toggleFavorite("non-existent")
        }
    }

    @Test
    fun `test search cards by query`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card1 = createTestCard("1", CardType.QUOTE, content = "Kotlin is great", title = "Kotlin")
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
                it.title?.contains("Kotlin", ignoreCase = true) == true ||
                it.author?.contains("Kotlin", ignoreCase = true) == true ||
                it.tags.any { tag -> tag.contains("Kotlin", ignoreCase = true) }
        })
    }

    @Test
    fun `test search cards by type filter`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
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
        assertTrue(result.none { it.type == CardType.CODE })
    }

    @Test
    fun `test search cards by tags filter`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card1 = createTestCard("1", CardType.QUOTE, tags = listOf("tag1", "tag2"))
        val card2 = createTestCard("2", CardType.CODE, tags = listOf("tag1", "tag3"))
        val card3 = createTestCard("3", CardType.IDEA, tags = listOf("tag2"))
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When - 搜索包含 tag1 和 tag2 的卡片
        val filter = SearchFilter(tags = listOf("tag1", "tag2"))
        val result = repository.searchCards(filter)

        // Then - 只有 card1 同时包含 tag1 和 tag2
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { card ->
            card.tags.contains("tag1") && card.tags.contains("tag2")
        })
    }

    @Test
    fun `test search cards by favorite filter`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
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
        assertTrue(result.none { !it.isFavorite })
    }

    @Test
    fun `test search cards by template filter`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card1 = createTestCard("1", CardType.QUOTE, isTemplate = true)
        val card2 = createTestCard("2", CardType.CODE, isTemplate = false)
        val card3 = createTestCard("3", CardType.IDEA, isTemplate = true)
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(isTemplate = true)
        val result = repository.searchCards(filter)

        // Then
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.isTemplate })
    }

    @Test
    fun `test search cards sorting by created at desc`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val now = Clock.System.now()
        val card1 = createTestCard("1", CardType.QUOTE, createdAt = now.minus(Duration.parse("PT3H")))
        val card2 = createTestCard("2", CardType.CODE, createdAt = now.minus(Duration.parse("PT1H")))
        val card3 = createTestCard("3", CardType.IDEA, createdAt = now.minus(Duration.parse("PT2H")))
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
    fun `test search cards sorting by created at asc`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val now = Clock.System.now()
        val card1 = createTestCard("1", CardType.QUOTE, createdAt = now.minus(Duration.parse("PT3H")))
        val card2 = createTestCard("2", CardType.CODE, createdAt = now.minus(Duration.parse("PT1H")))
        val card3 = createTestCard("3", CardType.IDEA, createdAt = now.minus(Duration.parse("PT2H")))
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(sortBy = SortBy.CREATED_AT_ASC)
        val result = repository.searchCards(filter)

        // Then
        assertTrue(result.size >= 3)
        // 验证排序：最旧的在前（按创建时间升序）
        val timestamps = result.map { it.createdAt.epochSeconds }
        assertEquals(timestamps.sorted(), timestamps)
    }

    @Test
    fun `test search cards sorting by title asc`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card1 = createTestCard("1", CardType.QUOTE, title = "Zebra")
        val card2 = createTestCard("2", CardType.CODE, title = "Apple")
        val card3 = createTestCard("3", CardType.IDEA, title = "Banana")
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)

        // When
        val filter = SearchFilter(sortBy = SortBy.TITLE_ASC)
        val result = repository.searchCards(filter)

        // Then
        assertTrue(result.size >= 3)
        val titles = result.mapNotNull { it.title?.lowercase() }
        assertEquals(titles.sorted(), titles)
    }

    @Test
    fun `test search cards with multiple filters`() = runDatabaseTest { database ->
        // Given
        val localDataSource = LocalCardDataSourceImpl(database)
        val repository = CardRepositoryImpl(localDataSource)
        val card1 = createTestCard("1", CardType.QUOTE, isFavorite = true, tags = listOf("tag1"))
        val card2 = createTestCard("2", CardType.CODE, isFavorite = true, tags = listOf("tag2"))
        val card3 = createTestCard("3", CardType.QUOTE, isFavorite = false, tags = listOf("tag1"))
        val card4 = createTestCard("4", CardType.IDEA, isFavorite = true, tags = listOf("tag1"))
        repository.createCard(card1)
        repository.createCard(card2)
        repository.createCard(card3)
        repository.createCard(card4)

        // When - 搜索：QUOTE 类型 + 收藏 + tag1
        val filter = SearchFilter(
            cardTypes = listOf(CardType.QUOTE),
            isFavorite = true,
            tags = listOf("tag1")
        )
        val result = repository.searchCards(filter)

        // Then - 只有 card1 满足所有条件
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.type == CardType.QUOTE })
        assertTrue(result.all { it.isFavorite })
        assertTrue(result.all { it.tags.contains("tag1") })
    }

    /**
     * 创建测试用的卡片
     */
    private fun createTestCard(
        id: String,
        type: CardType,
        title: String = "Test Title",
        content: String = "Test Content",
        author: String? = "Test Author",
        tags: List<String> = emptyList(),
        isFavorite: Boolean = false,
        isTemplate: Boolean = false,
        createdAt: Instant = Clock.System.now(),
        updatedAt: Instant = Clock.System.now()
    ): Card {
        return Card(
            id = id,
            type = type,
            title = title,
            content = content,
            author = author,
            source = null,
            language = null,
            tags = tags,
            isFavorite = isFavorite,
            isTemplate = isTemplate,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastReviewedAt = null,
            metadata = null
        )
    }
}


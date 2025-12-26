package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.TestUtils.createMockHttpClient
import tech.zhifu.app.myhub.datastore.model.CardDto
import tech.zhifu.app.myhub.datastore.model.CreateCardRequest
import tech.zhifu.app.myhub.datastore.model.UpdateCardRequest
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.SearchFilter
import tech.zhifu.app.myhub.datastore.model.SortBy
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

/**
 * RemoteCardDataSource 测试
 */
class RemoteCardDataSourceTest {

    private fun createTestCardDto(id: String = "1", type: CardType = CardType.QUOTE): CardDto {
        val now = Clock.System.now().toString()
        return CardDto(
            id = id,
            type = type.name.lowercase(),
            title = "Test Card $id",
            content = "Test Content $id",
            author = "Test Author",
            source = null,
            language = "en",
            tags = emptyList(),
            isFavorite = false,
            isTemplate = false,
            createdAt = now,
            updatedAt = now,
            lastReviewedAt = null,
            metadata = null
        )
    }

    @Test
    fun `test getAllCards success`() = runTest {
        // Given
        val expectedCards = listOf(
            createTestCardDto("1", CardType.QUOTE),
            createTestCardDto("2", CardType.CODE)
        )
        val httpClient = createMockHttpClient { request ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}", request.url.toString())
            respond(
                content = Json.encodeToString(expectedCards),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When
        val result = dataSource.getAllCards()

        // Then
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }

    @Test
    fun `test getAllCards api error`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.getAllCards()
        }
    }

    @Test
    fun `test getCardById success`() = runTest {
        // Given
        val expectedCard = createTestCardDto("1", CardType.QUOTE)
        val httpClient = createMockHttpClient { request ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/1", request.url.toString())
            respond(
                content = Json.encodeToString(expectedCard),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When
        val result = dataSource.getCardById("1")

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("Test Card 1", result.title)
    }

    @Test
    fun `test getCardById not found`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When
        val result = dataSource.getCardById("999")

        // Then
        assertNull(result)
    }

    @Test
    fun `test createCard success`() = runTest {
        // Given
        val request = CreateCardRequest(
            type = "quote",
            title = "New Card",
            content = "New Content",
            author = "Author",
            tags = emptyList()
        )
        val expectedCard = createTestCardDto("1", CardType.QUOTE).copy(
            title = "New Card",
            content = "New Content"
        )
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}", requestData.url.toString())
            respond(
                content = Json.encodeToString(expectedCard),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When
        val result = dataSource.createCard(request)

        // Then
        assertNotNull(result)
        assertEquals("New Card", result.title)
        assertEquals("New Content", result.content)
    }

    @Test
    fun `test updateCard success`() = runTest {
        // Given
        val request = UpdateCardRequest(
            title = "Updated Title",
            content = "Updated Content"
        )
        val expectedCard = createTestCardDto("1", CardType.QUOTE).copy(
            title = "Updated Title",
            content = "Updated Content"
        )
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/1", requestData.url.toString())
            respond(
                content = Json.encodeToString(expectedCard),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When
        val result = dataSource.updateCard("1", request)

        // Then
        assertNotNull(result)
        assertEquals("Updated Title", result.title)
        assertEquals("Updated Content", result.content)
    }

    @Test
    fun `test updateCard not found`() = runTest {
        // Given
        val request = UpdateCardRequest(title = "Updated Title")
        val httpClient = createMockHttpClient { requestData ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.updateCard("999", request)
        }
    }

    @Test
    fun `test deleteCard success`() = runTest {
        // Given
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/1", requestData.url.toString())
            respond(
                content = "",
                status = HttpStatusCode.NoContent
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When & Then (should not throw)
        dataSource.deleteCard("1")
    }

    @Test
    fun `test deleteCard not found`() = runTest {
        // Given
        val httpClient = createMockHttpClient { requestData ->
            respond(
                content = "",
                status = HttpStatusCode.NotFound
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When & Then (should not throw, NotFound is treated as success)
        dataSource.deleteCard("999")
    }

    @Test
    fun `test toggleFavorite success`() = runTest {
        // Given
        val expectedCard = createTestCardDto("1", CardType.QUOTE).copy(isFavorite = true)
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.CARDS_PATH}/1/favorite", requestData.url.toString())
            respond(
                content = Json.encodeToString(expectedCard),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When
        val result = dataSource.toggleFavorite("1")

        // Then
        assertNotNull(result)
        assertEquals(true, result.isFavorite)
    }

    @Test
    fun `test searchCards success`() = runTest {
        // Given
        val filter = SearchFilter(
            query = "test",
            cardTypes = listOf(CardType.QUOTE),
            tags = emptyList(),
            isFavorite = null,
            isTemplate = null,
            sortBy = SortBy.CREATED_AT_DESC
        )
        val expectedCards = listOf(createTestCardDto("1", CardType.QUOTE))
        val httpClient = createMockHttpClient { requestData ->
            assertNotNull(requestData.url.toString().contains("/search"))
            assertNotNull(requestData.url.toString().contains("q=test"))
            respond(
                content = Json.encodeToString(expectedCards),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteCardDataSource = RemoteCardDataSourceImpl(httpClient)

        // When
        val result = dataSource.searchCards(filter)

        // Then
        assertEquals(1, result.size)
        assertEquals("1", result[0].id)
    }
}


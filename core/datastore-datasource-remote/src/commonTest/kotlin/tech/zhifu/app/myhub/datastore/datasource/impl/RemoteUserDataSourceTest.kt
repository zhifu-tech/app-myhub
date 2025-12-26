package tech.zhifu.app.myhub.datastore.datasource.impl

import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.datasource.RemoteUserDataSource
import tech.zhifu.app.myhub.datastore.datasource.TestUtils.createMockHttpClient
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.datastore.model.User
import tech.zhifu.app.myhub.datastore.model.UserPreferences
import tech.zhifu.app.myhub.datastore.network.ApiConfig
import tech.zhifu.app.myhub.datastore.network.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

/**
 * RemoteUserDataSource 测试
 */
class RemoteUserDataSourceTest {

    private fun createTestUser(id: String = "1", username: String = "testuser"): User {
        return User(
            id = id,
            username = username,
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            createdAt = Clock.System.now(),
            preferences = UserPreferences(
                theme = "dark",
                language = "en",
                defaultCardType = CardType.QUOTE,
                autoSync = true,
                syncInterval = 3600000L
            )
        )
    }

    @Test
    fun `test getCurrentUser success`() = runTest {
        // Given
        val expectedUser = createTestUser("1", "testuser")
        val httpClient = createMockHttpClient { request ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/current", request.url.toString())
            respond(
                content = Json.encodeToString(expectedUser),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteUserDataSource = RemoteUserDataSourceImpl(httpClient)

        // When
        val result = dataSource.getCurrentUser()

        // Then
        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("testuser", result.username)
        assertNotNull(result.preferences)
        assertEquals("dark", result.preferences?.theme)
    }

    @Test
    fun `test getCurrentUser api error`() = runTest {
        // Given
        val httpClient = createMockHttpClient { request ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError
            )
        }
        val dataSource: RemoteUserDataSource = RemoteUserDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.getCurrentUser()
        }
    }

    @Test
    fun `test updateUser success`() = runTest {
        // Given
        val user = createTestUser("1", "updated-user").copy(
            displayName = "Updated Display Name"
        )
        val httpClient = createMockHttpClient { requestData ->
            assertEquals("${ApiConfig.BASE_URL}${ApiConfig.USERS_PATH}/current", requestData.url.toString())
            respond(
                content = Json.encodeToString(user),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val dataSource: RemoteUserDataSource = RemoteUserDataSourceImpl(httpClient)

        // When
        val result = dataSource.updateUser(user)

        // Then
        assertNotNull(result)
        assertEquals("updated-user", result.username)
        assertEquals("Updated Display Name", result.displayName)
    }

    @Test
    fun `test updateUser api error`() = runTest {
        // Given
        val user = createTestUser("1", "testuser")
        val httpClient = createMockHttpClient { requestData ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError
            )
        }
        val dataSource: RemoteUserDataSource = RemoteUserDataSourceImpl(httpClient)

        // When & Then
        assertFailsWith<ApiException> {
            dataSource.updateUser(user)
        }
    }
}


package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

/**
 * 数据库基础功能测试
 *
 * 注意：这些测试针对版本 2 的 Schema（包含 user_id 字段）
 * 真正的迁移测试（从版本 1 到版本 2）需要在集成测试中进行
 */
class DatabaseTest {

    private val testUserId = "test-user-1"

    @Test
    fun `test database schema creation`() = runDatabaseTest { database ->
        // 创建测试用户
        val now = Clock.System.now().toString()
        database.userQueries.insertUser(
            id = testUserId,
            username = "testuser",
            email = "test@example.com",
            display_name = "Test User",
            avatar_url = null,
            created_at = now
        )

        val result = database.cardQueries.selectAll(testUserId).awaitAsList()
        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `test database can insert and query after creation`() = runDatabaseTest { database ->
        // 创建测试用户
        val now = Clock.System.now().toString()
        database.userQueries.insertUser(
            id = testUserId,
            username = "testuser",
            email = "test@example.com",
            display_name = "Test User",
            avatar_url = null,
            created_at = now
        )

        insertTestCard(database, "test-1", "Test Content", testUserId)

        val result = database.cardQueries.selectById("test-1", testUserId).awaitAsOneOrNull()
        assertNotNull(result)
        assertEquals("test-1", result.id)
        assertEquals("QUOTE", result.type)
        assertEquals("Test Content", result.content)
    }

    @Test
    fun `test database transaction rollback`() = runDatabaseTest { database ->
        // 创建测试用户
        val now = Clock.System.now().toString()
        database.userQueries.insertUser(
            id = testUserId,
            username = "testuser",
            email = "test@example.com",
            display_name = "Test User",
            avatar_url = null,
            created_at = now
        )

        assertFailsWith<RuntimeException> {
            database.transaction {
                database.cardQueries.insertCard(
                    id = "test-rollback",
                    type = "QUOTE",
                    title = "Title",
                    content = "Should be rolled back",
                    author = null,
                    source = null,
                    language = null,
                    is_favorite = 0L,
                    is_template = 0L,
                    created_at = now,
                    updated_at = now,
                    last_reviewed_at = null,
                    user_id = testUserId
                )
                throw RuntimeException("Test rollback")
            }
        }

        val result = database.cardQueries.selectById("test-rollback", testUserId).awaitAsOneOrNull()
        assertEquals(null, result)
    }

    @Test
    fun `test foreign key constraints`() = runDatabaseTest { database ->
        // 创建测试用户
        val now = Clock.System.now().toString()
        database.userQueries.insertUser(
            id = testUserId,
            username = "testuser",
            email = "test@example.com",
            display_name = "Test User",
            avatar_url = null,
            created_at = now
        )

        insertTestCard(database, "card-1", "Content", testUserId)

        database.cardQueries.insertCardTag("card-1", "tag1", testUserId)
        val tags = database.cardQueries.selectCardTags("card-1", testUserId).awaitAsList()
        assertEquals(1, tags.size)
        assertEquals("tag1", tags[0])
    }

    @Test
    fun `test cascade delete`() = runDatabaseTest { database ->
        // 创建测试用户
        val now = Clock.System.now().toString()
        database.userQueries.insertUser(
            id = testUserId,
            username = "testuser",
            email = "test@example.com",
            display_name = "Test User",
            avatar_url = null,
            created_at = now
        )

        insertTestCard(database, "card-1", "Content", testUserId)
        database.cardQueries.insertCardTag("card-1", "tag1", testUserId)
        database.cardQueries.insertCardTag("card-1", "tag2", testUserId)

        database.cardQueries.deleteCard("card-1", testUserId)

        val tags = database.cardQueries.selectCardTags("card-1", testUserId).awaitAsList()
        assertEquals(0, tags.size)
    }

    @Test
    fun `test user data isolation`() = runDatabaseTest { database ->
        // Given - 创建两个用户
        val userId1 = "user-1"
        val userId2 = "user-2"
        val now = Clock.System.now().toString()

        database.userQueries.insertUser(
            id = userId1,
            username = "user1",
            email = "user1@example.com",
            display_name = "User 1",
            avatar_url = null,
            created_at = now
        )

        database.userQueries.insertUser(
            id = userId2,
            username = "user2",
            email = "user2@example.com",
            display_name = "User 2",
            avatar_url = null,
            created_at = now
        )

        // When - 插入不同用户的数据
        insertTestCard(database, "card-1", "User 1 Card", userId1)
        insertTestCard(database, "card-2", "User 2 Card", userId2)

        // Then - 验证用户数据隔离
        val user1Cards = database.cardQueries.selectAll(userId1).awaitAsList()
        assertEquals(1, user1Cards.size)
        assertEquals("card-1", user1Cards[0].id)

        val user2Cards = database.cardQueries.selectAll(userId2).awaitAsList()
        assertEquals(1, user2Cards.size)
        assertEquals("card-2", user2Cards[0].id)

        // 验证用户无法访问其他用户的数据
        val user1Card2 = database.cardQueries.selectById("card-2", userId1).awaitAsOneOrNull()
        assertEquals(null, user1Card2)
    }

    private suspend fun insertTestCard(
        database: MyHubDatabase,
        id: String,
        content: String = "Content",
        userId: String = testUserId
    ) {
        val now = Clock.System.now().toString()
        database.cardQueries.insertCard(
            id = id,
            type = "QUOTE",
            title = "Test",
            content = content,
            author = null,
            source = null,
            language = null,
            is_favorite = 0L,
            is_template = 0L,
            created_at = now,
            updated_at = now,
            last_reviewed_at = null,
            user_id = userId
        )
    }
}


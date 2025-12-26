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
 */
class DatabaseTest {

    @Test
    fun `test database schema creation`() = runDatabaseTest { database ->
        val result = database.cardQueries.selectAll().awaitAsList()
        assertNotNull(result)
        assertEquals(0, result.size)
    }

    @Test
    fun `test database can insert and query after creation`() = runDatabaseTest { database ->
        insertTestCard(database, "test-1", "Test Content")

        val result = database.cardQueries.selectById("test-1").awaitAsOneOrNull()
        assertNotNull(result)
        assertEquals("test-1", result.id)
        assertEquals("QUOTE", result.type)
        assertEquals("Test Content", result.content)
    }

    @Test
    fun `test database transaction rollback`() = runDatabaseTest { database ->
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
                    created_at = Clock.System.now().toString(),
                    updated_at = Clock.System.now().toString(),
                    last_reviewed_at = null
                )
                throw RuntimeException("Test rollback")
            }
        }

        val result = database.cardQueries.selectById("test-rollback").awaitAsOneOrNull()
        assertEquals(null, result)
    }

    @Test
    fun `test foreign key constraints`() = runDatabaseTest { database ->
        insertTestCard(database, "card-1")

        database.cardQueries.insertCardTag("card-1", "tag1")
        val tags = database.cardQueries.selectCardTags("card-1").awaitAsList()
        assertEquals(1, tags.size)
        assertEquals("tag1", tags[0])
    }

    @Test
    fun `test cascade delete`() = runDatabaseTest { database ->
        insertTestCard(database, "card-1")
        database.cardQueries.insertCardTag("card-1", "tag1")
        database.cardQueries.insertCardTag("card-1", "tag2")

        database.cardQueries.deleteCard("card-1")

        val tags = database.cardQueries.selectCardTags("card-1").awaitAsList()
        assertEquals(0, tags.size)
    }

    private suspend fun insertTestCard(database: MyHubDatabase, id: String, content: String = "Content") {
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
            last_reviewed_at = null
        )
    }
}


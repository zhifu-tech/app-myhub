package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.model.Tag
import kotlin.time.Instant

/**
 * 本地标签数据源实现（使用SQLDelight）
 */
class LocalTagDataSourceImpl(
    private val database: MyHubDatabase
) : LocalTagDataSource {

    override suspend fun getAllTags(userId: String): List<Tag> {
        return database.tagQueries.selectAll(userId).awaitAsList().map { it.toTag() }
    }

    override suspend fun getTagById(id: String, userId: String): Tag? {
        return database.tagQueries.selectById(id, userId).awaitAsOneOrNull()?.toTag()
    }

    override suspend fun getTagByName(name: String, userId: String): Tag? {
        return database.tagQueries.selectByName(name, userId).awaitAsOneOrNull()?.toTag()
    }

    override suspend fun insertTag(tag: Tag, userId: String) {
        database.tagQueries.insertTag(
            id = tag.id,
            name = tag.name,
            color = tag.color,
            description = tag.description,
            card_count = tag.cardCount.toLong(),
            created_at = tag.createdAt.toString(),
            user_id = userId
        )
    }

    override suspend fun updateTag(tag: Tag, userId: String) {
        database.tagQueries.updateTag(
            name = tag.name,
            color = tag.color,
            description = tag.description,
            card_count = tag.cardCount.toLong(),
            id = tag.id,
            user_id = userId
        )
    }

    override suspend fun deleteTag(id: String, userId: String) {
        database.tagQueries.deleteTag(id, userId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTags(userId: String): Flow<List<Tag>> {
        return database.tagQueries.selectAll(userId)
            .asFlow()
            .mapLatest { query ->
                query.awaitAsList().map { it.toTag() }
            }
    }

    /**
     * 将数据库行转换为Tag实体
     */
    private fun tech.zhifu.app.myhub.datastore.database.Tag.toTag(): Tag {
        return Tag(
            id = id,
            name = name,
            color = color,
            description = description,
            cardCount = card_count.toInt(),
            createdAt = Instant.parse(created_at)
        )
    }
}


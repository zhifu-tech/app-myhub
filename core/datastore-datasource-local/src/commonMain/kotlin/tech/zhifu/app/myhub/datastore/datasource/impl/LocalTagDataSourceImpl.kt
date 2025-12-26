package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
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

    override suspend fun getAllTags(): List<Tag> {
        return database.tagQueries.selectAll().awaitAsList().map { it.toTag() }
    }

    override suspend fun getTagById(id: String): Tag? {
        return database.tagQueries.selectById(id).awaitAsOneOrNull()?.toTag()
    }

    override suspend fun getTagByName(name: String): Tag? {
        return database.tagQueries.selectByName(name).awaitAsOneOrNull()?.toTag()
    }

    override suspend fun insertTag(tag: Tag) {
        database.tagQueries.insertTag(
            id = tag.id,
            name = tag.name,
            color = tag.color,
            description = tag.description,
            card_count = tag.cardCount.toLong(),
            created_at = tag.createdAt.toString()
        )
    }

    override suspend fun updateTag(tag: Tag) {
        database.tagQueries.updateTag(
            name = tag.name,
            color = tag.color,
            description = tag.description,
            card_count = tag.cardCount.toLong(),
            id = tag.id
        )
    }

    override suspend fun deleteTag(id: String) {
        database.tagQueries.deleteTag(id)
    }

    override fun observeTags(): Flow<List<Tag>> {
        return database.tagQueries.selectAll()
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


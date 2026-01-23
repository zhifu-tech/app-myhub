package tech.zhifu.app.myhub.datastore.datasource.impl

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.zhifu.app.myhub.datastore.database.MyHubDatabase
import tech.zhifu.app.myhub.datastore.datasource.LocalTagDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Tag

class LocalTagDataSourceImpl(
    private val database: MyHubDatabase
) : LocalTagDataSource {

    override suspend fun getTag(tagId: String): Tag? {
        return database.tagQueries
            .selectTagById(tagId)
            .awaitAsOneOrNull()
            ?.toDomain()
    }

    override fun observeTag(tagId: String): Flow<Tag> {
        return database.tagQueries.selectTagById(tagId)
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map(DbTag::toDomain)
    }

    override suspend fun getTags(userId: String): List<Tag> {
        return database.tagQueries.selectTagsByUserId(userId)
            .awaitAsList()
            .map(DbTag::toDomain)
    }

    override fun observeTags(userId: String): Flow<List<Tag>> {
        return database.tagQueries.selectTagsByUserId(userId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { tags -> tags.map(DbTag::toDomain) }
    }

    override suspend fun insertTag(tag: Tag) {
        database.tagQueries.insertTag(
            id = tag.id,
            name = tag.name,
            color = tag.color,
            description = tag.description,
            user_id = tag.userId,
            created_at = tag.createdAt.toString(),
            updated_at = tag.updatedAt.toString(),
            card_count = tag.cardCount.toLong()
        )
    }

    override suspend fun deleteTag(id: String) {
        database.tagQueries.deleteTag(id)
    }
}

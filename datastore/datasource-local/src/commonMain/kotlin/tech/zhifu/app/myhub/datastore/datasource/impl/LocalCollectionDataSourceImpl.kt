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
import tech.zhifu.app.myhub.datastore.datasource.LocalCollectionDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.database.Collection as DbCollection

class LocalCollectionDataSourceImpl(
    private val database: MyHubDatabase
) : LocalCollectionDataSource {

    override suspend fun insertCollection(collection: Collection) {
        database.transaction {
            database.collectionQueries.insertCollection(
                id = collection.id,
                name = collection.name,
                topic = collection.topic,
                description = collection.description,
                user_id = collection.userId,
                created_at = collection.createdAt.toString(),
                updated_at = collection.updatedAt.toString()
            )
            database.user_collectionQueries.insertUserCollection(
                user_id = collection.userId,
                collection_id = collection.id,
                role = "owner",
                created_at = collection.createdAt.toString()
            )
        }
    }

    override suspend fun getCollection(collectionId: String): Collection? {
        return database.collectionQueries
            .selectCollectionById(collectionId)
            .awaitAsOneOrNull()
            ?.toDomain()
    }

    override fun observeCollection(collectionId: String): Flow<Collection> {
        return database.collectionQueries
            .selectCollectionById(collectionId)
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map(DbCollection::toDomain)
    }

    override suspend fun getCollections(userId: String): List<Collection> {
        return database.collectionQueries
            .selectCollectionsByUserId(userId)
            .awaitAsList()
            .map(DbCollection::toDomain)
    }

    override fun observeCollections(userId: String): Flow<List<Collection>> {
        return database.collectionQueries
            .selectCollectionsByUserId(userId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { collections -> collections.map(DbCollection::toDomain) }
    }

    override suspend fun deleteCollection(collectionId: String) {
        database.collectionQueries.deleteCollection(collectionId)
    }

    override suspend fun deleteCollections(userId: String) {
        database.collectionQueries.deleteCollectionsByUserId(userId)
    }
}

package tech.zhifu.app.myhub.datastore.datasource.collection

import tech.zhifu.app.myhub.datastore.model.domain.Collection

class RemoteCollectionDataSourceNoop : RemoteCollectionDataSource {
    override suspend fun getCollections(
        userId: String,
        page: Int?,
        pageSize: Int?
    ): List<Collection> = emptyList()

    override suspend fun getCollectionById(id: String, userId: String): Collection? = null

    override suspend fun createCollection(collection: Collection, userId: String): Collection = collection

    override suspend fun updateCollection(id: String, collection: Collection, userId: String): Collection = collection

    override suspend fun deleteCollection(id: String, userId: String) = Unit
}

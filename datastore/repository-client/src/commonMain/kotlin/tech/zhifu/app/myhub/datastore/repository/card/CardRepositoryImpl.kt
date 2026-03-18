package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import tech.zhifu.app.myhub.datastore.datasource.card.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress
import tech.zhifu.app.myhub.datastore.repository.impl.recordDeleteOperation
import tech.zhifu.app.myhub.datastore.repository.impl.recordInsertOperation
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.tag.TagRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

class CardRepositoryImpl(
    private val store: CardStore,
    private val syncRepository: SyncRepository,
    private val tagRepository: TagRepository,
    private val localCardDataSource: LocalCardDataSource,
    private val logger: Logger = logger("CardRepo")
) : CardRepository {

    override suspend fun insertCard(
        card: Card,
        needSync: Boolean,
    ) {
        logger.debug { "insert card: $card" }
        // 1. 业务逻辑：确保 Tags 存在
        val resolvedTags = tagRepository.ensureTags(card.userId, card.tags, needSync = needSync)
        logger.debug { "resolved tags: $resolvedTags" }
        val updatedCard = card.copy(tags = resolvedTags)
        logger.debug { "resolved tags: $resolvedTags" }

        // 2. 通过 Store 写入
        store.write(
            StoreWriteRequest.of(
                key = CardStoreKey.ById(updatedCard.id),
                value = CardStoreData.Single(updatedCard)
            )
        )

        // 3. 记录 Sync 操作
        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = updatedCard.userId,
                entityType = SyncEntityType.Card,
                entityId = updatedCard.id,
                payload = updatedCard,
            )
        }
    }

    override suspend fun getCard(
        cardId: String
    ): CardStoreData = store.get<CardStoreKey<String>, CardStoreData, StoreWriteResponse>(
        key = CardStoreKey.ById(cardId)
    )

    override suspend fun getCards(
        userId: String,
        page: Int,
        size: Int,
        sort: StoreKey.Sort?
    ): CardStoreData = store.get<CardStoreKey<String>, CardStoreData, StoreWriteResponse>(
        key = CardStoreKey.ByUser(
            userId = userId,
            page = page,
            size = size,
            sort = sort,
        )
    )

    override suspend fun getCards(
        cardIds: List<String>
    ): CardStoreData = store.get<CardStoreKey<String>, CardStoreData, StoreWriteResponse>(
        key = CardStoreKey.ByIds(cardIds)
    )

    override fun streamCards(
        userId: String,
        page: Int,
        size: Int,
        sort: StoreKey.Sort?,
        refresh: Boolean
    ): Flow<StoreReadResponse<CardStoreData>> = store.stream<StoreWriteResponse>(
        request = StoreReadRequest.cached(
            key = CardStoreKey.ByUser(
                userId = userId,
                page = page,
                size = size,
                sort = sort,
            ),
            refresh = refresh
        )
    )

    override fun streamCard(
        cardId: String, refresh: Boolean
    ): Flow<StoreReadResponse<CardStoreData>> = store.stream<StoreWriteResponse>(
        request = StoreReadRequest.cached(
            key = CardStoreKey.ById(cardId),
            refresh = refresh
        )
    )

    override suspend fun fetchCards(
        userId: String,
        page: Int,
        size: Int,
        sort: StoreKey.Sort?,
        filters: List<StoreKey.Filter<*>>?
    ): Flow<StoreReadResponse<CardStoreData>> = store.stream<StoreWriteResponse>(
        request = StoreReadRequest.fresh(
            key = CardStoreKey.ByUser(
                userId = userId,
                page = page,
                size = size,
                sort = sort,
                filters = filters
            )
        )
    )

    override suspend fun fetchCard(
        cardId: String
    ): Flow<StoreReadResponse<CardStoreData>> = store.stream<StoreWriteResponse>(
        request = StoreReadRequest.fresh(
            key = CardStoreKey.ById(cardId)
        )
    )

    override suspend fun clearCard(
        cardId: String
    ) {
        val card = getCard(cardId).card ?: return
        store.clear(key = CardStoreKey.ById(cardId))

        syncRepository.recordDeleteOperation(
            userId = card.userId,
            entityType = SyncEntityType.Card,
            entityId = cardId,
            payload = card,
        )
    }

    override suspend fun clearCards(
        userId: String
    ) {
        store.clear(key = CardStoreKey.ByUser(userId = userId, page = 1, size = 1))

        syncRepository.recordDeleteOperation(
            userId = userId,
            entityType = SyncEntityType.User,
            entityId = userId,
            payload = "",
        )
    }

    override suspend fun getReviewProgress(userId: String): ReviewProgress {
        return localCardDataSource.getReviewProgress(userId)
    }
}

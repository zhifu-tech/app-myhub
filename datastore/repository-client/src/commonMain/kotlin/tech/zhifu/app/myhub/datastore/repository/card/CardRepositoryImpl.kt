package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.repository.impl.recordDeleteOperation
import tech.zhifu.app.myhub.datastore.repository.impl.recordInsertOperation
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.datastore.repository.tag.TagRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
class CardRepositoryImpl(
    private val store: CardStore,
    private val syncRepository: SyncRepository,
    private val tagRepository: TagRepository,
    private val localCardDataSource: LocalCardDataSource,
    private val logger: Logger = logger("CardRepo")
) : CardRepository {

    override suspend fun insertCard(card: Card, needSync: Boolean) {
        logger.debug { "insert card: $card" }
        // 1. 业务逻辑：确保 Tags 存在
        val resolvedTags: List<Tag> = tagRepository.ensureTags(card.userId, card.tags, needSync = needSync)
        logger.debug { "resoved tags: $resolvedTags" }
        val updatedCard = card.copy(tags = resolvedTags)
        logger.debug { "resolved tags: $resolvedTags" }

        // 2. 通过 Store 写入
        store.write(
            StoreWriteRequest.Companion.of(
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

    override suspend fun getCards(userId: String, page: Int, pageSize: Int): CardStoreData? {
        return runCatching {
            store.get<CardStoreKey, CardStoreData, StoreWriteResponse>(
                key = CardStoreKey.ByUser(userId, page, pageSize)
            )
        }.onFailure {
            logger.error(it) { "get cards for {user:$userId} from store failed" }
        }.getOrNull()
    }

    override suspend fun getCards(cardIds: List<String>): CardStoreData {
        return store.get<CardStoreKey, CardStoreData, StoreWriteResponse>(
            key = CardStoreKey.ByIds(cardIds)
        )
    }


    override suspend fun getReviewProgress(userId: String): tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress {
        return localCardDataSource.getReviewProgress(userId)
    }

    override suspend fun getCard(cardId: String): CardStoreData? =
        runCatching {
            store.get<CardStoreKey, CardStoreData, StoreWriteResponse>(
                key = CardStoreKey.ById(cardId)
            )
        }.onFailure {
            logger.error(it) { "get card for {card:$cardId} from store failed" }
        }.getOrNull()

    override fun streamCards(userId: String, refresh: Boolean): Flow<StoreReadResponse<CardStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = CardStoreKey.ByUser(userId, page = 1, pageSize = 20),
                refresh = refresh
            )
        )

    override fun streamCard(cardId: String, refresh: Boolean): Flow<StoreReadResponse<CardStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = CardStoreKey.ById(cardId),
                refresh = false
            )
        )

    override suspend fun fetchCards(userId: String): Flow<StoreReadResponse<CardStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.fresh(
                key = CardStoreKey.ByUser(userId, page = 1, pageSize = 20)
            )
        )

    override suspend fun fetchCard(cardId: String): Flow<StoreReadResponse<CardStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.fresh(
                key = CardStoreKey.ById(cardId)
            )
        )

    override suspend fun clearCard(cardId: String) {
        val card = getCard(cardId)?.card ?: return
        store.clear(key = CardStoreKey.ById(cardId))

        syncRepository.recordDeleteOperation(
            userId = card.userId,
            entityType = SyncEntityType.Card,
            entityId = cardId,
            payload = card,
        )
    }

    override suspend fun clearCards(userId: String) {
        val cardsData = getCards(userId, page = 1, pageSize = 1)
        cardsData?.cards ?: return
        store.clear(key = CardStoreKey.ByUser(userId, page = 1, pageSize = 1))

        syncRepository.recordDeleteOperation(
            userId = userId,
            entityType = SyncEntityType.User,
            entityId = userId,
            payload = "",
        )
    }
}

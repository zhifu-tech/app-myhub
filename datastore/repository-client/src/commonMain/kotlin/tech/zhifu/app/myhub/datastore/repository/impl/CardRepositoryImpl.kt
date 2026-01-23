package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource
import tech.zhifu.app.myhub.datastore.datasource.LocalSyncDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteCardDataSource
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.SyncChangeApplier
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.sync.SyncEntityType
import tech.zhifu.app.myhub.sync.SyncOperations
import tech.zhifu.app.myhub.sync.SyncPullChange
import kotlin.random.Random
import kotlin.time.Clock

/**
 * 卡片仓库实现（客户端）
 * 实现本地和远程数据源的协调，支持响应式接口
 */
class CardRepositoryImpl(
    private val localCardDataSource: LocalCardDataSource,
    private val remoteCardDataSource: RemoteCardDataSource,
    private val localSyncDataSource: LocalSyncDataSource,
    private val tagRepository: TagRepository,
) : CardRepository {

    override val syncChangeApplier: SyncChangeApplier = object : SyncChangeApplier {
        override suspend fun applyChanges(
            entity: SyncEntityType,
            operations: SyncOperations,
            change: SyncPullChange
        ) {
            when (operations) {
                SyncOperations.Insert -> localSyncDataSource.applyChange(
                    deserializer = Card.serializer(),
                    payload = change.payload
                ) {
                    insertCardWithTags(this, needSync = false)
                }

                SyncOperations.Delete -> localCardDataSource.deleteCard(change.entityId)
                else -> {}
            }
        }
    }

    override suspend fun getCards(userId: String): List<Card> {
        return localCardDataSource.getCards(userId)
    }

    override fun observeCards(userId: String): Flow<List<Card>> {
        return localCardDataSource.observeCards(userId)
    }

    override suspend fun getCard(cardId: String): Card? {
        return localCardDataSource.getCard(cardId)
    }

    override suspend fun insertCard(card: Card, needSync: Boolean) {
        insertCardWithTags(card, needSync)
    }

    override suspend fun insertCardWithTags(card: Card, needSync: Boolean) {
        val resolvedTags = ensureTags(card.userId, card.tags)
        val updatedCard = card.copy(tags = resolvedTags)
        localCardDataSource.insertCard(updatedCard)
        if (needSync) {
            localSyncDataSource.recordInsertOperation(
                userId = updatedCard.userId,
                entityType = SyncEntityType.Card,
                entityId = updatedCard.id,
                payload = updatedCard,
            )
        }
    }

    override suspend fun deleteCard(cardId: String) {
        val card = localCardDataSource.getCard(cardId) ?: return
        localCardDataSource.deleteCard(cardId)
        localSyncDataSource.recordDeleteOperation(
            userId = card.userId,
            entityType = SyncEntityType.Card,
            entityId = cardId,
            payload = card,
        )
    }

    override fun observeCard(cardId: String): Flow<Card> {
        return localCardDataSource.observeCard(cardId)
    }

    override suspend fun fetchCard(cardId: String): Card? {
        return remoteCardDataSource.getCardById(cardId)?.also { card ->
            localCardDataSource.insertCard(card)
        }
    }

    override suspend fun fetchCards(userId: String): List<Card> {
        return remoteCardDataSource.getCards(userId).also { cards ->
            cards.forEach {
                localCardDataSource.insertCard(it)
            }
        }
    }

    private suspend fun ensureTags(userId: String, tags: List<Tag>): List<Tag> {
        if (tags.isEmpty()) return emptyList()
        val existingTags = tagRepository.getTags(userId)
        val byId = existingTags.associateBy { it.id }
        val byName = existingTags.associateBy { it.name }
        return tags.map { tag ->
            val existing = tag.id.takeIf { it.isNotBlank() }?.let(byId::get) ?: byName[tag.name]
            if (existing != null) {
                existing
            } else {
                val now = Clock.System.now()
                val newTag = tag.copy(
                    id = generateTagId(now),
                    userId = userId,
                    createdAt = now,
                    updatedAt = now
                )
                tagRepository.insertTag(newTag, needSync = true)
                newTag
            }
        }
    }

    private fun generateTagId(now: kotlin.time.Instant): String {
        val rand = Random.nextInt(0, 1_000_000)
        return "tag-${now.toEpochMilliseconds()}-$rand"
    }
}

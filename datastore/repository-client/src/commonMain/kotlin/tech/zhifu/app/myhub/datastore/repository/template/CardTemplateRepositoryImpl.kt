package tech.zhifu.app.myhub.datastore.repository.template

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.impl.extensions.get
import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.repository.impl.recordInsertOperation
import tech.zhifu.app.myhub.datastore.repository.sync.SyncRepository
import tech.zhifu.app.myhub.logger.Logger
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.sync.SyncEntityType

@OptIn(ExperimentalStoreApi::class)
class CardTemplateRepositoryImpl(
    private val store: TemplateStore,
    private val syncRepository: SyncRepository,
    private val logger: Logger = logger("TemplateRepo")
) : CardTemplateRepository {

    override suspend fun insertTemplate(template: CardTemplate, userId: String, needSync: Boolean) {
        store.write(
            StoreWriteRequest.of(
                key = TemplateStoreKey.ById(template.id),
                value = TemplateStoreData.Single(template)
            )
        )

        if (needSync) {
            syncRepository.recordInsertOperation(
                userId = userId,
                entityType = SyncEntityType.Template,
                entityId = template.id,
                payload = template,
            )
        }
    }

    override suspend fun getTemplate(templateId: String): TemplateStoreData? =
        runCatching {
            store.get<TemplateStoreKey, TemplateStoreData, StoreWriteResponse>(
                key = TemplateStoreKey.ById(templateId)
            )
        }.onFailure {
            logger.error(it) { "get template for {template:$templateId} from store failed" }
        }.getOrNull()

    override suspend fun getTemplates(): TemplateStoreData? =
        runCatching {
            store.get<TemplateStoreKey, TemplateStoreData, StoreWriteResponse>(
                key = TemplateStoreKey.All()
            )
        }.onFailure {
            logger.error(it) { "get templates from store failed" }
        }.getOrNull()

    override fun streamTemplates(refresh: Boolean): Flow<StoreReadResponse<TemplateStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.cached(
                key = TemplateStoreKey.All(),
                refresh = refresh
            )
        )

    override suspend fun fetchTemplates(): Flow<StoreReadResponse<TemplateStoreData>> =
        store.stream<StoreWriteResponse>(
            request = StoreReadRequest.fresh(
                key = TemplateStoreKey.All()
            )
        )
}

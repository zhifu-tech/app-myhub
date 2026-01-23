//package tech.zhifu.app.myhub.datastore.repository.impl
//
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flatMapLatest
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.flow.map
//import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
//import tech.zhifu.app.myhub.datastore.datasource.LocalUserDataSource
//import tech.zhifu.app.myhub.datastore.datasource.RemoteTemplateDataSource
//import tech.zhifu.app.myhub.datastore.datasource.UserContextProvider
//import tech.zhifu.app.myhub.datastore.model.Card
//import tech.zhifu.app.myhub.datastore.model.CardType
//import tech.zhifu.app.myhub.datastore.model.Template
//import tech.zhifu.app.myhub.datastore.repository.ReactiveTemplateRepository
//import kotlin.time.Clock
//
///**
// * 模板仓库实现（客户端）
// * 实现本地和远程数据源的协调，支持响应式接口
// */
//class TemplateRepositoryImpl(
//    private val localDataSource: LocalTemplateDataSource,
//    private val remoteDataSource: RemoteTemplateDataSource,
//    private val userContextProvider: UserContextProvider,
//    private val userDataSource: LocalUserDataSource
//) : ReactiveTemplateRepository {
//
//    private suspend fun requireUserId(): String {
//        return userContextProvider.getCurrentUserId()
//            ?: throw IllegalStateException("User not authenticated")
//    }
//
//    override suspend fun getAllTemplates(): List<Template> {
//        val userId = requireUserId()
//        return localDataSource.getAllTemplates(userId)
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    override fun observeAllTemplates(): Flow<List<Template>> {
//        return userDataSource.observeUser().flatMapLatest { user ->
//            if (user == null) {
//                flowOf(emptyList())
//            } else {
//                localDataSource.observeTemplates(user.id)
//            }
//        }
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    override fun observeTemplatesByType(type: CardType): Flow<List<Template>> {
//        return userDataSource.observeUser().flatMapLatest { user ->
//            if (user == null) {
//                flowOf(emptyList())
//            } else {
//                localDataSource.observeTemplates(user.id).map { templates ->
//                    templates.filter { it.cardType == type }
//                }
//            }
//        }
//    }
//
//    override suspend fun getTemplateById(id: String): Template? {
//        val userId = requireUserId()
//        // 先从本地获取
//        val localTemplate = localDataSource.getTemplateById(id, userId)
//        if (localTemplate != null) {
//            return localTemplate
//        }
//
//        // 如果本地没有，从远程获取
//        return try {
//            val remoteTemplate = remoteDataSource.getTemplateById(id)
//            remoteTemplate?.let { localDataSource.insertTemplate(it, userId) }
//            remoteTemplate
//        } catch (_: Exception) {
//            null
//        }
//    }
//
//    override suspend fun createTemplate(template: Template): Template {
//        val userId = requireUserId()
//        // 先保存到本地
//        localDataSource.insertTemplate(template, userId)
//
//        // 然后同步到远程
//        return try {
//            val remoteTemplate = remoteDataSource.createTemplate(template)
//            localDataSource.updateTemplate(remoteTemplate, userId)
//            remoteTemplate
//        } catch (_: Exception) {
//            // 如果远程同步失败，返回本地模板
//            template
//        }
//    }
//
//    override suspend fun updateTemplate(template: Template): Template {
//        val userId = requireUserId()
//        // 先更新本地
//        val updated = template.copy(updatedAt = Clock.System.now())
//        localDataSource.updateTemplate(updated, userId)
//
//        // 然后同步到远程
//        return try {
//            val remoteTemplate = remoteDataSource.updateTemplate(updated)
//            localDataSource.updateTemplate(remoteTemplate, userId)
//            remoteTemplate
//        } catch (_: Exception) {
//            // 如果远程同步失败，返回本地模板
//            updated
//        }
//    }
//
//    override suspend fun deleteTemplate(id: String): Boolean {
//        return try {
//            val userId = requireUserId()
//            // 先删除本地
//            localDataSource.deleteTemplate(id, userId)
//
//            // 然后删除远程
//            try {
//                remoteDataSource.deleteTemplate(id)
//            } catch (_: Exception) {
//                // 如果远程删除失败，仍然返回成功（本地已删除）
//            }
//
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    /**
//     * 从模板创建卡片（客户端特有方法）
//     */
//    suspend fun createCardFromTemplate(templateId: String): Card {
//        val userId = requireUserId()
//        val template = getTemplateById(templateId)
//            ?: throw IllegalStateException("Template not found: $templateId")
//
//        // 增加模板使用计数（系统模板和用户模板都可以增加计数）
//        val updatedTemplate = template.copy(usageCount = template.usageCount + 1)
//        // 注意：系统模板的 user_id 是 "system"，需要特殊处理
//        if (template.isSystemTemplate) {
//            localDataSource.updateTemplate(updatedTemplate, "system")
//        } else {
//            updateTemplate(updatedTemplate)
//        }
//
//        // 创建卡片
//        val now = Clock.System.now()
//        return Card(
//            id = "${now.toEpochMilliseconds()}-${templateId.take(8)}",
//            type = template.cardType,
//            title = null,
//            content = template.defaultContent ?: "",
//            author = null,
//            source = null,
//            language = null,
//            tags = template.defaultTags,
//            isFavorite = false,
//            isTemplate = false,
//            createdAt = now,
//            updatedAt = now,
//            lastReviewedAt = null,
//            metadata = template.defaultMetadata
//        )
//    }
//}

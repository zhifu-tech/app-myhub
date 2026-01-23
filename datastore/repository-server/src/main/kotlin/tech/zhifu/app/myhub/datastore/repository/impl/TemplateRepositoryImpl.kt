//package tech.zhifu.app.myhub.datastore.repository.impl
//
//import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
//import tech.zhifu.app.myhub.datastore.datasource.UserContextProvider
//import tech.zhifu.app.myhub.datastore.model.Template
//import tech.zhifu.app.myhub.datastore.repository.TemplateRepository
//
///**
// * 模板仓库实现（服务端）
// * 使用 LocalTemplateDataSource 实现，避免代码重复
// */
//class TemplateRepositoryImpl(
//    private val localDataSource: LocalTemplateDataSource,
//    private val userContextProvider: UserContextProvider
//) : TemplateRepository {
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
//    override suspend fun getTemplateById(id: String): Template? {
//        val userId = requireUserId()
//        return localDataSource.getTemplateById(id, userId)
//    }
//
//    override suspend fun createTemplate(template: Template): Template {
//        val userId = requireUserId()
//        localDataSource.insertTemplate(template, userId)
//        return template
//    }
//
//    override suspend fun updateTemplate(template: Template): Template {
//        val userId = requireUserId()
//        localDataSource.updateTemplate(template, userId)
//        return template
//    }
//
//    override suspend fun deleteTemplate(id: String): Boolean {
//        return try {
//            val userId = requireUserId()
//            localDataSource.deleteTemplate(id, userId)
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//}
//

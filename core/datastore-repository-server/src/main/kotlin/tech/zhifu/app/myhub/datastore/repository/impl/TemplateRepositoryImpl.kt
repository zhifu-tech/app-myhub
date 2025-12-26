package tech.zhifu.app.myhub.datastore.repository.impl

import tech.zhifu.app.myhub.datastore.datasource.LocalTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.repository.TemplateRepository

/**
 * 模板仓库实现（服务端）
 * 使用 LocalTemplateDataSource 实现，避免代码重复
 */
class TemplateRepositoryImpl(
    private val localDataSource: LocalTemplateDataSource
) : TemplateRepository {

    override suspend fun getAllTemplates(): List<Template> {
        return localDataSource.getAllTemplates()
    }

    override suspend fun getTemplateById(id: String): Template? {
        return localDataSource.getTemplateById(id)
    }

    override suspend fun createTemplate(template: Template): Template {
        localDataSource.insertTemplate(template)
        return template
    }

    override suspend fun updateTemplate(template: Template): Template {
        localDataSource.updateTemplate(template)
        return template
    }

    override suspend fun deleteTemplate(id: String): Boolean {
        return try {
            localDataSource.deleteTemplate(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}


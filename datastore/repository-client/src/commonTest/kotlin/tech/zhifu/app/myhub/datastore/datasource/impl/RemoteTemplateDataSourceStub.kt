package tech.zhifu.app.myhub.datastore.datasource.impl

import tech.zhifu.app.myhub.datastore.datasource.RemoteTemplateDataSource
import tech.zhifu.app.myhub.datastore.model.Template

/**
 * 远程模板数据源占位实现（仅用于测试）
 */
class RemoteTemplateDataSourceStub : RemoteTemplateDataSource {
    override suspend fun getAllTemplates(): List<Template> {
        return emptyList()
    }

    override suspend fun getTemplateById(id: String): Template? {
        return null
    }

    override suspend fun createTemplate(template: Template): Template {
        return template
    }

    override suspend fun updateTemplate(template: Template): Template {
        return template
    }

    override suspend fun deleteTemplate(id: String) {
        // Stub implementation
    }
}


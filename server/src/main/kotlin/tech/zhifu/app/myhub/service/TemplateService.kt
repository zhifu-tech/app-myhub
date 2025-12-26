package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.Template
import tech.zhifu.app.myhub.datastore.repository.TemplateRepository

/**
 * 模板服务
 */
class TemplateService(
    private val templateRepository: TemplateRepository
) {
    suspend fun getAllTemplates(): List<Template> {
        return templateRepository.getAllTemplates()
    }

    suspend fun getTemplateById(id: String): Template? {
        return templateRepository.getTemplateById(id)
    }

    suspend fun createTemplate(template: Template): Template {
        if (template.name.isBlank()) {
            throw tech.zhifu.app.myhub.exception.ValidationException("Template name cannot be empty")
        }

        return templateRepository.createTemplate(template)
    }

    suspend fun updateTemplate(template: Template): Template {
        if (template.name.isBlank()) {
            throw tech.zhifu.app.myhub.exception.ValidationException("Template name cannot be empty")
        }

        return templateRepository.updateTemplate(template)
    }

    suspend fun deleteTemplate(id: String): Boolean {
        return templateRepository.deleteTemplate(id)
    }
}


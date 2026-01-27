package tech.zhifu.app.myhub.service

import tech.zhifu.app.myhub.datastore.model.domain.CardTemplate
import tech.zhifu.app.myhub.datastore.model.dto.CardTemplateResponse
import tech.zhifu.app.myhub.datastore.model.dto.CreateCardTemplateRequest
import tech.zhifu.app.myhub.datastore.model.dto.UpdateCardTemplateRequest
import tech.zhifu.app.myhub.datastore.model.dto.toResponse
import tech.zhifu.app.myhub.datastore.repository.CardTemplateRepository
import tech.zhifu.app.myhub.exception.NotFoundException
import kotlin.time.Clock

/**
 * 卡片模板服务
 * 处理卡片模板相关的业务逻辑
 */
class CardTemplateService(
    private val templateRepository: CardTemplateRepository
) {
    /**
     * 获取所有模板
     */
    suspend fun getTemplates(): List<CardTemplateResponse> {
        val templates = templateRepository.getTemplates()
        return templates.map { it.toResponse() }
    }

    /**
     * 根据类型获取模板
     */
    suspend fun getTemplatesByType(type: String): List<CardTemplateResponse> {
        val templates = templateRepository.getTemplatesByType(type)
        return templates.map { it.toResponse() }
    }

    /**
     * 获取指定模板
     */
    suspend fun getTemplate(id: String): CardTemplateResponse {
        val template = templateRepository.getTemplateById(id)
            ?: throw NotFoundException("CardTemplate", id)

        return template.toResponse()
    }

    /**
     * 创建模板
     */
    suspend fun createTemplate(request: CreateCardTemplateRequest): CardTemplateResponse {
        // 验证请求
        request.validate()

        // 创建模板
        val template = CardTemplate(
            id = generateTemplateId(),
            type = request.type,
            title = request.title?.trim(),
            content = request.content?.trim(),
            description = request.description?.trim(),
            createdAt = Clock.System.now()
        )

        val created = templateRepository.createTemplate(template)
        return created.toResponse()
    }

    /**
     * 更新模板
     */
    suspend fun updateTemplate(
        id: String,
        request: UpdateCardTemplateRequest
    ): CardTemplateResponse {
        // 验证请求
        request.validate()

        // 检查模板是否存在
        val existing = templateRepository.getTemplateById(id)
            ?: throw NotFoundException("CardTemplate", id)

        // 更新模板
        val updated = existing.copy(
            type = request.type,
            title = request.title?.trim(),
            content = request.content?.trim(),
            description = request.description?.trim()
        )

        val saved = templateRepository.updateTemplate(updated)
        return saved.toResponse()
    }

    /**
     * 删除模板
     */
    suspend fun deleteTemplate(id: String) {
        // 检查模板是否存在
        val existing = templateRepository.getTemplateById(id)
            ?: throw NotFoundException("CardTemplate", id)

        templateRepository.deleteTemplate(id)
    }

    /**
     * 生成模板 ID
     */
    private fun generateTemplateId(): String {
        return "template-${System.currentTimeMillis()}-${(0..9999).random()}"
    }
}

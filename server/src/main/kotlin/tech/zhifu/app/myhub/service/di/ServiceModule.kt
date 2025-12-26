package tech.zhifu.app.myhub.service.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.CardRepository
import tech.zhifu.app.myhub.datastore.repository.StatisticsRepository
import tech.zhifu.app.myhub.datastore.repository.TagRepository
import tech.zhifu.app.myhub.datastore.repository.TemplateRepository
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.service.CardService
import tech.zhifu.app.myhub.service.StatisticsService
import tech.zhifu.app.myhub.service.TagService
import tech.zhifu.app.myhub.service.TemplateService
import tech.zhifu.app.myhub.service.UserService

/**
 * 服务层依赖注入模块
 * 
 * 提供所有 Service 的实现
 */
val serviceModule = module {
    // Service 层
    single<CardService> {
        CardService(
            cardRepository = get<CardRepository>()
        )
    }
    
    single<TagService> {
        TagService(
            tagRepository = get<TagRepository>()
        )
    }
    
    single<TemplateService> {
        TemplateService(
            templateRepository = get<TemplateRepository>()
        )
    }
    
    single<UserService> {
        UserService(
            userRepository = get<UserRepository>()
        )
    }
    
    single<StatisticsService> {
        StatisticsService(
            statisticsRepository = get<StatisticsRepository>()
        )
    }
}


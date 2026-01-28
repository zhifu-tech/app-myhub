package tech.zhifu.app.myhub.component.card.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.component.card.ArticleCardComponent
import tech.zhifu.app.myhub.component.card.CardComponent
import tech.zhifu.app.myhub.component.card.CodeCardComponent
import tech.zhifu.app.myhub.component.card.IdeaCardComponent
import tech.zhifu.app.myhub.component.card.QuoteCardComponent
import tech.zhifu.app.myhub.component.card.TodoCardComponent
import tech.zhifu.app.myhub.component.card.VideoCardComponent
import tech.zhifu.app.myhub.component.card.WordCardComponent
import tech.zhifu.app.myhub.datastore.model.domain.CARD_TYPE_ARTICLE
import tech.zhifu.app.myhub.datastore.model.domain.CARD_TYPE_CODE
import tech.zhifu.app.myhub.datastore.model.domain.CARD_TYPE_IDEA
import tech.zhifu.app.myhub.datastore.model.domain.CARD_TYPE_QUOTE
import tech.zhifu.app.myhub.datastore.model.domain.CARD_TYPE_TODO
import tech.zhifu.app.myhub.datastore.model.domain.CARD_TYPE_VIDEO
import tech.zhifu.app.myhub.datastore.model.domain.CARD_TYPE_WORD
import tech.zhifu.app.myhub.datastore.model.domain.CardType

/**
 * 卡片组件 Koin 模块
 * 注册所有卡片类型到组件实现的映射
 *
 * 通过 CardComponent 接口实现解耦，每种卡片类型都有独立的 Component 实现
 * 新增卡片类型时，只需：
 * 1. 创建对应的 XXXCardComponent 类实现 CardComponent 接口
 * 2. 在此模块中添加一行注册即可
 */
val cardModule = module {
    // 注册卡片组件映射
    single<Map<CardType, CardComponent>> {
        mapOf(
            CARD_TYPE_ARTICLE to ArticleCardComponent(),
            CARD_TYPE_QUOTE to QuoteCardComponent(),
            CARD_TYPE_CODE to CodeCardComponent(),
            CARD_TYPE_IDEA to IdeaCardComponent(),
            CARD_TYPE_WORD to WordCardComponent(),
            CARD_TYPE_TODO to TodoCardComponent(),
            CARD_TYPE_VIDEO to VideoCardComponent()
        )
    }
}

package tech.zhifu.app.myhub.component.card.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.component.card.ArticleCardComponent
import tech.zhifu.app.myhub.component.card.CardComponent
import tech.zhifu.app.myhub.component.card.ChecklistCardComponent
import tech.zhifu.app.myhub.component.card.CodeCardComponent
import tech.zhifu.app.myhub.component.card.DictionaryCardComponent
import tech.zhifu.app.myhub.component.card.IdeaCardComponent
import tech.zhifu.app.myhub.component.card.QuoteCardComponent
import tech.zhifu.app.myhub.datastore.model.CardType

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
            CardType.QUOTE to QuoteCardComponent(),
            CardType.CODE to CodeCardComponent(),
            CardType.IDEA to IdeaCardComponent(),
            CardType.ARTICLE to ArticleCardComponent(),
            CardType.DICTIONARY to DictionaryCardComponent(),
            CardType.CHECKLIST to ChecklistCardComponent()
        )
    }
}


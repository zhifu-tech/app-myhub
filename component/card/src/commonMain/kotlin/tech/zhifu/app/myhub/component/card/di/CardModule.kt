package tech.zhifu.app.myhub.component.card.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.component.card.ArticleCard
import tech.zhifu.app.myhub.component.card.CardComponentFactory
import tech.zhifu.app.myhub.component.card.ChecklistCard
import tech.zhifu.app.myhub.component.card.CodeCard
import tech.zhifu.app.myhub.component.card.DictionaryCard
import tech.zhifu.app.myhub.component.card.IdeaCard
import tech.zhifu.app.myhub.component.card.QuoteCard
import tech.zhifu.app.myhub.datastore.model.CardType

/**
 * 卡片组件 Koin 模块
 * 注册所有卡片类型到组件工厂的映射
 *
 * 新增卡片类型时，只需在此模块中添加一行注册即可
 */
val cardModule = module {
    // 注册卡片组件工厂映射
    single<Map<CardType, CardComponentFactory>> {
        mapOf(
            CardType.QUOTE to { card, onEdit, onFavorite, onCardClick, modifier ->
                QuoteCard(
                    card = card,
                    onEdit = onEdit,
                    onFavorite = onFavorite,
                    onCardClick = onCardClick,
                    modifier = modifier
                )
            },
            CardType.CODE to { card, onEdit, onFavorite, onCardClick, modifier ->
                CodeCard(
                    card = card,
                    onEdit = onEdit,
                    onFavorite = onFavorite,
                    onCardClick = onCardClick,
                    modifier = modifier
                )
            },
            CardType.IDEA to { card, onEdit, onFavorite, onCardClick, modifier ->
                IdeaCard(
                    card = card,
                    onEdit = onEdit,
                    onFavorite = onFavorite,
                    onCardClick = onCardClick,
                    modifier = modifier
                )
            },
            CardType.ARTICLE to { card, onEdit, onFavorite, onCardClick, modifier ->
                ArticleCard(
                    card = card,
                    onEdit = onEdit,
                    onFavorite = onFavorite,
                    onCardClick = onCardClick,
                    modifier = modifier
                )
            },
            CardType.DICTIONARY to { card, onEdit, onFavorite, onCardClick, modifier ->
                DictionaryCard(
                    card = card,
                    onEdit = onEdit,
                    onFavorite = onFavorite,
                    onCardClick = onCardClick,
                    modifier = modifier
                )
            },
            CardType.CHECKLIST to { card, onEdit, onFavorite, onCardClick, modifier ->
                ChecklistCard(
                    card = card,
                    onEdit = onEdit,
                    onFavorite = onFavorite,
                    onCardClick = onCardClick,
                    modifier = modifier
                )
            }
        )
    }
}


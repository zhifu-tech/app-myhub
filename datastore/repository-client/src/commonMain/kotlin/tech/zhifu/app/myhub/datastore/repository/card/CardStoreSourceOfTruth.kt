package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.LocalCardDataSource

internal fun createCardStoreSourceOfTruth(
    localCardDataSource: LocalCardDataSource
): CardStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is CardStoreKey.ById -> flow {
                try {
                    localCardDataSource.observeCard(key.id).collect { card ->
                        emit(CardStoreData.Single(card))
                    }
                } catch (_: Exception) {
                    emit(null)
                }
            }

            is CardStoreKey.ByUser -> {
                localCardDataSource.observeCards(key.userId).map { cards ->
                    if (cards.isEmpty()) null
                    else CardStoreData.Collection.fromCards(cards, key.userId)
                }
            }
        }
    },
    writer = { key, data ->
        when {
            key is CardStoreKey.ById && data is CardStoreData.Single -> {
                localCardDataSource.insertCard(data.card)
            }

            key is CardStoreKey.ByUser && data is CardStoreData.Collection -> {
                data.cards.forEach { card ->
                    localCardDataSource.insertCard(card)
                }
            }

            else -> {
                // Store5 框架应该保证类型匹配，这里主要是防御性编程
            }
        }
    },
    delete = { key ->
        when (key) {
            is CardStoreKey.ById -> localCardDataSource.deleteCard(key.id)
            is CardStoreKey.ByUser -> localCardDataSource.deleteCards(key.userId)
        }
    },
    deleteAll = {
        // 全部删除需要特殊处理
    }
)

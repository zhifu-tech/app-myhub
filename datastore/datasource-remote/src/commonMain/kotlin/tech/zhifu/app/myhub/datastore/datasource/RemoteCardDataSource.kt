package tech.zhifu.app.myhub.datastore.datasource

import tech.zhifu.app.myhub.datastore.model.domain.Card

/**
 * 远程数据源接口
 * 负责与服务器API通信
 */
interface RemoteCardDataSource {
    suspend fun getCards(userId: String): List<Card>
    suspend fun getCardById(id: String): Card?
}

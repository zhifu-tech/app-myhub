package tech.zhifu.app.myhub.datastore.repository.card

import kotlinx.coroutines.flow.Flow
import org.mobilenativefoundation.store.cache5.StoreMultiCache
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater

typealias CardStore = MutableStore<CardStoreKey<String>, CardStoreData>
typealias CardStoreCache = StoreMultiCache<String, CardStoreKey<String>, CardStoreData.Single, CardStoreData.Collection, CardStoreData>
typealias CardStoreSourceOfTruth = SourceOfTruth<CardStoreKey<String>, CardStoreData, CardStoreData>
typealias CardStoreBookkeeper = Bookkeeper<CardStoreKey<String>>
typealias CardStoreUpdater = Updater<CardStoreKey<String>, CardStoreData, StoreWriteResponse>
typealias CardStoreFetcher = Fetcher<CardStoreKey<String>, CardStoreData>

fun CardStore.storeStreamCard(
    userId: String,
    cardId: String
): Flow<StoreReadResponse<CardStoreData>> =
    stream<StoreReadResponse<CardStoreData>>(
        request = StoreReadRequest.localOnly(
            key = CardStoreKey.ById(userId, cardId),
        )
    )

fun CardStore.storeStreamCards(
    userId: String,
    cursorCardId: String?,
    cursorTitle: String?,
    cursorUpdatedAt: Long?,
    orderByUpdated: Boolean,
    orderByTitle: Boolean,
    query: String?,
    limit: Int
): Flow<StoreReadResponse<CardStoreData>> =
    stream<StoreReadResponse<CardStoreData>>(
        request = StoreReadRequest.localOnly(
            key = CardStoreKey.ByUserCursor(
                userId = userId,
                cursor = cursorCardId,
                cursorTitle = cursorTitle,
                cursorUpdatedAt = cursorUpdatedAt,
                orderByUpdated = orderByUpdated,
                orderByTitle = orderByTitle,
                query = query,
                size = limit,
            ),
        )
    )

package tech.zhifu.app.myhub.datastore.repository.user

import org.mobilenativefoundation.store.cache5.Cache
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater

typealias UserStore = MutableStore<UserStoreKey, UserStoreData>
typealias UserStoreCache = Cache<UserStoreKey, UserStoreData>
typealias UserStoreSourceOfTruth = SourceOfTruth<UserStoreKey, UserStoreData, UserStoreData>
typealias UserStoreBookkeeper = Bookkeeper<UserStoreKey>
typealias UserStoreUpdater = Updater<UserStoreKey, UserStoreData, StoreWriteResponse>
typealias UserStoreFetcher = Fetcher<UserStoreKey, UserStoreData>

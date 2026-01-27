package tech.zhifu.app.myhub.datastore.repository.store

import org.mobilenativefoundation.store.cache5.Cache
import org.mobilenativefoundation.store.core5.StoreData
import org.mobilenativefoundation.store.core5.StoreKey
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import org.mobilenativefoundation.store.store5.Updater

object StoreFactory {

    fun <D : StoreData<String>> createIdentityConverter(): Converter<D, D, D> {
        return Converter.Builder<D, D, D>()
            .fromNetworkToLocal { it }
            .fromOutputToLocal { it }
            .build()
    }

    fun <K : StoreKey<String>, D : StoreData<String>> createMutableStore(
        cache: Cache<K, D>,
        sourceOfTruth: SourceOfTruth<K, D, D>,
        bookkeeper: Bookkeeper<K>,
        fetcher: Fetcher<K, D>,
        updater: Updater<K, D, StoreWriteResponse>
    ): MutableStore<K, D> {
        return StoreBuilder.from(
            memoryCache = cache,
            sourceOfTruth = sourceOfTruth,
            fetcher = fetcher,
        ).toMutableStoreBuilder(
            converter = createIdentityConverter<D>()
        ).build(
            updater = updater,
            bookkeeper = bookkeeper
        )
    }
}

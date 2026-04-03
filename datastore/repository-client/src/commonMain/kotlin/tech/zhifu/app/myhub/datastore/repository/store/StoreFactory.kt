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
import org.mobilenativefoundation.store.store5.Validator

fun <K : StoreKey<*>, D : StoreData<String>> createMutableStore(
    memoryCache: Cache<K, D>,
    sourceOfTruth: SourceOfTruth<K, D, D>,
    bookkeeper: Bookkeeper<K>,
    fetcher: Fetcher<K, D>,
    updater: Updater<K, D, StoreWriteResponse>,
    validator: Validator<D>? = null,
    converter: Converter<D, D, D> = Converter.Builder<D, D, D>()
        .fromNetworkToLocal { it }
        .fromOutputToLocal { it }
        .build()
): MutableStore<K, D> =
    StoreBuilder
        .from(
            memoryCache = memoryCache,
            sourceOfTruth = sourceOfTruth,
            fetcher = fetcher,
        )
        .also {
            if (validator != null) {
                it.validator(validator)
            }
        }
        .toMutableStoreBuilder(
            converter = converter,
        )
        .build(
            updater = updater,
            bookkeeper = bookkeeper
        )

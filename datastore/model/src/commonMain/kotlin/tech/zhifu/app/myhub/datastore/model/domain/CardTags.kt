package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

val Card.tags: ImmutableList<String>
    get() = getFromMap(key = "card.tags") {
        tagsRaw
            ?.let { raw ->
                getFromMap(
                    key = "card.tags.decoded",
                    raw = raw,
                    deserializer = ListSerializer(String.serializer())
                )
            }?.toImmutableList()
            ?: persistentListOf()
    } ?: persistentListOf()

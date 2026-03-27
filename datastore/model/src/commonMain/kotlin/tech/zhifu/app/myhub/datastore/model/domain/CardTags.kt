package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

val Card.tags: List<String>
    get() = getFromMap(
        key = "card.tags",
        raw = tagsRaw,
        deserializer = ListSerializer(String.serializer())
    ) ?: emptyList()

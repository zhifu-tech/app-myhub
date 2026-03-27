package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class CardUi(
    val cover: Cover?,
)

@Serializable
data class Cover(
    val iconKey: String,
    val bgColor: String,
    val tintColor: String,
    val imageUrl: String,
)

val Card.ui: CardUi?
    get() = getFromMap(
        key = "card.ui",
        raw = uiRaw,
        deserializer = CardUi.serializer()
    )

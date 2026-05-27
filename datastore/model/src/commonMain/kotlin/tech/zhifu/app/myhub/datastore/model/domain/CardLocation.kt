package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class CardLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String?,
    val address: String?,
)

val Card.location: CardLocation?
    get() = getFromMap(
        key = "card.location",
        raw = locationRaw,
        deserializer = CardLocation.serializer()
    )

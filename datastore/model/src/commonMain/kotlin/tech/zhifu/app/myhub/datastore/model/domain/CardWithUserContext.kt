package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class CardWithUserContext(
    val card: Card,
    val user: CardUserContext,
)

@Serializable
data class CardUserContext(
    val userId: String,
)

fun Card.withUserContext(
    userId: String,
): CardWithUserContext = CardWithUserContext(
    card = this,
    user = CardUserContext(
        userId = userId,
    )
)

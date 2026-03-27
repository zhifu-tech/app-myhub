package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class CardAggregate(
    val card: Card,
    val media: List<CardMedia> = emptyList(),
    val entities: List<CardEntity> = emptyList(),
    val relations: List<CardRelation> = emptyList(),
)

fun Card.toAggregate(
    media: List<CardMedia> = emptyList(),
    entities: List<CardEntity> = emptyList(),
    relations: List<CardRelation> = emptyList(),
): CardAggregate = CardAggregate(
    card = this,
    media = media,
    entities = entities,
    relations = relations,
)

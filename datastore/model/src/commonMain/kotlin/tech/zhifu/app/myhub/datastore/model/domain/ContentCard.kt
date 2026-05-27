package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class ContentCard(
    val card: Card,
    val medias: List<MediaAsset> = emptyList(),
)

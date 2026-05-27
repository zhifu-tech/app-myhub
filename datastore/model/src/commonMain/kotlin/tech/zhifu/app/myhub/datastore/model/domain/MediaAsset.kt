package tech.zhifu.app.myhub.datastore.model.domain

import kotlinx.serialization.Serializable

@Serializable
data class MediaAsset(
    val id: String,
    val cardId: String,
    val mediaType: String,
    val storageHandle: String,
    val accessUrl: String,
    val thumbStorageHandle: String? = null,
    val thumbAccessUrl: String? = null,
    val width: Long? = null,
    val height: Long? = null,
    val durationMs: Long? = null,
    val sizeBytes: Long,
    val sha256: String,
    val createdAt: Long,
)

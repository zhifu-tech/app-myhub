package tech.zhifu.app.myhub.datastore.model.domain

fun MediaAsset.isVideo() =
    mediaType.startsWith(prefix = "video/", ignoreCase = true)

fun MediaAsset.name() =
    accessUrl.substringAfterLast('/')

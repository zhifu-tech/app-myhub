package tech.zhifu.app.myhub.datastore.model.domain

fun MediaAsset.name() =
    accessUrl.substringAfterLast('/')

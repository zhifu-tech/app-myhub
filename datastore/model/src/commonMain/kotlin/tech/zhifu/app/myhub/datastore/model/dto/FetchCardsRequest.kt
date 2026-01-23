package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable


@Serializable
data class FetchCardsRequest(val userId: String)

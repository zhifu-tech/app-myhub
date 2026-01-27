package tech.zhifu.app.myhub.datastore.model.dto

import kotlinx.serialization.Serializable
import kotlin.time.Clock

/**
 * 错误详情
 */
@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null,
    val timestamp: String = Clock.System.now().toString(),
    val path: String? = null
)

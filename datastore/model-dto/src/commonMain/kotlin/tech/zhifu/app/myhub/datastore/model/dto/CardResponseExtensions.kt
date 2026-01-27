package tech.zhifu.app.myhub.datastore.model.dto

import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.Tag
import kotlin.time.Instant

/**
 * 扩展函数：Card 转 CardResponse
 */
fun Card.toResponse(): CardResponse {
    return CardResponse(
        id = id,
        type = type,
        title = title,
        content = content,
        userId = userId,
        tags = tags.map { it.toResponse() },
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}

/**
 * 扩展函数：CardResponse 转 Card
 */
fun CardResponse.toDomain(): Card {
    return Card(
        id = id,
        type = type,
        title = title,
        content = content,
        userId = userId,
        tags = tags.map { it.toDomain() },
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

/**
 * 扩展函数：Tag 转 TagResponse
 */
fun Tag.toResponse(): TagResponse {
    return TagResponse(
        id = id,
        name = name,
        color = color
    )
}

/**
 * 扩展函数：TagResponse 转 Tag
 * 注意：TagResponse 不包含 userId、description、cardCount、createdAt、updatedAt
 * 这些字段需要在调用后通过 copy() 设置
 */
fun TagResponse.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        color = color,
        userId = "", // 需要从上下文获取，调用后使用 copy(userId = ...)
        description = null, // TagResponse 不包含此字段
        cardCount = 0, // TagResponse 不包含此字段
        createdAt = kotlin.time.Instant.DISTANT_PAST, // TagResponse 不包含此字段
        updatedAt = kotlin.time.Instant.DISTANT_PAST // TagResponse 不包含此字段
    )
}

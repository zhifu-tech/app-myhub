package tech.zhifu.app.myhub.datastore.model.dto

import tech.zhifu.app.myhub.datastore.model.domain.Collection
import tech.zhifu.app.myhub.datastore.model.domain.User
import kotlin.time.Instant

/**
 * 扩展函数：Collection 转 CollectionResponse
 */
fun Collection.toResponse(): CollectionResponse {
    return CollectionResponse(
        id = id,
        name = name,
        topic = topic,
        description = description,
        userId = userId,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString()
    )
}

/**
 * 扩展函数：CollectionResponse 转 Collection
 */
fun CollectionResponse.toDomain(): Collection {
    return Collection(
        id = id,
        name = name,
        topic = topic,
        description = description,
        userId = userId,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

/**
 * 扩展函数：User 转 UserResponse
 */
fun User.toResponse(): UserResponse {
    return UserResponse(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        avatarText = avatarText,
        status = status,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
        lastLoginAt = lastLoginAt.toString()
    )
}

/**
 * 扩展函数：UserResponse 转 User
 */
fun UserResponse.toDomain(): User {
    return User(
        id = id,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl,
        avatarText = avatarText,
        status = status,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        lastLoginAt = Instant.parse(lastLoginAt)
    )
}

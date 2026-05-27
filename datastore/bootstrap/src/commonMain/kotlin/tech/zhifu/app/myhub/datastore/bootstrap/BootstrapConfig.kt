package tech.zhifu.app.myhub.datastore.bootstrap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.zhifu.app.myhub.datastore.bootstrap.resources.Res
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.datastore.model.serializer.deserialize

data class BootstrapConfig(
    val userId: String,
    val user: User,
    val userPreferences: UserPreferences,
)

suspend fun buildBootstrapConfig(
    userId: String,
    localeTag: String
): BootstrapConfig = withContext(Dispatchers.Default) {
    val localeDir = resolveLocaleDir(localeTag)
    val user =
        readResource("user.json", localeDir)
            .deserialize<User>()
            .getOrNull()
            ?.copy(id = userId)
            ?: error("invalid bootstrap user.json for locale=$localeDir")
    val userPreferences =
        readResource("user_preferences.json", localeDir)
            .deserialize<UserPreferences>()
            .getOrNull()
            ?.copy(userId = userId)
            ?: error("invalid bootstrap user_preferences.json for locale=$localeDir")

    BootstrapConfig(
        userId = userId,
        user = user,
        userPreferences = userPreferences,
    )
}

private suspend fun readResource(
    fileName: String,
    localeDir: String
): String =
    Res.readBytes(path = "files/$localeDir/$fileName")
        .decodeToString()

private fun resolveLocaleDir(
    localeTag: String
): String {
    val normalized = localeTag.trim().lowercase()
        .replace('-', '_')
    return "bootstrap_$normalized"
}

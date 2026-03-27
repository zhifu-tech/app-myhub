package tech.zhifu.app.myhub.datastore.bootstrap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.bootstrap.resources.Res
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences

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
    val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    val user =
        readResource("user.json", localeDir)
            .let { json.decodeFromString<User>(it) }
            .copy(id = userId)
    val userPreferences =
        readResource("user_preferences.json", localeDir)
            .let { json.decodeFromString<UserPreferences>(it) }
            .copy(userId = userId)

    BootstrapConfig(
        userId = userId,
        user = user,
        userPreferences = userPreferences,
    )
}

private suspend fun readResource(
    fileName: String,
    localeDir: String
): String = Res.readBytes(path = "files/$localeDir/$fileName")
    .decodeToString()

private fun resolveLocaleDir(
    localeTag: String
): String {
    val normalized = localeTag.trim().lowercase()
        .replace('-', '_')
    return "bootstrap_$normalized"
}

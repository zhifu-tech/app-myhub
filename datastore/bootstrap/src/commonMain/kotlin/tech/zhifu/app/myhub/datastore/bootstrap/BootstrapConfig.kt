package tech.zhifu.app.myhub.datastore.bootstrap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.datastore.bootstrap.model.BootstrapCardSeed
import tech.zhifu.app.myhub.datastore.bootstrap.model.toCard
import tech.zhifu.app.myhub.datastore.bootstrap.resources.Res
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import kotlin.time.Clock

data class BootstrapConfig(
    val userId: String,
    val user: User,
    val userPreferences: UserPreferences,
    val cards: List<Card>,
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
    val now = Clock.System.now()
    val cards = readResource("card.json", localeDir)
        .let { json.decodeFromString<List<BootstrapCardSeed>>(it) }
        .map { it.toCard(now = now, json = json) }

    BootstrapConfig(
        userId = userId,
        user = user,
        userPreferences = userPreferences,
        cards = cards,
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

package tech.zhifu.app.myhub.datastore.repository.user

import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.SourceOfTruth
import tech.zhifu.app.myhub.datastore.datasource.user.LocalUserDataSource

fun createUserStoreSourceOfTruth(
    localUserDataSource: LocalUserDataSource,
): UserStoreSourceOfTruth = SourceOfTruth.of(
    reader = { key ->
        when (key) {
            is UserStoreKey.ById -> {
                localUserDataSource
                    .userFlow(key.id)
                    .map { UserStoreData.UserData(it) }
            }

            is UserStoreKey.PreferencesById -> {
                localUserDataSource
                    .userPreferencesFlow(userId = key.id)
                    .map {
                        UserStoreData.PreferencesData(
                            id = key.id,
                            preferences = it
                        )
                    }
            }
        }
    },
    writer = { key, data ->
        when (key) {
            is UserStoreKey.ById if data is UserStoreData.UserData -> {
                val user = data.user ?: return@of
                localUserDataSource.insertUser(user)
            }

            is UserStoreKey.PreferencesById if data is UserStoreData.PreferencesData -> {
                when {
                    data.preferences != null -> {
                        localUserDataSource
                            .upsertUserPreferences(
                                userId = key.id,
                                preferences = data.preferences
                            )
                    }

                    data.themeToWrite != null -> {
                        localUserDataSource
                            .updateUserPreferencesTheme(
                                userId = data.id,
                                theme = data.themeToWrite
                            )
                    }

                    data.languageToWrite != null -> {
                        localUserDataSource
                            .updateUserPreferencesLanguage(
                                userId = data.id,
                                language = data.languageToWrite,
                            )
                    }

                    data.sortAsDateToWrite != null && data.sortAsNameToWrite != null -> {
                        localUserDataSource
                            .updateUserPreferencesSort(
                                userId = data.id,
                                sortAsDate = data.sortAsDateToWrite,
                                sortAsName = data.sortAsNameToWrite,
                            )
                    }

                    data.layoutAsListToWrite != null -> {
                        localUserDataSource
                            .updateUserPreferencesLayout(
                                userId = data.id,
                                layoutAsList = data.layoutAsListToWrite,
                            )
                    }
                }
            }

            else -> {}
        }
    },
)

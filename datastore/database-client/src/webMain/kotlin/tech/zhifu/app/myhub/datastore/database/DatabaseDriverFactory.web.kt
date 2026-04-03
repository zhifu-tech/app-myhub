package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.async.coroutines.awaitMigrate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * WASM 平台的数据库驱动工厂实现
 * 使用 WebWorkerDriver 在 Web Worker 中运行 SQL.js
 */

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = createDefaultWebWorkerDriver()
        val initDeferred = initCompletableDeferred(driver)

        return InitializingDriver(
            delegate = driver,
            initDeferred = initDeferred
        )
    }
}

internal fun initCompletableDeferred(driver: SqlDriver): CompletableDeferred<Unit> {
    val initDeferred = CompletableDeferred<Unit>()

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    scope.launch {
        try {
            val currentVersion = readUserVersion(driver)
            val targetVersion = MyHubDatabase.Schema.version

            when {
                // 修复旧数据库 version
                currentVersion == 0L && hasUserTables(driver) -> {
                    driver
                        .execute(
                            identifier = null,
                            sql = "PRAGMA user_version = $targetVersion;",
                            parameters = 0
                        )
                        .await()
                }

                // 新建
                currentVersion == 0L -> {
                    MyHubDatabase.Schema
                        .awaitCreate(
                            driver = driver
                        )
                }

                // 迁移
                currentVersion < targetVersion -> {
                    MyHubDatabase.Schema
                        .awaitMigrate(
                            driver = driver,
                            oldVersion = currentVersion,
                            newVersion = targetVersion
                        )
                }
            }

            // 外键支持
            driver.execute(
                identifier = null,
                sql = "PRAGMA foreign_keys = ON;",
                parameters = 0
            ).await()

            initDeferred.complete(Unit)

        } catch (e: Throwable) {
            initDeferred.completeExceptionally(e)
        }
    }
    return initDeferred
}

private suspend fun readUserVersion(driver: SqlDriver): Long =
    driver
        .executeQuery(
            identifier = null,
            sql = "PRAGMA user_version;",
            mapper = { cursor ->
                val version = if (cursor.next().value) {
                    cursor.getLong(0) ?: 0L
                } else {
                    0L
                }
                QueryResult.Value(version)
            },
            parameters = 0,
            binders = null
        )
        .await()

/**
 * 判断是否存在用户表（优化为 EXISTS）
 */
private suspend fun hasUserTables(driver: SqlDriver): Boolean =
    driver
        .executeQuery(
            identifier = null,
            sql = """
        SELECT EXISTS(
            SELECT 1
            FROM sqlite_master
            WHERE type = 'table'
            AND name NOT LIKE 'sqlite_%'
        );
    """.trimIndent(),
            mapper = { cursor ->
                val exists = if (cursor.next().value) {
                    (cursor.getLong(0) ?: 0L) == 1L
                } else {
                    false
                }
                QueryResult.Value(exists)
            },
            parameters = 0,
            binders = null
        )
        .await()

actual fun databaseDriverFactoryModule(): Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }
}

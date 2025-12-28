package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.async.coroutines.await
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.module.Module
import org.koin.dsl.module
import org.w3c.dom.Worker

/**
 * JavaScript 平台的数据库驱动工厂实现
 * 使用 WebWorkerDriver 在 Web Worker 中运行 SQL.js
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = WebWorkerDriver(
            Worker(js("""new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url)"""))
        )
        val initDeferred = CompletableDeferred<Unit>()

        MainScope().launch {
            try {
                // 异步创建数据库架构
                // 注意：在 JS 平台，WebWorkerDriver 是异步的，必须确保 Schema 创建完成后再执行查询
                MyHubDatabase.Schema.awaitCreate(driver)
                // 启用外键约束以支持级联删除
                driver.execute(null, "PRAGMA foreign_keys = ON;", 0).await()
                initDeferred.complete(Unit)
            } catch (e: Throwable) {
                initDeferred.completeExceptionally(e)
            }
        }

        // 返回一个包装驱动，它会拦截所有请求并等待初始化完成后再执行
        return InitializingDriver(driver, initDeferred)
    }

    /**
     * 包装驱动，确保在数据库架构创建完成之前不执行任何查询
     */
    private class InitializingDriver(
        private val delegate: SqlDriver,
        private val initDeferred: CompletableDeferred<Unit>
    ) : SqlDriver {
        override fun <R> executeQuery(
            identifier: Int?,
            sql: String,
            mapper: (SqlCursor) -> QueryResult<R>,
            parameters: Int,
            binders: (SqlPreparedStatement.() -> Unit)?
        ): QueryResult<R> {
            return QueryResult.AsyncValue {
                initDeferred.await()
                delegate.executeQuery(identifier, sql, mapper, parameters, binders).await()
            }
        }

        override fun execute(
            identifier: Int?,
            sql: String,
            parameters: Int,
            binders: (SqlPreparedStatement.() -> Unit)?
        ): QueryResult<Long> {
            return QueryResult.AsyncValue {
                initDeferred.await()
                delegate.execute(identifier, sql, parameters, binders).await()
            }
        }

        override fun newTransaction(): QueryResult<Transacter.Transaction> {
            return QueryResult.AsyncValue {
                initDeferred.await()
                delegate.newTransaction().await()
            }
        }

        override fun currentTransaction(): Transacter.Transaction? {
            return delegate.currentTransaction()
        }

        override fun addListener(vararg queryKeys: String, listener: Query.Listener) {
            delegate.addListener(*queryKeys, listener = listener)
        }

        override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {
            delegate.removeListener(*queryKeys, listener = listener)
        }

        override fun notifyListeners(vararg queryKeys: String) {
            delegate.notifyListeners(*queryKeys)
        }

        override fun close() {
            delegate.close()
        }
    }
}

actual fun databaseDriverFactoryModule(): Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }
}

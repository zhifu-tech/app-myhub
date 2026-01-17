package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.MainScope
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

        val initDeferred = CompletableDeferred<Unit>()
        MainScope().launch {
            MyHubDatabase.Schema.awaitCreate(driver)
            driver.execute(null, "PRAGMA foreign_keys = ON;", 0).await()
            initDeferred.complete(Unit)
        }

        return InitializingDriver(driver, initDeferred)
    }
}

/**
 * 包装驱动，确保在数据库架构创建完成之前不执行任何查询
 */
private class InitializingDriver(
    private val delegate: SqlDriver,
    private val initDeferred: CompletableDeferred<Unit>
) : SqlDriver by delegate {

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<R> = QueryResult.AsyncValue {
        initDeferred.await()
        delegate.executeQuery(identifier, sql, mapper, parameters, binders).await()
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<Long> = QueryResult.AsyncValue {
        initDeferred.await()
        delegate.execute(identifier, sql, parameters, binders).await()
    }

    override fun newTransaction(): QueryResult<Transacter.Transaction> = QueryResult.AsyncValue {
        initDeferred.await()
        delegate.newTransaction().await()
    }

//    override fun currentTransaction(): Transacter.Transaction? {
//        return delegate.currentTransaction()
//    }
//
//    override fun addListener(vararg queryKeys: String, listener: Query.Listener) {
//        delegate.addListener(*queryKeys, listener = listener)
//    }
//
//    override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {
//        delegate.removeListener(*queryKeys, listener = listener)
//    }
//
//    override fun notifyListeners(vararg queryKeys: String) {
//        delegate.notifyListeners(*queryKeys)
//    }
//
//    override fun close() {
//        delegate.close()
//    }
}

actual fun databaseDriverFactoryModule(): Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory()
    }
}

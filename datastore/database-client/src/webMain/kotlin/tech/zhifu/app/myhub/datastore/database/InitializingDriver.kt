package tech.zhifu.app.myhub.datastore.database

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import kotlinx.coroutines.CompletableDeferred

/**
 * 初始化保护 Driver
 *
 * 确保：
 * - 数据库未初始化完成前，所有操作都会 suspend
 * - 初始化失败会向上传播异常
 */
internal class InitializingDriver(
    private val delegate: SqlDriver,
    private val initDeferred: CompletableDeferred<Unit>
) : SqlDriver by delegate {

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<R> =
        QueryResult.AsyncValue {
            initDeferred.await()
            delegate
                .executeQuery(
                    identifier = identifier,
                    sql = sql,
                    mapper = mapper,
                    parameters = parameters,
                    binders = binders
                )
                .await()
        }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<Long> =
        QueryResult.AsyncValue {
            initDeferred.await()
            delegate
                .execute(
                    identifier = identifier,
                    sql = sql,
                    parameters = parameters,
                    binders = binders
                )
                .await()
        }

    override fun newTransaction(): QueryResult<Transacter.Transaction> =
        QueryResult.AsyncValue {
            initDeferred.await()
            delegate.newTransaction().await()
        }
}

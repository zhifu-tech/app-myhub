package tech.zhifu.app.myhub.datastore.repository.impl

import kotlinx.coroutines.flow.Flow
import tech.zhifu.app.myhub.datastore.datasource.LocalStatisticsDataSource
import tech.zhifu.app.myhub.datastore.datasource.RemoteStatisticsDataSource
import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.repository.ReactiveStatisticsRepository

/**
 * 统计信息仓库实现（客户端）
 * 实现本地和远程数据源的协调，支持响应式接口
 */
class StatisticsRepositoryImpl(
    private val localDataSource: LocalStatisticsDataSource,
    private val remoteDataSource: RemoteStatisticsDataSource
) : ReactiveStatisticsRepository {

    override suspend fun getStatistics(): Statistics {
        // 先从本地获取
        val localStats = localDataSource.getStatistics()
        if (localStats != null) {
            return localStats
        }

        // 如果本地没有，从远程获取
        return try {
            val remoteStats = remoteDataSource.getStatistics()
            localDataSource.saveStatistics(remoteStats)
            remoteStats
        } catch (_: Exception) {
            Statistics() // 如果远程获取失败，返回空统计
        }
    }

    override suspend fun refreshStatistics(): Statistics {
        return try {
            // 从远程刷新统计数据
            val remoteStats = remoteDataSource.getStatistics()
            localDataSource.saveStatistics(remoteStats)
            remoteStats
        } catch (_: Exception) {
            // 如果远程刷新失败，返回本地数据
            localDataSource.getStatistics() ?: Statistics()
        }
    }

    override fun observeStatistics(): Flow<Statistics> {
        // TODO: 需要 LocalStatisticsDataSource 支持 observeStatistics()
        // 目前返回一个 Flow，从本地数据源获取
        return kotlinx.coroutines.flow.flow {
            emit(localDataSource.getStatistics() ?: Statistics())
        }
    }
}


//package tech.zhifu.app.myhub.datastore.repository.impl
//
//import kotlinx.coroutines.flow.Flow
//import tech.zhifu.app.myhub.datastore.datasource.LocalStatisticsDataSource
//import tech.zhifu.app.myhub.datastore.datasource.RemoteStatisticsDataSource
//import tech.zhifu.app.myhub.datastore.datasource.UserContextProvider
//import tech.zhifu.app.myhub.datastore.model.Statistics
//import tech.zhifu.app.myhub.datastore.repository.ReactiveStatisticsRepository
//
///**
// * 统计信息仓库实现（客户端）
// * 实现本地和远程数据源的协调，支持响应式接口
// */
//class StatisticsRepositoryImpl(
//    private val localDataSource: LocalStatisticsDataSource,
//    private val remoteDataSource: RemoteStatisticsDataSource,
//    private val userContextProvider: UserContextProvider
//) : ReactiveStatisticsRepository {
//
//    private suspend fun requireUserId(): String {
//        return userContextProvider.getCurrentUserId()
//            ?: throw IllegalStateException("User not authenticated")
//    }
//
//    override suspend fun getStatistics(): Statistics {
//        val userId = requireUserId()
//        // 先从本地获取
//        val localStats = localDataSource.getStatistics(userId)
//        if (localStats != null) {
//            return localStats
//        }
//
//        // 如果本地没有，从远程获取
//        return try {
//            val remoteStats = remoteDataSource.getStatistics()
//            localDataSource.saveStatistics(remoteStats, userId)
//            remoteStats
//        } catch (_: Exception) {
//            Statistics() // 如果远程获取失败，返回空统计
//        }
//    }
//
//    override suspend fun refreshStatistics(): Statistics {
//        val userId = requireUserId()
//        return try {
//            // 从远程刷新统计数据
//            val remoteStats = remoteDataSource.getStatistics()
//            localDataSource.saveStatistics(remoteStats, userId)
//            remoteStats
//        } catch (_: Exception) {
//            // 如果远程刷新失败，返回本地数据
//            localDataSource.getStatistics(userId) ?: Statistics()
//        }
//    }
//
//    override fun observeStatistics(): Flow<Statistics> {
//        // 注意：Flow 需要特殊处理，暂时返回空统计
//        return kotlinx.coroutines.flow.flowOf(Statistics())
//    }
//}
//

package tech.zhifu.app.myhub.datastore.datasource

/**
 * 用户上下文提供者接口
 * 用于在数据访问时自动获取当前用户ID
 */
interface UserContextProvider {
    /**
     * 获取当前用户ID
     * @return 当前用户ID，如果用户未登录则返回 null
     */
    suspend fun getCurrentUserId(): String?
}


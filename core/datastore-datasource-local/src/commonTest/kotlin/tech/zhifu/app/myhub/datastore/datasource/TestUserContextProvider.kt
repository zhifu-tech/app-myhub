package tech.zhifu.app.myhub.datastore.datasource

/**
 * 测试用的 UserContextProvider 实现
 * 用于在测试中模拟用户上下文
 */
class TestUserContextProvider(
    private var userId: String? = "test-user-1"
) : UserContextProvider {
    
    /**
     * 设置当前用户ID
     */
    fun setUserId(userId: String?) {
        this.userId = userId
    }
    
    /**
     * 清除当前用户ID
     */
    fun clearUserId() {
        this.userId = null
    }
    
    override suspend fun getCurrentUserId(): String? {
        return userId
    }
}


package tech.zhifu.app.myhub.datastore.database

/**
 * WASM 平台测试数据库创建实现
 *
 * 使用 WebWorkerDriver 创建内存数据库，适用于浏览器环境测试。
 * 注意：必须使用异步方法（awaitCreate, await）因为 WebWorkerDriver 是异步的。
 *
 * 参考 SQLDelight 官方文档：
 * https://sqldelight.github.io/sqldelight/2.0.2/js_sqlite/sqljs_worker/
 *
 * SQLDelight 2.1.0+ 支持 Wasm，使用与 JS 相同的 WebWorkerDriver。
 *
 * 截至目前（2025）：
 *
 * ❌ Kotlin/Wasm 还无法在测试代码中可靠地拿到 JS 全局对象里的 SQLDelight Driver 实例
 * ❌ external fun + js() 在 suspend / expect-actual / test 环境 都不成立
 *
 * ✅ 官方 & 实践推荐方案：
 *
 * Web 数据库测试 → 用 jsMain
 *
 * WasmMain → 只跑 UI / 纯逻辑 / Fake DB
 *
 * 真正的 DB 行为测试不放在 wasmTest
 */
actual suspend fun createTestDatabase(): MyHubDatabase {
    error(
        """
        WASM test environment does not support accessing SQLDelight drivers via JS interop.

        Recommended alternatives:
        1. Use jsTest for web database testing
        2. Use fake/in-memory drivers for wasmTest
        3. Limit wasmTest to UI and pure logic

        This is a known limitation of Kotlin/Wasm + SQLDelight.
        """.trimIndent()
    )
}

/**
 * WASM 平台：空实现
 * 
 * WASM 平台不支持数据库测试，此函数不会被调用。
 */
actual fun destroyTestDatabase(database: MyHubDatabase) {
    // WASM 平台不支持数据库测试，此函数不会被调用
}


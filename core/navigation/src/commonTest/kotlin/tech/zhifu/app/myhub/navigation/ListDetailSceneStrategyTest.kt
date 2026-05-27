package tech.zhifu.app.myhub.navigation

import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlinx.serialization.Serializable

/**
 * ListDetailSceneStrategy 单元测试
 *
 * 使用最小 NavKey 实现验证策略可实例化；calculateScene 依赖 SceneStrategyScope，
 * 需在集成/UI 测试中验证返回 null 的行为。
 */
class ListDetailSceneStrategyTest {

    @Serializable
    data object TestNavKey : NavKey

    @Test
    fun `ListDetailSceneStrategy can be instantiated`() {
        val strategy = ListDetailSceneStrategy<TestNavKey>()
        assertNotNull(strategy)
    }
}

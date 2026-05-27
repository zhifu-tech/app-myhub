package tech.zhifu.app.myhub.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * 简单的 List-Detail 场景策略（KMP 全平台支持）
 *
 * 这是一个纯 KMP 实现，不依赖任何平台特定的库。
 *
 * 实现逻辑：
 * - 返回 null，让 NavDisplay 使用默认行为（显示最后一个 entry）
 *
 * 注意：这是一个简化实现。如果需要更复杂的 List-Detail 布局逻辑，
 * 可以根据窗口大小类（WindowSizeClass）来决定是否同时显示 List 和 Detail。
 *
 * 参考：Navigation 3 的 NavDisplay 默认会显示返回栈中的最后一个 entry，
 * 这对于大多数场景已经足够。
 */
class ListDetailSceneStrategy<Key : NavKey> : SceneStrategy<Key> {
    override fun SceneStrategyScope<Key>.calculateScene(entries: List<NavEntry<Key>>): Scene<Key>? {
        // 返回 null，让 NavDisplay 使用默认行为
        // NavDisplay 会默认显示返回栈中的最后一个 entry
        return null
    }
}

@Composable
fun <Key : NavKey> rememberListDetailSceneStrategy(): ListDetailSceneStrategy<Key> {
    return remember(Unit) {
        ListDetailSceneStrategy()
    }
}

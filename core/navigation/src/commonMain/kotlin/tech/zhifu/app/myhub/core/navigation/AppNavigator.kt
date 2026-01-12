package tech.zhifu.app.myhub.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * 应用导航器
 *
 * 负责处理导航逻辑，包括：
 * - 导航到指定的 NavKey
 * - 返回操作
 * - 应用级导航项切换
 * - 子栈管理
 *
 * @param state 应用导航状态
 */
class AppNavigator(val state: AppNavigationState) {
    /**
     * 导航到指定的 NavKey
     *
     * 行为说明：
     * - 如果导航到当前 AppKey → 清空当前 SubStack，返回到 AppKey 的根屏幕
     *   （遵循 Android BottomNav 再点回到 root 的标准行为）
     * - 如果导航到其他 AppKey → 切换到该 AppKey，保持其 SubStack 状态
     * - 如果导航到 FeatureKey → 在当前 SubStack 中导航
     *
     * @param key 目标导航键
     */
    fun navigate(key: NavKey) {
        when (key) {
            state.currentAppKey -> {
                //  清空当前 SubStack（返回到应用键的根屏幕）
                state.currentSubStack.run {
                    if (size > 1) subList(1, size).clear()
                }
            }

            in state.appKeys -> {
                // 导航到应用键（切换应用级导航项）
                state.appStack.apply {
                    if (key == state.startKey) {
                        clear()
                    } else {
                        remove(key)
                    }
                    add(key)
                }
            }

            else -> {
                // 导航到功能键（在当前 SubStack 中）
                state.currentSubStack.apply {
                    remove(key)
                    add(key)
                }
            }
        }
    }

    /**
     * 返回操作
     *
     * 行为说明：
     * - 如果当前在起始键 → 无法返回（抛出错误）
     * - 如果当前在应用键的底部 → 尝试返回到上一个应用栈
     * - 如果当前在子栈中 → 正常返回
     */
    fun goBack() {
        when (state.currentKey) {
            state.startKey -> {
                // 已在起始键，无法返回
                // ⚠️ 关键约束：App 级 root 不可被 pop
                // 在 Desktop / Web 平台，可能需要委托给平台处理（如关闭窗口）
                error("Cannot go back from start key")
            }

            state.currentAppKey -> {
                // 在应用键的底部，尝试返回到上一个应用栈
                // ⚠️ 关键修复：明确约束 AppStack 至少保留一个元素
                if (state.appStack.size > 1) {
                    state.appStack.removeLast()
                } else {
                    // AppStack 只剩一个元素（startKey），无法返回
                    // 在 Desktop / Web 平台，可能需要委托给平台处理（如关闭窗口）
                    // 在 Android / iOS 平台，通常忽略此操作或退出应用
                }
            }

            else -> {
                // 在子栈中，正常返回
                state.currentSubStack.removeLastOrNull()
            }
        }
    }
}

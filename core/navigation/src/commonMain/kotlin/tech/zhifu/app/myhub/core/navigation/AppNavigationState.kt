package tech.zhifu.app.myhub.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * 创建应用导航状态
 *
 * @param startKey 起始导航键（通常是默认的应用级导航项）
 * @param appKeys 所有应用级导航键的集合
 * @return AppNavigationState 实例
 */
@Composable
fun rememberAppNavigationState(
    startKey: NavKey,
    appKeys: Set<NavKey>,
): AppNavigationState {
    // ⚠️ v1.0.1 修正：`Navigation 3` 1.1.0-alpha01 版本需要显式提供 SavedStateConfiguration
    // 配置 SerializersModule 以支持多态序列化（NavKey 的所有子类型）
    // 注意：需要显式注册所有具体的 NavKey 子类型，不能只注册 sealed interface
    val savedStateConfiguration = remember {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    // TODO 考虑重构，满足开闭原则，新增或者移除不需要在这里手动注册
                    // 注册 AppNavKey 的所有具体子类型
                    subclass(AppNavKey.Dashboard::class)
                    subclass(AppNavKey.Profile::class)
                    // 注册 FeatureNavKey 的所有具体子类型
                    subclass(FeatureNavKey.CardDetail::class)
                    subclass(FeatureNavKey.AllCards::class)
                }
            }
        }
    }
    val appStack = rememberNavBackStack(savedStateConfiguration, startKey)

    // ⚠️ 关键修复：参考 NIA 的实现方式，直接在 associateWith 中调用 rememberNavBackStack
    // 注意：associateWith 的 lambda 是 Composable 上下文，可以安全地调用 rememberNavBackStack
    // 每个 subStack 使用相同的 savedStateConfiguration 以确保状态持久化
    val subStacks = appKeys.associateWith { key ->
        rememberNavBackStack(savedStateConfiguration, key)
    }

    // 直接返回 AppNavigationState
    // 注意：appStack 和 subStacks 已经通过 rememberNavBackStack 进行了 remember，所以不需要再次 remember
    return AppNavigationState(
        startKey = startKey,
        appStack = appStack,
        subStacks = subStacks,
    )
}

/**
 * 应用导航状态
 *
 * 管理双层返回栈架构：
 * - AppStack：应用级导航项的返回栈
 * - SubStacks：每个应用级导航项对应的子返回栈
 *
 * @param startKey 起始导航键
 * @param appStack 应用级返回栈
 * @param subStacks 子返回栈映射（key: AppNavKey, value: NavBackStack）
 */
class AppNavigationState(
    val startKey: NavKey,
    val appStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    /**
     * 当前应用级导航键
     */
    val currentAppKey: NavKey by derivedStateOf {
        appStack.last()
    }

    /**
     * 所有应用级导航键
     */
    val appKeys: Set<NavKey>
        get() = subStacks.keys

    /**
     * 当前子返回栈
     */
    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentAppKey]
            ?: error("Sub stack for $currentAppKey does not exist")

    /**
     * 当前导航键（当前子返回栈的最后一个元素）
     */
    val currentKey: NavKey by derivedStateOf {
        currentSubStack.last()
    }
}

/**
 * 将 AppNavigationState 转换为 NavEntry 列表
 *
 * 使用状态装饰器自动管理状态：
 * - SaveableStateHolder：保存和恢复 Compose 状态（全平台支持）
 *
 * @param entryProvider 将 NavKey 转换为 NavEntry 的函数
 * @return NavEntry 列表
 */
@Composable
fun AppNavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    // ⚠️ 性能说明：虽然每次 recomposition 都会执行 mapValues，
    // 但 rememberDecoratedNavEntries 内部会 remember，确保每个 SubStack 的
    // decorated entries 是独立且稳定的。
    //
    // 注意：此实现依赖 Map 的 iteration 顺序稳定性。
    // 如果 appKeys 在运行时变化，可能需要额外的稳定性保证。
    val decoratedEntries = subStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    // 注意：由于内部编译器错误，暂时不使用 remember
    // decoratedEntries 已经通过 rememberDecoratedNavEntries 进行了 remember
    return appStack
        .flatMap { key -> decoratedEntries[key] ?: emptyList() }
        .toMutableStateList()
}

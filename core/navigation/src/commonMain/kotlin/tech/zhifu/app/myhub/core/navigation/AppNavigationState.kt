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

@Composable
fun rememberAppNavigationState(
    startKey: NavKey,
    appKeys: Set<NavKey>,
): AppNavigationState {
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
    val featureStacks = appKeys.associateWith { key ->
        rememberNavBackStack(savedStateConfiguration, key)
    }

    return remember(startKey, appKeys) {
        AppNavigationState(
            startKey = startKey,
            appStack = appStack,
            featureStacks = featureStacks,
        )
    }
}

class AppNavigationState(
    val startKey: NavKey,
    val appStack: NavBackStack<NavKey>,
    val featureStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentAppKey: NavKey by derivedStateOf {
        appStack.last()
    }

    val appKeys: Set<NavKey>
        get() = featureStacks.keys

    val currentFeatureStack: NavBackStack<NavKey>
        get() = featureStacks[currentAppKey]
            ?: error("Feature stack for $currentAppKey does not exist")

    val currentKey: NavKey by derivedStateOf {
        currentFeatureStack.last()
    }
}

@Composable
fun AppNavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {

    val decoratedEntries = featureStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    // 优化：使用 remember 稳定返回的列表，避免每次重组都创建新对象
    // 使用 appStack 的内容（toList()）和 decoratedEntries 的 keys 作为依赖项
    // 这样只有当导航栈真正变化时才重新计算
    val appStackKeys = appStack.toList()
    val decoratedEntriesKeys = decoratedEntries.keys.toList()

    return remember(appStackKeys, decoratedEntriesKeys, decoratedEntries) {
        appStack
            .flatMap { key -> decoratedEntries[key] ?: emptyList() }
            .toMutableStateList()
    }
}

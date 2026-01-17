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

@Composable
fun rememberAppNavigationState(
    startKey: NavKey,
    appKeys: Set<NavKey>,
    navKeysSerializerModule: SerializersModule,
): AppNavigationState {
    val savedStateConfiguration = SavedStateConfiguration {
        serializersModule = navKeysSerializerModule
    }
    val appStack = rememberNavBackStack(savedStateConfiguration, startKey)
    val subStacks = appKeys.associateWith { key ->
        rememberNavBackStack(savedStateConfiguration, key)
    }

    return remember(startKey, appKeys) {
        AppNavigationState(
            startKey = startKey,
            appStack = appStack,
            subStacks = subStacks,
        )
    }
}

class AppNavigationState(
    val startKey: NavKey,
    val appStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentAppKey: NavKey by derivedStateOf {
        appStack.last()
    }

    val appKeys: Set<NavKey>
        get() = subStacks.keys

    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentAppKey]
            ?: error("Feature stack for $currentAppKey does not exist")

    val currentKey: NavKey by derivedStateOf {
        currentSubStack.last()
    }
}

@Composable
fun AppNavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {

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

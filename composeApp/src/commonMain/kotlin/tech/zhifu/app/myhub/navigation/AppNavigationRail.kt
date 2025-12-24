package tech.zhifu.app.myhub.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.components.Avatar
import tech.zhifu.app.myhub.resources.Res
import tech.zhifu.app.myhub.resources.app_name
import tech.zhifu.app.myhub.resources.new_card
import tech.zhifu.app.myhub.resources.premium
import tech.zhifu.app.myhub.resources.scholar_name

/**
 * NavigationRail intended for use on Desktop/Tablet.
 */
@Composable
fun AppNavigationRail(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier.Companion
) {
    val items = listOf(
        NavItem.Dashboard,
        NavItem.Favorites,
        NavItem.Settings
    )
    val railWidth by animateDpAsState(targetValue = if (isExpanded) 280.dp else 80.dp)

    NavigationRail(
        modifier = modifier.fillMaxHeight().width(railWidth),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Column(
                horizontalAlignment = if (isExpanded) Alignment.Companion.Start else Alignment.Companion.CenterHorizontally,
                modifier = Modifier.Companion.fillMaxWidth()
                    .padding(horizontal = if (isExpanded) 16.dp else 0.dp)
            ) {
                HeaderSection(isExpanded = isExpanded)
                Spacer(Modifier.Companion.height(12.dp))

                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { expanded ->
                    if (expanded) {
                        ExtendedFloatingActionButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier.Companion.fillMaxWidth().padding(vertical = 8.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.Companion.width(12.dp))
                            Text(stringResource(Res.string.new_card))
                        }
                    } else {
                        SmallFloatingActionButton(
                            onClick = { /* TODO */ },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                            modifier = Modifier.Companion.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(Res.string.new_card)
                            )
                        }
                    }
                }
            }
        },
        content = {
            Spacer(Modifier.Companion.height(16.dp))
            Column(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    AnimatedContent(
                        targetState = isExpanded,
                        transitionSpec = {
                            (fadeIn() + slideInHorizontally { it / 2 }) togetherWith (fadeOut() + slideOutHorizontally { -it / 2 })
                        }
                    ) { expanded ->
                        if (expanded) {
                            NavigationDrawerItem(
                                label = { Text(stringResource(item.labelKey)) },
                                selected = currentScreen == item.screen,
                                onClick = { onNavigate(item.screen) },
                                icon = { Icon(item.icon, contentDescription = null) },
                                modifier = Modifier.Companion.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    unselectedContainerColor = Color.Companion.Transparent
                                )
                            )
                        } else {
                            NavigationRailItem(
                                selected = currentScreen == item.screen,
                                onClick = { onNavigate(item.screen) },
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = stringResource(item.labelKey)
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(item.labelKey),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                alwaysShowLabel = true
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.Companion.weight(1f))
            Box(modifier = Modifier.Companion.padding(bottom = 16.dp)) {
                UserProfileSection(isExpanded = isExpanded)
            }
        }
    )
}

@Composable
private fun HeaderSection(isExpanded: Boolean) {
    Row(
        modifier = Modifier
            .then(if (isExpanded) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "MyHub",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun UserProfileSection(isExpanded: Boolean) {
    Surface(
        onClick = { /* TODO */ },
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        modifier = Modifier.padding(horizontal = if (isExpanded) 12.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .then(if (isExpanded) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Avatar()
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.scholar_name),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(Res.string.premium),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

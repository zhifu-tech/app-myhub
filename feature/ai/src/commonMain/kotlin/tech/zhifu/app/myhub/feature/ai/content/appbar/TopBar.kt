package tech.zhifu.app.myhub.feature.ai.content.appbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.resources.Res
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_top_bar_back
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_top_bar_settings
import tech.zhifu.app.myhub.feature.ai.resources.feature_ai_top_bar_title
import tech.zhifu.app.myhub.feature.settings.api.navigateToSettings
import tech.zhifu.app.myhub.navigation.AppNavigator

@Composable
fun TopBar(
    navigator: AppNavigator,
) {
    TopBarContent(
        onClickBack = navigator::goBack,
        actions = {
            IconButton(
                onClick = navigator::navigateToSettings
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(Res.string.feature_ai_top_bar_settings),
                )
            }
        }
    )
}

@Composable
internal fun TopBarContent(
    actions: @Composable RowScope.() -> Unit = {},
    onClickBack: () -> Unit,
) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
        navigationIcon = {
            IconButton(
                onClick = onClickBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(Res.string.feature_ai_top_bar_back),
                )
            }
        },
        title = {
            Text(
                text = stringResource(Res.string.feature_ai_top_bar_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        actions = actions
    )
}

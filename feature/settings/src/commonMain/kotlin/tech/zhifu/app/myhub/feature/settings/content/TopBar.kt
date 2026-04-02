package tech.zhifu.app.myhub.feature.settings.content

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.viewmodel.navigateBack
import tech.zhifu.app.myhub.ui.design.resources.settings
import tech.zhifu.app.myhub.ui.design.resources.Res as DesignRes

@Composable
internal fun TopBar(
    modifier: Modifier,
    viewModel: SettingsViewModel,
) {
    TopBarContent(
        modifier = modifier,
        navigateBack = viewModel::navigateBack
    )
}

@Composable
internal fun TopBarContent(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 12.dp),
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = Color.Transparent,
//            scrolledContainerColor = Color.Transparent,
//        ),
        navigationIcon = {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        title = {
            Text(stringResource(DesignRes.string.settings))
        },
    )

}

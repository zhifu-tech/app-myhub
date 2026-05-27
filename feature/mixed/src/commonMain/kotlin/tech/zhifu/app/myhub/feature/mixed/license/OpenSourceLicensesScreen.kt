package tech.zhifu.app.myhub.feature.mixed.license

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.mixed.resources.Res
import tech.zhifu.app.myhub.feature.mixed.resources.feature_mixed_open_source_licenses
import tech.zhifu.app.myhub.navigation.AppNavigator

@Composable
fun OpenSourceLicensesRoute(
    navigator: AppNavigator,
    config: OpenSourceLicensesConfig = OpenSourceLicensesCatalog.defaultConfig(),
) {
    OpenSourceLicensesScreen(
        config = config,
        onBack = navigator::goBack,
    )
}

@Composable
fun OpenSourceLicensesScreen(
    config: OpenSourceLicensesConfig,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(Res.string.feature_mixed_open_source_licenses))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
        ) {
            config.sections.forEach { section ->
                item {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                }
                items(section.items) { item ->
                    ListItem(
                        headlineContent = {
                            Text(text = item.name)
                        },
                        supportingContent = {
                            val artifactText = item.artifacts.joinToString()
                            val detailText = buildString {
                                append("License: ")
                                append(item.license)
                                if (artifactText.isNotBlank()) {
                                    append("\n")
                                    append(artifactText)
                                }
                                item.notice?.let {
                                    append("\n")
                                    append(it)
                                }
                            }
                            Text(
                                text = detailText,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = item.licenseText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

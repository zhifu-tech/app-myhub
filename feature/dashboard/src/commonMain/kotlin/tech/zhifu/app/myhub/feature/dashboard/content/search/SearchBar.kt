package tech.zhifu.app.myhub.feature.dashboard.content.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.collectSearchStateQuery
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.collectSideEffectResetSearch
import tech.zhifu.app.myhub.feature.dashboard.viewmodel.updateSearchStateQuery
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.logger

@Composable
fun SearchBar(
    modifier: Modifier,
    viewModel: DashboardViewModel
) {
    val query = viewModel.collectSearchStateQuery().value.orEmpty()

    logger.debug {
        "SearchBarRoute is called $query"
    }

    val focusManager = LocalFocusManager.current
    viewModel.collectSideEffectResetSearch { effect ->
        logger.debug { "SearchBarRoute collectSideEffect is  $effect" }
        focusManager.clearFocus()
    }

    SearchBarContent(
        query = query,
        onQueryChange = { query ->
            logger.debug { "SearchBarRoute updateSearchStateQuery  $query" }
            viewModel.updateSearchStateQuery(query)
        },
        modifier = modifier,
    )
}

@Composable
fun SearchBarContent(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val barHeight: Dp = 56.dp
    val iconGap: Dp = 12.dp
    val horizontalPadding: Dp = 20.dp
    val placeholderStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val inputStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    )

    Surface(
        shape = RoundedCornerShape(9999.dp),
        modifier = modifier.height(barHeight),
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface.copy(0.8f),
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = inputStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .padding(horizontal = horizontalPadding),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(iconGap))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = "搜索",
                                style = placeholderStyle
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
    Spacer(modifier = Modifier.size(12.dp))
}

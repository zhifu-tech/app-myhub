package tech.zhifu.app.myhub.component.mixed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.component.mixed.Avatar
import tech.zhifu.app.myhub.theme.AppTheme

/**
 * Preview 函数 - 浅色主题，默认大小
 */
@Preview
@Composable
private fun AvatarLightPreview() {
    AppTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar()
        }
    }
}

/**
 * Preview 函数 - 深色主题，默认大小
 */
@Preview
@Composable
private fun AvatarDarkPreview() {
    AppTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar()
        }
    }
}

/**
 * Preview 函数 - 不同尺寸
 */
@Preview
@Composable
private fun AvatarSizesPreview() {
    AppTheme(darkTheme = false) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Avatar(size = 24.dp)
                Avatar(size = 32.dp)
                Avatar(size = 40.dp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Avatar(size = 48.dp)
                Avatar(size = 64.dp)
                Avatar(size = 96.dp)
            }
        }
    }
}

/**
 * Preview 函数 - 小尺寸（导航栏使用）
 */
@Preview
@Composable
private fun AvatarSmallPreview() {
    AppTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar(size = 24.dp)
        }
    }
}

/**
 * Preview 函数 - 大尺寸（Profile 页面使用）
 */
@Preview
@Composable
private fun AvatarLargePreview() {
    AppTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar(size = 96.dp)
        }
    }
}


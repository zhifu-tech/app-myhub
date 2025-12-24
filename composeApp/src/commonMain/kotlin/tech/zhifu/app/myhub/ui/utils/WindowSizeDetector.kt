package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * 期望接口：平台特定的窗口大小检测
 */
@Composable
expect fun getWindowSize(): DpSize

/**
 * 默认实现：返回桌面尺寸
 */
@Composable
fun defaultWindowSize(): DpSize = DpSize(1920.dp, 1080.dp)

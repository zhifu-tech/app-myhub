package tech.zhifu.app.myhub.ui.design


import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Devices.DESKTOP
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview

private const val PHONE_LANDSCAPE = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420"
private const val TABLE_LANDSCAPE = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait"

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
@Preview(
    name = "Phone - Light",
    group = "Phone",
    device = PHONE
)
@Preview(
    group = "Phone",
    name = "Phone - Light, Landscape",
    device = PHONE_LANDSCAPE,
    showSystemUi = true,
)
@Preview(
    group = "Phone",
    name = "Phone - Dark",
    device = PHONE,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
@Preview(
    group = "Phone",
    name = "Phone - Dark, Landscape",
    device = PHONE_LANDSCAPE,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
annotation class PreviewPhoneLightDark

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
@Preview(
    name = "Table - Light",
    group = "Table",
    device = TABLET
)
@Preview(
    group = "Table",
    name = "Table - Light, Landscape",
    device = TABLE_LANDSCAPE,
    showSystemUi = true,
)
@Preview(
    group = "Table",
    name = "Table - Dark",
    device = TABLET,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
@Preview(
    group = "Table",
    name = "Table - Dark, Landscape",
    device = TABLE_LANDSCAPE,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
annotation class PreviewTabletLightDark

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
@Preview(
    name = "Desktop - Light",
    group = "Desktop",
    device = DESKTOP
)
@Preview(
    group = "Desktop",
    name = "Desktop - Dark",
    device = DESKTOP,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL
)
annotation class PreviewDesktopLightDark

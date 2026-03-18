package tech.zhifu.app.myhub.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Material 3 调色板
private val DarkColorScheme = darkColorScheme(

    primary = Color(0xFF9FB3FF),
    onPrimary = Color(0xFF0F1E66),
    primaryContainer = Color(0xFF1C2C7A),
    onPrimaryContainer = Color(0xFFDDE4FF),
    inversePrimary = Color(0xFF4A6CF7),

    secondary = Color(0xFFBFC6DA),
    onSecondary = Color(0xFF2A2F3D),
    secondaryContainer = Color(0xFF3A4050),
    onSecondaryContainer = Color(0xFFE3E7F0),

    tertiary = Color(0xFFC5CAE9),
    onTertiary = Color(0xFF1B1F2C),
    tertiaryContainer = Color(0xFF2C3142),
    onTertiaryContainer = Color(0xFFE6E9F4),

    background = Color(0xFF0F1116),
    onBackground = Color(0xFFE6E8EF),

    surface = Color(0xFF14161D),
    onSurface = Color(0xFFE6E8EF),

    surfaceVariant = Color(0xFF222530),
    onSurfaceVariant = Color(0xFFC4C9D4),

    inverseSurface = Color(0xFFE6E8EF),
    inverseOnSurface = Color(0xFF11131A),

    outline = Color(0xFF3B4050),
    outlineVariant = Color(0xFF2C3140),

    scrim = Color(0xFF000000),

    surfaceBright = Color(0xFF1A1C24),
    surfaceDim = Color(0xFF101218),

    surfaceContainerLowest = Color(0xFF0C0E13),
    surfaceContainerLow = Color(0xFF14161D),
    surfaceContainer = Color(0xFF191C24),
    surfaceContainerHigh = Color(0xFF1E212A),
    surfaceContainerHighest = Color(0xFF242833),

    primaryFixed = Color(0xFFDDE4FF),
    primaryFixedDim = Color(0xFF9FB3FF),
    onPrimaryFixed = Color(0xFF000E3C),
    onPrimaryFixedVariant = Color(0xFF2B3F9A),

    secondaryFixed = Color(0xFFE3E7F0),
    secondaryFixedDim = Color(0xFFBFC6DA),
    onSecondaryFixed = Color(0xFF11131A),
    onSecondaryFixedVariant = Color(0xFF3A4050),

    tertiaryFixed = Color(0xFFE6E9F4),
    tertiaryFixedDim = Color(0xFFC5CAE9),
    onTertiaryFixed = Color(0xFF0F1116),
    onTertiaryFixedVariant = Color(0xFF2C3142),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColorScheme = lightColorScheme(

    primary = Color(0xFF4A6CF7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4EAFF),
    onPrimaryContainer = Color(0xFF001B5E),
    inversePrimary = Color(0xFF9FB3FF),

    secondary = Color(0xFF5B6275),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE3E7F0),
    onSecondaryContainer = Color(0xFF171C28),

    tertiary = Color(0xFF7A84A3),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6E9F4),
    onTertiaryContainer = Color(0xFF101424),

//    background = Color(0xFFF7F8FB),
    background = Color(0xFF1F2F6),
    onBackground = Color(0xFF11131A),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF11131A),

    surfaceVariant = Color(0xFFE9ECF4),
    onSurfaceVariant = Color(0xFF424756),

    inverseSurface = Color(0xFF1A1C24),
    inverseOnSurface = Color(0xFFF1F2F8),

    outline = Color(0xFFC5CAD6),
    outlineVariant = Color(0xFFDDE1EA),

    scrim = Color(0xFF000000),

    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFEEF1F7),

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F8FB),
    surfaceContainer = Color(0xFFF0F2F8),
    surfaceContainerHigh = Color(0xFFE9ECF4),
    surfaceContainerHighest = Color(0xFFE2E6EF),

    primaryFixed = Color(0xFFE4EAFF),
    primaryFixedDim = Color(0xFF9FB3FF),
    onPrimaryFixed = Color(0xFF000E3C),
    onPrimaryFixedVariant = Color(0xFF2B3F9A),

    secondaryFixed = Color(0xFFE3E7F0),
    secondaryFixedDim = Color(0xFFBFC6DA),
    onSecondaryFixed = Color(0xFF11131A),
    onSecondaryFixedVariant = Color(0xFF3A4050),

    tertiaryFixed = Color(0xFFE6E9F4),
    tertiaryFixedDim = Color(0xFFC5CAE9),
    onTertiaryFixed = Color(0xFF0F1116),
    onTertiaryFixedVariant = Color(0xFF2C3142),

    error = Color(0xFFD32F2F),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFDECEA),
    onErrorContainer = Color(0xFF410002),
)

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = getTypography()

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = typography,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}

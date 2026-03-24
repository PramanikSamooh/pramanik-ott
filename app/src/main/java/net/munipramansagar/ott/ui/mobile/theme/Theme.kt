package net.munipramansagar.ott.ui.mobile.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import net.munipramansagar.ott.util.LanguageManager

private val DarkColorScheme = darkColorScheme(
    primary = Saffron,
    onPrimary = TextWhite,
    primaryContainer = SaffronDark,
    onPrimaryContainer = TextWhite,
    secondary = Gold,
    onSecondary = TextWhite,
    secondaryContainer = Gold,
    onSecondaryContainer = TextWhite,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = LiveRed,
    onError = TextWhite,
    outline = TextMuted,
    outlineVariant = CardBorder,
    inverseSurface = TextWhite,
    inverseOnSurface = Background,
    surfaceTint = Saffron
)

private val LightColorScheme = lightColorScheme(
    primary = Saffron,
    onPrimary = LightBackground,
    primaryContainer = SaffronLight,
    onPrimaryContainer = LightTextPrimary,
    secondary = Gold,
    onSecondary = LightBackground,
    secondaryContainer = GoldLight,
    onSecondaryContainer = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    error = LiveRed,
    onError = LightBackground,
    outline = LightTextMuted,
    outlineVariant = LightCardBorder,
    inverseSurface = LightTextPrimary,
    inverseOnSurface = LightBackground,
    surfaceTint = Saffron
)

private val PramanikShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun PramanikTheme(
    themeMode: String = LanguageManager.THEME_DARK,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        LanguageManager.THEME_LIGHT -> false
        LanguageManager.THEME_SYSTEM -> isSystemInDarkTheme()
        else -> true // dark (default)
    }

    MaterialTheme(
        colorScheme = if (useDark) DarkColorScheme else LightColorScheme,
        typography = PramanikTypography,
        shapes = PramanikShapes,
        content = content
    )
}

package net.munipramansagar.ott.ui.mobile.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Saffron,
    onPrimary = OnBackground,
    primaryContainer = SaffronDark,
    secondary = Gold,
    onSecondary = OnBackground,
    secondaryContainer = Gold,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = LiveRed,
    outline = OnSurfaceVariant
)

@Composable
fun PramanikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = PramanikTypography,
        content = content
    )
}

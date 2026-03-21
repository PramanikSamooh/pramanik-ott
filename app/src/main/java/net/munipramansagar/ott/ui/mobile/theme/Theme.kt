package net.munipramansagar.ott.ui.mobile.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

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

private val PramanikShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun PramanikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = PramanikTypography,
        shapes = PramanikShapes,
        content = content
    )
}

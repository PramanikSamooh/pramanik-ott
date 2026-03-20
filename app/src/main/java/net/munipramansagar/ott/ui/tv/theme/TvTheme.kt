package net.munipramansagar.ott.ui.tv.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme

val Saffron = Color(0xFFE8730A)
val DarkBackground = Color(0xFF1A1A2E)
val Surface = Color(0xFF16213E)
val GoldAccent = Color(0xFFC9932A)
val TextWhite = Color(0xFFEEEEEE)
val TextGray = Color(0xFFAAAAAA)
val CardSurface = Color(0xFF1E2A47)
val FocusBorder = Color(0xFFE8730A)
val Red = Color(0xFFFF1744)

@Immutable
data class TvTypography(
    val displayLarge: TextStyle = TextStyle(
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        color = TextWhite,
        letterSpacing = (-0.5).sp
    ),
    val displayMedium: TextStyle = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = TextWhite
    ),
    val headlineLarge: TextStyle = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextWhite
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextWhite
    ),
    val titleLarge: TextStyle = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = TextWhite
    ),
    val titleMedium: TextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = TextWhite
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = TextWhite
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = TextGray
    ),
    val labelLarge: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = TextWhite
    ),
    val labelMedium: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = TextGray
    )
)

@Immutable
data class TvShapes(
    val card: RoundedCornerShape = RoundedCornerShape(8.dp),
    val button: RoundedCornerShape = RoundedCornerShape(6.dp),
    val banner: RoundedCornerShape = RoundedCornerShape(12.dp),
    val badge: RoundedCornerShape = RoundedCornerShape(4.dp)
)

val LocalTvTypography = staticCompositionLocalOf { TvTypography() }
val LocalTvShapes = staticCompositionLocalOf { TvShapes() }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PramanikTvTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTvTypography provides TvTypography(),
        LocalTvShapes provides TvShapes()
    ) {
        MaterialTheme {
            content()
        }
    }
}

object PramanikTvTheme {
    val typography: TvTypography
        @Composable get() = LocalTvTypography.current
    val shapes: TvShapes
        @Composable get() = LocalTvShapes.current
}

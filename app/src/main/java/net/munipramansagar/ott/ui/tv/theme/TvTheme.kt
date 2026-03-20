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

// ── Premium Dark Palette ──────────────────────────────────────────
val DarkBg = Color(0xFF0B0B1A)
val DarkBg2 = Color(0xFF12122A)
val GlassSurface = Color(0x0FFFFFFF)   // 6% white
val GlassCard = Color(0x1AFFFFFF)      // 10% white
val GlassBorder = Color(0x14FFFFFF)    // 8% white
val GlassHighlight = Color(0x29FFFFFF) // 16% white (for hover/focus bg)

val Saffron = Color(0xFFE8730A)
val SaffronLight = Color(0xFFF59340)
val SaffronDim = Color(0x40E8730A)     // 25% saffron – glow
val Gold = Color(0xFFC9932A)
val GoldLight = Color(0xFFDDB040)

val TextWhite = Color(0xFFEEEEEE)
val TextGray = Color(0xFF888888)
val TextMuted = Color(0xFF555555)
val Red = Color(0xFFFF1744)

// Legacy aliases so existing code that references old names still compiles
val DarkBackground = DarkBg
val Surface = GlassSurface
val CardSurface = GlassCard
val FocusBorder = Saffron
val GoldAccent = Gold

// ── Typography ────────────────────────────────────────────────────
@Immutable
data class TvTypography(
    val displayLarge: TextStyle = TextStyle(
        fontSize = 44.sp,
        fontWeight = FontWeight.Bold,
        color = TextWhite,
        letterSpacing = (-0.5).sp,
        lineHeight = 50.sp
    ),
    val displayMedium: TextStyle = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = TextWhite,
        letterSpacing = (-0.3).sp,
        lineHeight = 42.sp
    ),
    val headlineLarge: TextStyle = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextWhite,
        lineHeight = 34.sp
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextWhite,
        lineHeight = 28.sp
    ),
    val titleLarge: TextStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = TextWhite,
        lineHeight = 26.sp
    ),
    val titleMedium: TextStyle = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium,
        color = TextWhite,
        lineHeight = 22.sp
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        color = TextWhite,
        lineHeight = 20.sp
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        color = TextGray,
        lineHeight = 18.sp
    ),
    val labelLarge: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextWhite,
        lineHeight = 20.sp
    ),
    val labelMedium: TextStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextGray,
        lineHeight = 16.sp
    )
)

// ── Shapes ────────────────────────────────────────────────────────
@Immutable
data class TvShapes(
    val card: RoundedCornerShape = RoundedCornerShape(12.dp),
    val cardTop: RoundedCornerShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
    val button: RoundedCornerShape = RoundedCornerShape(24.dp),
    val banner: RoundedCornerShape = RoundedCornerShape(20.dp),
    val badge: RoundedCornerShape = RoundedCornerShape(8.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(50),
    val sidebar: RoundedCornerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    val searchField: RoundedCornerShape = RoundedCornerShape(16.dp)
)

// ── Composition Locals ────────────────────────────────────────────
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

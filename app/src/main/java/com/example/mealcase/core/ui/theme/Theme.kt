package com.example.mealcase.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mealcase.R

/**
 * "Organic" design system: a cream base, two flat accent colors used in large blocks rather
 * than small highlights, an oversized display face carrying the visual weight, and generous
 * pill/rounded shapes. See [MealCaseTheme].
 */
object Organic {
    val Cream = Color(0xFFF5EAD8)
    val CreamHigh = Color(0xFFFBF5EA)
    val Ink = Color(0xFF2B2117)
    val InkMuted = Color(0xFF6F6355)
    val Divider = Color(0xFFE2D5BE)

    val Terracotta = Color(0xFFC67139)
    val TerracottaHigh = Color(0xFFDB8A52)
    val Terracotta600 = Color(0xFFA85A2B)

    val Sage = Color(0xFF7A8A5E)
    val SageHigh = Color(0xFF97A578)
}

private val CaprasimoFamily = FontFamily(
    Font(R.font.caprasimo, FontWeight.Normal),
)

private val FigtreeFamily = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.SemiBold),
    Font(R.font.figtree_bold, FontWeight.Bold),
)

/** Display styles use Caprasimo; everything else stays on Figtree. */
val OrganicTypography = Typography(
    displayLarge = TextStyle(fontFamily = CaprasimoFamily, fontSize = 40.sp, lineHeight = 44.sp),
    displayMedium = TextStyle(fontFamily = CaprasimoFamily, fontSize = 32.sp, lineHeight = 36.sp),
    displaySmall = TextStyle(fontFamily = CaprasimoFamily, fontSize = 26.sp, lineHeight = 30.sp),
    headlineLarge = TextStyle(fontFamily = CaprasimoFamily, fontSize = 30.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = CaprasimoFamily, fontSize = 24.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = CaprasimoFamily, fontSize = 20.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontFamily = CaprasimoFamily, fontSize = 20.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    bodyLarge = TextStyle(fontFamily = FigtreeFamily, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FigtreeFamily, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FigtreeFamily, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
    ),
)

private val OrganicShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val LightColors = lightColorScheme(
    primary = Organic.Terracotta,
    onPrimary = Organic.CreamHigh,
    primaryContainer = Organic.TerracottaHigh,
    onPrimaryContainer = Organic.CreamHigh,
    secondary = Organic.Sage,
    onSecondary = Organic.CreamHigh,
    secondaryContainer = Organic.SageHigh,
    onSecondaryContainer = Organic.Ink,
    background = Organic.Cream,
    onBackground = Organic.Ink,
    surface = Organic.Cream,
    onSurface = Organic.Ink,
    surfaceVariant = Organic.CreamHigh,
    onSurfaceVariant = Organic.InkMuted,
    outline = Organic.Divider,
    outlineVariant = Organic.Divider,
    error = Color(0xFFB3261E),
)

/**
 * Light-only on purpose: every screen paints the Organic cream/terracotta surfaces directly,
 * so a dark scheme would only swap the inherited content color — leaving shared status text
 * (loading, empty, error) near-white on cream. A real dark theme needs dark Organic surfaces
 * first.
 */
@Composable
fun MealCaseTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = OrganicTypography,
        shapes = OrganicShapes,
        content = content,
    )
}

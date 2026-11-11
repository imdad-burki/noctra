package com.imdadburki.noctra.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * NOCTRA design system — midnight + gold.
 * "Time Beyond Ordinary": near-black surfaces, warm gold accent, generous
 * tracking on display type, restrained animation.
 */
object NoctraColors {
    val Bg = Color(0xFF07070A)
    val Surface = Color(0xFF121218)
    val Elevated = Color(0xFF1C1C24)
    val Border = Color(0xFF2A2A33)
    val Text = Color(0xFFF5F5F7)
    val Secondary = Color(0xFF9A9AA5)
    val Muted = Color(0xFF6E6E78)
    val Gold = Color(0xFFD4AF37)
    val GoldLight = Color(0xFFE8C468)
    val GoldDark = Color(0xFFA88A2E)
    val Success = Color(0xFF30D158)
    val Danger = Color(0xFFFF453A)
    val AmberLed = Color(0xFFFFB340)
    val RedLed = Color(0xFFFF3B30)
    val GreenLed = Color(0xFF30D158)
    val MatrixGreen = Color(0xFF00FF66)
    val CyanGlow = Color(0xFF00E5FF)
}

val NoctraColorScheme = darkColorScheme(
    primary = NoctraColors.Gold,
    onPrimary = NoctraColors.Bg,
    secondary = NoctraColors.GoldLight,
    background = NoctraColors.Bg,
    surface = NoctraColors.Surface,
    surfaceVariant = NoctraColors.Elevated,
    onBackground = NoctraColors.Text,
    onSurface = NoctraColors.Text,
    onSurfaceVariant = NoctraColors.Secondary,
    outline = NoctraColors.Border,
    error = NoctraColors.Danger
)

/** AMOLED: pure black background/surface — every pixel off where possible. */
val NoctraAmoledScheme = darkColorScheme(
    primary = NoctraColors.Gold,
    onPrimary = Color.Black,
    secondary = NoctraColors.GoldLight,
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF0A0A0A),
    onBackground = NoctraColors.Text,
    onSurface = NoctraColors.Text,
    onSurfaceVariant = NoctraColors.Secondary,
    outline = Color(0xFF1A1A1A),
    error = NoctraColors.Danger
)

/** Light theme for the "light" appearance setting. */
val NoctraLightScheme = lightColorScheme(
    primary = NoctraColors.GoldDark,
    onPrimary = Color.White,
    secondary = NoctraColors.GoldDark,
    background = Color(0xFFF5F5F7),
    surface = Color.White,
    surfaceVariant = Color(0xFFEDEDEF),
    onBackground = Color(0xFF111114),
    onSurface = Color(0xFF111114),
    onSurfaceVariant = Color(0xFF55555E),
    outline = Color(0xFFD5D5DA),
    error = NoctraColors.Danger
)

val NoctraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    )
)

/** Monospace for terminal / LED / matrix clock styles. */
val NoctraMono = FontFamily.Monospace

/** Gold accent as a Compose Color, for call sites that don't import the object. */
val Color.Companion.NoctraGold: Color
    @Composable
    @ReadOnlyComposable
    get() = NoctraColors.Gold

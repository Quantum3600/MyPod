package com.bytekoders.mypod.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bytekoders.mypod.R

val ChicagoFontFamily = FontFamily(
    Font(R.font.chicago, FontWeight.Normal),
    Font(R.font.chicago, FontWeight.Medium),
    Font(R.font.chicago, FontWeight.SemiBold),
    Font(R.font.chicago, FontWeight.Bold),
    Font(R.font.chicago, FontWeight.Light),
    Font(R.font.chicago, FontWeight.Thin)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = ChicagoFontFamily),
    displayMedium = TextStyle(fontFamily = ChicagoFontFamily),
    displaySmall = TextStyle(fontFamily = ChicagoFontFamily),
    headlineLarge = TextStyle(fontFamily = ChicagoFontFamily),
    headlineMedium = TextStyle(fontFamily = ChicagoFontFamily),
    headlineSmall = TextStyle(fontFamily = ChicagoFontFamily),
    titleLarge = TextStyle(fontFamily = ChicagoFontFamily),
    titleMedium = TextStyle(fontFamily = ChicagoFontFamily),
    titleSmall = TextStyle(fontFamily = ChicagoFontFamily),
    bodyLarge = TextStyle(
        fontFamily = ChicagoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(fontFamily = ChicagoFontFamily),
    bodySmall = TextStyle(fontFamily = ChicagoFontFamily),
    labelLarge = TextStyle(fontFamily = ChicagoFontFamily),
    labelMedium = TextStyle(fontFamily = ChicagoFontFamily),
    labelSmall = TextStyle(fontFamily = ChicagoFontFamily)
)

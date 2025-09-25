package com.nullinnix.clippr.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import clippr.composeapp.generated.resources.Baloo2_Bold
import clippr.composeapp.generated.resources.Baloo2_ExtraBold
import clippr.composeapp.generated.resources.Baloo2_Medium
import clippr.composeapp.generated.resources.Baloo2_Regular
import clippr.composeapp.generated.resources.Baloo2_SemiBold
import clippr.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font
@Composable
fun Theme(
    content: @Composable () -> Unit
) {
    val BalooFontFamily = FontFamily (
        Font(Res.font.Baloo2_Regular),
        Font(Res.font.Baloo2_Bold, FontWeight.Bold),
        Font(Res.font.Baloo2_Medium, FontWeight.Medium),
        Font(Res.font.Baloo2_SemiBold, FontWeight.SemiBold),
        Font(Res.font.Baloo2_ExtraBold, FontWeight.ExtraBold),
    )

    val defaultTypography = Typography()

    val typography = Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = BalooFontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = BalooFontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = BalooFontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = BalooFontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = BalooFontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = BalooFontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = BalooFontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = BalooFontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = BalooFontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = BalooFontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = BalooFontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = BalooFontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = BalooFontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = BalooFontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = BalooFontFamily)
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = typography,
        content = content
    )
}

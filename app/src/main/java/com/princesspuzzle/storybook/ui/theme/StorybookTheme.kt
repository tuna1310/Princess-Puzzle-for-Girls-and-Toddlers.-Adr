package com.princesspuzzle.storybook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.art.IosThemeColors

private val StorybookColors =
    lightColorScheme(
        primary = IosThemeColors.castlePink,
        onPrimary = IosThemeColors.inkPrimary,
        secondary = IosThemeColors.skyTop,
        onSecondary = IosThemeColors.inkPrimary,
        background = IosThemeColors.skyMid,
        onBackground = IosThemeColors.inkPrimary,
        surface = Color.White,
        onSurface = IosThemeColors.inkPrimary
    )

private val StorybookTypography =
    Typography(
        titleLarge =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = IosThemeColors.inkPrimary
        ),
        titleMedium =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = IosThemeColors.inkPrimary
        ),
        labelLarge =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = IosThemeColors.inkPrimary
        )
    )

@Composable
fun PrincessPuzzleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StorybookColors,
        typography = StorybookTypography,
        content = content
    )
}

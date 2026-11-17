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
import com.princesspuzzle.storybook.art.StorybookPalette

private val StorybookColors =
    lightColorScheme(
        primary = Color(0xFFFF87C2),
        onPrimary = StorybookPalette.Ink,
        secondary = Color(0xFFA5E3FF),
        onSecondary = StorybookPalette.Ink,
        background = Color(0xFFEAF6FF),
        onBackground = StorybookPalette.Ink,
        surface = StorybookPalette.SoftWhite,
        onSurface = StorybookPalette.Ink
    )

private val StorybookTypography =
    Typography(
        titleLarge =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = StorybookPalette.Ink
        ),
        titleMedium =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = StorybookPalette.Ink
        ),
        labelLarge =
        TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = StorybookPalette.Ink
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

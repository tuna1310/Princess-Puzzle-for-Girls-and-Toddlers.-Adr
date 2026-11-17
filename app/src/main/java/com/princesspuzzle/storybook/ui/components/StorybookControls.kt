package com.princesspuzzle.storybook.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.art.StorybookPalette
import com.princesspuzzle.storybook.art.buildStarPath
import com.princesspuzzle.storybook.core.GameConfig

private const val HALF = 0.5f
private const val PILL_CORNER_PERCENT = 50
private const val PILL_MIN_WIDTH_DP = 128
private const val PILL_SHADOW_DP = 4
private const val PILL_BORDER_DP = 2.5f
private const val PILL_PAD_H_DP = 22
private const val PILL_PAD_V_DP = 12
private const val PILL_FONT_SP = 17
private const val BACK_ICON_SCALE = 0.48f
private const val BACK_STROKE_RATIO = 0.14f
private const val BACK_ARROW_RIGHT_X = 0.84f
private const val BACK_ARROW_LEFT_X = 0.18f
private const val BACK_ARROW_HEAD_X = 0.45f
private const val BACK_ARROW_TIP_X = 0.15f
private const val BACK_ARROW_TOP_Y = 0.22f
private const val BACK_ARROW_BOT_Y = 0.78f
private const val STAR_SPACING_DP = 10
private const val STAR_RADIUS_RATIO = 0.46f
private const val STAR_INNER_RATIO = 0.48f
private const val STAR_STROKE_RATIO = 0.08f
private const val STAR_EMPTY_FILL = 0x8CFFFFFF
private const val STAR_EMPTY_STROKE = 0x736A496E
private const val PROGRESS_MIN_HEIGHT_DP = 52
private const val PROGRESS_BORDER_DP = 2.2f
private const val PROGRESS_BG = 0xEBFFFDF9
private const val PROGRESS_ACCENT = 0xFFD964A0
private const val PROGRESS_GAP_DP = 12
private const val PROGRESS_PAD_H_DP = 18
private const val PROGRESS_PAD_V_DP = 10
private const val PROGRESS_FONT_SP = 16

@Composable
fun StorybookCircleBackButton(
    contentDescriptionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = GameConfig.MIN_BUTTON_TOUCH_TARGET_DP.dp
) {
    val shape = CircleShape
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(sizeDp)
            .shadow(PILL_SHADOW_DP.dp, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(StorybookPalette.SoftWhite, StorybookPalette.ButtonMintBottom)
                )
            )
            .border(PILL_BORDER_DP.dp, StorybookPalette.Ink, shape)
            .semantics { contentDescription = contentDescriptionText }
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Canvas(modifier = Modifier.size(sizeDp * BACK_ICON_SCALE)) {
            val w = size.width
            val h = size.height
            val cy = h * HALF
            val path = Path().apply {
                moveTo(w * BACK_ARROW_RIGHT_X, cy)
                lineTo(w * BACK_ARROW_LEFT_X, cy)
                moveTo(w * BACK_ARROW_HEAD_X, h * BACK_ARROW_TOP_Y)
                lineTo(w * BACK_ARROW_TIP_X, cy)
                lineTo(w * BACK_ARROW_HEAD_X, h * BACK_ARROW_BOT_Y)
            }
            drawPath(
                path = path,
                color = StorybookPalette.Ink,
                style = Stroke(width = w * BACK_STROKE_RATIO, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun StorybookPillButton(
    label: String,
    topColor: Color,
    bottomColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    minHeightDp: Dp = GameConfig.MIN_BUTTON_TOUCH_TARGET_DP.dp
) {
    val shape = RoundedCornerShape(PILL_CORNER_PERCENT)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minWidth = PILL_MIN_WIDTH_DP.dp, minHeight = minHeightDp)
            .shadow(PILL_SHADOW_DP.dp, shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .border(PILL_BORDER_DP.dp, StorybookPalette.Ink, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PILL_PAD_H_DP.dp, vertical = PILL_PAD_V_DP.dp)
    ) {
        Text(
            text = label,
            color = StorybookPalette.Ink,
            fontSize = PILL_FONT_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
fun StorybookStarRow(earnedStars: Int, modifier: Modifier = Modifier, totalStars: Int = 3, starSize: Dp = 36.dp) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(STAR_SPACING_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        for (i in 0 until totalStars) {
            val earned = i < earnedStars
            Canvas(modifier = Modifier.size(starSize)) {
                val r = size.minDimension * STAR_RADIUS_RATIO
                val path = buildStarPath(
                    cx = size.width * HALF,
                    cy = size.height * HALF,
                    outerR = r,
                    innerR = r * STAR_INNER_RATIO
                )
                drawPath(
                    path = path,
                    color = if (earned) StorybookPalette.StarGold else Color(STAR_EMPTY_FILL)
                )
                drawPath(
                    path = path,
                    color = if (earned) StorybookPalette.Ink else Color(STAR_EMPTY_STROKE),
                    style = Stroke(width = size.minDimension * STAR_STROKE_RATIO, join = StrokeJoin.Round)
                )
            }
        }
    }
}

@Composable
fun StorybookTopProgressPill(title: String, progressText: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(PILL_CORNER_PERCENT)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PROGRESS_GAP_DP.dp),
        modifier = modifier
            .defaultMinSize(minHeight = PROGRESS_MIN_HEIGHT_DP.dp)
            .clip(shape)
            .background(Color(PROGRESS_BG))
            .border(PROGRESS_BORDER_DP.dp, StorybookPalette.Ink, shape)
            .padding(horizontal = PROGRESS_PAD_H_DP.dp, vertical = PROGRESS_PAD_V_DP.dp)
    ) {
        Text(
            text = title,
            color = StorybookPalette.Ink,
            fontSize = PROGRESS_FONT_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
        Text(
            text = progressText,
            color = Color(PROGRESS_ACCENT),
            fontSize = PROGRESS_FONT_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

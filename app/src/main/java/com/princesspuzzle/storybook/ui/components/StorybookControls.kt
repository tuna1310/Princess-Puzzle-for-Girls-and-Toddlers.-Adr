package com.princesspuzzle.storybook.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.StorybookPalette
import com.princesspuzzle.storybook.art.buildStarPath
import com.princesspuzzle.storybook.core.GameConfig
import kotlin.math.cos
import kotlin.math.sin

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
private const val PROGRESS_GAP_DP = 8
private const val PROGRESS_PAD_H_DP = 13
private const val PROGRESS_PAD_V_DP = 10
private const val PROGRESS_FONT_SP = 14
private const val SUMMARY_STAR_DP = 22
private const val SUMMARY_GAP_DP = 6
private const val SUMMARY_STROKE_RATIO = 0.08f
private const val GROWNUPS_ICON_SCALE = 0.5f
private const val GEAR_TEETH_COUNT = 8
private const val GEAR_OUTER_R = 0.44f
private const val GEAR_INNER_R = 0.30f
private const val GEAR_CENTER_R = 0.14f
private const val GEAR_STROKE_RATIO = 0.08f
private const val LOCK_BODY_TOP = 0.44f
private const val LOCK_BODY_LEFT = 0.22f
private const val LOCK_BODY_WIDTH = 0.56f
private const val LOCK_BODY_HEIGHT = 0.44f
private const val LOCK_BODY_RADIUS = 0.12f
private const val LOCK_SHACKLE_LEFT = 0.32f
private const val LOCK_SHACKLE_TOP = 0.18f
private const val LOCK_SHACKLE_WIDTH = 0.36f
private const val LOCK_SHACKLE_HEIGHT = 0.48f
private const val LOCK_SHACKLE_STROKE = 0.11f
private const val LOCK_KEY_R = 0.07f
private const val LOCK_KEY_Y = 0.64f
private const val LOCK_BG_COLOR = 0xFFFFE594
private const val LOCK_FILL_COLOR = 0xFFFFB5D6
private const val SCRIM_COLOR = 0x66493055
private const val NOTICE_CORNER_DP = 28
private const val NOTICE_MAX_WIDTH_DP = 360
private const val NOTICE_PAD_DP = 24
private const val NOTICE_GAP_DP = 14
private const val NOTICE_TITLE_SP = 22
private const val NOTICE_BODY_SP = 16
private const val NOTICE_BORDER_DP = 3
private const val LOGO_CROWN_W_DP = 76
private const val LOGO_CROWN_H_DP = 44
private const val LOGO_TITLE_SP = 30
private const val LOGO_SUB_SP = 15
private const val LOGO_CORNER_DP = 26
private const val CROWN_LEFT_X = 0.12f
private const val CROWN_MID_L_X = 0.31f
private const val CROWN_RIGHT_X = 0.88f
private const val CROWN_MID_R_X = 0.69f
private const val CROWN_BASE_Y = 0.88f
private const val CROWN_SIDE_Y = 0.28f
private const val CROWN_VALLEY_Y = 0.58f
private const val CROWN_PEAK_Y = 0.16f
private const val CROWN_GEM_R = 0.085f
private const val CROWN_STROKE_DP = 2.6f
private const val FULL_CIRCLE_DEG = 360.0
private const val ARC_START_UP = 180f

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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
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

@Composable
fun StorybookBadgePill(label: String, modifier: Modifier = Modifier, containerColor: Color = Color(PROGRESS_BG)) {
    val shape = RoundedCornerShape(PILL_CORNER_PERCENT)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = PROGRESS_MIN_HEIGHT_DP.dp)
            .clip(shape)
            .background(containerColor)
            .border(PROGRESS_BORDER_DP.dp, StorybookPalette.Ink, shape)
            .padding(horizontal = PROGRESS_PAD_H_DP.dp, vertical = PROGRESS_PAD_V_DP.dp)
    ) {
        Text(
            text = label,
            color = StorybookPalette.Ink,
            fontSize = PROGRESS_FONT_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StorybookStarSummaryPill(earnedStars: Int, totalStars: Int, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(PILL_CORNER_PERCENT)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SUMMARY_GAP_DP.dp),
        modifier = modifier
            .defaultMinSize(minHeight = PROGRESS_MIN_HEIGHT_DP.dp)
            .clip(shape)
            .background(Color(PROGRESS_BG))
            .border(PROGRESS_BORDER_DP.dp, StorybookPalette.Ink, shape)
            .padding(horizontal = PROGRESS_PAD_H_DP.dp, vertical = PROGRESS_PAD_V_DP.dp)
    ) {
        Canvas(modifier = Modifier.size(SUMMARY_STAR_DP.dp)) {
            val r = size.minDimension * STAR_RADIUS_RATIO
            val path = buildStarPath(
                cx = size.width * HALF,
                cy = size.height * HALF,
                outerR = r,
                innerR = r * STAR_INNER_RATIO
            )
            drawPath(path = path, color = StorybookPalette.StarGold)
            drawPath(
                path = path,
                color = StorybookPalette.Ink,
                style = Stroke(width = size.minDimension * SUMMARY_STROKE_RATIO, join = StrokeJoin.Round)
            )
        }
        Text(
            text = stringResource(R.string.home_stars_summary, earnedStars, totalStars),
            color = StorybookPalette.Ink,
            fontSize = PROGRESS_FONT_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
fun StorybookCircleGrownUpsButton(
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
            .background(Brush.verticalGradient(listOf(StorybookPalette.SoftWhite, StorybookPalette.ButtonBlueBottom)))
            .border(PILL_BORDER_DP.dp, StorybookPalette.Ink, shape)
            .semantics { contentDescription = contentDescriptionText }
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Canvas(modifier = Modifier.size(sizeDp * GROWNUPS_ICON_SCALE)) {
            val cx = size.width * HALF
            val cy = size.height * HALF
            val dim = size.minDimension
            val ringStroke = Stroke(width = dim * GEAR_STROKE_RATIO, cap = StrokeCap.Round)
            for (i in 0 until GEAR_TEETH_COUNT) {
                val rad = Math.toRadians(i * (FULL_CIRCLE_DEG / GEAR_TEETH_COUNT))
                val rx = cos(rad).toFloat()
                val ry = sin(rad).toFloat()
                drawLine(
                    color = StorybookPalette.Ink,
                    start = Offset(cx + rx * dim * GEAR_INNER_R, cy + ry * dim * GEAR_INNER_R),
                    end = Offset(cx + rx * dim * GEAR_OUTER_R, cy + ry * dim * GEAR_OUTER_R),
                    strokeWidth = dim * GEAR_STROKE_RATIO,
                    cap = StrokeCap.Round
                )
            }
            drawCircle(StorybookPalette.SoftWhite, radius = dim * GEAR_INNER_R, center = Offset(cx, cy))
            drawCircle(StorybookPalette.Ink, radius = dim * GEAR_INNER_R, center = Offset(cx, cy), style = ringStroke)
            drawCircle(StorybookPalette.Ink, radius = dim * GEAR_CENTER_R, center = Offset(cx, cy), style = ringStroke)
        }
    }
}

@Composable
fun StorybookPadlockBadge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val dim = size.minDimension
        drawCircle(color = Color(LOCK_BG_COLOR), radius = dim * HALF)
        drawCircle(
            color = StorybookPalette.Ink,
            radius = dim * HALF,
            style = Stroke(width = dim * GEAR_STROKE_RATIO)
        )
        drawArc(
            color = StorybookPalette.Ink,
            startAngle = ARC_START_UP,
            sweepAngle = ARC_START_UP,
            useCenter = false,
            topLeft = Offset(w * LOCK_SHACKLE_LEFT, h * LOCK_SHACKLE_TOP),
            size = Size(w * LOCK_SHACKLE_WIDTH, h * LOCK_SHACKLE_HEIGHT),
            style = Stroke(width = dim * LOCK_SHACKLE_STROKE, cap = StrokeCap.Round)
        )
        val bodyTl = Offset(w * LOCK_BODY_LEFT, h * LOCK_BODY_TOP)
        val bodySz = Size(w * LOCK_BODY_WIDTH, h * LOCK_BODY_HEIGHT)
        val corner = CornerRadius(dim * LOCK_BODY_RADIUS, dim * LOCK_BODY_RADIUS)
        drawRoundRect(color = Color(LOCK_FILL_COLOR), topLeft = bodyTl, size = bodySz, cornerRadius = corner)
        drawRoundRect(
            color = StorybookPalette.Ink,
            topLeft = bodyTl,
            size = bodySz,
            cornerRadius = corner,
            style = Stroke(width = dim * GEAR_STROKE_RATIO)
        )
        drawCircle(color = StorybookPalette.Ink, radius = dim * LOCK_KEY_R, center = Offset(w * HALF, h * LOCK_KEY_Y))
    }
}

@Composable
fun StorybookNoticeOverlay(
    title: String,
    body: String,
    okLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(NOTICE_CORNER_DP.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(SCRIM_COLOR))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(NOTICE_PAD_DP.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NOTICE_GAP_DP.dp),
            modifier = Modifier
                .widthIn(max = NOTICE_MAX_WIDTH_DP.dp)
                .fillMaxWidth()
                .shadow(PILL_SHADOW_DP.dp, cardShape)
                .clip(cardShape)
                .background(StorybookPalette.SoftWhite)
                .border(NOTICE_BORDER_DP.dp, StorybookPalette.Ink, cardShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(NOTICE_PAD_DP.dp)
        ) {
            Text(
                text = title,
                color = StorybookPalette.Ink,
                fontSize = NOTICE_TITLE_SP.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = body,
                color = StorybookPalette.Ink,
                fontSize = NOTICE_BODY_SP.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(NOTICE_GAP_DP.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (secondaryLabel != null && onSecondaryClick != null) {
                    StorybookPillButton(
                        label = secondaryLabel,
                        topColor = StorybookPalette.ButtonBlueTop,
                        bottomColor = StorybookPalette.ButtonBlueBottom,
                        onClick = onSecondaryClick
                    )
                }
                StorybookPillButton(
                    label = okLabel,
                    topColor = StorybookPalette.ButtonPinkTop,
                    bottomColor = StorybookPalette.ButtonPinkBottom,
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
fun StorybookLogoCrest(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val bannerShape = RoundedCornerShape(LOGO_CORNER_DP.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(width = LOGO_CROWN_W_DP.dp, height = LOGO_CROWN_H_DP.dp)) {
            val w = size.width
            val h = size.height
            val crown = Path().apply {
                moveTo(w * CROWN_LEFT_X, h * CROWN_BASE_Y)
                lineTo(w * CROWN_LEFT_X, h * CROWN_SIDE_Y)
                lineTo(w * CROWN_MID_L_X, h * CROWN_VALLEY_Y)
                lineTo(w * HALF, h * CROWN_PEAK_Y)
                lineTo(w * CROWN_MID_R_X, h * CROWN_VALLEY_Y)
                lineTo(w * CROWN_RIGHT_X, h * CROWN_SIDE_Y)
                lineTo(w * CROWN_RIGHT_X, h * CROWN_BASE_Y)
                close()
            }
            drawPath(crown, StorybookPalette.StarGold)
            drawPath(
                crown,
                StorybookPalette.Ink,
                style = Stroke(width = CROWN_STROKE_DP * density, join = StrokeJoin.Round)
            )
            drawCircle(
                StorybookPalette.ButtonPinkBottom,
                radius = w * CROWN_GEM_R,
                center = Offset(w * HALF, h * CROWN_PEAK_Y)
            )
            drawCircle(
                StorybookPalette.Ink,
                radius = w * CROWN_GEM_R,
                center = Offset(w * HALF, h * CROWN_PEAK_Y),
                style = Stroke(width = PROGRESS_BORDER_DP * density)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(PILL_SHADOW_DP.dp, bannerShape)
                .clip(bannerShape)
                .background(Brush.verticalGradient(listOf(StorybookPalette.SoftWhite, StorybookPalette.PetalPink)))
                .border(NOTICE_BORDER_DP.dp, StorybookPalette.Ink, bannerShape)
                .padding(horizontal = PILL_PAD_H_DP.dp, vertical = PILL_PAD_V_DP.dp)
        ) {
            Text(
                text = title,
                color = StorybookPalette.Ink,
                fontSize = LOGO_TITLE_SP.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                color = Color(PROGRESS_ACCENT),
                fontSize = LOGO_SUB_SP.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

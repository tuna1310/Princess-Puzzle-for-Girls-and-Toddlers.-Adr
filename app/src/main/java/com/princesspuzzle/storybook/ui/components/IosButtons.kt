@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.IosThemeColors

/**
 * Glossy curved blue arrow matching iOS `CurvedBlueArrowButton`.
 */
@Composable
fun CurvedBlueArrowButton(
    clockwise: Boolean,
    contentDescriptionText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "blueArrowScale"
    )

    Box(
        modifier = modifier
            .size(68.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { contentDescription = contentDescriptionText }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft drop shadow on meadow sky
            drawOval(
                color = IosThemeColors.controlShadow,
                topLeft = Offset(w * 0.14f, h * 0.68f),
                size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.24f)
            )

            val arrowPath = buildCurvedArrowPath(w, h, mirrorHorizontal = !clockwise)
            drawPath(
                path = arrowPath,
                brush = Brush.verticalGradient(
                    colors = listOf(IosThemeColors.blueArrowTop, IosThemeColors.blueArrowBottom)
                )
            )
            drawPath(
                path = arrowPath,
                color = Color.White.copy(alpha = 0.92f),
                style = Stroke(width = w * 0.048f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/**
 * Glossy green sweeping back arrow matching iOS `GlossyGreenBackButton`.
 */
@Composable
fun GlossyGreenBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cdText = stringResource(R.string.cd_back_to_wheel)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "greenBackScale"
    )

    Box(
        modifier = modifier
            .size(66.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { contentDescription = cdText }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawOval(
                color = IosThemeColors.controlShadow,
                topLeft = Offset(w * 0.14f, h * 0.66f),
                size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.24f)
            )

            val backArrowPath = Path().apply {
                // Smooth left-pointing storybook return arrow
                moveTo(w * 0.12f, h * 0.44f)
                lineTo(w * 0.44f, h * 0.16f)
                lineTo(w * 0.44f, h * 0.31f)
                cubicTo(w * 0.68f, h * 0.29f, w * 0.85f, h * 0.44f, w * 0.86f, h * 0.72f)
                cubicTo(w * 0.74f, h * 0.58f, w * 0.60f, h * 0.54f, w * 0.44f, h * 0.56f)
                lineTo(w * 0.44f, h * 0.72f)
                close()
            }
            drawPath(
                path = backArrowPath,
                brush = Brush.verticalGradient(
                    colors = listOf(IosThemeColors.greenBackTop, IosThemeColors.greenBackBottom)
                )
            )
            drawPath(
                path = backArrowPath,
                color = Color.White,
                style = Stroke(width = w * 0.052f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
fun SoundCircleButton(soundEnabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val cdText = stringResource(if (soundEnabled) R.string.cd_mute_sound else R.string.cd_unmute_sound)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 420f),
        label = "soundBtnScale"
    )

    Box(
        modifier = modifier
            .size(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = 6.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (soundEnabled) {
                        listOf(Color.White, Color(0xFFFFEDF5))
                    } else {
                        listOf(Color(0xFFF3EFF5), Color(0xFFE1DAE6))
                    }
                )
            )
            .border(width = 2.5.dp, color = Color.White, shape = CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val iconColor = if (soundEnabled) IosThemeColors.pinkButtonBottom else IosThemeColors.inkSecondary
        Canvas(
            modifier = Modifier
                .size(28.dp)
                .semantics { contentDescription = cdText }
        ) {
            val w = size.width
            val h = size.height
            val speakerBody = Path().apply {
                moveTo(w * 0.14f, h * 0.36f)
                lineTo(w * 0.34f, h * 0.36f)
                lineTo(w * 0.56f, h * 0.16f)
                lineTo(w * 0.56f, h * 0.84f)
                lineTo(w * 0.34f, h * 0.64f)
                lineTo(w * 0.14f, h * 0.64f)
                close()
            }
            drawPath(path = speakerBody, color = iconColor)
            if (soundEnabled) {
                drawArc(
                    color = iconColor,
                    startAngle = -45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(w * 0.52f, h * 0.28f),
                    size = androidx.compose.ui.geometry.Size(w * 0.26f, h * 0.44f),
                    style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = iconColor,
                    startAngle = -50f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(w * 0.54f, h * 0.14f),
                    size = androidx.compose.ui.geometry.Size(w * 0.42f, h * 0.72f),
                    style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
                )
            } else {
                drawLine(
                    color = IosThemeColors.crimson,
                    start = Offset(w * 0.16f, h * 0.82f),
                    end = Offset(w * 0.86f, h * 0.18f),
                    strokeWidth = w * 0.11f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun GlossyActionPill(
    label: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    leadingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "pillScale"
    )

    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .background(brush = Brush.verticalGradient(colors = gradientColors))
            .border(width = 2.5.dp, color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            if (label.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

private fun buildCurvedArrowPath(w: Float, h: Float, mirrorHorizontal: Boolean): Path {
    fun mx(x: Float): Float = if (mirrorHorizontal) w - x else x
    return Path().apply {
        // Curved arc arrow pointing right/upward (clockwise) or mirrored (counter-clockwise)
        moveTo(mx(w * 0.16f), h * 0.72f)
        cubicTo(mx(w * 0.18f), h * 0.36f, mx(w * 0.36f), h * 0.22f, mx(w * 0.56f), h * 0.25f)
        lineTo(mx(w * 0.56f), h * 0.12f)
        lineTo(mx(w * 0.88f), h * 0.36f)
        lineTo(mx(w * 0.56f), h * 0.58f)
        lineTo(mx(w * 0.56f), h * 0.44f)
        cubicTo(mx(w * 0.38f), h * 0.42f, mx(w * 0.28f), h * 0.54f, mx(w * 0.24f), h * 0.74f)
        close()
    }
}

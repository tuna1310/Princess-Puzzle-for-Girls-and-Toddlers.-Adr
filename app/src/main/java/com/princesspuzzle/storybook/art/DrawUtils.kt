package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun buildStarPath(
    cx: Float,
    cy: Float,
    outerR: Float,
    innerR: Float,
    points: Int = 5,
    rotationRad: Float = (-PI / 2.0).toFloat(),
    targetPath: Path = Path()
): Path {
    targetPath.reset()
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = rotationRad + (i * PI.toFloat()) / points
        val px = cx + cos(angle) * r
        val py = cy + sin(angle) * r
        if (i == 0) targetPath.moveTo(px, py) else targetPath.lineTo(px, py)
    }
    targetPath.close()
    return targetPath
}

fun buildSparklePath(cx: Float, cy: Float, size: Float, targetPath: Path = Path()): Path {
    targetPath.reset()
    val k = size * 0.16f
    targetPath.moveTo(cx, cy - size)
    targetPath.quadraticTo(cx + k, cy - k, cx + size, cy)
    targetPath.quadraticTo(cx + k, cy + k, cx, cy + size)
    targetPath.quadraticTo(cx - k, cy + k, cx - size, cy)
    targetPath.quadraticTo(cx - k, cy - k, cx, cy - size)
    targetPath.close()
    return targetPath
}

fun buildHeartPath(cx: Float, cy: Float, size: Float, targetPath: Path = Path()): Path {
    targetPath.reset()
    targetPath.moveTo(cx, cy + size * 0.35f)
    targetPath.cubicTo(
        cx - size * 0.9f,
        cy - size * 0.2f,
        cx - size * 0.5f,
        cy - size * 0.9f,
        cx,
        cy - size * 0.35f
    )
    targetPath.cubicTo(
        cx + size * 0.5f,
        cy - size * 0.9f,
        cx + size * 0.9f,
        cy - size * 0.2f,
        cx,
        cy + size * 0.35f
    )
    targetPath.close()
    return targetPath
}

fun DrawScope.drawSparkle(
    cx: Float,
    cy: Float,
    size: Float,
    color: Color = Color(0xFFFFFDF2),
    alpha: Float = 1f,
    scratchPath: Path = Path()
) {
    buildSparklePath(cx, cy, size, scratchPath)
    drawPath(scratchPath, color = color, alpha = alpha.coerceIn(0f, 1f))
}

fun DrawScope.drawHeart(
    cx: Float,
    cy: Float,
    size: Float,
    fill: Color,
    strokeColor: Color? = null,
    strokeWidth: Float = 2f,
    alpha: Float = 1f,
    scratchPath: Path = Path()
) {
    buildHeartPath(cx, cy, size, scratchPath)
    drawPath(scratchPath, color = fill, alpha = alpha)
    if (strokeColor != null) {
        drawPath(
            scratchPath,
            color = strokeColor,
            alpha = alpha,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
    }
}

fun DrawScope.drawPuffyCloud(cx: Float, cy: Float, scaleFactor: Float, fill: Color = Color(0xEBFFFDF8)) {
    translate(cx, cy) {
        scale(scaleFactor, scaleFactor, pivot = Offset.Zero) {
            drawCircle(fill, radius = 22f, center = Offset(-28f, 6f))
            drawCircle(fill, radius = 28f, center = Offset(0f, -6f))
            drawCircle(fill, radius = 21f, center = Offset(28f, 6f))
            drawCircle(fill, radius = 24f, center = Offset(0f, 12f))
        }
    }
}

fun DrawScope.drawDaisyFlower(
    cx: Float,
    cy: Float,
    radius: Float,
    petalColor: Color = StorybookPalette.SoftWhite,
    centerColor: Color = StorybookPalette.DaisyCenter
) {
    translate(cx, cy) {
        val petalW = radius * 0.84f
        val petalH = radius * 0.56f
        for (i in 0 until 6) {
            val angleDeg = i * 60f
            rotate(angleDeg, pivot = Offset.Zero) {
                drawOval(
                    color = petalColor,
                    topLeft = Offset(radius * 0.58f - petalW * 0.5f, -petalH * 0.5f),
                    size = Size(petalW, petalH)
                )
            }
        }
        drawCircle(centerColor, radius = radius * 0.36f, center = Offset.Zero)
    }
}

fun DrawScope.drawGlossyEye(cx: Float, cy: Float, rx: Float, ry: Float, irisColor: Color = Color(0xFF5B7FD1)) {
    val topLeft = Offset(cx - rx, cy - ry)
    val eyeSize = Size(rx * 2f, ry * 2f)
    drawOval(Color.White, topLeft = topLeft, size = eyeSize)
    drawOval(
        StorybookPalette.Ink,
        topLeft = topLeft,
        size = eyeSize,
        style = Stroke(width = max(1.8f, rx * 0.16f))
    )

    val irisRx = rx * 0.72f
    val irisRy = ry * 0.76f
    drawOval(
        irisColor,
        topLeft = Offset(cx - irisRx, cy + ry * 0.06f - irisRy),
        size = Size(irisRx * 2f, irisRy * 2f)
    )

    val pupilRx = rx * 0.42f
    val pupilRy = ry * 0.46f
    drawOval(
        Color(0xFF3B2848),
        topLeft = Offset(cx - pupilRx, cy + ry * 0.08f - pupilRy),
        size = Size(pupilRx * 2f, pupilRy * 2f)
    )

    drawCircle(Color.White, radius = rx * 0.22f, center = Offset(cx - rx * 0.24f, cy - ry * 0.26f))
    drawCircle(Color.White, radius = rx * 0.11f, center = Offset(cx + rx * 0.22f, cy + ry * 0.25f))
}

fun DrawScope.drawBlushCheek(cx: Float, cy: Float, rx: Float, ry: Float) {
    drawOval(
        color = StorybookPalette.BlushPink,
        topLeft = Offset(cx - rx, cy - ry),
        size = Size(rx * 2f, ry * 2f)
    )
}

fun DrawScope.drawPastelRainbow(cx: Float, cy: Float, outerR: Float, bandThickness: Float, alpha: Float = 0.85f) {
    StorybookPalette.RainbowColors.forEachIndexed { idx, col ->
        val r = outerR - idx * bandThickness
        if (r > 4f) {
            drawArc(
                color = col,
                startAngle = 187f,
                sweepAngle = 166f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2f, r * 2f),
                alpha = alpha,
                style = Stroke(width = bandThickness + 1f, cap = StrokeCap.Round)
            )
        }
    }
}

fun DrawScope.drawButterfly(cx: Float, cy: Float, size: Float, wingColor: Color, flapPhase: Float) {
    val flap = 0.55f + abs(sin(flapPhase)) * 0.55f
    val stroke = Stroke(width = max(1.5f, size * 0.09f))
    translate(cx, cy) {
        scale(flap, 1f, pivot = Offset.Zero) {
            for (side in listOf(-1f, 1f)) {
                rotate(side * 20f, pivot = Offset(side * size * 0.48f, -size * 0.24f)) {
                    val topTopLeft =
                        Offset(side * size * 0.48f - size * 0.48f, -size * 0.24f - size * 0.34f)
                    val topSize = Size(size * 0.96f, size * 0.68f)
                    drawOval(wingColor, topLeft = topTopLeft, size = topSize)
                    drawOval(
                        StorybookPalette.Ink,
                        topLeft = topTopLeft,
                        size = topSize,
                        style = stroke
                    )
                }
                rotate(-side * 18f, pivot = Offset(side * size * 0.36f, size * 0.26f)) {
                    val botTopLeft =
                        Offset(side * size * 0.36f - size * 0.34f, size * 0.26f - size * 0.25f)
                    val botSize = Size(size * 0.68f, size * 0.5f)
                    drawOval(wingColor, topLeft = botTopLeft, size = botSize)
                    drawOval(
                        StorybookPalette.Ink,
                        topLeft = botTopLeft,
                        size = botSize,
                        style = stroke
                    )
                }
            }
            drawOval(
                color = StorybookPalette.Ink,
                topLeft = Offset(-size * 0.12f, -size * 0.42f),
                size = Size(size * 0.24f, size * 0.84f)
            )
        }
    }
}

fun DrawScope.drawRoundedBox(
    rect: Rect,
    cornerRadius: Float,
    fill: Color,
    strokeColor: Color? = null,
    strokeWidth: Float = 2f
) {
    drawRoundRect(
        color = fill,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )
    if (strokeColor != null) {
        drawRoundRect(
            color = strokeColor,
            topLeft = rect.topLeft,
            size = rect.size,
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth)
        )
    }
}

fun DrawScope.drawLeaf(cx: Float, cy: Float, length: Float, angleDeg: Float, fill: Color, strokeWidth: Float = 2f) {
    val leafPath = Path().apply {
        moveTo(-length * 0.5f, 0f)
        quadraticTo(0f, -length * 0.34f, length * 0.5f, 0f)
        quadraticTo(0f, length * 0.34f, -length * 0.5f, 0f)
        close()
    }
    translate(cx, cy) {
        rotate(angleDeg, pivot = Offset.Zero) {
            drawPath(leafPath, fill)
            drawPath(leafPath, StorybookPalette.Ink, style = Stroke(strokeWidth, join = StrokeJoin.Round))
        }
    }
}

fun DrawScope.drawMusicNote(
    cx: Float,
    cy: Float,
    size: Float,
    color: Color,
    strokeWidth: Float = 2f,
    alpha: Float = 1f
) {
    drawOval(
        color = color,
        topLeft = Offset(cx - size * 0.55f, cy + size * 0.12f),
        size = Size(size * 0.52f, size * 0.38f),
        alpha = alpha
    )
    drawLine(
        color = color,
        start = Offset(cx - size * 0.08f, cy + size * 0.25f),
        end = Offset(cx - size * 0.08f, cy - size * 0.55f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
        alpha = alpha
    )
    val flag = Path().apply {
        moveTo(cx - size * 0.08f, cy - size * 0.55f)
        quadraticTo(cx + size * 0.48f, cy - size * 0.4f, cx + size * 0.35f, cy - size * 0.1f)
    }
    drawPath(flag, color = color, alpha = alpha, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

fun DrawScope.drawPaperLantern(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    bodyColor: Color,
    glowColor: Color,
    strokeWidth: Float = 2f,
    alpha: Float = 1f
) {
    drawCircle(glowColor, radius = w * 1.1f, center = Offset(cx, cy), alpha = alpha * 0.35f)
    val tl = Offset(cx - w * 0.5f, cy - h * 0.5f)
    val sz = Size(w, h)
    drawRoundRect(bodyColor, topLeft = tl, size = sz, cornerRadius = CornerRadius(w * 0.38f), alpha = alpha)
    drawRoundRect(
        StorybookPalette.Ink,
        topLeft = tl,
        size = sz,
        cornerRadius = CornerRadius(w * 0.38f),
        alpha = alpha,
        style = Stroke(strokeWidth)
    )
    drawRoundRect(
        Color(0xFFFFE17D),
        topLeft = Offset(cx - w * 0.3f, cy - h * 0.5f - h * 0.08f),
        size = Size(w * 0.6f, h * 0.12f),
        cornerRadius = CornerRadius(w * 0.1f),
        alpha = alpha
    )
    drawRoundRect(
        Color(0xFFFFE17D),
        topLeft = Offset(cx - w * 0.28f, cy + h * 0.45f),
        size = Size(w * 0.56f, h * 0.1f),
        cornerRadius = CornerRadius(w * 0.08f),
        alpha = alpha
    )
}

fun DrawScope.drawCrescentMoon(cx: Float, cy: Float, radius: Float, fill: Color, strokeWidth: Float = 2f) {
    val crescent = Path().apply {
        moveTo(cx + radius * 0.25f, cy - radius * 0.92f)
        cubicTo(
            cx - radius * 0.95f,
            cy - radius * 0.85f,
            cx - radius * 0.95f,
            cy + radius * 0.85f,
            cx + radius * 0.25f,
            cy + radius * 0.92f
        )
        cubicTo(
            cx - radius * 0.25f,
            cy + radius * 0.55f,
            cx - radius * 0.25f,
            cy - radius * 0.55f,
            cx + radius * 0.25f,
            cy - radius * 0.92f
        )
        close()
    }
    drawPath(crescent, fill)
    drawPath(crescent, StorybookPalette.Ink, style = Stroke(strokeWidth, join = StrokeJoin.Round))
}

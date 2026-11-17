package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Reusable pre-allocated backdrop bitmap and path scratchpads for [drawMeadowBackdrop]
 * so background animation runs cleanly with zero per-frame Shader/Brush/Path allocations.
 */
class MeadowBackdropState {
    val farHillPath = Path()
    val midHillPath = Path()
    val streamPath = Path()
    val footpathPath = Path()
    val sparklePath = Path()
    private val bridgePlankStroke = Stroke(width = 2f)
    private var cachedWidth = -1f
    private var cachedHeight = -1f
    private var staticBitmap: ImageBitmap? = null
    private var staticWidthPx = -1
    private var staticHeightPx = -1

    fun ensureStaticBitmap(width: Float, height: Float): ImageBitmap {
        val wPx = width.roundToInt().coerceAtLeast(1)
        val hPx = height.roundToInt().coerceAtLeast(1)
        val existing = staticBitmap
        if (existing != null && staticWidthPx == wPx && staticHeightPx == hPx) {
            return existing
        }

        ensureGeometry(width, height)
        staticWidthPx = wPx
        staticHeightPx = hPx
        val bmp = ImageBitmap(wPx, hPx)
        val canvas = Canvas(bmp)
        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(width, height)
        ) {
            drawStaticTerrain(width, height)
        }
        staticBitmap = bmp
        return bmp
    }

    private fun DrawScope.drawStaticTerrain(width: Float, height: Float) {
        drawRect(
            brush =
            Brush.verticalGradient(
                0f to StorybookPalette.SkyTop,
                0.55f to StorybookPalette.SkyMid,
                1f to StorybookPalette.SkyWarm,
                startY = 0f,
                endY = height * 0.68f
            ),
            size = Size(width, height)
        )

        val sunCenter = Offset(width * 0.16f, height * 0.13f)
        val sunR = min(width, height) * 0.08f
        drawCircle(
            brush =
            Brush.radialGradient(
                0f to StorybookPalette.SunGlow,
                1f to Color.Transparent,
                center = sunCenter,
                radius = sunR * 2.2f
            ),
            radius = sunR * 2.2f,
            center = sunCenter
        )
        drawCircle(StorybookPalette.SunCore, radius = sunR * 0.65f, center = sunCenter)

        drawPath(
            path = farHillPath,
            brush =
            Brush.verticalGradient(
                0f to StorybookPalette.FarHillTop,
                1f to StorybookPalette.FarHillBottom,
                startY = height * 0.45f,
                endY = height
            )
        )

        drawPath(
            path = midHillPath,
            brush =
            Brush.verticalGradient(
                0f to StorybookPalette.MidHillTop,
                1f to StorybookPalette.MidHillBottom,
                startY = height * 0.58f,
                endY = height
            )
        )

        drawPath(
            path = streamPath,
            brush =
            Brush.linearGradient(
                0f to StorybookPalette.StreamStart,
                0.5f to StorybookPalette.StreamMid,
                1f to StorybookPalette.StreamEnd,
                start = Offset(0f, height * 0.74f),
                end = Offset(width, height * 0.88f)
            )
        )

        drawPath(footpathPath, color = StorybookPalette.Footpath)

        val bx = width * 0.61f
        val by = height * 0.815f
        translate(bx, by) {
            rotate(-12.6f, pivot = Offset.Zero) {
                for (i in -2..2) {
                    val topLeft = Offset(i * 11f - 4.5f, -14f)
                    val plankSize = Size(9f, 28f)
                    drawRoundRect(
                        StorybookPalette.BridgeWood,
                        topLeft = topLeft,
                        size = plankSize,
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    drawRoundRect(
                        StorybookPalette.BridgeStroke,
                        topLeft = topLeft,
                        size = plankSize,
                        cornerRadius = CornerRadius(3f, 3f),
                        style = bridgePlankStroke
                    )
                }
            }
        }
    }

    fun ensureGeometry(width: Float, height: Float) {
        if (cachedWidth == width && cachedHeight == height) return
        cachedWidth = width
        cachedHeight = height

        farHillPath.reset()
        farHillPath.moveTo(0f, height * 0.56f)
        farHillPath.quadraticTo(width * 0.28f, height * 0.44f, width * 0.58f, height * 0.53f)
        farHillPath.quadraticTo(width * 0.82f, height * 0.6f, width, height * 0.49f)
        farHillPath.lineTo(width, height)
        farHillPath.lineTo(0f, height)
        farHillPath.close()

        midHillPath.reset()
        midHillPath.moveTo(0f, height * 0.64f)
        midHillPath.quadraticTo(width * 0.35f, height * 0.57f, width * 0.68f, height * 0.67f)
        midHillPath.quadraticTo(width * 0.88f, height * 0.72f, width, height * 0.63f)
        midHillPath.lineTo(width, height)
        midHillPath.lineTo(0f, height)
        midHillPath.close()

        streamPath.reset()
        streamPath.moveTo(0f, height * 0.76f)
        streamPath.cubicTo(
            width * 0.3f,
            height * 0.72f,
            width * 0.65f,
            height * 0.85f,
            width,
            height * 0.8f
        )
        streamPath.lineTo(width, height * 0.87f)
        streamPath.cubicTo(
            width * 0.64f,
            height * 0.92f,
            width * 0.28f,
            height * 0.79f,
            0f,
            height * 0.83f
        )
        streamPath.close()

        footpathPath.reset()
        footpathPath.moveTo(width * 0.34f, height)
        footpathPath.quadraticTo(width * 0.42f, height * 0.88f, width * 0.6f, height * 0.81f)
        footpathPath.quadraticTo(width * 0.64f, height * 0.82f, width * 0.65f, height * 0.84f)
        footpathPath.quadraticTo(width * 0.49f, height * 0.9f, width * 0.44f, height)
        footpathPath.close()
    }
}

private val FLOWER_SPECS =
    arrayOf(
        floatArrayOf(0.08f, 0.88f, 9f),
        floatArrayOf(0.14f, 0.93f, 11f),
        floatArrayOf(0.24f, 0.87f, 8f),
        floatArrayOf(0.78f, 0.91f, 10f),
        floatArrayOf(0.88f, 0.87f, 12f),
        floatArrayOf(0.93f, 0.94f, 9f)
    )

fun DrawScope.drawMeadowBackdrop(timeSec: Float, state: MeadowBackdropState) {
    val width = size.width
    val height = size.height
    drawImage(state.ensureStaticBitmap(width, height))

    val cloudBaseScale = (width / 650f).coerceIn(0.65f, 1.25f)
    val span = width + 220f
    for (i in 0 until 4) {
        val speed = 6f + i * 2.5f
        val rawX = ((i * span) / 4f + timeSec * speed) % span
        val cx = rawX - 110f
        val cy = height * (0.09f + (i % 3) * 0.075f)
        val sc = cloudBaseScale * (0.82f + (i % 2) * 0.25f)
        drawPuffyCloud(cx, cy, sc, Color(0xDBFFFDF9))
    }

    for (i in 0 until 5) {
        val t = (i * 0.21f + timeSec * 0.03f) % 1f
        val rx = t * width
        val ry = height * (0.785f + sin(t * PI.toFloat() * 1.4f) * 0.04f)
        drawLine(
            color = Color(0x8CFFFFFF),
            start = Offset(rx - 14f, ry),
            end = Offset(rx + 14f, ry + 1.5f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
    }

    for (i in FLOWER_SPECS.indices) {
        val spec = FLOWER_SPECS[i]
        val sway = sin(timeSec * 1.6f + i) * 2f
        val petalCol = if (i % 2 == 1) StorybookPalette.PetalPink else StorybookPalette.SoftWhite
        drawDaisyFlower(width * spec[0] + sway, height * spec[1], spec[2], petalCol)
    }

    for (i in 0 until 6) {
        val sx = width * (0.12f + ((i * 0.17f + sin(timeSec * 0.3f + i) * 0.03f) % 0.8f))
        val sy = height * (0.18f + ((i * 0.13f + cos(timeSec * 0.4f + i) * 0.02f) % 0.52f))
        val pulse = 0.25f + 0.4f * abs(sin(timeSec * 1.4f + i * 1.3f))
        drawSparkle(sx, sy, 5f + (i % 2) * 2f, Color(0xFFFFFDF4), pulse, state.sparklePath)
    }
}

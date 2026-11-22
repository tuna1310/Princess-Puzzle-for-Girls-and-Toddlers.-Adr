package com.princesspuzzle.storybook.art

import android.util.LruCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawDaisy
import kotlin.math.roundToInt

/**
 * Full-screen storybook meadow background matching iOS `MeadowBackgroundView`,
 * rasterized into an offscreen LRU cache per quantized viewport size for 60 FPS draws.
 */
object IosMeadowBackdrop {
    private val cache = LruCache<String, ImageBitmap>(4)
    private val gameplayCache = LruCache<String, ImageBitmap>(4)

    fun getOrRender(widthPx: Int, heightPx: Int): ImageBitmap {
        val safeW = widthPx.coerceAtLeast(32)
        val safeH = heightPx.coerceAtLeast(32)
        val key = "${safeW}x$safeH"
        cache.get(key)?.let { return it }

        val bitmap = ImageBitmap(safeW, safeH)
        val drawScope = CanvasDrawScope()
        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(bitmap),
            size = Size(safeW.toFloat(), safeH.toFloat())
        ) {
            drawMeadowScene(safeW.toFloat(), safeH.toFloat())
        }
        cache.put(key, bitmap)
        return bitmap
    }

    fun getOrRenderGameplayBackdrop(
        widthPx: Int,
        heightPx: Int,
        glowCenterX: Float,
        glowCenterY: Float,
        glowRadius: Float,
        dimAlpha: Float
    ): ImageBitmap {
        val safeW = widthPx.coerceAtLeast(32)
        val safeH = heightPx.coerceAtLeast(32)
        val gcx = glowCenterX.roundToInt()
        val gcy = glowCenterY.roundToInt()
        val gr = glowRadius.roundToInt().coerceAtLeast(32)
        val key = "gp_${safeW}x${safeH}_${gcx}_${gcy}_$gr"
        gameplayCache.get(key)?.let { return it }

        val base = getOrRender(safeW, safeH)
        val bitmap = ImageBitmap(safeW, safeH)
        val drawScope = CanvasDrawScope()
        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(bitmap),
            size = Size(safeW.toFloat(), safeH.toFloat())
        ) {
            drawImage(image = base)
            drawRect(color = Color.Black.copy(alpha = dimAlpha), size = Size(safeW.toFloat(), safeH.toFloat()))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                    center = Offset(glowCenterX, glowCenterY),
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = Offset(glowCenterX, glowCenterY)
            )
        }
        gameplayCache.put(key, bitmap)
        return bitmap
    }

    fun clearCache() {
        cache.evictAll()
        gameplayCache.evictAll()
    }

    private fun DrawScope.drawMeadowScene(w: Float, h: Float) {
        drawSkyAndClouds(w, h)
        drawHillsAndRiver(w, h)
        drawMeadowFoliage(w, h)
    }

    private fun DrawScope.drawSkyAndClouds(w: Float, h: Float) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(IosThemeColors.skyTop, IosThemeColors.skyMid, IosThemeColors.skyBottom),
                startY = 0f,
                endY = h * 0.65f
            ),
            size = Size(w, h)
        )
        // Warm pastel sun halo top-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(IosThemeColors.sunGlow, Color.Transparent),
                center = Offset(w * 0.85f, h * 0.12f),
                radius = w * 0.34f
            ),
            center = Offset(w * 0.85f, h * 0.12f),
            radius = w * 0.34f
        )
        drawCloudCluster(w * 0.16f, h * 0.12f, w * 0.11f)
        drawCloudCluster(w * 0.54f, h * 0.08f, w * 0.09f)
        drawCloudCluster(w * 0.86f, h * 0.21f, w * 0.10f)
        drawCloudCluster(w * 0.32f, h * 0.26f, w * 0.075f)
    }

    private fun DrawScope.drawCloudCluster(cx: Float, cy: Float, r: Float) {
        drawOval(
            color = IosThemeColors.cloudWhite,
            topLeft = Offset(cx - r * 1.45f, cy - r * 0.45f),
            size = Size(r * 2.9f, r * 1.05f)
        )
        drawCircle(
            color = IosThemeColors.cloudWhite,
            radius = r * 0.72f,
            center = Offset(cx - r * 0.65f, cy - r * 0.18f)
        )
        drawCircle(
            color = IosThemeColors.cloudWhite,
            radius = r * 0.88f,
            center = Offset(cx + r * 0.08f, cy - r * 0.35f)
        )
        drawCircle(
            color = IosThemeColors.cloudWhite,
            radius = r * 0.64f,
            center = Offset(cx + r * 0.78f, cy - r * 0.12f)
        )
    }

    private fun DrawScope.drawHillsAndRiver(w: Float, h: Float) {
        val farHill = Path().apply {
            moveTo(0f, h * 0.56f)
            cubicTo(w * 0.25f, h * 0.44f, w * 0.48f, h * 0.46f, w * 0.70f, h * 0.54f)
            cubicTo(w * 0.85f, h * 0.48f, w * 0.94f, h * 0.49f, w, h * 0.52f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = farHill,
            brush = Brush.verticalGradient(
                colors = listOf(IosThemeColors.hillFarTop, IosThemeColors.hillFarBottom),
                startY = h * 0.44f,
                endY = h * 0.85f
            )
        )

        val midHill = Path().apply {
            moveTo(0f, h * 0.60f)
            cubicTo(w * 0.22f, h * 0.54f, w * 0.55f, h * 0.66f, w, h * 0.56f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = midHill,
            brush = Brush.verticalGradient(
                colors = listOf(IosThemeColors.hillMidTop, IosThemeColors.hillMidBottom),
                startY = h * 0.54f,
                endY = h * 0.95f
            )
        )

        // Winding sky-blue fairy brook matching iOS river bank
        val riverPath = Path().apply {
            moveTo(w * 0.24f, h * 0.58f)
            cubicTo(w * 0.48f, h * 0.64f, w * 0.24f, h * 0.76f, w * 0.56f, h * 0.85f)
            cubicTo(w * 0.76f, h * 0.91f, w * 0.86f, h * 0.96f, w * 0.92f, h)
            lineTo(w * 0.34f, h)
            cubicTo(w * 0.26f, h * 0.92f, w * 0.08f, h * 0.80f, w * 0.20f, h * 0.68f)
            cubicTo(w * 0.26f, h * 0.63f, w * 0.20f, h * 0.60f, w * 0.24f, h * 0.58f)
            close()
        }
        drawPath(
            path = riverPath,
            brush = Brush.linearGradient(
                colors = listOf(IosThemeColors.riverTop, IosThemeColors.riverBottom),
                start = Offset(w * 0.2f, h * 0.58f),
                end = Offset(w * 0.7f, h)
            )
        )
        // Soft water ripples
        drawLine(
            color = IosThemeColors.riverFoam,
            start = Offset(w * 0.25f, h * 0.71f),
            end = Offset(w * 0.42f, h * 0.72f),
            strokeWidth = (h * 0.006f).coerceAtLeast(3f),
            cap = StrokeCap.Round
        )
        drawLine(
            color = IosThemeColors.riverFoam,
            start = Offset(w * 0.36f, h * 0.84f),
            end = Offset(w * 0.62f, h * 0.86f),
            strokeWidth = (h * 0.007f).coerceAtLeast(3f),
            cap = StrokeCap.Round
        )

        // Foreground grassy bank left (riverbank ledge in physics gameplay)
        val foreLeft = Path().apply {
            moveTo(0f, h * 0.80f)
            cubicTo(w * 0.16f, h * 0.84f, w * 0.34f, h * 0.88f, w * 0.48f, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = foreLeft,
            brush = Brush.verticalGradient(
                colors = listOf(IosThemeColors.hillNearTop, IosThemeColors.hillNearBottom),
                startY = h * 0.80f,
                endY = h
            )
        )
        // Foreground grassy bank right
        val foreRight = Path().apply {
            moveTo(w, h * 0.77f)
            cubicTo(w * 0.84f, h * 0.82f, w * 0.74f, h * 0.90f, w * 0.68f, h)
            lineTo(w, h)
            close()
        }
        drawPath(
            path = foreRight,
            brush = Brush.verticalGradient(
                colors = listOf(IosThemeColors.hillNearTop, IosThemeColors.hillNearBottom),
                startY = h * 0.77f,
                endY = h
            )
        )
    }

    private fun DrawScope.drawMeadowFoliage(w: Float, h: Float) {
        drawFloweringBush(w * 0.07f, h * 0.79f, w * 0.11f, IosThemeColors.castlePink)
        drawFloweringBush(w * 0.93f, h * 0.76f, w * 0.11f, IosThemeColors.goldLight)
        drawFloweringBush(w * 0.14f, h * 0.94f, w * 0.09f, Color.White)
        drawFloweringBush(w * 0.88f, h * 0.95f, w * 0.095f, IosThemeColors.blushPink)

        // Scattered meadow daisies
        val flowers = listOf(
            Triple(0.11f, 0.68f, Color.White),
            Triple(0.84f, 0.64f, IosThemeColors.goldLight),
            Triple(0.06f, 0.89f, IosThemeColors.castlePinkLight),
            Triple(0.24f, 0.91f, Color.White),
            Triple(0.76f, 0.89f, IosThemeColors.castlePinkLight),
            Triple(0.95f, 0.87f, Color.White)
        )
        val flowerRadius = (minOf(w, h) * 0.015f).coerceAtLeast(6f)
        flowers.forEach { (fx, fy, petalColor) ->
            drawDaisy(
                cx = fx,
                cy = fy,
                radiusFrac = flowerRadius / minOf(w, h),
                petalColor = petalColor,
                centerColor = IosThemeColors.goldRich,
                w = w,
                h = h
            )
        }
        // Subtle horizon highlight rim
        drawRoundRect(
            color = IosThemeColors.riverFoam,
            topLeft = Offset(w * 0.44f, h * 0.93f),
            size = Size(w * 0.16f, (h * 0.005f).coerceAtLeast(2f)),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // Small stone stepping pebbles near stream edge
        listOf(
            Triple(0.41f, 0.91f, 0.018f),
            Triple(0.45f, 0.94f, 0.014f),
            Triple(0.72f, 0.92f, 0.016f)
        ).forEach { (px, py, pr) ->
            val rx = w * pr
            drawOval(
                color = Color(0xFFBBD0D8),
                topLeft = Offset(w * px - rx, h * py - rx * 0.55f),
                size = Size(rx * 2f, rx * 1.1f)
            )
            drawOval(
                color = Color(0x664A5B66),
                topLeft = Offset(w * px - rx, h * py - rx * 0.55f),
                size = Size(rx * 2f, rx * 1.1f),
                style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }

    private fun DrawScope.drawFloweringBush(cx: Float, cy: Float, r: Float, blossomColor: Color) {
        drawCircle(color = IosThemeColors.shrubDark, radius = r * 0.9f, center = Offset(cx, cy + r * 0.15f))
        drawCircle(color = IosThemeColors.shrubLight, radius = r * 0.72f, center = Offset(cx - r * 0.42f, cy))
        drawCircle(
            color = IosThemeColors.shrubLight,
            radius = r * 0.76f,
            center = Offset(cx + r * 0.38f, cy + r * 0.05f)
        )
        drawCircle(color = IosThemeColors.hillFarTop, radius = r * 0.65f, center = Offset(cx, cy - r * 0.28f))

        val br = (r * 0.18f).roundToInt().toFloat().coerceAtLeast(4f)
        listOf(
            Offset(cx - r * 0.35f, cy - r * 0.15f),
            Offset(cx + r * 0.18f, cy - r * 0.36f),
            Offset(cx + r * 0.42f, cy + r * 0.08f)
        ).forEach { pos ->
            drawCircle(color = blossomColor, radius = br, center = pos)
            drawCircle(color = IosThemeColors.goldRich, radius = br * 0.42f, center = pos)
        }
    }
}

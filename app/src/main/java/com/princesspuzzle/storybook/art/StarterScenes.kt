package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawSceneSkyAndMeadow(s: Float, skyTop: Color, skyBottom: Color, hillColor: Color) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to skyTop,
            0.7f to skyBottom,
            1f to Color(0xFFFFF8EE),
            startY = 0f,
            endY = s
        ),
        size = Size(s, s)
    )
    val hill = Path().apply {
        moveTo(0f, s * 0.74f)
        quadraticTo(s * 0.3f, s * 0.65f, s * 0.65f, s * 0.73f)
        quadraticTo(s * 0.86f, s * 0.78f, s, s * 0.7f)
        lineTo(s, s)
        lineTo(0f, s)
        close()
    }
    drawPath(hill, color = hillColor)
}

internal fun DrawScope.drawTiaraScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFD6C6FF), Color(0xFFFFE6F4), Color(0xFFFFD2EA))

    drawRect(
        brush = Brush.radialGradient(
            0f to Color(0xEBFFFDF0),
            1f to Color.Transparent,
            center = Offset(s * 0.5f, s * 0.45f),
            radius = s * 0.42f
        ),
        size = Size(s, s)
    )
    drawPuffyCloud(s * 0.2f, s * 0.2f, s / 420f, Color(0xBFFFFFFF))
    drawPuffyCloud(s * 0.82f, s * 0.22f, s / 470f, Color(0xBFFFFFFF))

    drawRoyalCushion(s)
    drawGoldenTiara(s)

    drawSparkle(s * 0.19f, s * 0.36f, s * 0.03f, Color(0xFFFFFDF4))
    drawSparkle(s * 0.82f, s * 0.38f, s * 0.026f, Color(0xFFFFFDF4))
    drawSparkle(s * 0.5f, s * 0.13f, s * 0.024f, Color(0xFFFFE985))
}

private fun DrawScope.drawRoyalCushion(s: Float) {
    val cushion = Path().apply {
        moveTo(s * 0.16f, s * 0.66f)
        quadraticTo(s * 0.5f, s * 0.52f, s * 0.84f, s * 0.66f)
        quadraticTo(s * 0.88f, s * 0.76f, s * 0.8f, s * 0.84f)
        quadraticTo(s * 0.5f, s * 0.91f, s * 0.2f, s * 0.84f)
        quadraticTo(s * 0.12f, s * 0.76f, s * 0.16f, s * 0.66f)
        close()
    }
    drawPath(
        cushion,
        brush = Brush.linearGradient(
            0f to Color(0xFFE9C5FF),
            0.5f to Color(0xFFD29EFF),
            1f to Color(0xFFB883EE),
            start = Offset(s * 0.2f, s * 0.54f),
            end = Offset(s * 0.8f, s * 0.84f)
        )
    )
    drawPath(cushion, StorybookPalette.Ink, style = Stroke(s * 0.012f, join = StrokeJoin.Round))

    val cord = Path().apply {
        moveTo(s * 0.19f, s * 0.74f)
        quadraticTo(s * 0.5f, s * 0.82f, s * 0.81f, s * 0.74f)
    }
    drawPath(cord, Color(0xFFFFD875), style = Stroke(s * 0.011f, cap = StrokeCap.Round))

    for (tx in listOf(s * 0.16f, s * 0.84f)) {
        drawCircle(Color(0xFFFFE28A), radius = s * 0.025f, center = Offset(tx, s * 0.73f))
        drawCircle(
            StorybookPalette.Ink,
            radius = s * 0.025f,
            center = Offset(tx, s * 0.73f),
            style = Stroke(s * 0.008f)
        )
    }

    val pillowStar = buildStarPath(s * 0.5f, s * 0.73f, s * 0.045f, s * 0.021f)
    drawPath(pillowStar, Color(0xFFFFF5BA))
    drawPath(pillowStar, StorybookPalette.Ink, style = Stroke(s * 0.007f))
}

private fun DrawScope.drawGoldenTiara(s: Float) {
    val tiara = Path().apply {
        moveTo(s * 0.24f, s * 0.58f)
        quadraticTo(s * 0.23f, s * 0.42f, s * 0.3f, s * 0.34f)
        quadraticTo(s * 0.37f, s * 0.47f, s * 0.4f, s * 0.46f)
        quadraticTo(s * 0.45f, s * 0.27f, s * 0.5f, s * 0.24f)
        quadraticTo(s * 0.55f, s * 0.27f, s * 0.6f, s * 0.46f)
        quadraticTo(s * 0.63f, s * 0.47f, s * 0.7f, s * 0.34f)
        quadraticTo(s * 0.77f, s * 0.42f, s * 0.76f, s * 0.58f)
        quadraticTo(s * 0.5f, s * 0.64f, s * 0.24f, s * 0.58f)
        close()
    }
    drawPath(
        tiara,
        brush = Brush.linearGradient(
            0f to Color(0xFFFFF09E),
            0.5f to Color(0xFFFFD369),
            1f to Color(0xFFF6B148),
            start = Offset(s * 0.28f, s * 0.28f),
            end = Offset(s * 0.72f, s * 0.62f)
        )
    )
    drawPath(tiara, StorybookPalette.Ink, style = Stroke(s * 0.013f, join = StrokeJoin.Round))

    val pearls = listOf(
        Triple(0.3f, 0.33f, 0.026f) to Color(0xFFFFB3D9),
        Triple(0.5f, 0.22f, 0.034f) to Color(0xFF9AE3FF),
        Triple(0.7f, 0.33f, 0.026f) to Color(0xFFFFB3D9)
    )
    for ((geom, col) in pearls) {
        val center = Offset(s * geom.first, s * geom.second)
        val r = s * geom.third
        drawCircle(col, radius = r, center = center)
        drawCircle(StorybookPalette.Ink, radius = r, center = center, style = Stroke(s * 0.009f))
        drawCircle(Color.White, radius = r * 0.24f, center = Offset(center.x - r * 0.28f, center.y - r * 0.28f))
    }

    drawHeart(s * 0.5f, s * 0.48f, s * 0.068f, Color(0xFFFF7EB6), StorybookPalette.Ink, s * 0.009f)
    drawHeart(s * 0.37f, s * 0.52f, s * 0.036f, Color(0xFFA3E4FF), StorybookPalette.Ink, s * 0.007f)
    drawHeart(s * 0.63f, s * 0.52f, s * 0.036f, Color(0xFFA3E4FF), StorybookPalette.Ink, s * 0.007f)
}

internal fun DrawScope.drawBunnyPrincessScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFBCE8FF), Color(0xFFEAF7FF), Color(0xFFA7DE8E))
    drawPuffyCloud(s * 0.2f, s * 0.17f, s / 420f)
    drawPuffyCloud(s * 0.8f, s * 0.2f, s / 460f)

    drawDaisyFlower(s * 0.16f, s * 0.85f, s * 0.045f, Color(0xFFFFFDF8))
    drawDaisyFlower(s * 0.84f, s * 0.83f, s * 0.05f, Color(0xFFFFE3F1))
    drawDaisyFlower(s * 0.74f, s * 0.9f, s * 0.038f, Color(0xFFFFFDF8))

    val inkW = s * 0.011f
    drawLine(
        color = Color(0xFFEBB866),
        start = Offset(s * 0.67f, s * 0.63f),
        end = Offset(s * 0.79f, s * 0.41f),
        strokeWidth = s * 0.016f,
        cap = StrokeCap.Round
    )
    val wandStar = buildStarPath(s * 0.8f, s * 0.39f, s * 0.06f, s * 0.028f)
    drawPath(wandStar, Color(0xFFFFE478))
    drawPath(wandStar, StorybookPalette.Ink, style = Stroke(s * 0.009f))

    drawBunnyBodyAndDress(s, inkW)
    drawBunnyHeadAndCrown(s, inkW)

    drawButterfly(s * 0.22f, s * 0.36f, s * 0.04f, Color(0xFFFFB8DC), 0.4f)
    drawSparkle(s * 0.82f, s * 0.28f, s * 0.026f, Color(0xFFFFFDF0))
}

private fun DrawScope.drawBunnyBodyAndDress(s: Float, inkW: Float) {
    for (fx in listOf(s * 0.43f, s * 0.57f)) {
        val tl = Offset(fx - s * 0.055f, s * 0.83f - s * 0.032f)
        val sz = Size(s * 0.11f, s * 0.064f)
        drawOval(Color(0xFFFFB5D5), topLeft = tl, size = sz)
        drawOval(StorybookPalette.Ink, topLeft = tl, size = sz, style = Stroke(inkW))
    }

    val dress = Path().apply {
        moveTo(s * 0.41f, s * 0.54f)
        lineTo(s * 0.59f, s * 0.54f)
        quadraticTo(s * 0.72f, s * 0.71f, s * 0.69f, s * 0.81f)
        quadraticTo(s * 0.5f, s * 0.85f, s * 0.31f, s * 0.81f)
        quadraticTo(s * 0.28f, s * 0.71f, s * 0.41f, s * 0.54f)
        close()
    }
    drawPath(
        dress,
        brush = Brush.linearGradient(
            0f to Color(0xFFE3C3FF),
            0.55f to Color(0xFFFFB8DE),
            1f to Color(0xFFFFA1D0),
            start = Offset(s * 0.32f, s * 0.52f),
            end = Offset(s * 0.68f, s * 0.82f)
        )
    )
    drawPath(dress, StorybookPalette.Ink, style = Stroke(inkW, join = StrokeJoin.Round))
    drawHeart(s * 0.5f, s * 0.62f, s * 0.035f, Color(0xFFFFF7CC), StorybookPalette.Ink, s * 0.006f)

    for (side in listOf(-1f, 1f)) {
        val sx = s * (0.5f + side * 0.12f)
        val sy = s * 0.58f
        val pawCenter = Offset(sx + side * s * 0.04f, sy + s * 0.04f)
        rotate(side * 26f, pivot = pawCenter) {
            val tl = Offset(pawCenter.x - s * 0.042f, pawCenter.y - s * 0.028f)
            val sz = Size(s * 0.084f, s * 0.056f)
            drawOval(Color(0xFFFFF7F2), topLeft = tl, size = sz)
            drawOval(StorybookPalette.Ink, topLeft = tl, size = sz, style = Stroke(inkW))
        }
        drawCircle(Color(0xFFEBCBFF), radius = s * 0.034f, center = Offset(sx, sy))
        drawCircle(StorybookPalette.Ink, radius = s * 0.034f, center = Offset(sx, sy), style = Stroke(inkW))
    }
}

private fun DrawScope.drawBunnyHeadAndCrown(s: Float, inkW: Float) {
    for (side in listOf(-1f, 1f)) {
        val earCenter = Offset(s * (0.5f + side * 0.095f), s * 0.25f)
        rotate(side * 10f, pivot = earCenter) {
            val outerTl = Offset(earCenter.x - s * 0.056f, earCenter.y - s * 0.14f)
            val outerSz = Size(s * 0.112f, s * 0.28f)
            drawOval(Color(0xFFFFF9F5), topLeft = outerTl, size = outerSz)
            drawOval(StorybookPalette.Ink, topLeft = outerTl, size = outerSz, style = Stroke(inkW))

            val innerTl = Offset(earCenter.x - s * 0.028f, earCenter.y + s * 0.015f - s * 0.09f)
            drawOval(Color(0xFFFFC2D9), topLeft = innerTl, size = Size(s * 0.056f, s * 0.18f))
        }
    }

    val headTl = Offset(s * 0.5f - s * 0.175f, s * 0.42f - s * 0.145f)
    val headSz = Size(s * 0.35f, s * 0.29f)
    drawOval(Color(0xFFFFF9F5), topLeft = headTl, size = headSz)
    drawOval(StorybookPalette.Ink, topLeft = headTl, size = headSz, style = Stroke(inkW))

    drawDaisyFlower(s * 0.42f, s * 0.295f, s * 0.036f, Color(0xFFFFB5D6))
    drawDaisyFlower(s * 0.5f, s * 0.28f, s * 0.042f, Color(0xFFFFF9C7))
    drawDaisyFlower(s * 0.58f, s * 0.295f, s * 0.036f, Color(0xFFB5E4FF))

    drawGlossyEye(s * 0.43f, s * 0.415f, s * 0.026f, s * 0.031f, Color(0xFF7B6BC9))
    drawGlossyEye(s * 0.57f, s * 0.415f, s * 0.026f, s * 0.031f, Color(0xFF7B6BC9))
    drawBlushCheek(s * 0.375f, s * 0.455f, s * 0.028f, s * 0.017f)
    drawBlushCheek(s * 0.625f, s * 0.455f, s * 0.028f, s * 0.017f)

    drawOval(
        Color(0xFFFF90B8),
        topLeft = Offset(s * 0.5f - s * 0.016f, s * 0.452f - s * 0.011f),
        size = Size(s * 0.032f, s * 0.022f)
    )
    val smile = Path().apply {
        moveTo(s * 0.468f, s * 0.47f)
        quadraticTo(s * 0.484f, s * 0.486f, s * 0.5f, s * 0.468f)
        quadraticTo(s * 0.516f, s * 0.486f, s * 0.532f, s * 0.47f)
    }
    drawPath(smile, StorybookPalette.Ink, style = Stroke(s * 0.008f, cap = StrokeCap.Round))
}

internal fun DrawScope.drawUnicornScene(s: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFFB5E4FF),
            0.65f to Color(0xFFFCE7F6),
            1f to Color(0xFFFFF5E2),
            startY = 0f,
            endY = s
        ),
        size = Size(s, s)
    )
    drawPastelRainbow(s * 0.5f, s * 0.66f, s * 0.44f, s * 0.024f, 0.88f)
    drawPuffyCloud(s * 0.22f, s * 0.72f, s / 260f, Color.White)
    drawPuffyCloud(s * 0.78f, s * 0.71f, s / 270f, Color.White)
    drawPuffyCloud(s * 0.5f, s * 0.8f, s / 230f, Color(0xFFFFFDF9))

    val inkW = s * 0.011f
    drawUnicornBodyAndLegs(s, inkW)
    drawUnicornHeadAndMane(s, inkW)

    drawSparkle(s * 0.2f, s * 0.24f, s * 0.028f, Color(0xFFFFFDF2))
    drawSparkle(s * 0.83f, s * 0.26f, s * 0.03f, Color(0xFFFFE98F))
}

private fun DrawScope.drawUnicornBodyAndLegs(s: Float, inkW: Float) {
    val tailColors = listOf(Color(0xFFFF9FC6), Color(0xFFFFE38E), Color(0xFF9EE5FF), Color(0xFFD2B3FF))
    tailColors.forEachIndexed { idx, col ->
        val center = Offset(s * (0.25f - idx * 0.018f), s * (0.56f + idx * 0.028f))
        rotate(-34f + idx * 8.5f, pivot = center) {
            val tl = Offset(center.x - s * 0.055f, center.y - s * 0.032f)
            val sz = Size(s * 0.11f, s * 0.064f)
            drawOval(col, topLeft = tl, size = sz)
            drawOval(StorybookPalette.Ink, topLeft = tl, size = sz, style = Stroke(inkW * 0.75f))
        }
    }

    val legs = listOf(
        Triple(0.37f, 0.67f, 12f),
        Triple(0.44f, 0.71f, 5f),
        Triple(0.57f, 0.7f, -9f),
        Triple(0.64f, 0.66f, -20f)
    )
    for ((lx, ly, rotDeg) in legs) {
        val pivot = Offset(s * lx, s * ly)
        rotate(rotDeg, pivot = pivot) {
            val legTl = Offset(pivot.x - s * 0.028f, pivot.y - s * 0.05f)
            val legSz = Size(s * 0.056f, s * 0.11f)
            drawRoundRect(Color(0xFFFFFDF9), topLeft = legTl, size = legSz, cornerRadius = CornerRadius(s * 0.026f))
            drawRoundRect(
                StorybookPalette.Ink,
                topLeft = legTl,
                size = legSz,
                cornerRadius = CornerRadius(s * 0.026f),
                style = Stroke(inkW)
            )
            val hoofTl = Offset(pivot.x - s * 0.026f, pivot.y + s * 0.028f)
            val hoofSz = Size(s * 0.052f, s * 0.032f)
            drawRoundRect(Color(0xFFE0B5FF), topLeft = hoofTl, size = hoofSz, cornerRadius = CornerRadius(s * 0.015f))
            drawRoundRect(
                StorybookPalette.Ink,
                topLeft = hoofTl,
                size = hoofSz,
                cornerRadius = CornerRadius(s * 0.015f),
                style = Stroke(inkW)
            )
        }
    }

    rotate(-4.5f, pivot = Offset(s * 0.48f, s * 0.58f)) {
        val tl = Offset(s * 0.48f - s * 0.18f, s * 0.58f - s * 0.125f)
        val sz = Size(s * 0.36f, s * 0.25f)
        drawOval(Color(0xFFFFFDF9), topLeft = tl, size = sz)
        drawOval(StorybookPalette.Ink, topLeft = tl, size = sz, style = Stroke(inkW))
    }
    drawPath(buildStarPath(s * 0.39f, s * 0.58f, s * 0.032f, s * 0.015f), Color(0xFFFFAFDA))
}

private fun DrawScope.drawUnicornHeadAndMane(s: Float, inkW: Float) {
    val headTl = Offset(s * 0.62f - s * 0.145f, s * 0.41f - s * 0.125f)
    val headSz = Size(s * 0.29f, s * 0.25f)
    drawOval(Color(0xFFFFFDF9), topLeft = headTl, size = headSz)
    drawOval(StorybookPalette.Ink, topLeft = headTl, size = headSz, style = Stroke(inkW))

    val muzTl = Offset(s * 0.72f - s * 0.072f, s * 0.44f - s * 0.058f)
    val muzSz = Size(s * 0.144f, s * 0.116f)
    drawOval(Color(0xFFFFE4F0), topLeft = muzTl, size = muzSz)
    drawOval(StorybookPalette.Ink, topLeft = muzTl, size = muzSz, style = Stroke(inkW))

    val curls = listOf(
        Triple(0.52f, 0.32f, 0.048f) to Color(0xFFFF9EC7),
        Triple(0.49f, 0.39f, 0.045f) to Color(0xFFFFE48E),
        Triple(0.47f, 0.46f, 0.042f) to Color(0xFF9CE4FF),
        Triple(0.62f, 0.29f, 0.042f) to Color(0xFFD3B2FF)
    )
    for ((geom, col) in curls) {
        val c = Offset(s * geom.first, s * geom.second)
        drawCircle(col, radius = s * geom.third, center = c)
        drawCircle(StorybookPalette.Ink, radius = s * geom.third, center = c, style = Stroke(inkW * 0.8f))
    }

    val hornPivot = Offset(s * 0.67f, s * 0.28f)
    rotate(18f, pivot = hornPivot) {
        val horn = Path().apply {
            moveTo(hornPivot.x - s * 0.026f, hornPivot.y)
            lineTo(hornPivot.x, hornPivot.y - s * 0.11f)
            lineTo(hornPivot.x + s * 0.026f, hornPivot.y)
            close()
        }
        drawPath(horn, Color(0xFFFFE27D))
        drawPath(horn, StorybookPalette.Ink, style = Stroke(inkW * 0.85f, join = StrokeJoin.Round))
    }

    drawGlossyEye(s * 0.64f, s * 0.39f, s * 0.028f, s * 0.032f, Color(0xFF866BE0))
    drawBlushCheek(s * 0.61f, s * 0.455f, s * 0.028f, s * 0.017f)
    drawArc(
        color = StorybookPalette.Ink,
        startAngle = 15f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(s * 0.701f, s * 0.431f),
        size = Size(s * 0.048f, s * 0.048f),
        style = Stroke(s * 0.008f, cap = StrokeCap.Round)
    )
}

internal fun DrawScope.drawCastleScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFAEE2FF), Color(0xFFE5F5FF), Color(0xFF9DD985))
    drawCastleSmilingSun(s)
    drawPastelRainbow(s * 0.56f, s * 0.52f, s * 0.36f, s * 0.016f, 0.65f)
    drawPuffyCloud(s * 0.81f, s * 0.17f, s / 420f)

    val inkW = s * 0.01f
    drawCastleTower(
        s, s * 0.2f, s * 0.43f, s * 0.13f, s * 0.34f,
        Color(
            0xFFFCE8F3
        ),
        Color(0xFFFFA6CF), Color(0xFF9EE5FF), inkW
    )
    drawCastleTower(
        s, s * 0.67f, s * 0.43f, s * 0.13f, s * 0.34f,
        Color(
            0xFFFCE8F3
        ),
        Color(0xFFFFA6CF), Color(0xFF9EE5FF), inkW
    )
    drawCastleTower(
        s, s * 0.42f, s * 0.29f, s * 0.16f, s * 0.48f,
        Color(
            0xFFFFF5FB
        ),
        Color(0xFFC7A6FF), Color(0xFFFFE17D), inkW
    )

    val hallTl = Offset(s * 0.29f, s * 0.51f)
    val hallSz = Size(s * 0.42f, s * 0.27f)
    drawRoundRect(Color(0xFFFFEAF4), topLeft = hallTl, size = hallSz, cornerRadius = CornerRadius(s * 0.02f))
    drawRoundRect(
        StorybookPalette.Ink,
        topLeft = hallTl,
        size = hallSz,
        cornerRadius = CornerRadius(s * 0.02f),
        style = Stroke(inkW)
    )

    val door = Path().apply {
        moveTo(s * 0.432f, s * 0.78f)
        lineTo(s * 0.432f, s * 0.65f)
        quadraticTo(s * 0.5f, s * 0.56f, s * 0.568f, s * 0.65f)
        lineTo(s * 0.568f, s * 0.78f)
        close()
    }
    drawPath(door, Color(0xFFDDA982))
    drawPath(door, StorybookPalette.Ink, style = Stroke(inkW))
    drawHeart(s * 0.5f, s * 0.56f, s * 0.036f, Color(0xFFFF88BC), StorybookPalette.Ink, s * 0.007f)

    drawDaisyFlower(s * 0.26f, s * 0.81f, s * 0.036f, Color(0xFFFFFDF9))
    drawDaisyFlower(s * 0.74f, s * 0.81f, s * 0.036f, Color(0xFFFFE3F1))
}

private fun DrawScope.drawCastleSmilingSun(s: Float) {
    val sunCx = s * 0.16f
    val sunCy = s * 0.16f
    val sunRad = s * 0.068f
    for (r in 0 until 8) {
        val a = (r * Math.PI / 4.0).toFloat()
        drawLine(
            color = Color(0xFFFFD768),
            start = Offset(sunCx + cos(a) * sunRad * 1.22f, sunCy + sin(a) * sunRad * 1.22f),
            end = Offset(sunCx + cos(a) * sunRad * 1.5f, sunCy + sin(a) * sunRad * 1.5f),
            strokeWidth = s * 0.011f,
            cap = StrokeCap.Round
        )
    }
    drawCircle(Color(0xFFFFE57E), radius = sunRad, center = Offset(sunCx, sunCy))
    drawCircle(StorybookPalette.Ink, radius = sunRad, center = Offset(sunCx, sunCy), style = Stroke(s * 0.009f))
    drawBlushCheek(sunCx - s * 0.032f, sunCy + s * 0.014f, s * 0.014f, s * 0.009f)
    drawBlushCheek(sunCx + s * 0.032f, sunCy + s * 0.014f, s * 0.014f, s * 0.009f)
    drawCircle(StorybookPalette.Ink, radius = s * 0.007f, center = Offset(sunCx - s * 0.022f, sunCy - s * 0.008f))
    drawCircle(StorybookPalette.Ink, radius = s * 0.007f, center = Offset(sunCx + s * 0.022f, sunCy - s * 0.008f))
    drawArc(
        color = StorybookPalette.Ink,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(sunCx - s * 0.018f, sunCy - s * 0.006f),
        size = Size(s * 0.036f, s * 0.036f),
        style = Stroke(s * 0.007f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawCastleTower(
    s: Float,
    tx: Float,
    ty: Float,
    tw: Float,
    th: Float,
    wallCol: Color,
    roofCol: Color,
    flagCol: Color,
    inkW: Float
) {
    val tl = Offset(tx, ty)
    val sz = Size(tw, th)
    drawRoundRect(wallCol, topLeft = tl, size = sz, cornerRadius = CornerRadius(s * 0.018f))
    drawRoundRect(
        StorybookPalette.Ink,
        topLeft = tl,
        size = sz,
        cornerRadius = CornerRadius(s * 0.018f),
        style = Stroke(inkW)
    )
    val cx = tx + tw * 0.5f
    val tipY = ty - tw * 1.15f
    drawLine(StorybookPalette.Ink, Offset(cx, ty - tw * 0.75f), Offset(cx, tipY), strokeWidth = inkW * 0.75f)
    val flag = Path().apply {
        moveTo(cx, tipY)
        lineTo(cx + s * 0.075f, tipY + s * 0.025f)
        lineTo(cx, tipY + s * 0.05f)
        close()
    }
    drawPath(flag, flagCol)
    drawPath(flag, StorybookPalette.Ink, style = Stroke(inkW * 0.75f))

    val roof = Path().apply {
        moveTo(tx - tw * 0.09f, ty + s * 0.008f)
        quadraticTo(cx - tw * 0.15f, ty - tw * 0.45f, cx, ty - tw * 0.82f)
        quadraticTo(cx + tw * 0.15f, ty - tw * 0.45f, tx + tw * 1.09f, ty + s * 0.008f)
        close()
    }
    drawPath(roof, roofCol)
    drawPath(roof, StorybookPalette.Ink, style = Stroke(inkW, join = StrokeJoin.Round))
}

internal fun DrawScope.drawStarterMedallionScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFF6E7FF), Color(0xFFDFF4FF), Color(0xFFBBE6A4))
    drawCircle(Color(0xC7FFFFFF), radius = s * 0.31f, center = Offset(s * 0.5f, s * 0.5f))
    drawHeart(s * 0.5f, s * 0.52f, s * 0.18f, Color(0xFFE2C3FF), StorybookPalette.Ink, s * 0.018f)
    drawSparkle(s * 0.32f, s * 0.34f, s * 0.045f, Color(0xFFFFFDF2))
    drawSparkle(s * 0.68f, s * 0.35f, s * 0.04f, Color(0xFFFFE987))
}

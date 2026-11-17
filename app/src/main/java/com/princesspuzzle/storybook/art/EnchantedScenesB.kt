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

internal fun DrawScope.drawHarpScene(s: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFF7D78C9),
            0.55f to Color(0xFFB5A9EC),
            1f to Color(0xFFE7DAFF),
            startY = 0f,
            endY = s
        ),
        size = Size(s, s)
    )
    drawCrescentMoon(s * 0.2f, s * 0.2f, s * 0.09f, Color(0xFFFFF3B0), s * 0.009f)
    drawSparkle(s * 0.76f, s * 0.18f, s * 0.028f, Color(0xFFFFFDF0))
    drawSparkle(s * 0.85f, s * 0.34f, s * 0.022f, Color(0xFFFFE985))

    // Starry meadow mound
    drawOval(Color(0xFFA0DFA3), topLeft = Offset(-s * 0.1f, s * 0.72f), size = Size(s * 1.2f, s * 0.4f))

    val inkW = s * 0.01f
    // Ornate golden swan-neck harp
    val framePath = Path().apply {
        moveTo(s * 0.38f, s * 0.74f)
        lineTo(s * 0.38f, s * 0.27f)
        cubicTo(s * 0.44f, s * 0.21f, s * 0.59f, s * 0.36f, s * 0.67f, s * 0.3f)
        lineTo(s * 0.58f, s * 0.74f)
        close()
    }
    for (i in 0 until 5) {
        val sx = s * (0.42f + i * 0.04f)
        drawLine(Color(0xFFFFFBE6), Offset(sx, s * 0.33f), Offset(sx, s * 0.72f), strokeWidth = s * 0.007f)
    }
    drawPath(framePath, Color.Transparent)
    drawLine(
        Color(0xFFFFD66B),
        Offset(s * 0.38f, s * 0.75f),
        Offset(s * 0.38f, s * 0.26f),
        strokeWidth = s * 0.032f,
        cap = StrokeCap.Round
    )
    drawLine(
        Color(0xFFFFD66B),
        Offset(s * 0.35f, s * 0.75f),
        Offset(s * 0.62f, s * 0.75f),
        strokeWidth = s * 0.038f,
        cap = StrokeCap.Round
    )
    val neck = Path().apply {
        moveTo(s * 0.38f, s * 0.27f)
        cubicTo(s * 0.46f, s * 0.2f, s * 0.59f, s * 0.35f, s * 0.68f, s * 0.3f)
        lineTo(s * 0.59f, s * 0.74f)
    }
    drawPath(neck, Color(0xFFFFCE5C), style = Stroke(s * 0.028f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawMusicNote(s * 0.26f, s * 0.48f, s * 0.055f, Color(0xFFFFE585), inkW)
    drawMusicNote(s * 0.76f, s * 0.52f, s * 0.05f, Color(0xFFFFB7DC), inkW)
}

internal fun DrawScope.drawSwanScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFC3ECFF), Color(0xFFEAF9FF), Color(0xFF9EE3DD))
    drawOval(Color(0xFF83D5EC), topLeft = Offset(s * 0.08f, s * 0.58f), size = Size(s * 0.84f, s * 0.28f))
    val inkW = s * 0.01f

    // Lilypads & lotus blossoms
    drawOval(Color(0xFF8ED992), topLeft = Offset(s * 0.16f, s * 0.71f), size = Size(s * 0.16f, s * 0.07f))
    drawOval(Color(0xFF8ED992), topLeft = Offset(s * 0.7f, s * 0.68f), size = Size(s * 0.16f, s * 0.07f))
    drawDaisyFlower(s * 0.24f, s * 0.7f, s * 0.042f, Color(0xFFFFB5D8))
    drawDaisyFlower(s * 0.78f, s * 0.67f, s * 0.038f, Color(0xFFFFD3EA))

    // Graceful curved-neck crystal swan
    val neck = Path().apply {
        moveTo(s * 0.56f, s * 0.56f)
        cubicTo(s * 0.66f, s * 0.4f, s * 0.59f, s * 0.24f, s * 0.5f, s * 0.29f)
    }
    drawPath(neck, Color(0xFFFFFDF9), style = Stroke(s * 0.068f, cap = StrokeCap.Round))
    drawOval(Color(0xFFFFFDF9), topLeft = Offset(s * 0.3f, s * 0.49f), size = Size(s * 0.34f, s * 0.19f))
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.3f, s * 0.49f),
        size = Size(s * 0.34f, s * 0.19f),
        style = Stroke(inkW)
    )
    drawCircle(Color(0xFFFFFDF9), radius = s * 0.055f, center = Offset(s * 0.5f, s * 0.29f))
    drawCircle(
        StorybookPalette.Ink,
        radius = s * 0.055f,
        center = Offset(s * 0.5f, s * 0.29f),
        style = Stroke(inkW * 0.9f)
    )
    drawCircle(Color(0xFFFFA9D4), radius = s * 0.025f, center = Offset(s * 0.55f, s * 0.41f))
    drawHeart(s * 0.42f, s * 0.58f, s * 0.035f, Color(0xFFA6E5FF), StorybookPalette.Ink, inkW * 0.6f)
    drawCircle(Color(0xFFFFDC6E), radius = s * 0.025f, center = Offset(s * 0.5f, s * 0.22f))
    drawGlossyEye(s * 0.485f, s * 0.285f, s * 0.014f, s * 0.017f, StorybookPalette.Ink)
}

internal fun DrawScope.drawPegasusScene(s: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFFFFCDE5),
            0.55f to Color(0xFFFFE7D0),
            1f to Color(0xFFBDE8FF),
            startY = 0f,
            endY = s
        ),
        size = Size(s, s)
    )
    drawPuffyCloud(s * 0.24f, s * 0.74f, s / 260f, Color.White)
    drawPuffyCloud(s * 0.75f, s * 0.72f, s / 250f, Color.White)
    val inkW = s * 0.01f

    // Lavender pegasus foal body, cream wings & rosy mane
    val foalTl = Offset(s * 0.34f, s * 0.45f)
    val foalSz = Size(s * 0.32f, s * 0.21f)
    drawOval(Color(0xFFE7CEFF), topLeft = foalTl, size = foalSz)
    drawOval(StorybookPalette.Ink, topLeft = foalTl, size = foalSz, style = Stroke(inkW))

    // Feathered wing
    rotate(-18f, pivot = Offset(s * 0.45f, s * 0.43f)) {
        drawOval(Color(0xFFFFFDF8), topLeft = Offset(s * 0.32f, s * 0.28f), size = Size(s * 0.2f, s * 0.14f))
        drawOval(
            StorybookPalette.Ink,
            topLeft = Offset(s * 0.32f, s * 0.28f),
            size = Size(s * 0.2f, s * 0.14f),
            style = Stroke(inkW * 0.85f)
        )
    }

    drawOval(Color(0xFFEBD4FF), topLeft = Offset(s * 0.54f, s * 0.28f), size = Size(s * 0.2f, s * 0.19f))
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.54f, s * 0.28f),
        size = Size(s * 0.2f, s * 0.19f),
        style = Stroke(inkW)
    )
    drawGlossyEye(s * 0.65f, s * 0.36f, s * 0.02f, s * 0.024f, Color(0xFF7B67D1))
    drawBlushCheek(s * 0.61f, s * 0.405f, s * 0.02f, s * 0.012f)
    drawSparkle(s * 0.22f, s * 0.24f, s * 0.032f, Color(0xFFFFE985))
    drawSparkle(s * 0.82f, s * 0.24f, s * 0.028f, Color(0xFFFFFDF4))
}

internal fun DrawScope.drawLanternScene(s: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFF5D6BB8),
            0.55f to Color(0xFF9E8CD8),
            1f to Color(0xFFFFD4C8),
            startY = 0f,
            endY = s
        ),
        size = Size(s, s)
    )
    val inkW = s * 0.009f
    drawPaperLantern(s * 0.23f, s * 0.24f, s * 0.1f, s * 0.13f, Color(0xFFFFCC78), Color(0xFFFFF4AF), inkW)
    drawPaperLantern(s * 0.78f, s * 0.2f, s * 0.09f, s * 0.12f, Color(0xFFFFA9C7), Color(0xFFFFDFEC), inkW)
    drawPaperLantern(s * 0.52f, s * 0.17f, s * 0.08f, s * 0.1f, Color(0xFFFFDF88), Color(0xFFFFF7BE), inkW)

    // Twilight hill + lantern terrace princess
    drawOval(Color(0xFF8EC99D), topLeft = Offset(-s * 0.1f, s * 0.7f), size = Size(s * 1.2f, s * 0.42f))
    val gown = Path().apply {
        moveTo(s * 0.42f, s * 0.52f)
        lineTo(s * 0.58f, s * 0.52f)
        quadraticTo(s * 0.66f, s * 0.73f, s * 0.64f, s * 0.82f)
        lineTo(s * 0.36f, s * 0.82f)
        close()
    }
    drawPath(gown, Color(0xFFFFB2D0))
    drawPath(gown, StorybookPalette.Ink, style = Stroke(inkW, join = StrokeJoin.Round))
    drawCircle(Color(0xFFFFE6D6), radius = s * 0.082f, center = Offset(s * 0.5f, s * 0.43f))
    drawCircle(StorybookPalette.Ink, radius = s * 0.082f, center = Offset(s * 0.5f, s * 0.43f), style = Stroke(inkW))
    drawPaperLantern(s * 0.7f, s * 0.55f, s * 0.095f, s * 0.12f, Color(0xFFFFDF7A), Color(0xFFFFF5B8), inkW)
    drawGlossyEye(s * 0.47f, s * 0.425f, s * 0.015f, s * 0.018f, StorybookPalette.Ink)
    drawGlossyEye(s * 0.53f, s * 0.425f, s * 0.015f, s * 0.018f, StorybookPalette.Ink)
}

internal fun DrawScope.drawCarouselScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFC1ECFF), Color(0xFFFFF0FA), Color(0xFFA2DF90))
    val inkW = s * 0.01f

    // Center gold pole & festive base
    drawLine(Color(0xFFFFD05C), Offset(s * 0.5f, s * 0.25f), Offset(s * 0.5f, s * 0.8f), strokeWidth = s * 0.03f)
    drawRoundRect(
        Color(0xFFFFC1DF),
        topLeft = Offset(s * 0.18f, s * 0.76f),
        size = Size(s * 0.64f, s * 0.08f),
        cornerRadius = CornerRadius(s * 0.03f)
    )

    // Striped canopy tent
    val tent = Path().apply {
        moveTo(s * 0.15f, s * 0.34f)
        lineTo(s * 0.5f, s * 0.16f)
        lineTo(s * 0.85f, s * 0.34f)
        close()
    }
    drawPath(tent, Color(0xFFFF9EC8))
    drawPath(tent, StorybookPalette.Ink, style = Stroke(inkW, join = StrokeJoin.Round))
    for (i in 0 until 6) {
        drawCircle(
            if (i % 2 == 0) Color(0xFFFFE57D) else Color(0xFFFFFDF8),
            radius = s * 0.028f,
            center = Offset(s * (0.2f + i * 0.12f), s * 0.35f)
        )
    }

    // Mint carousel pony
    drawOval(Color(0xFFA9EDE1), topLeft = Offset(s * 0.36f, s * 0.52f), size = Size(s * 0.25f, s * 0.16f))
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.36f, s * 0.52f),
        size = Size(s * 0.25f, s * 0.16f),
        style = Stroke(inkW)
    )
    drawCircle(Color(0xFFA9EDE1), radius = s * 0.068f, center = Offset(s * 0.59f, s * 0.47f))
    drawCircle(StorybookPalette.Ink, radius = s * 0.068f, center = Offset(s * 0.59f, s * 0.47f), style = Stroke(inkW))
    drawGlossyEye(s * 0.61f, s * 0.46f, s * 0.015f, s * 0.018f, Color(0xFF7B67D1))
    drawHeart(s * 0.47f, s * 0.59f, s * 0.032f, Color(0xFFFFB2D8), StorybookPalette.Ink, inkW * 0.7f)
}

internal fun DrawScope.drawBallScene(s: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFFF3DBFF),
            0.55f to Color(0xFFFFEEF7),
            1f to Color(0xFFFFF4DC),
            startY = 0f,
            endY = s
        ),
        size = Size(s, s)
    )
    val inkW = s * 0.01f

    // Grand archway & golden chandelier
    drawRoundRect(
        Color(0xFFFFFBF4),
        topLeft = Offset(s * 0.2f, s * 0.24f),
        size = Size(s * 0.6f, s * 0.54f),
        cornerRadius = CornerRadius(s * 0.26f)
    )
    drawRoundRect(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.2f, s * 0.24f),
        size = Size(s * 0.6f, s * 0.54f),
        cornerRadius = CornerRadius(s * 0.26f),
        style = Stroke(inkW)
    )
    drawCircle(Color(0xFFFFE17D), radius = s * 0.055f, center = Offset(s * 0.5f, s * 0.2f))
    drawCircle(Color(0xFFFFF8CB), radius = s * 0.035f, center = Offset(s * 0.42f, s * 0.23f))
    drawCircle(Color(0xFFFFF8CB), radius = s * 0.035f, center = Offset(s * 0.58f, s * 0.23f))

    // Royal ballroom carpet & dancing bunny + dragon silhouettes
    drawRoundRect(
        Color(0xFFFFB6D9),
        topLeft = Offset(s * 0.34f, s * 0.72f),
        size = Size(s * 0.32f, s * 0.16f),
        cornerRadius = CornerRadius(s * 0.04f)
    )
    drawCircle(Color(0xFFFFF7EF), radius = s * 0.08f, center = Offset(s * 0.39f, s * 0.52f))
    drawCircle(Color(0xFFA7ECBA), radius = s * 0.08f, center = Offset(s * 0.61f, s * 0.52f))
    drawOval(Color(0xFFFFA9D4), topLeft = Offset(s * 0.3f, s * 0.58f), size = Size(s * 0.18f, s * 0.2f))
    drawOval(Color(0xFF9EE8B2), topLeft = Offset(s * 0.52f, s * 0.58f), size = Size(s * 0.18f, s * 0.2f))
    drawHeart(s * 0.5f, s * 0.42f, s * 0.045f, Color(0xFFFF88BC), StorybookPalette.Ink, inkW * 0.7f)
}

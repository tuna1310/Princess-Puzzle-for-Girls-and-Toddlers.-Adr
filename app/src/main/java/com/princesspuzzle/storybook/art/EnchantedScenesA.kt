package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin

internal fun DrawScope.drawDragonScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFBDF0D8), Color(0xFFEDFBE9), Color(0xFF9BE08A))
    drawPuffyCloud(s * 0.22f, s * 0.17f, s / 420f)
    drawPuffyCloud(s * 0.8f, s * 0.21f, s / 450f)

    val inkW = s * 0.011f
    // Cozy spotted dragon egg on right
    val eggTl = Offset(s * 0.67f, s * 0.58f)
    val eggSz = Size(s * 0.17f, s * 0.22f)
    drawOval(Color(0xFFFFF5D4), topLeft = eggTl, size = eggSz)
    drawOval(StorybookPalette.Ink, topLeft = eggTl, size = eggSz, style = Stroke(inkW))
    drawCircle(Color(0xFFA9E6B8), radius = s * 0.022f, center = Offset(s * 0.73f, s * 0.66f))
    drawCircle(Color(0xFFFFB5CF), radius = s * 0.018f, center = Offset(s * 0.78f, s * 0.72f))

    // Fluffy curling dragon tail & spines
    val tail = Path().apply {
        moveTo(s * 0.32f, s * 0.67f)
        quadraticTo(s * 0.14f, s * 0.66f, s * 0.16f, s * 0.53f)
        quadraticTo(s * 0.19f, s * 0.45f, s * 0.26f, s * 0.5f)
        quadraticTo(s * 0.21f, s * 0.55f, s * 0.34f, s * 0.61f)
        close()
    }
    drawPath(tail, Color(0xFF9DE6AF))
    drawPath(tail, StorybookPalette.Ink, style = Stroke(inkW, join = StrokeJoin.Round))

    // Pastel coral wings
    for (side in listOf(-1f, 1f)) {
        val wx = s * (0.45f + side * 0.15f)
        rotate(side * 22f, pivot = Offset(wx, s * 0.5f)) {
            val tl = Offset(wx - s * 0.07f, s * 0.42f)
            val sz = Size(s * 0.14f, s * 0.11f)
            drawOval(Color(0xFFFFB4CE), topLeft = tl, size = sz)
            drawOval(StorybookPalette.Ink, topLeft = tl, size = sz, style = Stroke(inkW * 0.9f))
        }
    }

    // Chubby mint dragon body & cream belly
    drawOval(Color(0xFF9EE8B2), topLeft = Offset(s * 0.29f, s * 0.46f), size = Size(s * 0.32f, s * 0.32f))
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.29f, s * 0.46f),
        size = Size(s * 0.32f, s * 0.32f),
        style = Stroke(inkW)
    )
    drawOval(Color(0xFFFFF6C9), topLeft = Offset(s * 0.35f, s * 0.53f), size = Size(s * 0.2f, s * 0.22f))

    // Little horns, head, glossy eyes, blush & flame flower
    for (hx in listOf(s * 0.38f, s * 0.52f)) {
        drawOval(Color(0xFFFFDC7A), topLeft = Offset(hx - s * 0.02f, s * 0.23f), size = Size(s * 0.04f, s * 0.07f))
        drawOval(
            StorybookPalette.Ink,
            topLeft = Offset(hx - s * 0.02f, s * 0.23f),
            size = Size(s * 0.04f, s * 0.07f),
            style = Stroke(inkW * 0.8f)
        )
    }
    val headTl = Offset(s * 0.3f, s * 0.28f)
    val headSz = Size(s * 0.3f, s * 0.23f)
    drawOval(Color(0xFFA9EEBA), topLeft = headTl, size = headSz)
    drawOval(StorybookPalette.Ink, topLeft = headTl, size = headSz, style = Stroke(inkW))
    drawGlossyEye(s * 0.39f, s * 0.38f, s * 0.025f, s * 0.029f, Color(0xFF4F9D7E))
    drawGlossyEye(s * 0.51f, s * 0.38f, s * 0.025f, s * 0.029f, Color(0xFF4F9D7E))
    drawBlushCheek(s * 0.34f, s * 0.43f, s * 0.026f, s * 0.015f)
    drawBlushCheek(s * 0.56f, s * 0.43f, s * 0.026f, s * 0.015f)
    drawDaisyFlower(s * 0.21f, s * 0.81f, s * 0.04f, Color(0xFFFFDF8A))
}

internal fun DrawScope.drawCarriageScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFCDE6FF), Color(0xFFFFEFF9), Color(0xFFB3E39A))
    drawPastelRainbow(s * 0.72f, s * 0.35f, s * 0.28f, s * 0.014f, 0.72f)
    drawPuffyCloud(s * 0.2f, s * 0.16f, s / 410f)

    // Lavender garden road
    drawRoundRect(
        Color(0xFFF6DFB8),
        topLeft = Offset(0f, s * 0.74f),
        size = Size(s, s * 0.16f),
        cornerRadius = CornerRadius(s * 0.02f)
    )

    val inkW = s * 0.01f
    // Canopy cabin
    val bodyTl = Offset(s * 0.24f, s * 0.3f)
    val bodySz = Size(s * 0.52f, s * 0.39f)
    drawOval(
        brush = Brush.verticalGradient(
            0f to Color(0xFFFFC5E4),
            1f to Color(0xFFFF96C8),
            startY = bodyTl.y,
            endY = bodyTl.y + bodySz.height
        ),
        topLeft = bodyTl,
        size = bodySz
    )
    drawOval(StorybookPalette.Ink, topLeft = bodyTl, size = bodySz, style = Stroke(inkW))

    // Golden arched coach window + heart crest
    val winTl = Offset(s * 0.41f, s * 0.38f)
    val winSz = Size(s * 0.18f, s * 0.18f)
    drawRoundRect(Color(0xFFFFF9DA), topLeft = winTl, size = winSz, cornerRadius = CornerRadius(s * 0.09f))
    drawRoundRect(
        Color(0xFFFFCE67),
        topLeft = winTl,
        size = winSz,
        cornerRadius = CornerRadius(s * 0.09f),
        style = Stroke(s * 0.016f)
    )
    drawRoundRect(
        StorybookPalette.Ink,
        topLeft = winTl,
        size = winSz,
        cornerRadius = CornerRadius(s * 0.09f),
        style = Stroke(inkW * 0.8f)
    )
    drawHeart(s * 0.5f, s * 0.62f, s * 0.036f, Color(0xFFFFF49E), StorybookPalette.Ink, s * 0.007f)

    // Roof butterfly crown
    drawButterfly(s * 0.5f, s * 0.23f, s * 0.065f, Color(0xFFB3E6FF), 0.35f)

    // Twin golden carriage wheels
    for ((wx, r) in listOf(s * 0.33f to s * 0.115f, s * 0.67f to s * 0.115f)) {
        val cy = s * 0.72f
        drawCircle(Color(0xFFFFF7E5), radius = r, center = Offset(wx, cy))
        drawCircle(Color(0xFFFFCA5C), radius = r, center = Offset(wx, cy), style = Stroke(s * 0.02f))
        drawCircle(StorybookPalette.Ink, radius = r, center = Offset(wx, cy), style = Stroke(inkW))
        for (sp in 0 until 6) {
            val a = sp * (Math.PI / 3.0).toFloat()
            drawLine(
                Color(0xFFE6A744),
                Offset(wx, cy),
                Offset(wx + cos(a) * r * 0.88f, cy + sin(a) * r * 0.88f),
                strokeWidth = s * 0.01f
            )
        }
        drawCircle(Color(0xFFFF90C2), radius = r * 0.24f, center = Offset(wx, cy))
    }
    drawDaisyFlower(s * 0.14f, s * 0.85f, s * 0.04f, Color(0xFFFFFDF9))
    drawDaisyFlower(s * 0.86f, s * 0.86f, s * 0.042f, Color(0xFFFFDFF0))
}

internal fun DrawScope.drawWillowScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFBDE8FF), Color(0xFFFFF7E1), Color(0xFFA2DF8C))
    val inkW = s * 0.01f

    // Weeping willow tree trunk & drooping leafy curtain on left
    val trunk = Path().apply {
        moveTo(s * 0.16f, s * 0.82f)
        quadraticTo(s * 0.24f, s * 0.56f, s * 0.26f, s * 0.36f)
        lineTo(s * 0.36f, s * 0.36f)
        quadraticTo(s * 0.34f, s * 0.58f, s * 0.39f, s * 0.82f)
        close()
    }
    drawPath(trunk, Color(0xFFC59B78))
    drawPath(trunk, StorybookPalette.Ink, style = Stroke(inkW, join = StrokeJoin.Round))

    for (i in 0 until 5) {
        val bx = s * (0.14f + i * 0.12f)
        drawOval(Color(0xFF9EE0AC), topLeft = Offset(bx - s * 0.08f, s * 0.14f), size = Size(s * 0.18f, s * 0.24f))
        drawOval(
            Color(0xFF83D09A),
            topLeft = Offset(bx - s * 0.05f, s * 0.25f),
            size = Size(s * 0.09f, s * 0.22f)
        )
    }

    // Gentle storybook girl wearing mint-lavender cloak
    val cloak = Path().apply {
        moveTo(s * 0.45f, s * 0.54f)
        lineTo(s * 0.59f, s * 0.54f)
        quadraticTo(s * 0.67f, s * 0.73f, s * 0.64f, s * 0.82f)
        quadraticTo(s * 0.52f, s * 0.85f, s * 0.4f, s * 0.82f)
        close()
    }
    drawPath(cloak, Color(0xFFB3E5CC))
    drawPath(cloak, StorybookPalette.Ink, style = Stroke(inkW, join = StrokeJoin.Round))

    // Hair & face
    drawOval(Color(0xFFE7AA6E), topLeft = Offset(s * 0.43f, s * 0.36f), size = Size(s * 0.18f, s * 0.2f))
    drawOval(Color(0xFFFFE5D5), topLeft = Offset(s * 0.445f, s * 0.39f), size = Size(s * 0.15f, s * 0.15f))
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.445f, s * 0.39f),
        size = Size(s * 0.15f, s * 0.15f),
        style = Stroke(inkW)
    )
    drawGlossyEye(s * 0.49f, s * 0.46f, s * 0.017f, s * 0.02f, Color(0xFF5D7DC9))
    drawGlossyEye(s * 0.55f, s * 0.46f, s * 0.017f, s * 0.02f, Color(0xFF5D7DC9))
    drawBlushCheek(s * 0.47f, s * 0.495f, s * 0.018f, s * 0.01f)
    drawBlushCheek(s * 0.57f, s * 0.495f, s * 0.018f, s * 0.01f)

    // Friendly bluebird perched near outstretched hand
    val birdCenter = Offset(s * 0.74f, s * 0.48f)
    drawOval(
        Color(0xFF83D2FF),
        topLeft = Offset(birdCenter.x - s * 0.07f, birdCenter.y - s * 0.05f),
        size = Size(s * 0.14f, s * 0.1f)
    )
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(birdCenter.x - s * 0.07f, birdCenter.y - s * 0.05f),
        size = Size(s * 0.14f, s * 0.1f),
        style = Stroke(inkW)
    )
    drawCircle(Color(0xFFFFE47A), radius = s * 0.022f, center = Offset(birdCenter.x - s * 0.07f, birdCenter.y))
    drawGlossyEye(birdCenter.x - s * 0.025f, birdCenter.y - s * 0.015f, s * 0.012f, s * 0.014f, StorybookPalette.Ink)
    drawMusicNote(s * 0.83f, s * 0.34f, s * 0.055f, Color(0xFFFF88BC), s * 0.01f)
}

internal fun DrawScope.drawMermaidScene(s: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            0f to Color(0xFFA6F0EA),
            0.6f to Color(0xFF7AD8DC),
            1f to Color(0xFFFFE7BE),
            startY = 0f,
            endY = s
        ),
        size = Size(s, s)
    )
    val inkW = s * 0.01f

    // Coral & sea plants
    for ((kx, kh, col) in listOf(
        Triple(0.16f, 0.34f, Color(0xFF88D9A2)),
        Triple(0.84f, 0.38f, Color(0xFFFFA5C7)),
        Triple(0.24f, 0.25f, Color(0xFF7ECFA8))
    )) {
        drawRoundRect(
            col,
            topLeft = Offset(s * kx - s * 0.03f, s * (0.86f - kh)),
            size = Size(s * 0.06f, s * kh),
            cornerRadius = CornerRadius(s * 0.03f)
        )
    }

    // Giant pearl oyster shell on bottom right
    drawOval(Color(0xFFFFC8E2), topLeft = Offset(s * 0.62f, s * 0.65f), size = Size(s * 0.24f, s * 0.15f))
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.62f, s * 0.65f),
        size = Size(s * 0.24f, s * 0.15f),
        style = Stroke(inkW)
    )
    drawCircle(Color(0xFFFFFDF8), radius = s * 0.042f, center = Offset(s * 0.74f, s * 0.71f))
    drawCircle(
        StorybookPalette.Ink,
        radius = s * 0.042f,
        center = Offset(s * 0.74f, s * 0.71f),
        style = Stroke(inkW * 0.8f)
    )

    // Mermaid kitten tail & fin
    val tailPath = Path().apply {
        moveTo(s * 0.36f, s * 0.56f)
        lineTo(s * 0.52f, s * 0.56f)
        quadraticTo(s * 0.56f, s * 0.78f, s * 0.39f, s * 0.81f)
        quadraticTo(s * 0.27f, s * 0.78f, s * 0.36f, s * 0.56f)
        close()
    }
    drawPath(tailPath, Color(0xFFFFA8D3))
    drawPath(tailPath, StorybookPalette.Ink, style = Stroke(inkW, join = StrokeJoin.Round))
    drawHeart(s * 0.31f, s * 0.79f, s * 0.068f, Color(0xFFFFDF86), StorybookPalette.Ink, s * 0.008f)

    // Kitten mermaid head, ears, shell crown
    drawOval(Color(0xFFFFF5EB), topLeft = Offset(s * 0.32f, s * 0.3f), size = Size(s * 0.24f, s * 0.22f))
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.32f, s * 0.3f),
        size = Size(s * 0.24f, s * 0.22f),
        style = Stroke(inkW)
    )
    drawGlossyEye(s * 0.39f, s * 0.4f, s * 0.022f, s * 0.026f, Color(0xFF56A9BD))
    drawGlossyEye(s * 0.49f, s * 0.4f, s * 0.022f, s * 0.026f, Color(0xFF56A9BD))
    drawBlushCheek(s * 0.355f, s * 0.445f, s * 0.022f, s * 0.013f)
    drawBlushCheek(s * 0.525f, s * 0.445f, s * 0.022f, s * 0.013f)
    drawCircle(Color(0xFFFFE57F), radius = s * 0.03f, center = Offset(s * 0.44f, s * 0.27f))
    drawCircle(Color(0xC0FFFFFF), radius = s * 0.025f, center = Offset(s * 0.68f, s * 0.32f))
    drawCircle(Color(0xC0FFFFFF), radius = s * 0.018f, center = Offset(s * 0.25f, s * 0.26f))
}

internal fun DrawScope.drawTeapartyScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFFFDFEB), Color(0xFFFFF7E5), Color(0xFFA7E092))
    val inkW = s * 0.01f

    // Striped garden table & cloth
    drawRoundRect(
        Color(0xFFFFF6FC),
        topLeft = Offset(s * 0.14f, s * 0.65f),
        size = Size(s * 0.72f, s * 0.2f),
        cornerRadius = CornerRadius(s * 0.05f)
    )
    drawRoundRect(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.14f, s * 0.65f),
        size = Size(s * 0.72f, s * 0.2f),
        cornerRadius = CornerRadius(s * 0.05f),
        style = Stroke(inkW)
    )

    // Smiling pastel teapot
    drawOval(Color(0xFF9DE3FF), topLeft = Offset(s * 0.26f, s * 0.42f), size = Size(s * 0.24f, s * 0.22f))
    drawOval(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.26f, s * 0.42f),
        size = Size(s * 0.24f, s * 0.22f),
        style = Stroke(inkW)
    )
    drawCircle(Color(0xFFFFE38A), radius = s * 0.028f, center = Offset(s * 0.38f, s * 0.41f))
    drawGlossyEye(s * 0.34f, s * 0.51f, s * 0.016f, s * 0.018f, StorybookPalette.Ink)
    drawGlossyEye(s * 0.42f, s * 0.51f, s * 0.016f, s * 0.018f, StorybookPalette.Ink)
    drawBlushCheek(s * 0.31f, s * 0.545f, s * 0.016f, s * 0.01f)
    drawBlushCheek(s * 0.45f, s * 0.545f, s * 0.016f, s * 0.01f)

    // Berry cupcake & stacked teacup
    drawRoundRect(
        Color(0xFFFFB2D4),
        topLeft = Offset(s * 0.58f, s * 0.54f),
        size = Size(s * 0.16f, s * 0.11f),
        cornerRadius = CornerRadius(s * 0.02f)
    )
    drawOval(Color(0xFFFFF8F2), topLeft = Offset(s * 0.56f, s * 0.46f), size = Size(s * 0.2f, s * 0.11f))
    drawCircle(Color(0xFFFF7AA8), radius = s * 0.026f, center = Offset(s * 0.66f, s * 0.44f))
    drawSparkle(s * 0.46f, s * 0.28f, s * 0.03f, Color(0xFFFFFDF0))
    drawHeart(s * 0.7f, s * 0.33f, s * 0.038f, Color(0xFFFF90C0), StorybookPalette.Ink, s * 0.007f)
}

internal fun DrawScope.drawTreehouseScene(s: Float) {
    drawSceneSkyAndMeadow(s, Color(0xFFBFE9FF), Color(0xFFF6FCE7), Color(0xFF94D77B))
    val inkW = s * 0.01f

    // Tree canopy clouds & warm oak trunk
    drawCircle(Color(0xFF98DF8B), radius = s * 0.25f, center = Offset(s * 0.5f, s * 0.25f))
    drawCircle(Color(0xFFA7E997), radius = s * 0.19f, center = Offset(s * 0.32f, s * 0.3f))
    drawCircle(Color(0xFFA7E997), radius = s * 0.19f, center = Offset(s * 0.68f, s * 0.3f))

    drawRoundRect(
        Color(0xFFD7A57E),
        topLeft = Offset(s * 0.31f, s * 0.33f),
        size = Size(s * 0.38f, s * 0.49f),
        cornerRadius = CornerRadius(s * 0.04f)
    )
    drawRoundRect(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.31f, s * 0.33f),
        size = Size(s * 0.38f, s * 0.49f),
        cornerRadius = CornerRadius(s * 0.04f),
        style = Stroke(inkW)
    )

    // Cozy rounded peach cottage door & window
    drawRoundRect(
        Color(0xFFFFC9DA),
        topLeft = Offset(s * 0.41f, s * 0.56f),
        size = Size(s * 0.18f, s * 0.26f),
        cornerRadius = CornerRadius(s * 0.09f)
    )
    drawRoundRect(
        StorybookPalette.Ink,
        topLeft = Offset(s * 0.41f, s * 0.56f),
        size = Size(s * 0.18f, s * 0.26f),
        cornerRadius = CornerRadius(s * 0.09f),
        style = Stroke(inkW)
    )
    drawCircle(Color(0xFFFFF4AC), radius = s * 0.068f, center = Offset(s * 0.5f, s * 0.44f))
    drawCircle(StorybookPalette.Ink, radius = s * 0.068f, center = Offset(s * 0.5f, s * 0.44f), style = Stroke(inkW))

    // Toadstool mushrooms & acorn lantern
    for ((mx, my, col) in listOf(
        Triple(0.2f, 0.78f, Color(0xFFFF9DC2)),
        Triple(0.8f, 0.77f, Color(0xFFFFC988))
    )) {
        drawRoundRect(
            Color(0xFFFFF6E6),
            topLeft = Offset(s * mx - s * 0.025f, s * my),
            size = Size(s * 0.05f, s * 0.07f),
            cornerRadius = CornerRadius(s * 0.02f)
        )
        drawArc(
            color = col,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(s * mx - s * 0.07f, s * my - s * 0.06f),
            size = Size(s * 0.14f, s * 0.11f)
        )
    }
    drawCircle(Color(0xFFFFE985), radius = s * 0.028f, center = Offset(s * 0.26f, s * 0.46f))
    drawSparkle(s * 0.74f, s * 0.44f, s * 0.026f, Color(0xFFFFF8BE))
}

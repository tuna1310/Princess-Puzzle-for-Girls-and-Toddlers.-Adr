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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Shared geometric storybook drawing primitives with warm ink outlines and soft highlights.
 */
object IosVectorPrimitives {
    fun strokeWidth(w: Float, h: Float): Float = (min(w, h) * 0.014f).coerceIn(2f, 7f)

    fun DrawScope.outlinedOval(
        cx: Float,
        cy: Float,
        vw: Float,
        vh: Float,
        fill: Color,
        w: Float,
        h: Float,
        outline: Color = IosThemeColors.outlineInk,
        highlightAlpha: Float = 0.25f
    ) {
        val left = (cx - vw * 0.5f) * w
        val top = (cy - vh * 0.5f) * h
        val bw = vw * w
        val bh = vh * h
        drawOval(color = fill, topLeft = Offset(left, top), size = Size(bw, bh))
        if (highlightAlpha > 0f) {
            drawOval(
                color = Color.White.copy(alpha = highlightAlpha),
                topLeft = Offset(left + bw * 0.16f, top + bh * 0.10f),
                size = Size(bw * 0.58f, bh * 0.28f)
            )
        }
        drawOval(
            color = outline,
            topLeft = Offset(left, top),
            size = Size(bw, bh),
            style = Stroke(width = strokeWidth(w, h), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    fun DrawScope.outlinedRoundRect(
        cx: Float,
        cy: Float,
        vw: Float,
        vh: Float,
        radiusFrac: Float,
        fill: Color,
        w: Float,
        h: Float,
        outline: Color = IosThemeColors.outlineInk
    ) {
        val left = (cx - vw * 0.5f) * w
        val top = (cy - vh * 0.5f) * h
        val bw = vw * w
        val bh = vh * h
        val cr = min(bw, bh) * radiusFrac
        drawRoundRect(
            color = fill,
            topLeft = Offset(left, top),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(cr, cr)
        )
        drawRoundRect(
            color = outline,
            topLeft = Offset(left, top),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(cr, cr),
            style = Stroke(width = strokeWidth(w, h), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    fun DrawScope.outlinedPath(
        path: Path,
        fill: Color,
        w: Float,
        h: Float,
        outline: Color = IosThemeColors.outlineInk
    ) {
        drawPath(path = path, color = fill)
        drawPath(
            path = path,
            color = outline,
            style = Stroke(width = strokeWidth(w, h), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    fun DrawScope.gradientPath(
        path: Path,
        brush: Brush,
        w: Float,
        h: Float,
        outline: Color = IosThemeColors.outlineInk
    ) {
        drawPath(path = path, brush = brush)
        drawPath(
            path = path,
            color = outline,
            style = Stroke(width = strokeWidth(w, h), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    fun DrawScope.drawCrown(
        cx: Float,
        cy: Float,
        cw: Float,
        ch: Float,
        baseColor: Color,
        jewelColor: Color,
        w: Float,
        h: Float
    ) {
        val x0 = (cx - cw * 0.48f) * w
        val x1 = (cx + cw * 0.48f) * w
        val yBottom = (cy + ch * 0.42f) * h
        val yTop = (cy - ch * 0.40f) * h
        val yMid = (cy + ch * 0.05f) * h
        val bw = x1 - x0

        val path = Path().apply {
            moveTo(x0 + bw * 0.08f, yBottom)
            lineTo(x0, yTop + ch * h * 0.20f)
            lineTo(x0 + bw * 0.28f, yMid)
            lineTo(cx * w, yTop)
            lineTo(x1 - bw * 0.28f, yMid)
            lineTo(x1, yTop + ch * h * 0.20f)
            lineTo(x1 - bw * 0.08f, yBottom)
            close()
        }
        outlinedPath(path, baseColor, w, h)
        // Crown tips & center gem
        val tipR = (min(w, h) * 0.016f).coerceAtLeast(3f)
        drawCircle(color = IosThemeColors.creamPearl, radius = tipR, center = Offset(x0, yTop + ch * h * 0.20f))
        drawCircle(color = IosThemeColors.creamPearl, radius = tipR * 1.15f, center = Offset(cx * w, yTop))
        drawCircle(color = IosThemeColors.creamPearl, radius = tipR, center = Offset(x1, yTop + ch * h * 0.20f))
        drawCircle(color = jewelColor, radius = tipR * 1.4f, center = Offset(cx * w, (cy + ch * 0.14f) * h))
    }

    fun DrawScope.drawPrincessFace(
        cx: Float,
        cy: Float,
        fw: Float,
        fh: Float,
        skin: Color,
        eyeColor: Color,
        w: Float,
        h: Float
    ) {
        outlinedOval(cx, cy, fw, fh, skin, w, h, highlightAlpha = 0.15f)
        val eyeOffsetX = fw * 0.21f * w
        val eyeY = (cy - fh * 0.04f) * h
        val eyeR = (min(fw * w, fh * h) * 0.105f).coerceAtLeast(2.5f)
        listOf(-eyeOffsetX, eyeOffsetX).forEach { dx ->
            val ex = cx * w + dx
            drawCircle(color = IosThemeColors.outlineInk, radius = eyeR * 1.25f, center = Offset(ex, eyeY))
            drawCircle(color = eyeColor, radius = eyeR * 0.85f, center = Offset(ex, eyeY))
            drawCircle(
                color = Color.White,
                radius = eyeR * 0.42f,
                center = Offset(ex - eyeR * 0.35f, eyeY - eyeR * 0.35f)
            )
        }
        // Rosy cheeks
        val cheekR = eyeR * 1.35f
        val cheekY = (cy + fh * 0.14f) * h
        drawOval(
            color = IosThemeColors.blushPink.copy(alpha = 0.55f),
            topLeft = Offset(cx * w - eyeOffsetX * 1.42f - cheekR, cheekY - cheekR * 0.55f),
            size = Size(cheekR * 2f, cheekR * 1.1f)
        )
        drawOval(
            color = IosThemeColors.blushPink.copy(alpha = 0.55f),
            topLeft = Offset(cx * w + eyeOffsetX * 1.42f - cheekR, cheekY - cheekR * 0.55f),
            size = Size(cheekR * 2f, cheekR * 1.1f)
        )
        // Sweet curved smile
        val smilePath = Path().apply {
            val sx0 = (cx - fw * 0.14f) * w
            val sx1 = (cx + fw * 0.14f) * w
            val sy = (cy + fh * 0.20f) * h
            moveTo(sx0, sy)
            quadraticTo(cx * w, (cy + fh * 0.33f) * h, sx1, sy)
        }
        drawPath(
            path = smilePath,
            color = IosThemeColors.crimson,
            style = Stroke(width = strokeWidth(w, h) * 0.9f, cap = StrokeCap.Round)
        )
    }

    fun DrawScope.drawDaisy(
        cx: Float,
        cy: Float,
        radiusFrac: Float,
        petalColor: Color,
        centerColor: Color,
        w: Float,
        h: Float
    ) {
        val r = min(w, h) * radiusFrac
        val center = Offset(cx * w, cy * h)
        for (i in 0 until 6) {
            val angle = (i * PI / 3.0)
            val px = center.x + cos(angle).toFloat() * r * 0.62f
            val py = center.y + sin(angle).toFloat() * r * 0.62f
            drawCircle(color = petalColor, radius = r * 0.48f, center = Offset(px, py))
            drawCircle(
                color = IosThemeColors.outlineInk,
                radius = r * 0.48f,
                center = Offset(px, py),
                style = Stroke(width = (r * 0.10f).coerceAtLeast(1f))
            )
        }
        drawCircle(color = centerColor, radius = r * 0.42f, center = center)
    }

    fun DrawScope.drawHeart(cx: Float, cy: Float, hw: Float, hh: Float, fill: Color, w: Float, h: Float) {
        val x = cx * w
        val y = cy * h
        val rw = hw * w * 0.5f
        val rh = hh * h * 0.5f
        val path = Path().apply {
            moveTo(x, y + rh * 0.88f)
            cubicTo(x - rw * 1.15f, y + rh * 0.15f, x - rw * 0.95f, y - rh * 0.95f, x, y - rh * 0.28f)
            cubicTo(x + rw * 0.95f, y - rh * 0.95f, x + rw * 1.15f, y + rh * 0.15f, x, y + rh * 0.88f)
            close()
        }
        outlinedPath(path, fill, w, h)
    }

    fun DrawScope.drawFivePointStar(
        cx: Float,
        cy: Float,
        outerFrac: Float,
        innerFrac: Float,
        fill: Color,
        w: Float,
        h: Float
    ) {
        val minDim = min(w, h)
        val outerR = minDim * outerFrac
        val innerR = minDim * innerFrac
        val px = cx * w
        val py = cy * h
        val path = Path()
        for (i in 0 until 10) {
            val angle = -PI / 2.0 + i * PI / 5.0
            val rad = if (i % 2 == 0) outerR else innerR
            val vx = px + cos(angle).toFloat() * rad
            val vy = py + sin(angle).toFloat() * rad
            if (i == 0) path.moveTo(vx, vy) else path.lineTo(vx, vy)
        }
        path.close()
        outlinedPath(path, fill, w, h)
    }

    fun DrawScope.drawButterfly(
        cx: Float,
        cy: Float,
        bw: Float,
        bh: Float,
        wingColor: Color,
        accentColor: Color,
        w: Float,
        h: Float
    ) {
        // Upper wings
        outlinedOval(cx - bw * 0.24f, cy - bh * 0.16f, bw * 0.44f, bh * 0.48f, wingColor, w, h)
        outlinedOval(cx + bw * 0.24f, cy - bh * 0.16f, bw * 0.44f, bh * 0.48f, wingColor, w, h)
        // Lower wings
        outlinedOval(cx - bw * 0.20f, cy + bh * 0.20f, bw * 0.34f, bh * 0.38f, accentColor, w, h)
        outlinedOval(cx + bw * 0.20f, cy + bh * 0.20f, bw * 0.34f, bh * 0.38f, accentColor, w, h)
        // Body
        outlinedOval(cx, cy, bw * 0.12f, bh * 0.65f, IosThemeColors.outlineInk, w, h, highlightAlpha = 0f)
    }

    fun DrawScope.drawSpireAndFlag(
        cx: Float,
        cy: Float,
        sw: Float,
        sh: Float,
        coneColor: Color,
        flagColor: Color,
        w: Float,
        h: Float
    ) {
        val x = cx * w
        val topY = (cy - sh * 0.45f) * h
        val coneTopY = (cy - sh * 0.18f) * h
        val botY = (cy + sh * 0.45f) * h
        val halfW = sw * w * 0.46f

        // Flagpole & fluttering pennant
        drawLine(
            color = IosThemeColors.outlineInk,
            start = Offset(x, coneTopY),
            end = Offset(x, topY),
            strokeWidth = strokeWidth(w, h),
            cap = StrokeCap.Round
        )
        val flag = Path().apply {
            moveTo(x, topY + sh * h * 0.02f)
            lineTo(x + sw * w * 0.42f, topY + sh * h * 0.11f)
            lineTo(x, topY + sh * h * 0.21f)
            close()
        }
        outlinedPath(flag, flagColor, w, h)

        // Cone roof
        val cone = Path().apply {
            moveTo(x, coneTopY)
            lineTo(x + halfW, botY)
            quadraticTo(x, botY + sh * h * 0.05f, x - halfW, botY)
            close()
        }
        outlinedPath(cone, coneColor, w, h)
    }

    fun DrawScope.drawPrincessArms(
        cx: Float,
        cy: Float,
        spanW: Float,
        armH: Float,
        skin: Color,
        sleeveColor: Color,
        w: Float,
        h: Float
    ) {
        outlinedOval(cx - spanW * 0.34f, cy + armH * 0.05f, spanW * 0.30f, armH * 0.38f, skin, w, h)
        outlinedOval(cx + spanW * 0.34f, cy + armH * 0.05f, spanW * 0.30f, armH * 0.38f, skin, w, h)
        outlinedOval(cx - spanW * 0.22f, cy - armH * 0.04f, spanW * 0.18f, armH * 0.48f, sleeveColor, w, h)
        outlinedOval(cx + spanW * 0.22f, cy - armH * 0.04f, spanW * 0.18f, armH * 0.48f, sleeveColor, w, h)
    }

    fun DrawScope.drawPrincessShoes(
        cx: Float,
        cy: Float,
        spanW: Float,
        legsH: Float,
        skin: Color,
        shoeColor: Color,
        w: Float,
        h: Float
    ) {
        outlinedRoundRect(cx - spanW * 0.20f, cy - legsH * 0.10f, spanW * 0.14f, legsH * 0.65f, 0.4f, skin, w, h)
        outlinedRoundRect(cx + spanW * 0.20f, cy - legsH * 0.10f, spanW * 0.14f, legsH * 0.65f, 0.4f, skin, w, h)
        outlinedOval(cx - spanW * 0.23f, cy + legsH * 0.30f, spanW * 0.26f, legsH * 0.28f, shoeColor, w, h)
        outlinedOval(cx + spanW * 0.23f, cy + legsH * 0.30f, spanW * 0.26f, legsH * 0.28f, shoeColor, w, h)
    }

    fun DrawScope.drawPrincessGown(
        cx: Float,
        cy: Float,
        waistW: Float,
        hemW: Float,
        gh: Float,
        skirtColor: Color,
        trimColor: Color,
        w: Float,
        h: Float
    ) {
        val topY = (cy - gh * 0.45f) * h
        val botY = (cy + gh * 0.42f) * h
        val hw = hemW * 0.5f * w
        val ww = waistW * 0.5f * w
        val x = cx * w
        val path = Path().apply {
            moveTo(x - ww, topY)
            lineTo(x + ww, topY)
            cubicTo(x + ww * 1.15f, topY + gh * h * 0.38f, x + hw, botY - gh * h * 0.15f, x + hw, botY)
            quadraticTo(x + hw * 0.5f, botY + gh * h * 0.10f, x, botY + gh * h * 0.04f)
            quadraticTo(x - hw * 0.5f, botY + gh * h * 0.10f, x - hw, botY)
            cubicTo(x - hw, botY - gh * h * 0.15f, x - ww * 1.15f, topY + gh * h * 0.38f, x - ww, topY)
            close()
        }
        outlinedPath(path, skirtColor, w, h)
        // Center pleat & glossy sheen lines
        drawLine(
            color = trimColor.copy(alpha = 0.72f),
            start = Offset(x, topY + gh * h * 0.12f),
            end = Offset(x, botY + gh * h * 0.02f),
            strokeWidth = strokeWidth(w, h) * 0.9f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(x - ww * 0.45f, topY + gh * h * 0.15f),
            end = Offset(x - hw * 0.46f, botY - gh * h * 0.06f),
            strokeWidth = strokeWidth(w, h) * 0.9f,
            cap = StrokeCap.Round
        )
    }

    fun DrawScope.drawWavyHairBack(cx: Float, cy: Float, hw: Float, hh: Float, hairColor: Color, w: Float, h: Float) {
        outlinedOval(cx - hw * 0.28f, cy + hh * 0.12f, hw * 0.40f, hh * 0.76f, hairColor, w, h)
        outlinedOval(cx + hw * 0.28f, cy + hh * 0.12f, hw * 0.40f, hh * 0.76f, hairColor, w, h)
        outlinedOval(cx, cy - hh * 0.08f, hw * 0.78f, hh * 0.78f, hairColor, w, h)
    }

    fun DrawScope.drawHairBangs(cx: Float, cy: Float, fw: Float, fh: Float, hairColor: Color, w: Float, h: Float) {
        outlinedOval(cx - fw * 0.24f, cy - fh * 0.34f, fw * 0.56f, fh * 0.24f, hairColor, w, h, highlightAlpha = 0.12f)
        outlinedOval(cx + fw * 0.24f, cy - fh * 0.34f, fw * 0.56f, fh * 0.24f, hairColor, w, h, highlightAlpha = 0.12f)
    }

    fun DrawScope.drawDragonWing(
        cx: Float,
        cy: Float,
        ww: Float,
        wh: Float,
        pointRight: Boolean,
        fill: Color,
        w: Float,
        h: Float
    ) {
        val dir = if (pointRight) 1f else -1f
        val x0 = cx * w
        val y0 = cy * h
        val rw = ww * 0.48f * w
        val rh = wh * 0.46f * h
        val path = Path().apply {
            moveTo(x0 - dir * rw * 0.75f, y0 + rh * 0.35f)
            cubicTo(
                x0 - dir * rw * 0.4f,
                y0 - rh * 0.9f,
                x0 + dir * rw * 0.6f,
                y0 - rh * 0.9f,
                x0 + dir * rw,
                y0 - rh * 0.3f
            )
            quadraticTo(x0 + dir * rw * 0.45f, y0 + rh * 0.2f, x0 + dir * rw * 0.35f, y0 + rh * 0.72f)
            quadraticTo(x0, y0 + rh * 0.4f, x0 - dir * rw * 0.75f, y0 + rh * 0.35f)
            close()
        }
        outlinedPath(path, fill, w, h)
    }
}

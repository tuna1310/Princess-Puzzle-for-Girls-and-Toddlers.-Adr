package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.princesspuzzle.storybook.puzzle.SceneThemeId
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

internal object LivingCelebrations {

    private val strokeWidth2 = Stroke(width = 2.4f, cap = StrokeCap.Round)
    private val rippleStroke = Stroke(width = 3f)

    fun DrawScope.drawForTheme(
        theme: SceneThemeId,
        boardX: Float,
        boardY: Float,
        boardSize: Float,
        timeSec: Float,
        scratchPath: Path
    ) {
        translate(boardX, boardY) {
            clipRect(0f, 0f, boardSize, boardSize) {
                drawAmbientTwinkles(boardSize, timeSec, scratchPath)
                when (theme) {
                    SceneThemeId.TIARA -> drawTiaraLiving(boardSize, timeSec, scratchPath)
                    SceneThemeId.BUNNY -> drawBunnyLiving(boardSize, timeSec)
                    SceneThemeId.UNICORN -> drawUnicornLiving(boardSize, timeSec, scratchPath)
                    SceneThemeId.CASTLE -> drawCastleLiving(boardSize, timeSec)
                    SceneThemeId.DRAGON -> drawDragonLiving(boardSize, timeSec)
                    SceneThemeId.CARRIAGE -> drawCarriageLiving(boardSize, timeSec)
                    SceneThemeId.WILLOW -> drawWillowLiving(boardSize, timeSec)
                    SceneThemeId.MERMAID -> drawMermaidLiving(boardSize, timeSec)
                    SceneThemeId.TEAPARTY -> drawTeapartyLiving(boardSize, timeSec, scratchPath)
                    SceneThemeId.TREEHOUSE -> drawTreehouseLiving(boardSize, timeSec)
                    SceneThemeId.HARP -> drawHarpLiving(boardSize, timeSec)
                    SceneThemeId.SWAN -> drawSwanLiving(boardSize, timeSec)
                    SceneThemeId.PEGASUS -> drawPegasusLiving(boardSize, timeSec)
                    SceneThemeId.LANTERN -> drawLanternLiving(boardSize, timeSec)
                    SceneThemeId.CAROUSEL -> drawCarouselLiving(boardSize, timeSec)
                    SceneThemeId.BALL -> drawBallLiving(boardSize, timeSec, scratchPath)
                }
            }
        }
    }

    private fun DrawScope.drawAmbientTwinkles(s: Float, t: Float, scratchPath: Path) {
        for (i in 0 until 6) {
            val px = s * (0.14f + ((i * 0.23f) % 0.74f))
            val py = s * (0.12f + ((i * 0.19f) % 0.68f)) + sin(t * 2.5f + i) * s * 0.014f
            val scale = 0.45f + 0.55f * abs(sin(t * 3.1f + i * 1.1f))
            drawSparkle(px, py, s * 0.026f * scale, Color(0xFFFFFDF2), 0.85f, scratchPath)
        }
    }

    private fun DrawScope.drawTiaraLiving(s: Float, t: Float, scratchPath: Path) {
        for (i in 0 until 4) {
            val hx = s * (0.25f + i * 0.16f)
            val hy = s * (0.35f - ((t * 0.13f + i * 0.25f) % 0.22f))
            val alpha = sin(((t * 0.5f + i * 0.25f) % 1f) * PI.toFloat()).coerceIn(0f, 0.85f)
            drawHeart(hx, hy, s * 0.034f, Color(0xFFFF96C5), Color(0xFFFFFDF8), 2f, alpha, scratchPath)
        }
    }

    private fun DrawScope.drawBunnyLiving(s: Float, t: Float) {
        drawButterfly(
            s * 0.24f + cos(t * 1.9f) * s * 0.05f,
            s * 0.3f + sin(t * 2.6f) * s * 0.04f,
            s * 0.042f,
            Color(0xFFFFB3DA),
            t * 11f
        )
        drawButterfly(
            s * 0.78f + sin(t * 1.7f) * s * 0.04f,
            s * 0.34f + cos(t * 2.4f) * s * 0.04f,
            s * 0.036f,
            Color(0xFFA6E4FF),
            t * 12f + 1f
        )
    }

    private fun DrawScope.drawUnicornLiving(s: Float, t: Float, scratchPath: Path) {
        for (i in 0 until 6) {
            val ang = PI.toFloat() * 1.12f + i * 0.15f + sin(t * 2f + i) * 0.04f
            val r = s * 0.44f
            drawSparkle(
                s * 0.5f + cos(ang) * r,
                s * 0.65f + sin(ang) * r,
                s * 0.025f,
                Color(0xFFFFE887),
                0.85f,
                scratchPath
            )
        }
    }

    private fun DrawScope.drawCastleLiving(s: Float, t: Float) {
        val sunCenter = Offset(s * 0.16f, s * 0.16f)
        for (i in 0 until 8) {
            val a = i * (PI.toFloat() / 4f) + t * 0.5f
            val r0 = s * 0.095f
            val r1 = s * (0.13f + 0.02f * abs(sin(t * 3f + i)))
            drawLine(
                Color(0xFFFFDF7A),
                Offset(sunCenter.x + cos(a) * r0, sunCenter.y + sin(a) * r0),
                Offset(sunCenter.x + cos(a) * r1, sunCenter.y + sin(a) * r1),
                strokeWidth = s * 0.01f,
                cap = StrokeCap.Round,
                alpha = 0.85f
            )
        }
    }

    private fun DrawScope.drawDragonLiving(s: Float, t: Float) {
        for (i in 0 until 5) {
            val phase = (t * 0.35f + i * 0.2f) % 1f
            val bx = s * (0.36f + sin(phase * 6f + i) * 0.08f)
            val by = s * (0.58f - phase * 0.38f)
            drawCircle(
                Color(0xFFFFD5B8),
                radius = s * (0.018f + 0.012f * phase),
                center = Offset(bx, by),
                alpha = (1f - phase) * 0.8f
            )
        }
    }

    private fun DrawScope.drawCarriageLiving(s: Float, t: Float) {
        for (wx in listOf(s * 0.33f, s * 0.67f)) {
            val cy = s * 0.72f
            val r = s * 0.095f
            rotate(t * 75f, pivot = Offset(wx, cy)) {
                drawLine(Color(0xFFFFE89C), Offset(wx - r, cy), Offset(wx + r, cy), strokeWidth = s * 0.012f)
                drawLine(Color(0xFFFFE89C), Offset(wx, cy - r), Offset(wx, cy + r), strokeWidth = s * 0.012f)
            }
        }
        for (i in 0 until 4) {
            val px = s * (0.2f + i * 0.2f + sin(t * 1.8f + i) * 0.03f)
            val py = s * (0.15f + ((t * 0.18f + i * 0.27f) % 0.6f))
            drawLeaf(px, py, s * 0.038f, t * 40f + i * 35f, Color(0xFFFFB7DC), s * 0.005f)
        }
    }

    private fun DrawScope.drawWillowLiving(s: Float, t: Float) {
        for (i in 0 until 3) {
            val p = (t * 0.3f + i * 0.33f) % 1f
            drawMusicNote(
                s * (0.76f + cos(p * 4f + i) * 0.04f),
                s * (0.45f - p * 0.28f),
                s * 0.045f,
                Color(0xFFFF93C7),
                s * 0.008f,
                (1f - p).coerceIn(0f, 0.9f)
            )
        }
        for (i in 0 until 4) {
            val p = (t * 0.22f + i * 0.25f) % 1f
            drawLeaf(
                s * (0.14f + i * 0.1f + sin(p * 5f) * 0.02f),
                s * (0.22f + p * 0.56f),
                s * 0.04f,
                25f + sin(t * 2f + i) * 20f,
                Color(0xFF9BE6AA),
                s * 0.005f
            )
        }
    }

    private fun DrawScope.drawMermaidLiving(s: Float, t: Float) {
        for (i in 0 until 7) {
            val p = (t * 0.32f + i * 0.14f) % 1f
            val cx = s * (0.18f + i * 0.1f + sin(p * 6f + i) * 0.02f)
            val cy = s * (0.85f - p * 0.72f)
            val r = s * (0.014f + (i % 3) * 0.007f)
            drawCircle(Color(0xA6FFFFFF), radius = r, center = Offset(cx, cy))
            drawCircle(Color.White, radius = r, center = Offset(cx, cy), style = strokeWidth2)
        }
    }

    private fun DrawScope.drawTeapartyLiving(s: Float, t: Float, scratchPath: Path) {
        for (i in 0 until 3) {
            val p = (t * 0.38f + i * 0.32f) % 1f
            val cx = s * (0.46f + sin(p * 6f + i) * 0.03f)
            val cy = s * (0.46f - p * 0.24f)
            drawHeart(cx, cy, s * 0.026f, Color(0xFFFFC1DF), Color.White, 1.5f, (1f - p) * 0.85f, scratchPath)
        }
    }

    private fun DrawScope.drawTreehouseLiving(s: Float, t: Float) {
        for (i in 0 until 6) {
            val fx = s * (0.2f + ((i * 0.13f + sin(t * 1.4f + i) * 0.05f) % 0.65f))
            val fy = s * (0.36f + ((i * 0.11f + cos(t * 1.6f + i) * 0.04f) % 0.45f))
            val glowAlpha = (0.35f + 0.65f * abs(sin(t * 3.2f + i))).coerceIn(0f, 1f)
            drawCircle(Color(0xFFFFEC84), radius = s * 0.024f, center = Offset(fx, fy), alpha = glowAlpha * 0.45f)
            drawCircle(Color(0xFFFFFDF0), radius = s * 0.01f, center = Offset(fx, fy), alpha = glowAlpha)
        }
    }

    private fun DrawScope.drawHarpLiving(s: Float, t: Float) {
        for (i in 0 until 3) {
            val p = (t * 0.45f + i * 0.33f) % 1f
            drawCircle(
                Color(0xFFFFEE9F),
                radius = s * (0.08f + p * 0.34f),
                center = Offset(s * 0.52f, s * 0.52f),
                alpha = (1f - p) * 0.7f,
                style = rippleStroke
            )
        }
        for (i in 0 until 3) {
            val p = (t * 0.28f + i * 0.33f) % 1f
            drawMusicNote(
                s * (0.28f + i * 0.22f),
                s * (0.42f - p * 0.22f),
                s * 0.044f,
                Color(0xFFFFE585),
                s * 0.008f,
                1f - p
            )
        }
    }

    private fun DrawScope.drawSwanLiving(s: Float, t: Float) {
        for (i in 0 until 3) {
            val p = (t * 0.4f + i * 0.33f) % 1f
            val rx = s * (0.14f + p * 0.26f)
            val ry = rx * 0.36f
            drawOval(
                Color(0xC2FFFFFF),
                topLeft = Offset(s * 0.47f - rx, s * 0.69f - ry),
                size = Size(rx * 2f, ry * 2f),
                alpha = (1f - p) * 0.8f,
                style = rippleStroke
            )
        }
    }

    private fun DrawScope.drawPegasusLiving(s: Float, t: Float) {
        for (i in 0 until 5) {
            val p = (t * 0.5f + i * 0.2f) % 1f
            val sx = s * (0.85f - p * 0.7f)
            val sy = s * (0.14f + i * 0.09f + p * 0.2f)
            drawLine(
                Color(0xFFFFF2A6),
                Offset(sx, sy),
                Offset(sx + s * 0.07f, sy - s * 0.02f),
                strokeWidth = s * 0.01f,
                cap = StrokeCap.Round,
                alpha = (1f - abs(p - 0.5f) * 2f).coerceIn(0f, 1f)
            )
        }
    }

    private fun DrawScope.drawLanternLiving(s: Float, t: Float) {
        for (i in 0 until 4) {
            val p = (t * 0.2f + i * 0.25f) % 1f
            val lx = s * (0.18f + i * 0.21f + sin(p * 4f + i) * 0.025f)
            val ly = s * (0.82f - p * 0.72f)
            drawPaperLantern(
                lx,
                ly,
                s * 0.055f,
                s * 0.072f,
                Color(0xFFFFDF84),
                Color(0xFFFFF6BF),
                s * 0.006f,
                (1f - p * 0.3f).coerceIn(0f, 0.9f)
            )
        }
    }

    private fun DrawScope.drawCarouselLiving(s: Float, t: Float) {
        val colors = listOf(Color(0xFFFFE57D), Color(0xFFFFB2D8), Color(0xFF9DE3FF))
        for (i in 0 until 6) {
            val pulse = 0.45f + 0.55f * abs(sin(t * 5f + i * 0.9f))
            drawCircle(
                colors[i % colors.size],
                radius = s * 0.032f * pulse,
                center = Offset(s * (0.2f + i * 0.12f), s * 0.35f),
                alpha = 0.9f
            )
        }
    }

    private fun DrawScope.drawBallLiving(s: Float, t: Float, scratchPath: Path) {
        val sway = sin(t * 2.2f) * s * 0.025f
        for (i in 0 until 5) {
            val cx = s * (0.34f + i * 0.08f) + sway
            val cy = s * (0.22f + abs(i - 2) * 0.025f)
            drawSparkle(cx, cy, s * 0.028f, Color(0xFFFFF5A8), 0.9f, scratchPath)
        }
        drawHeart(
            s * 0.5f,
            s * (0.4f + sin(t * 3f) * 0.02f),
            s * 0.045f,
            Color(0xFFFF93C6),
            Color.White,
            2f,
            0.85f,
            scratchPath
        )
    }
}

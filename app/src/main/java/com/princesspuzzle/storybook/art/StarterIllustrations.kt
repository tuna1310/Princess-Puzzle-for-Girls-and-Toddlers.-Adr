package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.puzzle.SceneThemeId
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object StarterIllustrations {
    private val cache = HashMap<SceneThemeId, ImageBitmap>()

    fun getOrRenderArtwork(theme: SceneThemeId, density: Float): ImageBitmap {
        return cache.getOrPut(theme) {
            val sizePx = GameConfig.ARTWORK_CACHE_SIZE_PX
            val bitmap = ImageBitmap(sizePx, sizePx)
            val canvas = Canvas(bitmap)
            val drawScope = CanvasDrawScope()
            drawScope.draw(
                density = Density(density),
                layoutDirection = LayoutDirection.Ltr,
                canvas = canvas,
                size = Size(sizePx.toFloat(), sizePx.toFloat())
            ) {
                when (theme) {
                    SceneThemeId.TIARA -> drawTiaraScene(sizePx.toFloat())
                    SceneThemeId.BUNNY -> drawBunnyPrincessScene(sizePx.toFloat())
                    SceneThemeId.UNICORN -> drawUnicornScene(sizePx.toFloat())
                    SceneThemeId.CASTLE -> drawCastleScene(sizePx.toFloat())
                    else -> drawStarterMedallionScene(sizePx.toFloat())
                }
            }
            bitmap
        }
    }

    fun DrawScope.drawLivingCelebration(
        theme: SceneThemeId,
        boardX: Float,
        boardY: Float,
        boardSize: Float,
        timeSec: Float,
        scratchPath: Path
    ) {
        translate(boardX, boardY) {
            clipRect(0f, 0f, boardSize, boardSize) {
                for (i in 0 until 8) {
                    val px = boardSize * (0.14f + ((i * 0.23f) % 0.74f))
                    val py = boardSize * (0.12f + ((i * 0.19f) % 0.68f)) +
                        sin(timeSec * 2.5f + i) * boardSize * 0.015f
                    val scale = 0.5f + 0.5f * abs(sin(timeSec * 3.1f + i * 1.1f))
                    drawSparkle(px, py, boardSize * 0.032f * scale, Color(0xFFFFFDF2), 0.9f, scratchPath)
                }

                when (theme) {
                    SceneThemeId.TIARA -> {
                        for (i in 0 until 4) {
                            val hx = boardSize * (0.25f + i * 0.16f)
                            val hy = boardSize * (0.34f - ((timeSec * 0.12f + i * 0.25f) % 0.22f))
                            val alpha = sin(((timeSec * 0.5f + i * 0.25f) % 1f) * Math.PI.toFloat())
                            drawHeart(
                                hx,
                                hy,
                                boardSize * 0.035f,
                                Color(0xFFFF96C5),
                                Color(0xFFFFFDF8),
                                2f,
                                alpha.coerceIn(0f, 0.85f),
                                scratchPath
                            )
                        }
                    }
                    SceneThemeId.BUNNY -> {
                        drawButterfly(
                            boardSize * 0.24f + cos(timeSec * 1.9f) * boardSize * 0.05f,
                            boardSize * 0.3f + sin(timeSec * 2.6f) * boardSize * 0.04f,
                            boardSize * 0.042f,
                            Color(0xFFFFB3DA),
                            timeSec * 11f
                        )
                        drawButterfly(
                            boardSize * 0.78f + sin(timeSec * 1.7f) * boardSize * 0.04f,
                            boardSize * 0.34f + cos(timeSec * 2.4f) * boardSize * 0.04f,
                            boardSize * 0.036f,
                            Color(0xFFA6E4FF),
                            timeSec * 12f + 1f
                        )
                    }
                    SceneThemeId.UNICORN -> {
                        for (i in 0 until 6) {
                            val ang = Math.PI.toFloat() * 1.12f + i * 0.15f + sin(timeSec * 2f + i) * 0.04f
                            val r = boardSize * 0.44f
                            drawSparkle(
                                boardSize * 0.5f + cos(ang) * r,
                                boardSize * 0.65f + sin(ang) * r,
                                boardSize * 0.025f,
                                Color(0xFFFFE887),
                                0.85f,
                                scratchPath
                            )
                        }
                    }
                    else -> {
                        drawButterfly(
                            boardSize * 0.22f + sin(timeSec * 1.6f) * boardSize * 0.05f,
                            boardSize * 0.26f + cos(timeSec * 2.2f) * boardSize * 0.03f,
                            boardSize * 0.035f,
                            Color(0xFFFFD29D),
                            timeSec * 10f
                        )
                        drawButterfly(
                            boardSize * 0.78f + cos(timeSec * 1.5f) * boardSize * 0.05f,
                            boardSize * 0.22f + sin(timeSec * 2.1f) * boardSize * 0.03f,
                            boardSize * 0.032f,
                            Color(0xFFFFAFDA),
                            timeSec * 11f + 2f
                        )
                    }
                }
            }
        }
    }
}

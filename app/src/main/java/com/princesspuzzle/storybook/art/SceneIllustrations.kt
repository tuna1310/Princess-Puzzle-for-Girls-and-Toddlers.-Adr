package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.princesspuzzle.storybook.art.LivingCelebrations.drawForTheme
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.puzzle.SceneThemeId

/**
 * Renders and caches all 16 original pastel storybook illustrations and lightweight
 * grid thumbnails, plus dispatches unique per-level living celebration rewards.
 */
object SceneIllustrations {
    private val artworkLru = object : LinkedHashMap<SceneThemeId, ImageBitmap>(
        GameConfig.ARTWORK_LRU_CAPACITY + 1,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<SceneThemeId, ImageBitmap>?): Boolean {
            return size > GameConfig.ARTWORK_LRU_CAPACITY
        }
    }

    private val thumbnailCache = HashMap<SceneThemeId, ImageBitmap>()

    fun getOrRenderArtwork(theme: SceneThemeId, density: Float): ImageBitmap {
        return artworkLru.getOrPut(theme) {
            renderSquareBitmap(theme, GameConfig.ARTWORK_CACHE_SIZE_PX, density)
        }
    }

    fun getOrRenderThumbnail(theme: SceneThemeId, density: Float): ImageBitmap {
        return thumbnailCache.getOrPut(theme) {
            renderSquareBitmap(theme, GameConfig.THUMBNAIL_CACHE_SIZE_PX, density)
        }
    }

    private fun renderSquareBitmap(theme: SceneThemeId, sizePx: Int, density: Float): ImageBitmap {
        val bitmap = ImageBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)
        CanvasDrawScope().draw(
            density = Density(density),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(sizePx.toFloat(), sizePx.toFloat())
        ) {
            drawThemeScene(theme, sizePx.toFloat())
        }
        return bitmap
    }

    internal fun DrawScope.drawThemeScene(theme: SceneThemeId, s: Float) {
        when (theme) {
            SceneThemeId.TIARA -> drawTiaraScene(s)
            SceneThemeId.BUNNY -> drawBunnyPrincessScene(s)
            SceneThemeId.UNICORN -> drawUnicornScene(s)
            SceneThemeId.CASTLE -> drawCastleScene(s)
            SceneThemeId.DRAGON -> drawDragonScene(s)
            SceneThemeId.CARRIAGE -> drawCarriageScene(s)
            SceneThemeId.WILLOW -> drawWillowScene(s)
            SceneThemeId.MERMAID -> drawMermaidScene(s)
            SceneThemeId.TEAPARTY -> drawTeapartyScene(s)
            SceneThemeId.TREEHOUSE -> drawTreehouseScene(s)
            SceneThemeId.HARP -> drawHarpScene(s)
            SceneThemeId.SWAN -> drawSwanScene(s)
            SceneThemeId.PEGASUS -> drawPegasusScene(s)
            SceneThemeId.LANTERN -> drawLanternScene(s)
            SceneThemeId.CAROUSEL -> drawCarouselScene(s)
            SceneThemeId.BALL -> drawBallScene(s)
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
        drawForTheme(theme, boardX, boardY, boardSize, timeSec, scratchPath)
    }
}

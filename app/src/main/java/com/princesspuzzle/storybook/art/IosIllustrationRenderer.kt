package com.princesspuzzle.storybook.art

import android.graphics.Bitmap
import android.util.LruCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.createBitmap
import com.princesspuzzle.storybook.art.IosScenesGroupA.drawGroupAPart
import com.princesspuzzle.storybook.art.IosScenesGroupB.drawGroupBPart
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.puzzle.LevelDefinition
import com.princesspuzzle.storybook.puzzle.PuzzlePieceDefinition
import com.princesspuzzle.storybook.puzzle.SceneThemeId
import kotlin.math.roundToInt

/**
 * Color & soft grayscale silhouette pair cached for a single puzzle piece sprite.
 */
data class PieceBitmapPair(
    val colorBitmap: ImageBitmap,
    val silhouetteBitmap: ImageBitmap
)

/**
 * Offscreen cached procedural renderer for all 16 iOS-matched princess storybook levels
 * and their individual anatomical/object puzzle parts.
 */
object IosIllustrationRenderer {
    private val fullCache = LruCache<String, ImageBitmap>(GameConfig.ILLUSTRATION_CACHE_LIMIT)
    private val pieceCache = LruCache<String, PieceBitmapPair>(GameConfig.ILLUSTRATION_CACHE_LIMIT * 4)

    fun getFullIllustration(level: LevelDefinition, widthPx: Int, heightPx: Int): ImageBitmap {
        val safeW = widthPx.coerceIn(48, 900)
        val safeH = heightPx.coerceIn(48, 1000)
        val key = "${level.slugId}_${safeW}x$safeH"
        fullCache.get(key)?.let { return it }

        val androidBitmap = createBitmap(safeW, safeH)
        val imageBitmap = androidBitmap.asImageBitmap()
        val drawScope = CanvasDrawScope()
        val sortedPieces = level.pieces.sortedBy { it.zIndex }

        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(imageBitmap),
            size = Size(safeW.toFloat(), safeH.toFloat())
        ) {
            sortedPieces.forEach { piece ->
                drawThemePart(level.theme, piece.partIndex, safeW.toFloat(), safeH.toFloat())
            }
        }
        fullCache.put(key, imageBitmap)
        return imageBitmap
    }

    fun getPieceBitmaps(
        level: LevelDefinition,
        piece: PuzzlePieceDefinition,
        boardWidthPx: Int,
        boardHeightPx: Int
    ): PieceBitmapPair {
        val safeBoardW = boardWidthPx.coerceIn(80, 960)
        val safeBoardH = boardHeightPx.coerceIn(80, 1100)
        val pieceW = (piece.normalizedWidth * safeBoardW).roundToInt().coerceAtLeast(24)
        val pieceH = (piece.normalizedHeight * safeBoardH).roundToInt().coerceAtLeast(24)
        val key = "${level.slugId}_${piece.id}_${safeBoardW}x$safeBoardH"
        pieceCache.get(key)?.let { return it }

        val originX = (piece.normalizedCenterX - piece.normalizedWidth * 0.5f) * safeBoardW
        val originY = (piece.normalizedCenterY - piece.normalizedHeight * 0.5f) * safeBoardH

        val colorAndroid = createBitmap(pieceW, pieceH)
        val colorImage = colorAndroid.asImageBitmap()
        val drawScope = CanvasDrawScope()
        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(colorImage),
            size = Size(pieceW.toFloat(), pieceH.toFloat())
        ) {
            translate(left = -originX, top = -originY) {
                drawThemePart(level.theme, piece.partIndex, safeBoardW.toFloat(), safeBoardH.toFloat())
            }
        }

        val silhouetteImage = createGrayscaleSilhouette(colorImage.asAndroidBitmap()).asImageBitmap()
        val pair = PieceBitmapPair(colorBitmap = colorImage, silhouetteBitmap = silhouetteImage)
        pieceCache.put(key, pair)
        return pair
    }

    fun clearAll() {
        fullCache.evictAll()
        pieceCache.evictAll()
    }

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawThemePart(
        theme: SceneThemeId,
        partIndex: Int,
        w: Float,
        h: Float
    ) {
        when (theme) {
            SceneThemeId.PINK_CASTLE,
            SceneThemeId.BRUNETTE_PRINCESS,
            SceneThemeId.ROYAL_PONY,
            SceneThemeId.CHERRY_ICECREAM,
            SceneThemeId.BLONDE_PRINCESS,
            SceneThemeId.FLOWER_PRINCESS,
            SceneThemeId.ROYAL_KITTY,
            SceneThemeId.FRIENDLY_DRAGON -> drawGroupAPart(theme, partIndex, w, h)
            else -> drawGroupBPart(theme, partIndex, w, h)
        }
    }

    private fun createGrayscaleSilhouette(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val c = pixels[i]
            val a = (c ushr 24) and 0xFF
            if (a == 0) {
                pixels[i] = 0
            } else {
                val r = (c ushr 16) and 0xFF
                val g = (c ushr 8) and 0xFF
                val b = c and 0xFF
                val lum = 0.299f * r + 0.587f * g + 0.114f * b
                val softGray = (lum * 0.58f + 92f).roundToInt().coerceIn(120, 236)
                pixels[i] = (a shl 24) or (softGray shl 16) or (softGray shl 8) or softGray
            }
        }
        return createBitmap(w, h).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }
}

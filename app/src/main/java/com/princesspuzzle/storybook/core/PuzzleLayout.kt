package com.princesspuzzle.storybook.core

import com.princesspuzzle.storybook.puzzle.JigsawPieceSpec
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class LayoutRect(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float
) {
    fun contains(px: Float, py: Float, pad: Float = 0f): Boolean {
        return px >= x - pad && px <= x + w + pad && py >= y - pad && py <= y + h + pad
    }
}

data class TraySlotLayout(
    val pieceId: Int,
    val x: Float,
    val y: Float,
    val scale: Float
)

data class PuzzleLayout(
    val width: Float,
    val height: Float,
    val density: Float,
    val isLandscape: Boolean,
    val topBar: LayoutRect,
    val board: LayoutRect,
    val tray: LayoutRect,
    val traySlots: List<TraySlotLayout>
)

private const val HALF = 0.5f
private const val PAD_MIN_DP = 12f
private const val PAD_MAX_DP = 24f
private const val PAD_RATIO = 0.03f
private const val TOP_BAR_Y_PAD_MULT = 0.6f
private const val TOP_BAR_HEIGHT_RATIO = 0.088f
private const val CONTENT_GAP_TOP_MULT = 0.65f
private const val CONTENT_MIN_HEIGHT_DP = 180f
private const val CONTENT_MIN_WIDTH_DP = 220f
private const val LANDSCAPE_MIN_BOARD_DP = 160f
private const val LANDSCAPE_MAX_BOARD_HEIGHT_RATIO = 0.94f
private const val LANDSCAPE_TRAY_X_GAP_MULT = 0.6f
private const val LANDSCAPE_TRAY_Y_PAD_MULT = 0.15f
private const val LANDSCAPE_TRAY_H_PAD_MULT = 0.3f
private const val PORTRAIT_GAP_MULT = 0.8f
private const val PORTRAIT_BOARD_MIN_TOP_DP = 4f
private const val PORTRAIT_MIN_BOARD_DP = 150f
private const val PORTRAIT_MAX_BOARD_WIDTH_RATIO = 0.92f
private const val TRAY_MIN_WIDTH_DP = 120f
private const val TRAY_MIN_HEIGHT_DP = 96f
private const val TRAY_HINT_RESERVE_DP = 24f
private const val TRAY_COMPACT_RESERVE_DP = 8f
private const val TRAY_COMPACT_HEIGHT_DP = 135f
private const val TRAY_INNER_PAD_MIN_DP = 10f
private const val TRAY_INNER_PAD_RATIO = 0.06f
private const val TRAY_MIN_CELL_DP = 40f
private const val MIN_PIECE_EXTENT = 0.1f
private const val PORTRAIT_ASPECT_BONUS = 4f
private const val EMPTY_SLOT_PENALTY = 2f

/**
 * Compute responsive board, tray, and non-overlapping piece slot coordinates
 * for any screen width/height in portrait or landscape orientation.
 */
fun computePuzzleLayout(width: Float, height: Float, density: Float, pieces: List<JigsawPieceSpec>): PuzzleLayout {
    val safeDensity = max(1f, density)
    val minPx = GameConfig.MIN_LAYOUT_DIMENSION_PX
    val safeW = max(minPx, width)
    val safeH = max(minPx, height)
    val isLandscape = safeW > safeH * GameConfig.LANDSCAPE_ASPECT_THRESHOLD
    val pad = (min(safeW, safeH) * PAD_RATIO).coerceIn(PAD_MIN_DP * safeDensity, PAD_MAX_DP * safeDensity)
    val topBarHeight = (safeH * TOP_BAR_HEIGHT_RATIO).coerceIn(
        GameConfig.TOP_BAR_MIN_HEIGHT_DP * safeDensity,
        GameConfig.TOP_BAR_MAX_HEIGHT_DP * safeDensity
    )

    val topBar = LayoutRect(x = pad, y = pad * TOP_BAR_Y_PAD_MULT, w = safeW - pad * 2f, h = topBarHeight)
    val contentTop = topBar.y + topBar.h + pad * CONTENT_GAP_TOP_MULT
    val contentH = max(CONTENT_MIN_HEIGHT_DP * safeDensity, safeH - contentTop - pad)
    val contentW = max(CONTENT_MIN_WIDTH_DP * safeDensity, safeW - pad * 2f)

    val board: LayoutRect
    val tray: LayoutRect

    if (isLandscape) {
        val maxBoardW = contentW * GameConfig.LANDSCAPE_BOARD_SHARE - pad * HALF
        val boardSide = max(
            LANDSCAPE_MIN_BOARD_DP * safeDensity,
            min(maxBoardW, contentH * LANDSCAPE_MAX_BOARD_HEIGHT_RATIO)
        )
        val boardAreaW = max(boardSide + pad * HALF, contentW * GameConfig.LANDSCAPE_BOARD_SHARE)
        board = LayoutRect(
            x = pad + (boardAreaW - boardSide) * HALF,
            y = contentTop + (contentH - boardSide) * HALF,
            w = boardSide,
            h = boardSide
        )
        val trayX = pad + boardAreaW + pad * LANDSCAPE_TRAY_X_GAP_MULT
        tray = LayoutRect(
            x = trayX,
            y = contentTop + pad * LANDSCAPE_TRAY_Y_PAD_MULT,
            w = max(TRAY_MIN_WIDTH_DP * safeDensity, safeW - pad - trayX),
            h = contentH - pad * LANDSCAPE_TRAY_H_PAD_MULT
        )
    } else {
        val gap = pad * PORTRAIT_GAP_MULT
        val trayRatio = if (pieces.size > GameConfig.TRAY_LARGE_PIECE_THRESHOLD) {
            GameConfig.PORTRAIT_TRAY_SHARE_LARGE
        } else {
            GameConfig.PORTRAIT_TRAY_SHARE_SMALL
        }
        val maxBoardH = contentH * (1f - trayRatio) - gap
        val boardSide = max(
            PORTRAIT_MIN_BOARD_DP * safeDensity,
            min(contentW * PORTRAIT_MAX_BOARD_WIDTH_RATIO, maxBoardH)
        )
        board = LayoutRect(
            x = (safeW - boardSide) * HALF,
            y = contentTop + max(PORTRAIT_BOARD_MIN_TOP_DP * safeDensity, (maxBoardH - boardSide) * HALF),
            w = boardSide,
            h = boardSide
        )
        val trayY = board.y + board.h + gap
        tray = LayoutRect(
            x = pad,
            y = trayY,
            w = contentW,
            h = max(TRAY_MIN_HEIGHT_DP * safeDensity, safeH - pad - trayY)
        )
    }

    val traySlots = computeTraySlots(tray, board, pieces, isLandscape, safeDensity)
    return PuzzleLayout(safeW, safeH, safeDensity, isLandscape, topBar, board, tray, traySlots)
}

private fun computeTraySlots(
    tray: LayoutRect,
    board: LayoutRect,
    pieces: List<JigsawPieceSpec>,
    isLandscape: Boolean,
    density: Float
): List<TraySlotLayout> {
    val count = pieces.size
    if (count == 0) return emptyList()

    val innerPad = max(TRAY_INNER_PAD_MIN_DP * density, min(tray.w, tray.h) * TRAY_INNER_PAD_RATIO)
    val topReserve = if (tray.h > TRAY_COMPACT_HEIGHT_DP * density) {
        TRAY_HINT_RESERVE_DP * density
    } else {
        TRAY_COMPACT_RESERVE_DP * density
    }
    val availW = max(TRAY_MIN_CELL_DP * density, tray.w - innerPad * 2f)
    val availH = max(TRAY_MIN_CELL_DP * density, tray.h - innerPad * 2f - topReserve)

    var bestCols = 1
    var bestScore = Float.NEGATIVE_INFINITY
    for (cols in 1..count) {
        val rows = ceil(count.toFloat() / cols).toInt()
        val cellW = availW / cols
        val cellH = availH / rows
        val aspectBonus = if (!isLandscape && cols >= rows) PORTRAIT_ASPECT_BONUS * density else 0f
        val emptySlots = cols * rows - count
        val score = min(cellW, cellH) + aspectBonus - emptySlots * EMPTY_SLOT_PENALTY * density
        if (score > bestScore) {
            bestScore = score
            bestCols = cols
        }
    }

    val bestRows = ceil(count.toFloat() / bestCols).toInt()
    val cellW = availW / bestCols
    val cellH = availH / bestRows

    var maxPieceNormW = MIN_PIECE_EXTENT
    var maxPieceNormH = MIN_PIECE_EXTENT
    for (p in pieces) {
        maxPieceNormW = max(maxPieceNormW, p.bounds.maxX - p.bounds.minX)
        maxPieceNormH = max(maxPieceNormH, p.bounds.maxY - p.bounds.minY)
    }

    val fitScaleX = (cellW * GameConfig.TRAY_CELL_FILL_RATIO) / max(1f, maxPieceNormW * board.w)
    val fitScaleY = (cellH * GameConfig.TRAY_CELL_FILL_RATIO) / max(1f, maxPieceNormH * board.h)
    val trayScale = min(fitScaleX, fitScaleY).coerceIn(GameConfig.TRAY_SCALE_MIN, GameConfig.TRAY_SCALE_MAX)

    return List(count) { idx ->
        val piece = pieces[idx]
        val row = idx / bestCols
        val col = idx % bestCols
        val itemsInRow = if (row == bestRows - 1) count - row * bestCols else bestCols
        val rowStartX = tray.x + innerPad + (availW - itemsInRow * cellW) * HALF
        val slotCenterX = rowStartX + (col + HALF) * cellW
        val slotCenterY = tray.y + innerPad + topReserve + (row + HALF) * cellH

        val boundsMidX = (piece.bounds.minX + piece.bounds.maxX) * HALF
        val boundsMidY = (piece.bounds.minY + piece.bounds.maxY) * HALF
        TraySlotLayout(
            pieceId = piece.id,
            x = slotCenterX + (piece.centerX - boundsMidX) * board.w * trayScale,
            y = slotCenterY + (piece.centerY - boundsMidY) * board.h * trayScale,
            scale = trayScale
        )
    }
}

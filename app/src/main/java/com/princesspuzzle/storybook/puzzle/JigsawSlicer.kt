package com.princesspuzzle.storybook.puzzle

import androidx.compose.ui.graphics.Path
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

sealed interface CurveCommand {
    val x: Float
    val y: Float

    data class Move(override val x: Float, override val y: Float) : CurveCommand
    data class Line(override val x: Float, override val y: Float) : CurveCommand
    data class Cubic(
        val cp1x: Float,
        val cp1y: Float,
        val cp2x: Float,
        val cp2y: Float,
        override val x: Float,
        override val y: Float
    ) : CurveCommand
}

data class NormalizedBounds(
    val minX: Float,
    val minY: Float,
    val maxX: Float,
    val maxY: Float
)

data class JigsawPieceSpec(
    val id: Int,
    val row: Int,
    val col: Int,
    val cellX: Float,
    val cellY: Float,
    val cellW: Float,
    val cellH: Float,
    val centerX: Float,
    val centerY: Float,
    val commands: List<CurveCommand>,
    val bounds: NormalizedBounds
)

private data class DirectedEdge(
    val startX: Float,
    val startY: Float,
    val segments: List<CurveCommand>
)

private data class HorizontalSubEdge(
    val x0: Float,
    val x1: Float,
    val edgeLR: DirectedEdge
)

private const val MIN_PIECE_COUNT = 2
private const val MAX_PIECE_COUNT = 12
private const val HASH_MUL_A = 12.9898f
private const val HASH_ADD_B = 78.233f
private const val HASH_SCALE = 43758.545f
private const val MIN_TAB_EDGE_LENGTH = 0.12f
private const val MIN_TAB_DEPTH = 0.001f
private const val MAX_TAB_LENGTH_RATIO = 0.27f
private const val CELL_TAB_DEPTH_RATIO = 0.22f
private const val DEDUP_EPS = 1e-6f
private const val HALF = 0.5f

private const val SHOULDER_U1 = 0.22f
private const val NECK_U = 0.38f
private const val NECK_V = 0.04f
private const val WAIST_V = 0.28f
private const val BULB_FLARE_V = 0.44f
private const val BULB_LEFT_U = 0.3f
private const val CROWN_U = 0.5f
private const val CROWN_V = 0.98f
private const val BULB_RIGHT_U = 0.7f
private const val NECK_RIGHT_U = 0.62f
private const val SHOULDER_U2 = 0.78f

private const val SEED_ROW_FACTOR = 97
private const val SEED_COL_FACTOR = 31
private const val SEED_SEG_FACTOR = 7
private const val VERT_SEED_FACTOR = 131
private const val VERT_ROW_FACTOR = 43
private const val VERT_COL_FACTOR = 13

private val ROW_LAYOUTS: Map<Int, List<Int>> = mapOf(
    2 to listOf(2),
    3 to listOf(3),
    4 to listOf(2, 2),
    5 to listOf(2, 3),
    6 to listOf(3, 3),
    7 to listOf(3, 4),
    8 to listOf(4, 4),
    9 to listOf(3, 3, 3),
    10 to listOf(3, 4, 3),
    11 to listOf(3, 4, 4),
    12 to listOf(4, 4, 4)
)

private fun seededUnit(seed: Int): Float {
    val raw = sin(seed * HASH_MUL_A + HASH_ADD_B) * HASH_SCALE
    return raw - floor(raw)
}

private fun buildEdgeSegments(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    tabDirection: Int,
    tabDepth: Float
): DirectedEdge {
    val dx = x1 - x0
    val dy = y1 - y0
    val len = hypot(dx, dy)

    if (tabDirection == 0 || len < MIN_TAB_EDGE_LENGTH || tabDepth <= MIN_TAB_DEPTH) {
        return DirectedEdge(x0, y0, listOf(CurveCommand.Line(x1, y1)))
    }

    val tx = dx / len
    val ty = dy / len
    val nx = ty
    val ny = -tx
    val h = min(tabDepth, len * MAX_TAB_LENGTH_RATIO) * tabDirection

    fun px(u: Float, v: Float): Float = x0 + tx * (u * len) + nx * (v * h)
    fun py(u: Float, v: Float): Float = y0 + ty * (u * len) + ny * (v * h)

    return DirectedEdge(
        startX = x0,
        startY = y0,
        segments = listOf(
            CurveCommand.Cubic(
                px(SHOULDER_U1, 0f),
                py(SHOULDER_U1, 0f),
                px(NECK_U, NECK_V),
                py(NECK_U, NECK_V),
                px(NECK_U, WAIST_V),
                py(NECK_U, WAIST_V)
            ),
            CurveCommand.Cubic(
                px(NECK_U, BULB_FLARE_V),
                py(NECK_U, BULB_FLARE_V),
                px(BULB_LEFT_U, CROWN_V),
                py(BULB_LEFT_U, CROWN_V),
                px(CROWN_U, CROWN_V),
                py(CROWN_U, CROWN_V)
            ),
            CurveCommand.Cubic(
                px(BULB_RIGHT_U, CROWN_V),
                py(BULB_RIGHT_U, CROWN_V),
                px(NECK_RIGHT_U, BULB_FLARE_V),
                py(NECK_RIGHT_U, BULB_FLARE_V),
                px(NECK_RIGHT_U, WAIST_V),
                py(NECK_RIGHT_U, WAIST_V)
            ),
            CurveCommand.Cubic(
                px(NECK_RIGHT_U, NECK_V),
                py(NECK_RIGHT_U, NECK_V),
                px(SHOULDER_U2, 0f),
                py(SHOULDER_U2, 0f),
                px(1f, 0f),
                py(1f, 0f)
            )
        )
    )
}

private fun reverseEdge(edge: DirectedEdge): DirectedEdge {
    val xs = FloatArray(edge.segments.size + 1)
    val ys = FloatArray(edge.segments.size + 1)
    xs[0] = edge.startX
    ys[0] = edge.startY
    edge.segments.forEachIndexed { idx, seg ->
        xs[idx + 1] = seg.x
        ys[idx + 1] = seg.y
    }

    val reversed = ArrayList<CurveCommand>(edge.segments.size)
    for (i in edge.segments.indices.reversed()) {
        val seg = edge.segments[i]
        val prevX = xs[i]
        val prevY = ys[i]
        when (seg) {
            is CurveCommand.Line -> reversed.add(CurveCommand.Line(prevX, prevY))
            is CurveCommand.Cubic -> reversed.add(
                CurveCommand.Cubic(seg.cp2x, seg.cp2y, seg.cp1x, seg.cp1y, prevX, prevY)
            )
            is CurveCommand.Move -> Unit
        }
    }
    return DirectedEdge(xs.last(), ys.last(), reversed)
}

private fun uniqueSortedFloats(values: List<Float>): List<Float> {
    val sorted = values.sorted()
    val result = ArrayList<Float>(sorted.size)
    for (v in sorted) {
        if (result.isEmpty() || abs(v - result.last()) > DEDUP_EPS) {
            result.add(v)
        }
    }
    return result
}

private fun computeBounds(commands: List<CurveCommand>): NormalizedBounds {
    var minX = Float.POSITIVE_INFINITY
    var minY = Float.POSITIVE_INFINITY
    var maxX = Float.NEGATIVE_INFINITY
    var maxY = Float.NEGATIVE_INFINITY

    fun include(x: Float, y: Float) {
        if (x < minX) minX = x
        if (y < minY) minY = y
        if (x > maxX) maxX = x
        if (y > maxY) maxY = y
    }

    for (cmd in commands) {
        include(cmd.x, cmd.y)
        if (cmd is CurveCommand.Cubic) {
            include(cmd.cp1x, cmd.cp1y)
            include(cmd.cp2x, cmd.cp2y)
        }
    }
    return NormalizedBounds(minX, minY, maxX, maxY)
}

fun getDefaultRowLayout(pieceCount: Int): List<Int> {
    val clamped = pieceCount.coerceIn(MIN_PIECE_COUNT, MAX_PIECE_COUNT)
    return ROW_LAYOUTS[clamped] ?: listOf(MIN_PIECE_COUNT)
}

private fun appendHorizontalSubEdges(
    seam: List<HorizontalSubEdge>,
    x0: Float,
    x1: Float,
    reversed: Boolean,
    out: MutableList<CurveCommand>
) {
    val indices = if (reversed) seam.indices.reversed() else seam.indices
    for (i in indices) {
        val sub = seam[i]
        if (sub.x0 >= x0 - DEDUP_EPS && sub.x1 <= x1 + DEDUP_EPS) {
            val edge = if (reversed) reverseEdge(sub.edgeLR) else sub.edgeLR
            out.addAll(edge.segments)
        }
    }
}

private fun buildPieceContour(
    row: Int,
    col: Int,
    numRows: Int,
    cols: Int,
    x0: Float,
    x1: Float,
    y0: Float,
    y1: Float,
    horizontalSeams: List<List<HorizontalSubEdge>>,
    verticalSeams: List<List<DirectedEdge>>
): List<CurveCommand> {
    val segments = ArrayList<CurveCommand>()
    if (row == 0) {
        segments.add(CurveCommand.Line(x1, y0))
    } else {
        appendHorizontalSubEdges(horizontalSeams[row - 1], x0, x1, reversed = false, out = segments)
    }

    if (col == cols - 1) {
        segments.add(CurveCommand.Line(x1, y1))
    } else {
        segments.addAll(verticalSeams[row][col].segments)
    }

    if (row == numRows - 1) {
        segments.add(CurveCommand.Line(x0, y1))
    } else {
        appendHorizontalSubEdges(horizontalSeams[row], x0, x1, reversed = true, out = segments)
    }

    if (col == 0) {
        segments.add(CurveCommand.Line(x0, y0))
    } else {
        segments.addAll(reverseEdge(verticalSeams[row][col - 1]).segments)
    }

    return listOf(CurveCommand.Move(x0, y0)) + segments
}

fun sliceJigsawPieces(pieceCount: Int, seed: Int = 1): List<JigsawPieceSpec> {
    val rowCounts = getDefaultRowLayout(pieceCount)
    val numRows = rowCounts.size
    val rowHeight = 1f / numRows
    val maxCols = rowCounts.max()
    val baseTabDepth = min(rowHeight, 1f / maxCols) * CELL_TAB_DEPTH_RATIO

    val rowCuts = rowCounts.map { cols -> List(cols + 1) { c -> c.toFloat() / cols } }
    val horizontalSeams = List(max(0, numRows - 1)) { r ->
        val y = (r + 1) * rowHeight
        val unionX = uniqueSortedFloats(rowCuts[r] + rowCuts[r + 1])
        List(unionX.size - 1) { i ->
            val dir = if (seededUnit(seed * SEED_ROW_FACTOR + r * SEED_COL_FACTOR + i * SEED_SEG_FACTOR) > HALF) {
                1
            } else {
                -1
            }
            HorizontalSubEdge(
                unionX[i],
                unionX[i + 1],
                buildEdgeSegments(unionX[i], y, unionX[i + 1], y, dir, baseTabDepth)
            )
        }
    }

    val verticalSeams = List(numRows) { r ->
        val cols = rowCounts[r]
        val y0 = r * rowHeight
        val y1 = (r + 1) * rowHeight
        List(max(0, cols - 1)) { cIdx ->
            val x = rowCuts[r][cIdx + 1]
            val dir = if (
                seededUnit(seed * VERT_SEED_FACTOR + r * VERT_ROW_FACTOR + (cIdx + 1) * VERT_COL_FACTOR) > HALF
            ) {
                1
            } else {
                -1
            }
            buildEdgeSegments(x, y0, x, y1, dir, baseTabDepth)
        }
    }

    val pieces = ArrayList<JigsawPieceSpec>(rowCounts.sum())
    var nextId = 0
    for (r in 0 until numRows) {
        val cols = rowCounts[r]
        val cuts = rowCuts[r]
        val y0 = r * rowHeight
        val y1 = (r + 1) * rowHeight
        for (c in 0 until cols) {
            val x0 = cuts[c]
            val x1 = cuts[c + 1]
            val commands = buildPieceContour(r, c, numRows, cols, x0, x1, y0, y1, horizontalSeams, verticalSeams)
            pieces.add(
                JigsawPieceSpec(
                    id = nextId++,
                    row = r,
                    col = c,
                    cellX = x0,
                    cellY = y0,
                    cellW = x1 - x0,
                    cellH = y1 - y0,
                    centerX = (x0 + x1) * HALF,
                    centerY = (y0 + y1) * HALF,
                    commands = commands,
                    bounds = computeBounds(commands)
                )
            )
        }
    }
    return pieces
}

fun buildComposePath(
    piece: JigsawPieceSpec,
    boardW: Float,
    boardH: Float,
    originX: Float = 0f,
    originY: Float = 0f
): Path {
    val path = Path()
    for (cmd in piece.commands) {
        when (cmd) {
            is CurveCommand.Move -> path.moveTo(originX + cmd.x * boardW, originY + cmd.y * boardH)
            is CurveCommand.Line -> path.lineTo(originX + cmd.x * boardW, originY + cmd.y * boardH)
            is CurveCommand.Cubic -> path.cubicTo(
                originX + cmd.cp1x * boardW,
                originY + cmd.cp1y * boardH,
                originX + cmd.cp2x * boardW,
                originY + cmd.cp2y * boardH,
                originX + cmd.x * boardW,
                originY + cmd.y * boardH
            )
        }
    }
    path.close()
    return path
}

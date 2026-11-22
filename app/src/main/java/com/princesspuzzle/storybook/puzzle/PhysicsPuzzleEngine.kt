@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.puzzle

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.GeometryHelpers
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class PuzzleBoardGeometry(
    val viewportWidthPx: Float,
    val viewportHeightPx: Float,
    val density: Float,
    val isTablet: Boolean,
    val isLandscape: Boolean,
    val boardCenterX: Float,
    val boardCenterY: Float,
    val boardWidthPx: Float,
    val boardHeightPx: Float,
    val magneticSnapRadiusPx: Float,
    val releaseSnapRadiusPx: Float,
    val trayMinX: Float,
    val trayMaxX: Float,
    val trayMinY: Float,
    val trayMaxY: Float
) {
    val boardLeftPx: Float get() = boardCenterX - boardWidthPx * 0.5f
    val boardTopPx: Float get() = boardCenterY - boardHeightPx * 0.5f
    val boardRightPx: Float get() = boardCenterX + boardWidthPx * 0.5f
    val boardBottomPx: Float get() = boardCenterY + boardHeightPx * 0.5f
}

data class PiecePhysicsBody(
    val definition: PuzzlePieceDefinition,
    val targetX: Float,
    val targetY: Float,
    val widthPx: Float,
    val heightPx: Float,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var angleRad: Float = 0f,
    var angularVelocity: Float = 0f,
    var scale: Float = 1f,
    var placed: Boolean = false,
    var dragging: Boolean = false,
    var snapAnimElapsedSec: Float = 1f,
    var grayFadeElapsedSec: Float = 0f,
    var grayAlpha: Float = 1f,
    var drawOrder: Int = definition.zIndex,
    var hintPulse: Float = 0f
) {
    var shelfFloorLiftPx: Float = 0f
}

sealed interface DragMoveOutcome {
    data object None : DragMoveOutcome
    data object Dragging : DragMoveOutcome
    data class Snapped(val piece: PiecePhysicsBody) : DragMoveOutcome
}

sealed interface DragEndOutcome {
    data object None : DragEndOutcome
    data class Snapped(val piece: PiecePhysicsBody) : DragEndOutcome
    data class Returned(val piece: PiecePhysicsBody) : DragEndOutcome
}

/**
 * Zero-allocation 60 FPS 2D rigid-body & magnetic snap engine reproducing iOS SpriteKit puzzle physics.
 */
class PhysicsPuzzleEngine {
    val bodies: MutableList<PiecePhysicsBody> = ArrayList(12)
    lateinit var geometry: PuzzleBoardGeometry
        private set

    var activeDragPieceId: String? = null
        private set
    private var topDrawOrder: Int = 20
    private var victoryElapsedSec: Float = 0f

    fun initialize(
        level: LevelDefinition,
        viewportWidthPx: Float,
        viewportHeightPx: Float,
        density: Float,
        placedPieceIds: Set<String> = emptySet()
    ) {
        val safeW = max(120f, viewportWidthPx)
        val safeH = max(120f, viewportHeightPx)
        val safeDensity = max(1f, density)
        geometry = computeBoardGeometry(safeW, safeH, safeDensity)

        bodies.clear()
        activeDragPieceId = null
        topDrawOrder = 20
        victoryElapsedSec = 0f

        val unplaced = level.pieces.filter { it.id !in placedPieceIds }
        val rng = Random(level.id * 97 + 13)
        val unplacedCount = max(1, unplaced.size)

        level.pieces.forEach { def ->
            val targetX = geometry.boardLeftPx + def.normalizedCenterX * geometry.boardWidthPx
            val targetY = geometry.boardTopPx + def.normalizedCenterY * geometry.boardHeightPx
            val pieceW = def.normalizedWidth * geometry.boardWidthPx
            val pieceH = def.normalizedHeight * geometry.boardHeightPx
            val isAlreadyPlaced = def.id in placedPieceIds

            if (isAlreadyPlaced) {
                bodies.add(
                    PiecePhysicsBody(
                        definition = def,
                        targetX = targetX,
                        targetY = targetY,
                        widthPx = pieceW,
                        heightPx = pieceH,
                        x = targetX,
                        y = targetY,
                        placed = true,
                        snapAnimElapsedSec = GameConfig.SNAP_ANIM_DURATION_SEC,
                        grayFadeElapsedSec = GameConfig.GRAY_FADEOUT_DURATION_SEC,
                        grayAlpha = 0f,
                        drawOrder = def.zIndex
                    )
                )
            } else {
                val trayIdx = unplaced.indexOfFirst { it.id == def.id }.coerceAtLeast(0)
                val cols = if (geometry.isLandscape) 2 else min(3, max(2, (unplacedCount + 1) / 2))
                val rows = max(1, (unplacedCount + cols - 1) / cols)
                val col = trayIdx % cols
                val row = trayIdx / cols
                val u = (col + 0.5f) / cols.toFloat()
                val v = (row + 0.5f) / rows.toFloat()

                val jitterX = (rng.nextFloat() - 0.5f) * (geometry.trayMaxX - geometry.trayMinX) * 0.08f
                val spawnX = GeometryHelpers.clampCoord(
                    geometry.trayMinX + u * (geometry.trayMaxX - geometry.trayMinX) + jitterX,
                    geometry.trayMinX,
                    geometry.trayMaxX
                )
                val traySpanY = max(40f * geometry.density, geometry.trayMaxY - geometry.trayMinY)
                val spawnY = GeometryHelpers.clampCoord(
                    geometry.trayMinY + v * traySpanY,
                    geometry.trayMinY,
                    geometry.trayMaxY
                )
                val tilt = (rng.nextFloat() - 0.5f) * 0.42f
                val shelfLift = if (geometry.isLandscape || rows <= 1) {
                    0f
                } else {
                    val rowFromBottom = (rows - 1 - row).toFloat() / (rows - 1).toFloat()
                    rowFromBottom * traySpanY * 0.72f
                }

                bodies.add(
                    PiecePhysicsBody(
                        definition = def,
                        targetX = targetX,
                        targetY = targetY,
                        widthPx = pieceW,
                        heightPx = pieceH,
                        x = spawnX,
                        y = spawnY - shelfLift * 0.15f,
                        angleRad = tilt,
                        drawOrder = 10 + def.zIndex
                    ).apply {
                        shelfFloorLiftPx = shelfLift
                    }
                )
            }
        }
    }

    fun findTopHitPiece(px: Float, py: Float): PiecePhysicsBody? {
        if (!::geometry.isInitialized) return null
        val pad = GameConfig.PIECE_HIT_PADDING_DP * geometry.density
        var best: PiecePhysicsBody? = null
        for (i in bodies.indices) {
            val body = bodies[i]
            if (body.placed) continue
            val halfW = body.widthPx * 0.5f + pad
            val halfH = body.heightPx * 0.5f + pad
            val dx = px - body.x
            val dy = py - body.y
            val cosA = cos(-body.angleRad)
            val sinA = sin(-body.angleRad)
            val localX = dx * cosA - dy * sinA
            val localY = dx * sinA + dy * cosA
            if (abs(localX) <= halfW && abs(localY) <= halfH) {
                if (best == null || body.drawOrder >= best.drawOrder) {
                    best = body
                }
            }
        }
        return best
    }

    fun beginDrag(pieceId: String): PiecePhysicsBody? {
        val body = findBodyById(pieceId)?.takeIf { !it.placed } ?: return null
        activeDragPieceId = pieceId
        topDrawOrder += 1
        body.drawOrder = topDrawOrder
        body.dragging = true
        body.vx = 0f
        body.vy = 0f
        body.angularVelocity = 0f
        body.scale = GameConfig.PIECE_DRAG_SCALE
        return body
    }

    fun moveDrag(deltaX: Float, deltaY: Float): DragMoveOutcome {
        val body = activeDragPieceId?.let { findBodyById(it) }?.takeIf { !it.placed }
            ?: return DragMoveOutcome.None

        val margin = GameConfig.VIEWPORT_EDGE_MARGIN_DP * geometry.density
        body.x = GeometryHelpers.clampCoord(body.x + deltaX, margin, geometry.viewportWidthPx - margin)
        body.y = GeometryHelpers.clampCoord(body.y + deltaY, margin, geometry.viewportHeightPx - margin)

        // Straighten angle cleanly while child drags towards target silhouette
        body.angleRad *= 0.65f
        if (abs(body.angleRad) < 0.01f) body.angleRad = 0f

        val dist = GeometryHelpers.distance(body.x, body.y, body.targetX, body.targetY)
        return if (dist <= geometry.magneticSnapRadiusPx) {
            snapBodyIntoSlot(body)
            DragMoveOutcome.Snapped(body)
        } else {
            DragMoveOutcome.Dragging
        }
    }

    fun endDrag(releaseVxPxPerSec: Float = 0f, releaseVyPxPerSec: Float = 0f): DragEndOutcome {
        val activeId = activeDragPieceId
        activeDragPieceId = null
        val body = activeId?.let { findBodyById(it) }?.takeIf { !it.placed }
            ?: return DragEndOutcome.None

        body.dragging = false
        body.scale = 1f
        val dist = GeometryHelpers.distance(body.x, body.y, body.targetX, body.targetY)
        return if (dist <= geometry.releaseSnapRadiusPx) {
            snapBodyIntoSlot(body)
            DragEndOutcome.Snapped(body)
        } else {
            val maxSpeed = 680f * geometry.density
            body.vx = releaseVxPxPerSec.coerceIn(-maxSpeed, maxSpeed) * 0.45f
            body.vy = releaseVyPxPerSec.coerceIn(-maxSpeed, maxSpeed) * 0.45f
            body.angularVelocity = (body.vx / max(1f, geometry.density)) * 0.004f
            DragEndOutcome.Returned(body)
        }
    }

    fun snapBodyById(pieceId: String): PiecePhysicsBody? {
        val body = findBodyById(pieceId) ?: return null
        snapBodyIntoSlot(body)
        return body
    }

    fun step(dtSec: Float, victoryActive: Boolean, hintTargetId: String?, hintPhase: Float) {
        if (!::geometry.isInitialized) return
        val dt = dtSec.coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
        if (victoryActive) {
            victoryElapsedSec += dt
        } else {
            victoryElapsedSec = 0f
        }

        val gravityPx = GameConfig.GRAVITY_DP_PER_SEC_SQ * geometry.density
        val linDamp = exp(-GameConfig.PIECE_LINEAR_DAMPING * dt)
        val angDamp = exp(-GameConfig.PIECE_ANGULAR_DAMPING * dt)
        val margin = GameConfig.VIEWPORT_EDGE_MARGIN_DP * geometry.density

        for (i in bodies.indices) {
            val body = bodies[i]
            body.hintPulse = if (!body.placed && body.definition.id == hintTargetId) hintPhase else 0f

            if (body.placed) {
                stepPlacedBody(body, dt, victoryActive)
            } else if (body.dragging) {
                body.angleRad *= 0.80f
                body.scale = GameConfig.PIECE_DRAG_SCALE
            } else {
                stepLooseBody(body, dt, gravityPx, linDamp, angDamp, margin)
            }
        }

        applyLoosePieceSeparation(dt)
    }

    private fun snapBodyIntoSlot(body: PiecePhysicsBody) {
        activeDragPieceId = null
        body.dragging = false
        body.placed = true
        body.x = body.targetX
        body.y = body.targetY
        body.vx = 0f
        body.vy = 0f
        body.angleRad = 0f
        body.angularVelocity = 0f
        body.snapAnimElapsedSec = 0f
        body.grayFadeElapsedSec = 0f
        body.grayAlpha = 1f
        body.drawOrder = body.definition.zIndex
    }

    private fun stepPlacedBody(body: PiecePhysicsBody, dt: Float, victoryActive: Boolean) {
        if (body.snapAnimElapsedSec < GameConfig.SNAP_ANIM_DURATION_SEC) {
            body.snapAnimElapsedSec = min(GameConfig.SNAP_ANIM_DURATION_SEC, body.snapAnimElapsedSec + dt)
            val t = body.snapAnimElapsedSec / GameConfig.SNAP_ANIM_DURATION_SEC
            val peak = GameConfig.PIECE_SNAP_PEAK_SCALE
            body.scale = if (t < 0.45f) {
                1f + (peak - 1f) * (t / 0.45f)
            } else {
                peak - (peak - 1f) * ((t - 0.45f) / 0.55f)
            }
        } else {
            body.scale = 1f
        }

        if (body.grayFadeElapsedSec < GameConfig.GRAY_FADEOUT_DURATION_SEC) {
            body.grayFadeElapsedSec = min(GameConfig.GRAY_FADEOUT_DURATION_SEC, body.grayFadeElapsedSec + dt)
            body.grayAlpha = 1f - (body.grayFadeElapsedSec / GameConfig.GRAY_FADEOUT_DURATION_SEC)
        } else {
            body.grayAlpha = 0f
        }

        if (victoryActive) {
            val bounceWave = sin(victoryElapsedSec * 5.2f + body.definition.partIndex * 0.35f)
            body.y = body.targetY + bounceWave * 5f * geometry.density
        } else {
            body.y = body.targetY
        }
    }

    private fun stepLooseBody(
        body: PiecePhysicsBody,
        dt: Float,
        gravityPx: Float,
        linDamp: Float,
        angDamp: Float,
        margin: Float
    ) {
        body.vy += gravityPx * dt
        body.vx *= linDamp
        body.vy *= linDamp
        body.angularVelocity *= angDamp

        body.x += body.vx * dt
        body.y += body.vy * dt
        body.angleRad = (body.angleRad + body.angularVelocity * dt).coerceIn(-0.65f, 0.65f)

        val halfW = min(body.widthPx * 0.42f, geometry.viewportWidthPx * 0.16f)
        val halfH = min(body.heightPx * 0.42f, geometry.viewportHeightPx * 0.14f)

        val minX = margin + halfW
        val maxX = geometry.viewportWidthPx - margin - halfW
        if (body.x < minX) {
            body.x = minX
            body.vx = abs(body.vx) * GameConfig.PIECE_RESTITUTION
        } else if (body.x > maxX) {
            body.x = maxX
            body.vx = -abs(body.vx) * GameConfig.PIECE_RESTITUTION
        }

        val minY = margin + halfH
        val bottomSurface = resolveFloorY(body)
        val maxY = max(minY, bottomSurface - halfH)
        if (body.y < minY) {
            body.y = minY
            body.vy = abs(body.vy) * GameConfig.PIECE_RESTITUTION
        } else if (body.y > maxY) {
            body.y = maxY
            body.vy = -abs(body.vy) * GameConfig.PIECE_RESTITUTION
            if (abs(body.vy) < 18f * geometry.density) {
                body.vy = 0f
            }
            body.vx *= (1f - GameConfig.PIECE_BOUNDARY_FRICTION * dt * 8f).coerceIn(0.4f, 0.96f)
            body.angularVelocity *= 0.85f
            body.angleRad *= 0.96f
        }
    }

    private fun resolveFloorY(body: PiecePhysicsBody): Float {
        val h = geometry.viewportHeightPx
        val w = geometry.viewportWidthPx
        val marginBottom = 16f * geometry.density
        if (geometry.isLandscape) return h - marginBottom

        val xNorm = (body.x / max(1f, w)).coerceIn(0f, 1f)
        val bankFraction = if (xNorm in 0.03f..0.44f) {
            val t = (xNorm - 0.03f) / 0.41f
            0.90f + 0.035f * t
        } else {
            0.945f
        }
        val baseFloor = min(h - marginBottom, h * bankFraction)
        val trayFloorCeiling = geometry.boardBottomPx + body.heightPx * 0.55f + 24f * geometry.density
        return max(trayFloorCeiling, baseFloor - body.shelfFloorLiftPx)
    }

    private fun applyLoosePieceSeparation(dt: Float) {
        for (i in bodies.indices) {
            val a = bodies[i]
            if (!a.placed && !a.dragging) {
                separateFromFollowingBodies(a, i + 1, dt)
            }
        }
    }

    private fun separateFromFollowingBodies(a: PiecePhysicsBody, startIndex: Int, dt: Float) {
        val strength = 140f * geometry.density
        for (j in startIndex until bodies.size) {
            val b = bodies[j]
            if (!b.placed && !b.dragging) {
                nudgeLoosePair(a, b, strength, dt)
            }
        }
    }

    private fun nudgeLoosePair(a: PiecePhysicsBody, b: PiecePhysicsBody, strength: Float, dt: Float) {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val minDistX = (min(a.widthPx, b.widthPx) + max(a.widthPx, b.widthPx)) * 0.34f
        val minDistY = (a.heightPx + b.heightPx) * 0.30f
        if (abs(dx) < minDistX && abs(dy) < minDistY) {
            val sign = if (dx >= 0f) 1f else -1f
            val push = ((minDistX - abs(dx)) / max(1f, minDistX)) * strength * dt
            a.x -= sign * push
            b.x += sign * push
        }
    }

    private fun findBodyById(id: String): PiecePhysicsBody? {
        for (i in bodies.indices) {
            if (bodies[i].definition.id == id) return bodies[i]
        }
        return null
    }

    companion object {
        fun computeBoardGeometry(viewportWidthPx: Float, viewportHeightPx: Float, density: Float): PuzzleBoardGeometry {
            val widthDp = viewportWidthPx / density
            val isTablet = widthDp >= GameConfig.TABLET_WIDTH_THRESHOLD_DP
            val isLandscape = viewportWidthPx > viewportHeightPx * 1.15f

            val magSnapDp = if (isTablet) GameConfig.MAGNETIC_SNAP_TABLET_DP else GameConfig.MAGNETIC_SNAP_PHONE_DP
            val relSnapDp = if (isTablet) GameConfig.RELEASE_SNAP_TABLET_DP else GameConfig.RELEASE_SNAP_PHONE_DP
            val magSnap = magSnapDp * density
            val relSnap = relSnapDp * density

            return if (isLandscape) {
                val boardW = min(viewportWidthPx * 0.36f, viewportHeightPx * 0.68f)
                val boardH = min(boardW * 1.06f, viewportHeightPx * 0.78f)
                PuzzleBoardGeometry(
                    viewportWidthPx = viewportWidthPx,
                    viewportHeightPx = viewportHeightPx,
                    density = density,
                    isTablet = isTablet,
                    isLandscape = true,
                    boardCenterX = viewportWidthPx * 0.31f,
                    boardCenterY = viewportHeightPx * 0.50f,
                    boardWidthPx = boardW,
                    boardHeightPx = boardH,
                    magneticSnapRadiusPx = magSnap,
                    releaseSnapRadiusPx = relSnap,
                    trayMinX = viewportWidthPx * 0.58f,
                    trayMaxX = viewportWidthPx * 0.90f,
                    trayMinY = viewportHeightPx * 0.22f,
                    trayMaxY = viewportHeightPx * 0.80f
                )
            } else {
                val maxBoardW = (if (isTablet) 410f else 280f) * density
                val wFrac = if (isTablet) 0.56f else 0.72f
                val boardW = min(viewportWidthPx * wFrac, maxBoardW)
                val boardH = min(boardW * 1.10f, viewportHeightPx * 0.43f)
                val centerY = max(boardH * 0.55f + 72f * density, viewportHeightPx * 0.33f)
                val trayTop = min(viewportHeightPx * 0.74f, centerY + boardH * 0.5f + 42f * density)
                val trayBottom = max(trayTop + 40f * density, viewportHeightPx * 0.85f)
                PuzzleBoardGeometry(
                    viewportWidthPx = viewportWidthPx,
                    viewportHeightPx = viewportHeightPx,
                    density = density,
                    isTablet = isTablet,
                    isLandscape = false,
                    boardCenterX = viewportWidthPx * 0.50f,
                    boardCenterY = centerY,
                    boardWidthPx = boardW,
                    boardHeightPx = boardH,
                    magneticSnapRadiusPx = magSnap,
                    releaseSnapRadiusPx = relSnap,
                    trayMinX = viewportWidthPx * 0.16f,
                    trayMaxX = viewportWidthPx * 0.84f,
                    trayMinY = trayTop,
                    trayMaxY = trayBottom
                )
            }
        }
    }
}

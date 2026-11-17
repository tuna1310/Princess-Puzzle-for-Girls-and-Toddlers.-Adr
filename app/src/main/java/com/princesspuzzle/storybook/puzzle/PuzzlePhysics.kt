package com.princesspuzzle.storybook.puzzle

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.LayoutRect
import com.princesspuzzle.storybook.core.PuzzleLayout
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

enum class PieceStatus {
    TRAY,
    DRAGGING,
    RETURNING,
    SNAPPING,
    PLACED
}

class PieceRuntime(
    val spec: JigsawPieceSpec,
    var x: Float,
    var y: Float,
    var scale: Float,
    var targetScale: Float,
    var zIndex: Int,
    var snapTargetX: Float,
    var snapTargetY: Float,
    var trayHomeX: Float,
    var trayHomeY: Float,
    var trayScale: Float
) {
    var status: PieceStatus = PieceStatus.TRAY
    var vx: Float = 0f
    var vy: Float = 0f
    var rotation: Float = 0f
    var glowIntensity: Float = 0f
    var snapAnimProgress: Float = 0f
    var returnBounceTimer: Float = 0f
    var returningElapsedSec: Float = 0f
}

/**
 * Pre-allocated event container mutated in-place each physics tick
 * to eliminate GC allocations in the `withFrameNanos` hot loop.
 */
class PhysicsStepResult {
    var justSnappedPieceId: Int = -1
    var justSnappedX: Float = 0f
    var justSnappedY: Float = 0f
    var placedCount: Int = 0
    var allCompleted: Boolean = false

    fun reset() {
        justSnappedPieceId = -1
        justSnappedX = 0f
        justSnappedY = 0f
        placedCount = 0
        allCompleted = false
    }
}

private const val HALF = 0.5f
private const val FALLBACK_TRAY_SCALE = 0.6f
private const val INITIAL_Z_OFFSET = 10
private const val LCG_A = 1664525L
private const val LCG_C = 1013904223L
private const val LCG_MASK = 0xFFFFFFFFL
private const val SEED_MUL = 9301L
private const val SEED_ADD = 49297L
private const val ROTATION_DECAY_BASE = 0.02f
private const val SNAP_ROTATION_DECAY_BASE = 0.01f
private const val DRAG_VELOCITY_FACTOR = 0.45f
private const val DRAG_TOUCH_CENTER_BLEND = 0.35f
private const val DRAG_ROTATION_EASE_RATE = 12f
private const val MIN_MOVE_DT_SEC = 0.008f
private const val MS_PER_SECOND = 1000f

class PuzzlePhysicsEngine(
    pieces: List<JigsawPieceSpec>,
    initialLayout: PuzzleLayout,
    seed: Int
) {
    val runtimes: Array<PieceRuntime>
    val stepResult = PhysicsStepResult()

    var activePieceIndex: Int = -1
        private set
    private var activePointerId: Long = -1L
    private var grabOffsetX = 0f
    private var grabOffsetY = 0f
    private var lastMoveMillis = 0L
    private var topZ = pieces.size + INITIAL_Z_OFFSET

    var inactivitySec: Float = 0f
        private set
    var hintPieceIndex: Int = -1
        private set

    init {
        val shuffled = seededShuffle(pieces, seed)
        val slotByPieceId = HashMap<Int, Int>(pieces.size)
        shuffled.forEachIndexed { idx, spec -> slotByPieceId[spec.id] = idx }

        runtimes = Array(pieces.size) { idx ->
            val spec = pieces[idx]
            val slotIdx = slotByPieceId[spec.id] ?: idx
            val slot = initialLayout.traySlots.getOrNull(slotIdx)
            val homeX = slot?.x ?: (initialLayout.tray.x + initialLayout.tray.w * HALF)
            val homeY = slot?.y ?: (initialLayout.tray.y + initialLayout.tray.h * HALF)
            val homeScale = slot?.scale ?: FALLBACK_TRAY_SCALE

            PieceRuntime(
                spec = spec,
                x = homeX,
                y = homeY,
                scale = homeScale,
                targetScale = homeScale,
                zIndex = idx + 1,
                snapTargetX = initialLayout.board.x + spec.centerX * initialLayout.board.w,
                snapTargetY = initialLayout.board.y + spec.centerY * initialLayout.board.h,
                trayHomeX = homeX,
                trayHomeY = homeY,
                trayScale = homeScale
            )
        }
    }

    fun hasActivePointer(): Boolean = activePieceIndex >= 0

    fun isTrackingPointer(pointerId: Long): Boolean = activePieceIndex >= 0 && activePointerId == pointerId

    fun isActivelyDragging(): Boolean = activePieceIndex >= 0

    fun resetInactivityHint() {
        inactivitySec = 0f
        hintPieceIndex = -1
    }

    fun syncToLayout(layout: PuzzleLayout) {
        val sortedByHome = runtimes.indices.sortedWith(
            compareBy({ runtimes[it].trayHomeY }, { runtimes[it].trayHomeX })
        )
        val slotOrder = IntArray(runtimes.size)
        sortedByHome.forEachIndexed { orderIdx, pieceIdx -> slotOrder[pieceIdx] = orderIdx }

        for (i in runtimes.indices) {
            val piece = runtimes[i]
            piece.snapTargetX = layout.board.x + piece.spec.centerX * layout.board.w
            piece.snapTargetY = layout.board.y + piece.spec.centerY * layout.board.h

            val slot = layout.traySlots.getOrNull(slotOrder[i]) ?: layout.traySlots.firstOrNull()
            if (slot != null) {
                piece.trayHomeX = slot.x
                piece.trayHomeY = slot.y
                piece.trayScale = slot.scale
            }
            applyLayoutToPieceState(piece, layout)
        }
    }

    private fun applyLayoutToPieceState(piece: PieceRuntime, layout: PuzzleLayout) {
        when (piece.status) {
            PieceStatus.PLACED, PieceStatus.SNAPPING -> {
                piece.x = piece.snapTargetX
                piece.y = piece.snapTargetY
                piece.scale = 1f
                piece.targetScale = 1f
            }
            PieceStatus.TRAY -> {
                piece.x = piece.trayHomeX
                piece.y = piece.trayHomeY
                piece.scale = piece.trayScale
                piece.targetScale = piece.trayScale
            }
            PieceStatus.RETURNING -> piece.targetScale = piece.trayScale
            PieceStatus.DRAGGING -> {
                val margin = GameConfig.VIEWPORT_EDGE_MARGIN_DP * layout.density
                piece.x = piece.x.coerceIn(margin, layout.width - margin)
                piece.y = piece.y.coerceIn(margin, layout.height - margin)
            }
        }
    }

    fun startDrag(
        pieceIndex: Int,
        pointerId: Long,
        pointerX: Float,
        pointerY: Float,
        layout: PuzzleLayout,
        isTouch: Boolean,
        nowMillis: Long
    ) {
        if (pieceIndex !in runtimes.indices || activePieceIndex >= 0) return
        val piece = runtimes[pieceIndex]
        if (piece.status == PieceStatus.PLACED || piece.status == PieceStatus.SNAPPING) return

        resetInactivityHint()
        activePieceIndex = pieceIndex
        activePointerId = pointerId
        piece.status = PieceStatus.DRAGGING
        piece.targetScale = 1f
        piece.vx = 0f
        piece.vy = 0f
        piece.returningElapsedSec = 0f
        piece.zIndex = ++topZ

        val liftOffset = computeTouchLiftOffset(layout.board, layout.density, piece.spec, isTouch)
        grabOffsetX = piece.x - pointerX
        grabOffsetY = piece.y - pointerY + liftOffset
        if (isTouch) {
            grabOffsetX *= DRAG_TOUCH_CENTER_BLEND
            grabOffsetY = liftOffset
            val margin = GameConfig.VIEWPORT_EDGE_MARGIN_DP * layout.density
            piece.x = (pointerX + grabOffsetX).coerceIn(margin, layout.width - margin)
            piece.y = (pointerY + grabOffsetY).coerceIn(margin, layout.height - margin)
        }
        lastMoveMillis = nowMillis
    }

    fun moveDrag(pointerId: Long, pointerX: Float, pointerY: Float, layout: PuzzleLayout, nowMillis: Long) {
        if (!isTrackingPointer(pointerId)) return
        val piece = runtimes[activePieceIndex]
        val dtSec = max(MIN_MOVE_DT_SEC, (nowMillis - lastMoveMillis) / MS_PER_SECOND)
        val margin = GameConfig.VIEWPORT_EDGE_MARGIN_DP * layout.density
        val nextX = (pointerX + grabOffsetX).coerceIn(margin, layout.width - margin)
        val nextY = (pointerY + grabOffsetY).coerceIn(margin, layout.height - margin)

        piece.vx = (nextX - piece.x) / dtSec
        piece.vy = (nextY - piece.y) / dtSec
        piece.x = nextX
        piece.y = nextY
        lastMoveMillis = nowMillis
    }

    fun endDrag(pointerId: Long, layout: PuzzleLayout, nowMillis: Long): Boolean {
        if (!isTrackingPointer(pointerId)) return false
        val piece = runtimes[activePieceIndex]
        activePieceIndex = -1
        activePointerId = -1L

        if (nowMillis - lastMoveMillis > GameConfig.STALE_MOVE_THRESHOLD_MS) {
            piece.vx = 0f
            piece.vy = 0f
        }
        return releasePiece(piece, layout)
    }

    fun cancelActiveDrag(layout: PuzzleLayout) {
        if (activePieceIndex < 0) return
        val piece = runtimes[activePieceIndex]
        activePieceIndex = -1
        activePointerId = -1L
        piece.vx = 0f
        piece.vy = 0f
        releasePiece(piece, layout)
    }

    fun releasePiece(piece: PieceRuntime, layout: PuzzleLayout): Boolean {
        if (isNearMatchingSlot(piece, layout.board, layout.density)) {
            piece.status = PieceStatus.SNAPPING
            piece.vx = 0f
            piece.vy = 0f
            piece.rotation = 0f
            piece.targetScale = 1f
            piece.glowIntensity = 1f
            piece.snapAnimProgress = 0f
            resetInactivityHint()
            return true
        }

        val maxVel = GameConfig.RETURN_VELOCITY_CLAMP_DP * layout.density
        val dropImpulse = GameConfig.RETURN_DROP_IMPULSE_Y_DP * layout.density
        piece.status = PieceStatus.RETURNING
        piece.targetScale = piece.trayScale
        piece.glowIntensity = 0f
        piece.vx = (piece.vx * DRAG_VELOCITY_FACTOR).coerceIn(-maxVel, maxVel)
        piece.vy = (piece.vy * DRAG_VELOCITY_FACTOR + dropImpulse).coerceIn(-maxVel, maxVel)
        piece.returnBounceTimer = GameConfig.RETURN_BOUNCE_DURATION_SEC
        piece.returningElapsedSec = 0f
        return false
    }

    fun step(dtSec: Float, layout: PuzzleLayout): PhysicsStepResult {
        stepResult.reset()
        val dt = dtSec.coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
        val scaleRate = min(1f, dt * GameConfig.SCALE_EASE_RATE)

        var anyBusy = false
        var firstTrayIdx = -1
        var placedOrSnappingCount = 0

        for (i in runtimes.indices) {
            val piece = runtimes[i]
            val scaleDiff = piece.targetScale - piece.scale
            if (abs(scaleDiff) <= GameConfig.SCALE_SNAP_EPSILON) {
                piece.scale = piece.targetScale
            } else {
                piece.scale += scaleDiff * scaleRate
            }

            when (piece.status) {
                PieceStatus.TRAY -> if (firstTrayIdx < 0) firstTrayIdx = i
                PieceStatus.DRAGGING -> {
                    anyBusy = true
                    stepDraggingPiece(piece, dt, layout)
                }
                PieceStatus.SNAPPING -> {
                    anyBusy = true
                    placedOrSnappingCount++
                    stepSnappingPiece(piece, dt)
                }
                PieceStatus.RETURNING -> {
                    anyBusy = true
                    stepReturningPiece(piece, dt, layout)
                }
                PieceStatus.PLACED -> placedOrSnappingCount++
            }
        }

        var allPlaced = runtimes.isNotEmpty()
        for (i in runtimes.indices) {
            if (runtimes[i].status != PieceStatus.PLACED) {
                allPlaced = false
                break
            }
        }
        updateHintTimer(dt, allPlaced, anyBusy, firstTrayIdx)
        stepResult.placedCount = placedOrSnappingCount
        stepResult.allCompleted = allPlaced
        return stepResult
    }

    private fun stepDraggingPiece(piece: PieceRuntime, dt: Float, layout: PuzzleLayout) {
        val targetGlow = if (isNearMatchingSlot(piece, layout.board, layout.density)) 1f else 0f
        piece.glowIntensity += (targetGlow - piece.glowIntensity) * min(1f, dt * GameConfig.GLOW_EASE_RATE)
        val targetRot = (piece.vx * GameConfig.DRAG_TILT_VELOCITY_FACTOR / layout.density)
            .coerceIn(-GameConfig.DRAG_TILT_MAX_RAD, GameConfig.DRAG_TILT_MAX_RAD)
        piece.rotation += (targetRot - piece.rotation) * min(1f, dt * DRAG_ROTATION_EASE_RATE)
        val decay = GameConfig.DRAG_VELOCITY_DECAY_BASE.pow(dt)
        piece.vx *= decay
        piece.vy *= decay
    }

    private fun stepSnappingPiece(piece: PieceRuntime, dt: Float) {
        val posEase = min(1f, dt * GameConfig.SNAP_POSITION_EASE_RATE)
        piece.x += (piece.snapTargetX - piece.x) * posEase
        piece.y += (piece.snapTargetY - piece.y) * posEase
        piece.rotation *= SNAP_ROTATION_DECAY_BASE.pow(dt)
        piece.snapAnimProgress = min(1f, piece.snapAnimProgress + dt * GameConfig.SNAP_ANIM_SPEED)

        if (piece.snapAnimProgress >= 1f) {
            piece.x = piece.snapTargetX
            piece.y = piece.snapTargetY
            piece.scale = 1f
            piece.rotation = 0f
            piece.glowIntensity = 0f
            piece.status = PieceStatus.PLACED
            stepResult.justSnappedPieceId = piece.spec.id
            stepResult.justSnappedX = piece.snapTargetX
            stepResult.justSnappedY = piece.snapTargetY
        }
    }

    private fun stepReturningPiece(piece: PieceRuntime, dt: Float, layout: PuzzleLayout) {
        piece.returningElapsedSec += dt
        if (piece.returnBounceTimer > 0f) {
            piece.returnBounceTimer = max(0f, piece.returnBounceTimer - dt)
            piece.vy += GameConfig.RETURN_BOUNCE_GRAVITY_DP * layout.density * dt
        }

        val dx = piece.trayHomeX - piece.x
        val dy = piece.trayHomeY - piece.y
        val ax = dx * GameConfig.RETURN_SPRING_STIFFNESS - piece.vx * GameConfig.RETURN_SPRING_DAMPING
        val ay = dy * GameConfig.RETURN_SPRING_STIFFNESS - piece.vy * GameConfig.RETURN_SPRING_DAMPING

        piece.vx += ax * dt
        piece.vy += ay * dt
        piece.x += piece.vx * dt
        piece.y += piece.vy * dt
        clampToViewportBounds(piece, layout)
        piece.rotation *= ROTATION_DECAY_BASE.pow(dt)

        val dist = hypot(piece.trayHomeX - piece.x, piece.trayHomeY - piece.y)
        val speed = hypot(piece.vx, piece.vy)
        val settled = dist < GameConfig.RETURN_SETTLE_DIST_DP * layout.density &&
            speed < GameConfig.RETURN_SETTLE_SPEED_DP * layout.density &&
            piece.returnBounceTimer <= 0f
        if (settled || piece.returningElapsedSec >= GameConfig.RETURN_SETTLE_TIMEOUT_SEC) {
            piece.x = piece.trayHomeX
            piece.y = piece.trayHomeY
            piece.vx = 0f
            piece.vy = 0f
            piece.rotation = 0f
            piece.scale = piece.trayScale
            piece.status = PieceStatus.TRAY
            piece.returningElapsedSec = 0f
        }
    }

    private fun clampToViewportBounds(piece: PieceRuntime, layout: PuzzleLayout) {
        val margin = GameConfig.VIEWPORT_EDGE_MARGIN_DP * layout.density
        if (piece.x < margin) {
            piece.x = margin
            piece.vx = abs(piece.vx) * GameConfig.RETURN_BOUNCE_RESTITUTION
        } else if (piece.x > layout.width - margin) {
            piece.x = layout.width - margin
            piece.vx = -abs(piece.vx) * GameConfig.RETURN_BOUNCE_RESTITUTION
        }
        if (piece.y < margin) {
            piece.y = margin
            piece.vy = abs(piece.vy) * GameConfig.RETURN_BOUNCE_RESTITUTION
        } else if (piece.y > layout.height - margin) {
            piece.y = layout.height - margin
            piece.vy = -abs(piece.vy) * GameConfig.RETURN_BOUNCE_RESTITUTION
        }
    }

    private fun updateHintTimer(dt: Float, allPlaced: Boolean, anyBusy: Boolean, firstTrayIdx: Int) {
        if (!allPlaced && !anyBusy && firstTrayIdx >= 0) {
            inactivitySec += dt
            if (inactivitySec >= GameConfig.HINT_INACTIVITY_DELAY_SEC) {
                hintPieceIndex = firstTrayIdx
            }
        } else if (activePieceIndex >= 0 || allPlaced) {
            resetInactivityHint()
        }
    }

    companion object {
        fun computeSnapRadius(board: LayoutRect, density: Float, spec: JigsawPieceSpec): Float {
            val pieceW = spec.cellW * board.w
            val pieceH = spec.cellH * board.h
            return max(
                GameConfig.MIN_SNAP_RADIUS_DP * density,
                min(pieceW, pieceH) * GameConfig.SNAP_RADIUS_PIECE_FRACTION
            )
        }

        fun computeTouchLiftOffset(board: LayoutRect, density: Float, spec: JigsawPieceSpec, isTouch: Boolean): Float {
            if (!isTouch) return 0f
            val pieceH = spec.cellH * board.h
            return -min(
                GameConfig.TOUCH_LIFT_OFFSET_MAX_DP * density,
                max(GameConfig.TOUCH_LIFT_OFFSET_MIN_DP * density, pieceH * GameConfig.TOUCH_LIFT_PIECE_FRACTION)
            )
        }

        fun isNearMatchingSlot(piece: PieceRuntime, board: LayoutRect, density: Float): Boolean {
            return hypot(piece.x - piece.snapTargetX, piece.y - piece.snapTargetY) <=
                computeSnapRadius(board, density, piece.spec)
        }

        fun <T> seededShuffle(items: List<T>, seed: Int): List<T> {
            val copy = items.toMutableList()
            var s = (seed.toLong() * SEED_MUL + SEED_ADD) and LCG_MASK
            for (i in copy.lastIndex downTo 1) {
                s = (s * LCG_A + LCG_C) and LCG_MASK
                val j = (s % (i + 1)).toInt()
                val tmp = copy[i]
                copy[i] = copy[j]
                copy[j] = tmp
            }
            return copy
        }
    }
}

@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.puzzle

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.GeometryHelpers
import kotlin.math.sin

/**
 * Pure state & coordinator for a single iOS-matched puzzle gameplay session:
 * mid-puzzle restoration, snap validation, wrong-drop counting, 3-star rating, and bubble pops.
 */
class GameplayViewModel(
    initialLevel: LevelDefinition = PuzzleLevels.ALL.first(),
    initialPlacedPieceIds: Set<String> = emptySet()
) {
    val physics = PhysicsPuzzleEngine()
    val effects = SurpriseBubbleSystem()

    var currentLevel: LevelDefinition = initialLevel
        private set

    val placedPieceIds: MutableSet<String> = initialPlacedPieceIds.toMutableSet()

    var wrongDropsCount: Int = 0
        private set

    var poppedBubblesCount: Int = 0
        private set

    var isCompleted: Boolean = false
        private set

    var earnedStars: Int = GameConfig.MAX_STARS
        private set

    var inactivityElapsedSec: Float = 0f
        private set

    var hintPieceId: String? = null
        private set

    private var hasViewportInitialized: Boolean = false

    val totalPieces: Int
        get() = currentLevel.pieces.size

    val placedCount: Int
        get() = placedPieceIds.size

    val nextHintCandidateId: String?
        get() = currentLevel.pieces.firstOrNull { it.id !in placedPieceIds }?.id

    init {
        if (placedPieceIds.size >= currentLevel.pieces.size && currentLevel.pieces.isNotEmpty()) {
            isCompleted = true
        }
    }

    fun loadLevel(level: LevelDefinition, restoredPlacedIds: Set<String> = emptySet()) {
        currentLevel = level
        placedPieceIds.clear()
        val validIds = level.pieces.map { it.id }.toSet()
        placedPieceIds.addAll(restoredPlacedIds.filter { it in validIds })
        wrongDropsCount = 0
        poppedBubblesCount = 0
        isCompleted = placedPieceIds.size >= level.pieces.size && level.pieces.isNotEmpty()
        earnedStars = GameConfig.MAX_STARS
        inactivityElapsedSec = 0f
        hintPieceId = null
        hasViewportInitialized = false
        effects.resetAll()
    }

    fun ensureViewportLayout(widthPx: Float, heightPx: Float, density: Float) {
        val changed = !hasViewportInitialized ||
            kotlin.math.abs(physics.geometry.viewportWidthPx - widthPx) > 2f ||
            kotlin.math.abs(physics.geometry.viewportHeightPx - heightPx) > 2f
        if (!changed) return
        hasViewportInitialized = true
        physics.initialize(
            level = currentLevel,
            viewportWidthPx = widthPx,
            viewportHeightPx = heightPx,
            density = density,
            placedPieceIds = placedPieceIds
        )
        if (isCompleted) {
            effects.startVictoryCelebration(widthPx, heightPx, density)
        }
    }

    fun resetLevelForReplay(viewportWidthPx: Float, viewportHeightPx: Float, density: Float) {
        loadLevel(currentLevel, emptySet())
        ensureViewportLayout(viewportWidthPx, viewportHeightPx, density)
    }

    fun shouldSnapPiece(piece: PiecePhysicsBody, isDragging: Boolean): Boolean {
        if (piece.placed || !hasViewportInitialized) return false
        val threshold = if (isDragging) {
            physics.geometry.magneticSnapRadiusPx
        } else {
            physics.geometry.releaseSnapRadiusPx
        }
        return GeometryHelpers.distance(piece.x, piece.y, piece.targetX, piece.targetY) <= threshold
    }

    fun markPiecePlaced(piece: PiecePhysicsBody): Boolean {
        val wasNew = placedPieceIds.add(piece.definition.id)
        inactivityElapsedSec = 0f
        hintPieceId = null
        if (hasViewportInitialized) {
            effects.spawnSnapBurst(piece.targetX, piece.targetY, physics.geometry.density)
        }
        val finishedNow = placedPieceIds.size >= currentLevel.pieces.size && !isCompleted
        if (finishedNow) {
            completeLevelNow()
        }
        return finishedNow || (wasNew && isCompleted)
    }

    fun handleWrongPlacementDrop() {
        wrongDropsCount += 1
        inactivityElapsedSec = 0f
    }

    fun registerInteractionActivity() {
        inactivityElapsedSec = 0f
        hintPieceId = null
    }

    fun recordSurpriseBubblePop(px: Float, py: Float): SurpriseBubble? {
        val popped = effects.tryPopBubbleAt(px, py)
        if (popped != null) {
            poppedBubblesCount += 1
        }
        return popped
    }

    fun recordDirectBubblePopCount() {
        poppedBubblesCount += 1
    }

    fun stepSimulation(dtSec: Float) {
        val dt = dtSec.coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
        if (!isCompleted && physics.activeDragPieceId == null) {
            inactivityElapsedSec += dt
            if (inactivityElapsedSec >= GameConfig.HINT_INACTIVITY_DELAY_SEC) {
                hintPieceId = nextHintCandidateId
            }
        } else {
            hintPieceId = null
        }

        val hintPulse = if (hintPieceId != null) {
            0.5f + 0.5f * sin(inactivityElapsedSec * 5.0f)
        } else {
            0f
        }
        physics.step(dt, isCompleted, hintPieceId, hintPulse)
        effects.step(dt)
    }

    fun calculateStarsForWrongDrops(wrongDrops: Int): Int = when {
        wrongDrops <= 2 -> 3
        wrongDrops <= 6 -> 2
        else -> 1
    }

    private fun completeLevelNow() {
        isCompleted = true
        earnedStars = calculateStarsForWrongDrops(wrongDropsCount)
        if (hasViewportInitialized) {
            effects.startVictoryCelebration(
                viewportWidthPx = physics.geometry.viewportWidthPx,
                viewportHeightPx = physics.geometry.viewportHeightPx,
                density = physics.geometry.density
            )
        }
    }
}

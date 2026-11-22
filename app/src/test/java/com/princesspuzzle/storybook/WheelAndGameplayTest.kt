package com.princesspuzzle.storybook

import com.princesspuzzle.storybook.core.GeometryHelpers
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.puzzle.DragEndOutcome
import com.princesspuzzle.storybook.puzzle.DragMoveOutcome
import com.princesspuzzle.storybook.puzzle.GameplayViewModel
import com.princesspuzzle.storybook.puzzle.PhysicsPuzzleEngine
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.puzzle.WheelViewModel
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.Test

class WheelAndGameplayTest {
    @Test
    fun `wheel rotates clockwise and counter-clockwise wrapping across all 16 items`() {
        val wheel = WheelViewModel(PuzzleLevels.ALL, initialWheelIndex = 0)
        assertEquals(0, wheel.activeLevelIndex)
        assertEquals(1, wheel.activeLevel.id)

        // Clockwise wraps 0 -> 15
        assertTrue(wheel.rotateClockwise())
        assertEquals(15, wheel.activeLevelIndex)
        assertEquals(16, wheel.activeLevel.id)

        // Counter-clockwise wraps 15 -> 0
        assertTrue(wheel.rotateCounterClockwise())
        assertEquals(0, wheel.activeLevelIndex)
        assertEquals(1, wheel.activeLevel.id)

        // Angular drag delta and snap align with activeIndexOnWheel
        wheel.beginDrag()
        wheel.applyDragDelta(-wheel.stepAngleRad * 3.1f)
        val snapped = wheel.snapToNearestLevel()
        assertEquals(3, snapped)
        assertEquals(4, wheel.activeLevel.id)
        assertEquals(wheel.rotationRad, WheelViewModel.rotationForIndex(3, 16), 0.001f)
        assertEquals(3, GeometryHelpers.activeIndexOnWheel(wheel.rotationRad.toDouble(), 16))
    }

    @Test
    fun `wheel tracks unlock rules completed slugs and summary stars`() {
        val wheel = WheelViewModel()
        val lockedState = AppProgressState(
            starsByLevel = mapOf(1 to 3),
            completedLevelSlugs = setOf("royal_pony"),
            unlockedAll = false
        )
        assertTrue(wheel.isLevelUnlocked(PuzzleLevels.getById(1), lockedState))
        assertFalse(wheel.isLevelUnlocked(PuzzleLevels.getById(6), lockedState))
        assertTrue(wheel.isLevelUnlocked(PuzzleLevels.getById(6), lockedState, debugUnlockAll = true))

        assertTrue(wheel.isLevelCompleted(PuzzleLevels.getById(1), lockedState))
        assertTrue(wheel.isLevelCompleted(PuzzleLevels.getById(3), lockedState))
        assertFalse(wheel.isLevelCompleted(PuzzleLevels.getById(2), lockedState))
        assertEquals(2, wheel.completionSummaryCount(lockedState))
    }

    @Test
    fun `gameplay viewmodel restores partial progress and completes with 24 bubbles`() {
        val level = PuzzleLevels.getById(2) // 6 pieces
        val vm = GameplayViewModel(
            initialLevel = level,
            initialPlacedPieceIds = setOf(level.pieces[0].id, level.pieces[1].id)
        )
        vm.ensureViewportLayout(widthPx = 1080f, heightPx = 2400f, density = 2.625f)

        assertEquals(2, vm.placedCount)
        assertFalse(vm.isCompleted)
        val unplacedBodies = vm.physics.bodies.filter { !it.placed }
        assertEquals(4, unplacedBodies.size)

        // Drag one piece near its target silhouette and snap magically
        val nextPiece = unplacedBodies.first()
        assertNotNull(vm.physics.beginDrag(nextPiece.definition.id))
        val snapMove = vm.physics.moveDrag(
            deltaX = nextPiece.targetX - nextPiece.x,
            deltaY = nextPiece.targetY - nextPiece.y
        )
        assertTrue(snapMove is DragMoveOutcome.Snapped)
        val finishedFromThird = vm.markPiecePlaced(nextPiece)
        assertFalse(finishedFromThird)
        assertEquals(3, vm.placedCount)

        // Simulate one wrong placement drop and snap remaining pieces
        vm.handleWrongPlacementDrop()
        assertEquals(1, vm.wrongDropsCount)

        vm.physics.bodies.filter { !it.placed }.forEach { remaining ->
            val snapped = vm.physics.snapBodyById(remaining.definition.id)!!
            vm.markPiecePlaced(snapped)
        }
        assertTrue(vm.isCompleted)
        assertEquals(3, vm.earnedStars)
        assertTrue(vm.effects.celebrationActive)
        assertTrue(vm.effects.bubbles.any { it.active })

        // Step simulation to spawn & float surprise bubbles, then pop one
        repeat(60) {
            vm.stepSimulation(0.05f)
        }
        val activeBubble = vm.effects.bubbles.first { it.active }
        val popped = vm.recordSurpriseBubblePop(activeBubble.x, activeBubble.y)
        assertNotNull(popped)
        assertEquals(1, vm.poppedBubblesCount)
    }

    @Test
    fun `physics engine enforces gravity bounds and release-snap fallback`() {
        val level = PuzzleLevels.getById(1)
        val engine = PhysicsPuzzleEngine()
        engine.initialize(level, 1080f, 2200f, 2.5f)
        assertEquals(level.pieceCount, engine.bodies.size)

        // Simulate 2 seconds of free-fall gravity and verify pieces stay inside viewport
        repeat(120) {
            engine.step(1f / 60f, victoryActive = false, hintTargetId = null, hintPhase = 0f)
        }
        engine.bodies.forEach { body ->
            assertTrue(body.x in 0f..1080f, "Piece ${body.definition.id} X out of bounds: ${body.x}")
            assertTrue(body.y in 0f..2200f, "Piece ${body.definition.id} Y out of bounds: ${body.y}")
        }

        // Release near edge returns piece; release inside tolerance snaps
        val testBody = engine.bodies.first()
        engine.beginDrag(testBody.definition.id)
        testBody.x = testBody.targetX + engine.geometry.releaseSnapRadiusPx * 0.5f
        testBody.y = testBody.targetY
        val endOutcome = engine.endDrag()
        assertTrue(endOutcome is DragEndOutcome.Snapped)
        assertTrue(abs(testBody.x - testBody.targetX) < 0.01f)
    }
}

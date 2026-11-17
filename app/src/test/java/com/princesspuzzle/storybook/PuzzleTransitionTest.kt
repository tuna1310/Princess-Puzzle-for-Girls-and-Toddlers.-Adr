package com.princesspuzzle.storybook

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.data.InMemoryProgressRepository
import com.princesspuzzle.storybook.puzzle.PieceStatus
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.puzzle.PuzzleViewModel
import com.princesspuzzle.storybook.ui.NextLevelDestination
import com.princesspuzzle.storybook.ui.isRouteLevelAccessible
import com.princesspuzzle.storybook.ui.resolveNextLevelDestination
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PuzzleTransitionTest {
    @Test
    fun `completing levels 1 to 4 transitions cleanly via Next and resets ViewModel and caches`() = runTest {
        val repo = InMemoryProgressRepository(AppProgressState())

        for (levelId in 1..4) {
            val expectedLevel = PuzzleLevels.getById(levelId)
            val completions = mutableListOf<Pair<Int, Int>>()
            val vm = PuzzleViewModel(
                initialLevelId = levelId,
                onLevelCompleted = { id, stars -> completions.add(id to stars) },
                enableRenderCache = false
            )

            assertEquals(levelId, vm.uiState.value.level.id)
            assertEquals(expectedLevel.pieceCount, vm.uiState.value.totalCount)
            assertEquals(0, vm.uiState.value.placedCount)
            assertFalse(vm.uiState.value.completed)
            assertFalse(vm.uiState.value.showCelebrationActions)
            assertEquals(0f, vm.winTimerSec)
            assertEquals(1f, vm.seamAlpha)

            val layout = vm.ensureViewportLayout(720f, 1612f, 2.0f, null)
            val engine = assertNotNull(vm.physicsEngine)
            assertEquals(expectedLevel.pieceCount, engine.runtimes.size)
            assertTrue(engine.runtimes.all { it.status == PieceStatus.TRAY })

            // Snap every piece in the tray into its target slot using touch drag (accounting for lift offset)
            for (i in engine.runtimes.indices) {
                val piece = engine.runtimes[i]
                val fingerStartX = piece.x
                val fingerStartY = piece.y
                engine.startDrag(i, 100L + i, fingerStartX, fingerStartY, layout, isTouch = true, nowMillis = 100L)
                val targetFingerY = fingerStartY + (piece.snapTargetY - piece.y)
                engine.moveDrag(100L + i, piece.snapTargetX, targetFingerY, layout, nowMillis = 120L)
                assertTrue(engine.endDrag(100L + i, layout, nowMillis = 140L))
            }

            // Step frames past snap settling and celebration action delay
            val totalSteps = ((GameConfig.CELEBRATION_PANEL_DELAY_SEC + 0.5f) / 0.02f).toInt()
            repeat(totalSteps) {
                vm.onFrame(0.02f)
            }

            assertTrue(vm.uiState.value.completed)
            assertTrue(vm.uiState.value.showCelebrationActions)
            assertEquals(expectedLevel.pieceCount, vm.uiState.value.placedCount)
            assertTrue(vm.winTimerSec >= GameConfig.CELEBRATION_PANEL_DELAY_SEC)
            assertTrue(vm.seamAlpha < 1f)
            assertEquals(listOf(levelId to GameConfig.MAX_STARS), completions)

            repo.recordLevelCompletion(levelId, GameConfig.MAX_STARS)
            assertEquals(GameConfig.MAX_STARS, repo.progressFlow.value.starsFor(levelId))

            val nextDest = resolveNextLevelDestination(
                currentLevelId = levelId,
                progressState = repo.progressFlow.value,
                debugUnlockAll = false
            )
            if (levelId < 4) {
                assertEquals(NextLevelDestination.OpenPuzzle(levelId + 1), nextDest)
            } else {
                // Level 5 is locked unless unlockedAll or debug bypass is active
                assertEquals(NextLevelDestination.BlockedLocked(5), nextDest)
            }

            // Verify transitioning in-place resets all runtime state, timers, particles, and revision
            val prevRev = vm.boardRevision
            vm.advanceToNextLevel()
            val expectedNextId = levelId + 1
            assertEquals(expectedNextId, vm.uiState.value.level.id)
            assertEquals(0, vm.uiState.value.placedCount)
            assertFalse(vm.uiState.value.completed)
            assertFalse(vm.uiState.value.showCelebrationActions)
            assertEquals(0f, vm.winTimerSec)
            assertEquals(0f, vm.elapsedTimeSec)
            assertEquals(1f, vm.seamAlpha)
            assertFalse(vm.particles.hasActiveParticles())
            assertTrue(vm.geometryCache.isEmpty())
            assertNotEquals(prevRev, vm.boardRevision)
        }
    }

    @Test
    fun `completing final level 16 returns to level select cleanly`() {
        val unlockedState = AppProgressState(unlockedAll = true)
        val dest = resolveNextLevelDestination(
            currentLevelId = 16,
            progressState = unlockedState,
            debugUnlockAll = false
        )
        assertEquals(NextLevelDestination.ReturnToLevelSelect, dest)
    }

    @Test
    fun `replaying a completed level starts fresh while retaining saved stars`() = runTest {
        val repo = InMemoryProgressRepository(AppProgressState())
        repo.recordLevelCompletion(2, 3)

        val vm = PuzzleViewModel(initialLevelId = 2, enableRenderCache = false)
        val layout = vm.ensureViewportLayout(1080f, 2160f, 2.5f, null)
        val engine = assertNotNull(vm.physicsEngine)
        for (i in engine.runtimes.indices) {
            val piece = engine.runtimes[i]
            engine.startDrag(i, 1L + i, piece.x, piece.y, layout, isTouch = false, nowMillis = 10L)
            engine.moveDrag(1L + i, piece.snapTargetX, piece.snapTargetY, layout, nowMillis = 20L)
            engine.endDrag(1L + i, layout, nowMillis = 30L)
        }
        repeat(40) { vm.onFrame(0.02f) }
        assertTrue(vm.uiState.value.completed)

        vm.replay()
        assertEquals(2, vm.uiState.value.level.id)
        assertFalse(vm.uiState.value.completed)
        assertEquals(0, vm.uiState.value.placedCount)
        assertFalse(vm.particles.hasActiveParticles())
        assertEquals(3, repo.progressFlow.value.starsFor(2))
    }

    @Test
    fun `locked levels and out-of-range ids are safely blocked and bounce back to level select`() {
        val lockedState = AppProgressState(unlockedAll = false)
        assertTrue(isRouteLevelAccessible(1, lockedState, debugUnlockAll = false))
        assertTrue(isRouteLevelAccessible(4, lockedState, debugUnlockAll = false))
        assertFalse(isRouteLevelAccessible(5, lockedState, debugUnlockAll = false))
        assertFalse(isRouteLevelAccessible(16, lockedState, debugUnlockAll = false))
        assertFalse(isRouteLevelAccessible(99, lockedState, debugUnlockAll = false))
        assertFalse(isRouteLevelAccessible(0, lockedState, debugUnlockAll = false))

        assertEquals(
            NextLevelDestination.BlockedLocked(5),
            resolveNextLevelDestination(4, lockedState, debugUnlockAll = false)
        )

        val unlockedState = AppProgressState(unlockedAll = true)
        assertTrue(isRouteLevelAccessible(5, unlockedState, debugUnlockAll = false))
        assertTrue(isRouteLevelAccessible(16, unlockedState, debugUnlockAll = false))
        assertEquals(
            NextLevelDestination.OpenPuzzle(5),
            resolveNextLevelDestination(4, unlockedState, debugUnlockAll = false)
        )
    }
}

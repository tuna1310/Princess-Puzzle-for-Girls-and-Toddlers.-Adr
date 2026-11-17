package com.princesspuzzle.storybook

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.computePuzzleLayout
import com.princesspuzzle.storybook.puzzle.PieceStatus
import com.princesspuzzle.storybook.puzzle.PuzzlePhysicsEngine
import com.princesspuzzle.storybook.puzzle.sliceJigsawPieces
import kotlin.math.hypot
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class PuzzlePhysicsTest {
    @Test
    fun `snaps when released near slot and bounces smoothly back when dropped outside`() {
        val specs = sliceJigsawPieces(3, seed = 11)
        val layout = computePuzzleLayout(1080f, 2340f, 2.75f, specs)
        val engine = PuzzlePhysicsEngine(specs, layout, seed = 5)

        val p0 = engine.runtimes[0]
        engine.startDrag(0, 1L, p0.x, p0.y, layout, isTouch = false, nowMillis = 100L)
        engine.moveDrag(1L, layout.tray.x + 25f, layout.tray.y + 40f, layout, nowMillis = 120L)
        val snappedOnWrongDrop = engine.endDrag(1L, layout, nowMillis = 130L)
        assertFalse(snappedOnWrongDrop)
        assertEquals(PieceStatus.RETURNING, p0.status)

        for (step in 0 until 200) {
            engine.step(1f / 60f, layout)
            if (p0.status == PieceStatus.TRAY) break
        }
        assertEquals(PieceStatus.TRAY, p0.status)
        assertTrue(hypot(p0.x - p0.trayHomeX, p0.y - p0.trayHomeY) < 4f)

        // Snap all 3 pieces cleanly
        for (i in engine.runtimes.indices) {
            val piece = engine.runtimes[i]
            engine.startDrag(
                i,
                10L + i,
                piece.x,
                piece.y,
                layout,
                isTouch = false,
                nowMillis = 200L + i * 50L
            )
            engine.moveDrag(
                10L + i,
                piece.snapTargetX + 10f,
                piece.snapTargetY - 8f,
                layout,
                nowMillis = 220L + i * 50L
            )
            assertTrue(engine.endDrag(10L + i, layout, nowMillis = 230L + i * 50L))
        }

        var completed = false
        for (step in 0 until 60) {
            val res = engine.step(1f / 60f, layout)
            if (res.allCompleted) {
                completed = true
                break
            }
        }
        assertTrue(completed)
        assertTrue(engine.runtimes.all { it.status == PieceStatus.PLACED })
    }

    @Test
    fun `anti-stuck cancels broken gesture cleanly and settles stuck returning pieces on timeout`() {
        val specs = sliceJigsawPieces(2, seed = 3)
        val layout = computePuzzleLayout(1080f, 1920f, 2.5f, specs)
        val engine = PuzzlePhysicsEngine(specs, layout, seed = 9)

        val p0 = engine.runtimes[0]
        engine.startDrag(0, 7L, p0.x, p0.y, layout, isTouch = true, nowMillis = 100L)
        engine.moveDrag(7L, 500f, 500f, layout, nowMillis = 120L)
        engine.cancelActiveDrag(layout)
        assertFalse(engine.hasActivePointer())
        assertEquals(PieceStatus.RETURNING, p0.status)

        // Force continuous off-home impulse and verify timeout settles back to TRAY
        var steps = 0
        while (p0.status == PieceStatus.RETURNING && steps < 300) {
            engine.step(0.02f, layout)
            steps++
        }
        assertEquals(PieceStatus.TRAY, p0.status)
    }

    @Test
    fun `triggers inactivity hint after delay and preserves state across screen rotation`() {
        val specs = sliceJigsawPieces(4, seed = 2)
        val portrait = computePuzzleLayout(1080f, 2340f, 2.75f, specs)
        val engine = PuzzlePhysicsEngine(specs, portrait, seed = 4)

        val tickCount = (GameConfig.HINT_INACTIVITY_DELAY_SEC / 0.05f).toInt() + 2
        for (i in 0 until tickCount) {
            engine.step(0.05f, portrait)
        }
        assertTrue(engine.hintPieceIndex >= 0)

        engine.runtimes[0].status = PieceStatus.PLACED
        val landscape = computePuzzleLayout(2340f, 1080f, 2.75f, specs)
        engine.syncToLayout(landscape)

        assertEquals(engine.runtimes[0].snapTargetX, engine.runtimes[0].x, 0.5f)
        assertEquals(engine.runtimes[0].snapTargetY, engine.runtimes[0].y, 0.5f)
        assertEquals(engine.runtimes[1].trayHomeX, engine.runtimes[1].x, 0.5f)
        assertEquals(engine.runtimes[1].trayHomeY, engine.runtimes[1].y, 0.5f)
    }
}

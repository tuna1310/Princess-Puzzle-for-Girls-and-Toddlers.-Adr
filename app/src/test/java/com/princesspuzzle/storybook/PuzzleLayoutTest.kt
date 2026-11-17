package com.princesspuzzle.storybook

import com.princesspuzzle.storybook.core.computePuzzleLayout
import com.princesspuzzle.storybook.puzzle.sliceJigsawPieces
import kotlin.math.hypot
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class PuzzleLayoutTest {
    @Test
    fun `computes non-overlapping tray slots across portrait, phone landscape, and tablet`() {
        val specs = sliceJigsawPieces(12, seed = 7)
        val viewports =
            listOf(
                Triple(1080f, 2340f, 2.75f),
                Triple(2340f, 1080f, 2.75f),
                Triple(2048f, 1536f, 2.0f)
            )

        for ((w, h, density) in viewports) {
            val layout = computePuzzleLayout(w, h, density, specs)
            assertEquals(12, layout.traySlots.size)
            assertTrue(layout.board.w > 140f * density)

            for (i in layout.traySlots.indices) {
                for (j in i + 1 until layout.traySlots.size) {
                    val a = layout.traySlots[i]
                    val b = layout.traySlots[j]
                    val dist = hypot(a.x - b.x, a.y - b.y)
                    assertTrue(dist > 24f * density, "Overlapping tray slots in ${w}x$h")
                }
            }
        }
    }
}

package com.princesspuzzle.storybook

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.data.InMemoryPurchaseService
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.puzzle.SceneThemeId
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LevelsAndPurchaseTest {
    @Test
    fun `catalog is a single source of truth for 16 progressive levels`() {
        assertEquals(GameConfig.TOTAL_LEVEL_COUNT, PuzzleLevels.TOTAL_COUNT)
        assertEquals(GameConfig.TOTAL_LEVEL_COUNT * GameConfig.MAX_STARS, PuzzleLevels.MAX_TOTAL_STARS)
        val freeLevels = PuzzleLevels.ALL.filter { it.free }
        assertEquals(GameConfig.FREE_LEVEL_COUNT, freeLevels.size)
        assertEquals(setOf(1, 2, 3, 4), PuzzleLevels.DEFAULT_FREE_IDS)

        assertEquals(SceneThemeId.entries.size, PuzzleLevels.ALL.map { it.theme }.toSet().size)
        assertEquals((1..16).toList(), PuzzleLevels.ALL.map { it.id })
        assertEquals(16, PuzzleLevels.ALL.map { it.titleRes }.toSet().size)

        for (i in 1 until PuzzleLevels.ALL.size) {
            assertTrue(PuzzleLevels.ALL[i].pieceCount >= PuzzleLevels.ALL[i - 1].pieceCount)
        }
        assertEquals(2, PuzzleLevels.ALL.first().pieceCount)
        assertEquals(12, PuzzleLevels.ALL.last().pieceCount)
        assertEquals(2, PuzzleLevels.nextAfter(1)?.id)
        assertNull(PuzzleLevels.nextAfter(16))
    }

    @Test
    fun `purchase service and extra unlock ids control locked level access`() = runTest {
        val service = InMemoryPurchaseService(initialUnlockedAll = false)
        assertFalse(service.isUnlockedAll())
        assertTrue(PuzzleLevels.isUnlocked(PuzzleLevels.getById(1), service.isUnlockedAll()))
        assertFalse(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), service.isUnlockedAll()))
        assertTrue(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), false, extraUnlockedIds = setOf(5)))

        assertTrue(service.purchaseUnlockAll())
        assertTrue(service.restorePurchases())
        assertTrue(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), service.isUnlockedAll()))

        service.resetSimulatedPurchase()
        assertFalse(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), service.isUnlockedAll()))
    }
}

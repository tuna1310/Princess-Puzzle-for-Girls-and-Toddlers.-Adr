package com.princesspuzzle.storybook

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.data.InMemoryPurchaseService
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LevelsAndPurchaseTest {
    @Test
    fun `catalog contains 16 progressive levels with first 4 free`() {
        assertEquals(GameConfig.TOTAL_LEVEL_COUNT, PuzzleLevels.ALL.size)
        val freeLevels = PuzzleLevels.ALL.filter { it.free }
        assertEquals(GameConfig.FREE_LEVEL_COUNT, freeLevels.size)

        for (i in 1 until PuzzleLevels.ALL.size) {
            assertTrue(PuzzleLevels.ALL[i].pieceCount >= PuzzleLevels.ALL[i - 1].pieceCount)
        }
        assertEquals(2, PuzzleLevels.ALL.first().pieceCount)
        assertEquals(12, PuzzleLevels.ALL.last().pieceCount)
    }

    @Test
    fun `purchase service controls locked level access`() = runTest {
        val service = InMemoryPurchaseService(initialUnlockedAll = false)
        assertFalse(service.isUnlockedAll())
        assertTrue(PuzzleLevels.isUnlocked(PuzzleLevels.getById(1), service.isUnlockedAll()))
        assertFalse(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), service.isUnlockedAll()))

        assertTrue(service.purchaseUnlockAll())
        assertTrue(service.restorePurchases())
        assertTrue(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), service.isUnlockedAll()))

        service.resetSimulatedPurchase()
        assertFalse(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), service.isUnlockedAll()))
    }
}

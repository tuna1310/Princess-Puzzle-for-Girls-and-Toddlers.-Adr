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
    fun `catalog defines all 16 iOS-matched levels slugs parts and themes`() {
        assertEquals(GameConfig.TOTAL_LEVEL_COUNT, PuzzleLevels.TOTAL_COUNT)
        assertEquals(16, PuzzleLevels.ALL.size)
        assertEquals(GameConfig.TOTAL_LEVEL_COUNT * GameConfig.MAX_STARS, PuzzleLevels.MAX_TOTAL_STARS)

        val freeLevels = PuzzleLevels.ALL.filter { it.free }
        assertEquals(GameConfig.FREE_LEVEL_COUNT, freeLevels.size)
        assertEquals(setOf(1, 2, 3, 4), PuzzleLevels.DEFAULT_FREE_IDS)

        assertEquals(SceneThemeId.entries.size, PuzzleLevels.ALL.map { it.theme }.toSet().size)
        assertEquals((1..16).toList(), PuzzleLevels.ALL.map { it.id })
        assertEquals(16, PuzzleLevels.ALL.map { it.slugId }.toSet().size)
        assertEquals(16, PuzzleLevels.ALL.map { it.titleRes }.toSet().size)

        PuzzleLevels.ALL.forEach { level ->
            assertTrue(level.pieceCount in 5..9, "Level ${level.id} expected 5..9 pieces")
            assertEquals(level.pieceCount, level.pieces.map { it.id }.toSet().size)
            level.pieces.forEach { part ->
                assertTrue(part.normalizedCenterX in 0.05f..0.95f)
                assertTrue(part.normalizedCenterY in 0.05f..0.95f)
                assertTrue(part.normalizedWidth > 0.1f && part.normalizedHeight > 0.1f)
            }
        }

        assertEquals(2, PuzzleLevels.nextAfter(1)?.id)
        assertNull(PuzzleLevels.nextAfter(16))
        assertEquals(1, PuzzleLevels.nextCircularAfter(15).id)
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

    @Test
    fun `debug unlock override opens all 16 levels without changing default release rule`() {
        for (level in PuzzleLevels.ALL) {
            assertTrue(PuzzleLevels.isUnlocked(level, unlockedAll = false, debugUnlockAll = true))
        }
        assertFalse(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), unlockedAll = false))
    }
}

package com.princesspuzzle.storybook

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.data.DataStoreProgressRepository
import com.princesspuzzle.storybook.data.DataStorePurchaseService
import com.princesspuzzle.storybook.data.ProgressSchemaMigration
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.ui.NextLevelDestination
import com.princesspuzzle.storybook.ui.components.computeParentalHoldProgress
import com.princesspuzzle.storybook.ui.components.generateParentalMathChallenge
import com.princesspuzzle.storybook.ui.components.isParentalHoldSatisfied
import com.princesspuzzle.storybook.ui.components.isParentalMathAnswerValid
import com.princesspuzzle.storybook.ui.resolveNextLevelDestination
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ParentalGateAndSettingsTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `parental gate requires full 3s hold or exact multiplication answer`() {
        assertFalse(isParentalHoldSatisfied(0f))
        assertFalse(isParentalHoldSatisfied(GameConfig.PARENTAL_GATE_HOLD_SECONDS - 0.1f))
        assertTrue(computeParentalHoldProgress(GameConfig.PARENTAL_GATE_HOLD_SECONDS / 2f) in 0.49f..0.51f)
        assertTrue(isParentalHoldSatisfied(GameConfig.PARENTAL_GATE_HOLD_SECONDS))
        assertEquals(1f, computeParentalHoldProgress(GameConfig.PARENTAL_GATE_HOLD_SECONDS + 1f))

        for (seed in 0L..25L) {
            val challenge = generateParentalMathChallenge(seed)
            assertTrue(challenge.left in 3..9)
            assertTrue(challenge.right in 4..9)
            assertEquals(challenge.left * challenge.right, challenge.expectedAnswer)
            assertTrue(isParentalMathAnswerValid(challenge, challenge.expectedAnswer.toString()))
            assertFalse(isParentalMathAnswerValid(challenge, ""))
            assertFalse(isParentalMathAnswerValid(challenge, "abc"))
            assertFalse(isParentalMathAnswerValid(challenge, (challenge.expectedAnswer + 1).toString()))
        }
    }

    @Test
    fun `unlockAll unlocks all 16 levels in DataStore and survives cold reopen and restore`() = runTest {
        val storeFile = File(tempFolder.root, "step5_unlock.preferences_pb")

        val scope1 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val ds1 = PreferenceDataStoreFactory.create(
            migrations = listOf(ProgressSchemaMigration),
            scope = scope1
        ) { storeFile }
        val repo1 = DataStoreProgressRepository(ds1, scope1)
        val purchase1 = DataStorePurchaseService(repo1)

        val initial = repo1.awaitLoaded()
        assertFalse(initial.unlockedAll)
        assertFalse(purchase1.restorePurchases())
        assertTrue((1..4).all { PuzzleLevels.isUnlocked(PuzzleLevels.getById(it), initial.unlockedAll) })
        assertTrue((5..16).none { PuzzleLevels.isUnlocked(PuzzleLevels.getById(it), initial.unlockedAll) })

        assertTrue(purchase1.purchaseUnlockAll())
        val unlockedState = repo1.awaitLoaded()
        assertTrue(unlockedState.unlockedAll)
        assertTrue(PuzzleLevels.ALL.all { PuzzleLevels.isUnlocked(it, unlockedState.unlockedAll) })
        assertEquals(
            NextLevelDestination.OpenPuzzle(5),
            resolveNextLevelDestination(4, unlockedState, debugUnlockAll = false)
        )
        scope1.coroutineContext.job.cancelAndJoin()

        val scope2 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val ds2 = PreferenceDataStoreFactory.create(
            migrations = listOf(ProgressSchemaMigration),
            scope = scope2
        ) { storeFile }
        val repo2 = DataStoreProgressRepository(ds2, scope2)
        val purchase2 = DataStorePurchaseService(repo2)

        val reloaded = repo2.awaitLoaded()
        assertTrue(reloaded.unlockedAll)
        assertTrue(purchase2.restorePurchases())
        assertTrue(PuzzleLevels.ALL.all { PuzzleLevels.isUnlocked(it, reloaded.unlockedAll) })

        purchase2.resetSimulatedPurchase()
        val relocked = repo2.awaitLoaded()
        assertFalse(relocked.unlockedAll)
        assertFalse(PuzzleLevels.isUnlocked(PuzzleLevels.getById(5), relocked.unlockedAll))
        scope2.coroutineContext.job.cancelAndJoin()
    }
}

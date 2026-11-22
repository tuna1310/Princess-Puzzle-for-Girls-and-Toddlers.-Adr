package com.princesspuzzle.storybook

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.princesspuzzle.storybook.data.DataStoreProgressRepository
import com.princesspuzzle.storybook.data.DataStorePurchaseService
import com.princesspuzzle.storybook.data.ProgressSchemaMigration
import com.princesspuzzle.storybook.puzzle.ParentalChallenge
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.puzzle.WheelViewModel
import java.io.File
import kotlin.random.Random
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
    fun `parental challenge matches iOS operand bounds and 4 distinct options`() {
        for (seed in 0..40) {
            val challenge = ParentalChallenge.generate(Random(seed))
            assertTrue(challenge.leftOperand in 12..28)
            assertTrue(challenge.rightOperand in 11..24)
            assertEquals(4, challenge.options.size)
            assertEquals(4, challenge.options.toSet().size)
            assertEquals(challenge.leftOperand + challenge.rightOperand, challenge.correctAnswer)
            assertTrue(challenge.correctAnswer in challenge.options)
            assertTrue(challenge.isCorrect(challenge.correctAnswer))
            val wrong = challenge.options.first { it != challenge.correctAnswer }
            assertFalse(challenge.isCorrect(wrong))
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
        val wheelVm = WheelViewModel()
        assertTrue(wheelVm.isLevelUnlocked(PuzzleLevels.getById(5), unlockedState, debugUnlockAll = false))
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

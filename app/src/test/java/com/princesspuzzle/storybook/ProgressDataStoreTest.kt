package com.princesspuzzle.storybook

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.data.DataStoreProgressRepository
import com.princesspuzzle.storybook.data.DataStorePurchaseService
import com.princesspuzzle.storybook.data.ProgressKeys
import com.princesspuzzle.storybook.data.ProgressSchemaMigration
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
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

class ProgressDataStoreTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `progress slugs wheel index mid-puzzle pieces and settings persist across cold restart`() = runTest {
        val storeFile = File(tempFolder.root, "progress_v2_persist.preferences_pb")

        val scope1 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val ds1 = PreferenceDataStoreFactory.create(
            migrations = listOf(ProgressSchemaMigration),
            scope = scope1
        ) { storeFile }
        val repo1 = DataStoreProgressRepository(ds1, scope1)
        val initial = repo1.awaitLoaded()
        assertEquals(ProgressKeys.CURRENT_SCHEMA_VERSION, initial.schemaVersion)
        assertEquals(PuzzleLevels.DEFAULT_FREE_IDS, initial.unlockedLevelIds)
        assertEquals(0, initial.totalStars)
        assertEquals(0, initial.lastSelectedWheelIndex)

        repo1.recordLevelCompletion(levelId = 2, stars = 3)
        repo1.recordLevelCompletion(levelId = 2, stars = 1)
        repo1.saveInProgressPieces(levelId = 3, placedPieceIds = setOf("royal_pony_p0", "royal_pony_p2"))
        repo1.setLastSelectedWheelIndex(7)
        repo1.setUnlockedAll(true)
        repo1.setSoundEnabled(false)
        repo1.setMusicEnabled(false)
        val saved = repo1.awaitLoaded()
        assertEquals(3, saved.starsFor(2))
        assertTrue(saved.isLevelCompleted(PuzzleLevels.getById(2)))
        assertEquals(setOf("royal_pony_p0", "royal_pony_p2"), saved.inProgressPiecesFor(3))
        assertEquals(7, saved.lastSelectedWheelIndex)
        scope1.coroutineContext.job.cancelAndJoin()

        val scope2 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val ds2 = PreferenceDataStoreFactory.create(
            migrations = listOf(ProgressSchemaMigration),
            scope = scope2
        ) { storeFile }
        val repo2 = DataStoreProgressRepository(ds2, scope2)
        val reloaded = repo2.awaitLoaded()
        assertEquals(ProgressKeys.CURRENT_SCHEMA_VERSION, reloaded.schemaVersion)
        assertEquals(3, reloaded.starsFor(2))
        assertTrue("princess_brunette" in reloaded.completedLevelSlugs)
        assertEquals(setOf("royal_pony_p0", "royal_pony_p2"), reloaded.inProgressPiecesFor(3))
        assertEquals(7, reloaded.lastSelectedWheelIndex)
        assertTrue(reloaded.unlockedAll)
        assertFalse(reloaded.soundEnabled)
        assertFalse(reloaded.musicEnabled)

        repo2.recordLevelCompletion(levelId = 3, stars = 3)
        val afterWin3 = repo2.awaitLoaded()
        assertTrue(afterWin3.inProgressPiecesFor(3).isEmpty())
        scope2.coroutineContext.job.cancelAndJoin()
    }

    @Test
    fun `schema migration v0_v1 to v2 upgrades stars into completed slugs and wheel index`() = runTest {
        val storeFile = File(tempFolder.root, "legacy_v1.preferences_pb")

        val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val rawStore = PreferenceDataStoreFactory.create(scope = seedScope) { storeFile }
        rawStore.edit { prefs ->
            prefs[intPreferencesKey("schema_version")] = 1
            prefs[intPreferencesKey("level_stars_2")] = 99
            prefs[intPreferencesKey("level_stars_3")] = 2
            prefs[intPreferencesKey("level_stars_4")] = -5
            prefs[intPreferencesKey("last_selected_level_id")] = 3
            prefs[booleanPreferencesKey("sound_enabled")] = false
        }
        seedScope.coroutineContext.job.cancelAndJoin()

        val migratedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val migratedStore = PreferenceDataStoreFactory.create(
            migrations = listOf(ProgressSchemaMigration),
            scope = migratedScope
        ) { storeFile }
        val repo = DataStoreProgressRepository(migratedStore, migratedScope)
        val state = repo.awaitLoaded()

        assertEquals(ProgressKeys.CURRENT_SCHEMA_VERSION, state.schemaVersion)
        assertEquals(GameConfig.MAX_STARS, state.starsFor(2))
        assertEquals(2, state.starsFor(3))
        assertEquals(0, state.starsFor(4))
        assertTrue("princess_brunette" in state.completedLevelSlugs)
        assertTrue("royal_pony" in state.completedLevelSlugs)
        assertEquals(2, state.lastSelectedWheelIndex)
        assertFalse(state.soundEnabled)
        migratedScope.coroutineContext.job.cancelAndJoin()
    }

    @Test
    fun `datastore purchase service and progress reset follow lock rules`() = runTest {
        val storeFile = File(tempFolder.root, "purchase_reset_v2.preferences_pb")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val store = PreferenceDataStoreFactory.create(
            migrations = listOf(ProgressSchemaMigration),
            scope = scope
        ) { storeFile }
        val repo = DataStoreProgressRepository(store, scope)
        val purchaseService = DataStorePurchaseService(repo)

        repo.awaitLoaded()
        assertFalse(purchaseService.isUnlockedAll())
        assertTrue(purchaseService.purchaseUnlockAll())
        repo.awaitLoaded()
        assertTrue(purchaseService.isUnlockedAll())

        repo.recordLevelCompletion(1, 3)
        repo.saveInProgressPieces(2, setOf("princess_brunette_p0"))
        repo.resetProgress()
        val afterReset = repo.awaitLoaded()
        assertEquals(0, afterReset.totalStars)
        assertTrue(afterReset.completedLevelSlugs.isEmpty())
        assertTrue(afterReset.inProgressPiecesByLevel.isEmpty())
        assertEquals(0, afterReset.lastSelectedWheelIndex)

        purchaseService.resetSimulatedPurchase()
        val afterPurchaseReset = repo.awaitLoaded()
        assertFalse(afterPurchaseReset.unlockedAll)
        assertFalse(PuzzleLevels.isUnlocked(PuzzleLevels.getById(10), afterPurchaseReset.unlockedAll))
        scope.coroutineContext.job.cancelAndJoin()
    }
}

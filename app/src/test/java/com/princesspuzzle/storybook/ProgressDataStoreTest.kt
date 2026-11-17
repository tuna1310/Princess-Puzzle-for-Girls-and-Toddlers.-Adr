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
    fun `progress stars unlocked levels and settings persist across DataStore restarts`() = runTest {
        val storeFile = File(tempFolder.root, "progress_persist.preferences_pb")

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

        repo1.recordLevelCompletion(levelId = 2, stars = 3)
        repo1.recordLevelCompletion(levelId = 2, stars = 1) // Never downgrades earned stars
        repo1.recordLevelCompletion(levelId = 1, stars = 2)
        repo1.unlockLevel(5)
        repo1.setUnlockedAll(true)
        repo1.setSoundEnabled(false)
        repo1.setMusicEnabled(false)
        repo1.setLastSelectedLevelId(5)
        val saved = repo1.awaitLoaded()
        assertEquals(3, saved.starsFor(2))
        assertEquals(2, saved.starsFor(1))
        scope1.coroutineContext.job.cancelAndJoin()

        // Cold reopen against the same backing .preferences_pb file
        val scope2 = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val ds2 = PreferenceDataStoreFactory.create(
            migrations = listOf(ProgressSchemaMigration),
            scope = scope2
        ) { storeFile }
        val repo2 = DataStoreProgressRepository(ds2, scope2)
        val reloaded = repo2.awaitLoaded()
        assertEquals(ProgressKeys.CURRENT_SCHEMA_VERSION, reloaded.schemaVersion)
        assertEquals(3, reloaded.starsFor(2))
        assertEquals(2, reloaded.starsFor(1))
        assertEquals(5, reloaded.totalStars)
        assertTrue(5 in reloaded.unlockedLevelIds)
        assertTrue(reloaded.unlockedAll)
        assertFalse(reloaded.soundEnabled)
        assertFalse(reloaded.musicEnabled)
        assertEquals(5, reloaded.lastSelectedLevelId)
        scope2.coroutineContext.job.cancelAndJoin()
    }

    @Test
    fun `schema migration upgrades unversioned v0 prefs and preserves clamped stars`() = runTest {
        val storeFile = File(tempFolder.root, "legacy_v0.preferences_pb")

        // Seed legacy unversioned preferences (no schema_version key, out-of-range stars)
        val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val rawStore = PreferenceDataStoreFactory.create(scope = seedScope) { storeFile }
        rawStore.edit { prefs ->
            prefs[intPreferencesKey("level_stars_2")] = 99
            prefs[intPreferencesKey("level_stars_3")] = 2
            prefs[intPreferencesKey("level_stars_4")] = -5
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
        assertEquals(PuzzleLevels.DEFAULT_FREE_IDS, state.unlockedLevelIds)
        assertFalse(state.soundEnabled)
        assertTrue(state.musicEnabled)
        assertFalse(state.unlockedAll)
        migratedScope.coroutineContext.job.cancelAndJoin()
    }

    @Test
    fun `datastore purchase service and progress reset follow lock rules`() = runTest {
        val storeFile = File(tempFolder.root, "purchase_reset.preferences_pb")
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
        assertTrue(PuzzleLevels.isUnlocked(PuzzleLevels.getById(10), purchaseService.isUnlockedAll()))

        repo.recordLevelCompletion(1, 3)
        repo.unlockLevel(6)
        repo.resetProgress()
        val afterReset = repo.awaitLoaded()
        assertEquals(0, afterReset.totalStars)
        assertEquals(PuzzleLevels.DEFAULT_FREE_IDS, afterReset.unlockedLevelIds)
        assertEquals(1, afterReset.lastSelectedLevelId)

        purchaseService.resetSimulatedPurchase()
        val afterPurchaseReset = repo.awaitLoaded()
        assertFalse(afterPurchaseReset.unlockedAll)
        assertFalse(PuzzleLevels.isUnlocked(PuzzleLevels.getById(10), afterPurchaseReset.unlockedAll))
        scope.coroutineContext.job.cancelAndJoin()
    }
}

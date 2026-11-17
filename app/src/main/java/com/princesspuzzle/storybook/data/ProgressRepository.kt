package com.princesspuzzle.storybook.data

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Persisted player progression, unlock state, and grown-up settings.
 */
data class AppProgressState(
    val schemaVersion: Int = ProgressKeys.CURRENT_SCHEMA_VERSION,
    val starsByLevel: Map<Int, Int> = emptyMap(),
    val unlockedLevelIds: Set<Int> = PuzzleLevels.DEFAULT_FREE_IDS,
    val unlockedAll: Boolean = false,
    val lastSelectedLevelId: Int = 1,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true
) {
    val totalStars: Int
        get() = starsByLevel.values.sum()

    fun starsFor(levelId: Int): Int = starsByLevel[levelId] ?: 0
}

object ProgressKeys {
    const val CURRENT_SCHEMA_VERSION = 1
    const val DATASTORE_FILE_NAME = "princess_puzzle_progress"

    val SCHEMA_VERSION = intPreferencesKey("schema_version")
    val UNLOCKED_LEVEL_IDS = stringSetPreferencesKey("unlocked_level_ids")
    val UNLOCKED_ALL = booleanPreferencesKey("unlocked_all")
    val LAST_SELECTED_LEVEL_ID = intPreferencesKey("last_selected_level_id")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")

    fun levelStarsKey(levelId: Int): Preferences.Key<Int> = intPreferencesKey("level_stars_$levelId")
}

interface ProgressRepository {
    val progressFlow: StateFlow<AppProgressState>

    suspend fun recordLevelCompletion(levelId: Int, stars: Int = GameConfig.MAX_STARS)

    suspend fun unlockLevel(levelId: Int)

    suspend fun setUnlockedAll(unlocked: Boolean)

    suspend fun setLastSelectedLevelId(levelId: Int)

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun setMusicEnabled(enabled: Boolean)

    suspend fun resetProgress()
}

/**
 * Migrates unversioned / legacy v0 preferences cleanly to [ProgressKeys.CURRENT_SCHEMA_VERSION].
 */
object ProgressSchemaMigration : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        val version = currentData[ProgressKeys.SCHEMA_VERSION] ?: 0
        return version < ProgressKeys.CURRENT_SCHEMA_VERSION
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutable: MutablePreferences = currentData.toMutablePreferences()
        for (level in PuzzleLevels.ALL) {
            val key = ProgressKeys.levelStarsKey(level.id)
            val existing = mutable[key]
            if (existing != null) {
                val clamped = existing.coerceIn(0, GameConfig.MAX_STARS)
                if (clamped == 0) {
                    mutable.remove(key)
                } else {
                    mutable[key] = clamped
                }
            }
        }

        val savedIds = mutable[ProgressKeys.UNLOCKED_LEVEL_IDS]
            ?.mapNotNull { it.toIntOrNull() }
            ?.filter { id -> id in 1..PuzzleLevels.TOTAL_COUNT }
            ?.toSet()
            ?: emptySet()
        val mergedIds = (PuzzleLevels.DEFAULT_FREE_IDS + savedIds).map { it.toString() }.toSet()
        mutable[ProgressKeys.UNLOCKED_LEVEL_IDS] = mergedIds

        if (mutable[ProgressKeys.SOUND_ENABLED] == null) {
            mutable[ProgressKeys.SOUND_ENABLED] = true
        }
        if (mutable[ProgressKeys.MUSIC_ENABLED] == null) {
            mutable[ProgressKeys.MUSIC_ENABLED] = true
        }
        if (mutable[ProgressKeys.UNLOCKED_ALL] == null) {
            mutable[ProgressKeys.UNLOCKED_ALL] = false
        }
        val lastId = mutable[ProgressKeys.LAST_SELECTED_LEVEL_ID] ?: 1
        mutable[ProgressKeys.LAST_SELECTED_LEVEL_ID] = lastId.coerceIn(1, PuzzleLevels.TOTAL_COUNT)
        mutable[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        return mutable.toPreferences()
    }

    override suspend fun cleanUp() = Unit
}

class DataStoreProgressRepository(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : ProgressRepository {

    private val mappedFlow: Flow<AppProgressState> = dataStore.data
        .catch { err ->
            if (err is IOException) {
                emit(emptyPreferences())
            } else {
                throw err
            }
        }
        .map { prefs -> prefs.toAppProgressState() }

    override val progressFlow: StateFlow<AppProgressState> = mappedFlow.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AppProgressState()
    )

    suspend fun awaitLoaded(): AppProgressState = mappedFlow.first()

    override suspend fun recordLevelCompletion(levelId: Int, stars: Int) {
        val clampedStars = stars.coerceIn(1, GameConfig.MAX_STARS)
        val validLevelId = PuzzleLevels.getById(levelId).id
        dataStore.edit { prefs ->
            val key = ProgressKeys.levelStarsKey(validLevelId)
            val prev = prefs[key] ?: 0
            if (clampedStars > prev) {
                prefs[key] = clampedStars
            }
            prefs[ProgressKeys.LAST_SELECTED_LEVEL_ID] = validLevelId
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun unlockLevel(levelId: Int) {
        val validId = PuzzleLevels.getById(levelId).id
        dataStore.edit { prefs ->
            val cur = prefs[ProgressKeys.UNLOCKED_LEVEL_IDS] ?: defaultFreeStringSet()
            prefs[ProgressKeys.UNLOCKED_LEVEL_IDS] = cur + validId.toString()
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun setUnlockedAll(unlocked: Boolean) {
        dataStore.edit { prefs ->
            prefs[ProgressKeys.UNLOCKED_ALL] = unlocked
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun setLastSelectedLevelId(levelId: Int) {
        val validId = PuzzleLevels.getById(levelId).id
        dataStore.edit { prefs ->
            prefs[ProgressKeys.LAST_SELECTED_LEVEL_ID] = validId
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[ProgressKeys.SOUND_ENABLED] = enabled
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[ProgressKeys.MUSIC_ENABLED] = enabled
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun resetProgress() {
        dataStore.edit { prefs ->
            for (level in PuzzleLevels.ALL) {
                prefs.remove(ProgressKeys.levelStarsKey(level.id))
            }
            prefs[ProgressKeys.UNLOCKED_LEVEL_IDS] = defaultFreeStringSet()
            prefs[ProgressKeys.LAST_SELECTED_LEVEL_ID] = 1
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    private fun Preferences.toAppProgressState(): AppProgressState {
        val stars = HashMap<Int, Int>()
        for (level in PuzzleLevels.ALL) {
            val s = this[ProgressKeys.levelStarsKey(level.id)] ?: 0
            if (s > 0) {
                stars[level.id] = s.coerceIn(1, GameConfig.MAX_STARS)
            }
        }
        val parsedIds = this[ProgressKeys.UNLOCKED_LEVEL_IDS]
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()

        return AppProgressState(
            schemaVersion = this[ProgressKeys.SCHEMA_VERSION] ?: ProgressKeys.CURRENT_SCHEMA_VERSION,
            starsByLevel = stars,
            unlockedLevelIds = PuzzleLevels.DEFAULT_FREE_IDS + parsedIds,
            unlockedAll = this[ProgressKeys.UNLOCKED_ALL] ?: false,
            lastSelectedLevelId = (this[ProgressKeys.LAST_SELECTED_LEVEL_ID] ?: 1)
                .coerceIn(1, PuzzleLevels.TOTAL_COUNT),
            soundEnabled = this[ProgressKeys.SOUND_ENABLED] ?: true,
            musicEnabled = this[ProgressKeys.MUSIC_ENABLED] ?: true
        )
    }

    private fun defaultFreeStringSet(): Set<String> = PuzzleLevels.DEFAULT_FREE_IDS.map { it.toString() }.toSet()
}

object ProgressRepositoryProvider {
    @Volatile
    private var instance: DataStoreProgressRepository? = null

    fun get(context: Context): DataStoreProgressRepository {
        return instance ?: synchronized(this) {
            instance ?: createRepository(context.applicationContext).also { instance = it }
        }
    }

    private fun createRepository(appContext: Context): DataStoreProgressRepository {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val store = PreferenceDataStoreFactory.create(
            migrations = listOf(ProgressSchemaMigration),
            scope = scope
        ) {
            appContext.preferencesDataStoreFile(ProgressKeys.DATASTORE_FILE_NAME)
        }
        return DataStoreProgressRepository(store, scope)
    }
}

class InMemoryProgressRepository(initial: AppProgressState = AppProgressState()) : ProgressRepository {
    private val state = MutableStateFlow(initial)
    override val progressFlow: StateFlow<AppProgressState> = state

    override suspend fun recordLevelCompletion(levelId: Int, stars: Int) {
        val clamped = stars.coerceIn(1, GameConfig.MAX_STARS)
        val cur = state.value
        val prev = cur.starsFor(levelId)
        val updatedStars = if (clamped > prev) cur.starsByLevel + (levelId to clamped) else cur.starsByLevel
        state.value = cur.copy(starsByLevel = updatedStars, lastSelectedLevelId = levelId)
    }

    override suspend fun unlockLevel(levelId: Int) {
        val cur = state.value
        state.value = cur.copy(unlockedLevelIds = cur.unlockedLevelIds + levelId)
    }

    override suspend fun setUnlockedAll(unlocked: Boolean) {
        state.value = state.value.copy(unlockedAll = unlocked)
    }

    override suspend fun setLastSelectedLevelId(levelId: Int) {
        state.value = state.value.copy(lastSelectedLevelId = levelId)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        state.value = state.value.copy(soundEnabled = enabled)
    }

    override suspend fun setMusicEnabled(enabled: Boolean) {
        state.value = state.value.copy(musicEnabled = enabled)
    }

    override suspend fun resetProgress() {
        val cur = state.value
        state.value = cur.copy(
            starsByLevel = emptyMap(),
            unlockedLevelIds = PuzzleLevels.DEFAULT_FREE_IDS,
            lastSelectedLevelId = 1
        )
    }
}

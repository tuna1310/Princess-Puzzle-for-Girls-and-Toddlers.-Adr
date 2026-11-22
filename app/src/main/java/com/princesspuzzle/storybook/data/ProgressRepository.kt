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
import com.princesspuzzle.storybook.puzzle.LevelDefinition
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
 * Persisted player progression, wheel selection, mid-puzzle piece snapshots,
 * unlock state, and grown-up audio preferences.
 */
data class AppProgressState(
    val schemaVersion: Int = ProgressKeys.CURRENT_SCHEMA_VERSION,
    val starsByLevel: Map<Int, Int> = emptyMap(),
    val completedLevelSlugs: Set<String> = emptySet(),
    val unlockedLevelIds: Set<Int> = PuzzleLevels.DEFAULT_FREE_IDS,
    val unlockedAll: Boolean = false,
    val lastSelectedLevelId: Int = 1,
    val lastSelectedWheelIndex: Int = 0,
    val inProgressPiecesByLevel: Map<Int, Set<String>> = emptyMap(),
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true
) {
    val totalStars: Int
        get() = starsByLevel.values.sum()

    fun starsFor(levelId: Int): Int = starsByLevel[levelId] ?: 0

    fun isLevelCompleted(level: LevelDefinition): Boolean =
        level.slugId in completedLevelSlugs || starsFor(level.id) > 0

    fun inProgressPiecesFor(levelId: Int): Set<String> = inProgressPiecesByLevel[levelId].orEmpty()
}

object ProgressKeys {
    const val CURRENT_SCHEMA_VERSION = 2
    const val DATASTORE_FILE_NAME = "princess_puzzle_progress"

    val SCHEMA_VERSION = intPreferencesKey("schema_version")
    val UNLOCKED_LEVEL_IDS = stringSetPreferencesKey("unlocked_level_ids")
    val COMPLETED_LEVEL_SLUGS = stringSetPreferencesKey("completed_level_slugs")
    val UNLOCKED_ALL = booleanPreferencesKey("unlocked_all")
    val LAST_SELECTED_LEVEL_ID = intPreferencesKey("last_selected_level_id")
    val LAST_WHEEL_INDEX = intPreferencesKey("last_wheel_index")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")

    fun levelStarsKey(levelId: Int): Preferences.Key<Int> = intPreferencesKey("level_stars_$levelId")

    fun inProgressPiecesKey(levelId: Int): Preferences.Key<Set<String>> =
        stringSetPreferencesKey("in_progress_pieces_$levelId")
}

interface ProgressRepository {
    val progressFlow: StateFlow<AppProgressState>

    suspend fun recordLevelCompletion(levelId: Int, stars: Int = GameConfig.MAX_STARS)

    suspend fun saveInProgressPieces(levelId: Int, placedPieceIds: Set<String>)

    suspend fun clearInProgressPieces(levelId: Int)

    suspend fun unlockLevel(levelId: Int)

    suspend fun setUnlockedAll(unlocked: Boolean)

    suspend fun setLastSelectedLevelId(levelId: Int)

    suspend fun setLastSelectedWheelIndex(index: Int)

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun setMusicEnabled(enabled: Boolean)

    suspend fun resetProgress()
}

/**
 * Migrates unversioned / v1 preferences cleanly to [ProgressKeys.CURRENT_SCHEMA_VERSION].
 */
object ProgressSchemaMigration : DataMigration<Preferences> {
    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        val version = currentData[ProgressKeys.SCHEMA_VERSION] ?: 0
        return version < ProgressKeys.CURRENT_SCHEMA_VERSION
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutable: MutablePreferences = currentData.toMutablePreferences()
        val migratedCompleted = (mutable[ProgressKeys.COMPLETED_LEVEL_SLUGS] ?: emptySet()).toMutableSet()
        for (level in PuzzleLevels.ALL) {
            val key = ProgressKeys.levelStarsKey(level.id)
            val existing = mutable[key]
            if (existing != null) {
                val clamped = existing.coerceIn(0, GameConfig.MAX_STARS)
                if (clamped == 0) {
                    mutable.remove(key)
                } else {
                    mutable[key] = clamped
                    migratedCompleted.add(level.slugId)
                }
            }
        }
        mutable[ProgressKeys.COMPLETED_LEVEL_SLUGS] = migratedCompleted

        val savedIds = mutable[ProgressKeys.UNLOCKED_LEVEL_IDS]
            ?.mapNotNull { it.toIntOrNull() }
            ?.filter { id -> id in 1..PuzzleLevels.TOTAL_COUNT }
            ?.toSet()
            ?: emptySet()
        val mergedIds = PuzzleLevels.DEFAULT_FREE_IDS + savedIds
        mutable[ProgressKeys.UNLOCKED_LEVEL_IDS] = mergedIds.map { it.toString() }.toSet()

        if (mutable[ProgressKeys.SOUND_ENABLED] == null) {
            mutable[ProgressKeys.SOUND_ENABLED] = true
        }
        if (mutable[ProgressKeys.MUSIC_ENABLED] == null) {
            mutable[ProgressKeys.MUSIC_ENABLED] = true
        }
        if (mutable[ProgressKeys.UNLOCKED_ALL] == null) {
            mutable[ProgressKeys.UNLOCKED_ALL] = false
        }
        val lastId = (mutable[ProgressKeys.LAST_SELECTED_LEVEL_ID] ?: 1).coerceIn(1, PuzzleLevels.TOTAL_COUNT)
        mutable[ProgressKeys.LAST_SELECTED_LEVEL_ID] = lastId
        val wheelIdx = (mutable[ProgressKeys.LAST_WHEEL_INDEX] ?: (lastId - 1))
            .coerceIn(0, PuzzleLevels.TOTAL_COUNT - 1)
        mutable[ProgressKeys.LAST_WHEEL_INDEX] = wheelIdx
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
        val level = PuzzleLevels.getById(levelId)
        dataStore.edit { prefs ->
            val key = ProgressKeys.levelStarsKey(level.id)
            val prev = prefs[key] ?: 0
            if (clampedStars > prev) {
                prefs[key] = clampedStars
            }
            val completed = prefs[ProgressKeys.COMPLETED_LEVEL_SLUGS] ?: emptySet()
            prefs[ProgressKeys.COMPLETED_LEVEL_SLUGS] = completed + level.slugId
            prefs.remove(ProgressKeys.inProgressPiecesKey(level.id))
            prefs[ProgressKeys.LAST_SELECTED_LEVEL_ID] = level.id
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun saveInProgressPieces(levelId: Int, placedPieceIds: Set<String>) {
        val validId = PuzzleLevels.getById(levelId).id
        dataStore.edit { prefs ->
            val key = ProgressKeys.inProgressPiecesKey(validId)
            if (placedPieceIds.isEmpty()) {
                prefs.remove(key)
            } else {
                prefs[key] = placedPieceIds
            }
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun clearInProgressPieces(levelId: Int) {
        val validId = PuzzleLevels.getById(levelId).id
        dataStore.edit { prefs ->
            prefs.remove(ProgressKeys.inProgressPiecesKey(validId))
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
        val level = PuzzleLevels.getById(levelId)
        dataStore.edit { prefs ->
            prefs[ProgressKeys.LAST_SELECTED_LEVEL_ID] = level.id
            prefs[ProgressKeys.LAST_WHEEL_INDEX] = level.index
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    override suspend fun setLastSelectedWheelIndex(index: Int) {
        val level = PuzzleLevels.getByIndex(index)
        dataStore.edit { prefs ->
            prefs[ProgressKeys.LAST_WHEEL_INDEX] = level.index
            prefs[ProgressKeys.LAST_SELECTED_LEVEL_ID] = level.id
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
                prefs.remove(ProgressKeys.inProgressPiecesKey(level.id))
            }
            prefs[ProgressKeys.COMPLETED_LEVEL_SLUGS] = emptySet()
            prefs[ProgressKeys.UNLOCKED_LEVEL_IDS] = defaultFreeStringSet()
            prefs[ProgressKeys.LAST_SELECTED_LEVEL_ID] = 1
            prefs[ProgressKeys.LAST_WHEEL_INDEX] = 0
            prefs[ProgressKeys.SCHEMA_VERSION] = ProgressKeys.CURRENT_SCHEMA_VERSION
        }
    }

    private fun Preferences.toAppProgressState(): AppProgressState {
        val stars = HashMap<Int, Int>()
        val inProgress = HashMap<Int, Set<String>>()
        for (level in PuzzleLevels.ALL) {
            val s = this[ProgressKeys.levelStarsKey(level.id)] ?: 0
            if (s > 0) {
                stars[level.id] = s.coerceIn(1, GameConfig.MAX_STARS)
            }
            val savedPieces = this[ProgressKeys.inProgressPiecesKey(level.id)]
            if (!savedPieces.isNullOrEmpty()) {
                inProgress[level.id] = savedPieces
            }
        }
        val parsedIds = this[ProgressKeys.UNLOCKED_LEVEL_IDS]
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
        val lastId = (this[ProgressKeys.LAST_SELECTED_LEVEL_ID] ?: 1).coerceIn(1, PuzzleLevels.TOTAL_COUNT)
        val wheelIndex = (this[ProgressKeys.LAST_WHEEL_INDEX] ?: (lastId - 1))
            .coerceIn(0, PuzzleLevels.TOTAL_COUNT - 1)

        return AppProgressState(
            schemaVersion = this[ProgressKeys.SCHEMA_VERSION] ?: ProgressKeys.CURRENT_SCHEMA_VERSION,
            starsByLevel = stars,
            completedLevelSlugs = this[ProgressKeys.COMPLETED_LEVEL_SLUGS] ?: emptySet(),
            unlockedLevelIds = PuzzleLevels.DEFAULT_FREE_IDS + parsedIds,
            unlockedAll = this[ProgressKeys.UNLOCKED_ALL] ?: false,
            lastSelectedLevelId = lastId,
            lastSelectedWheelIndex = wheelIndex,
            inProgressPiecesByLevel = inProgress,
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
        val level = PuzzleLevels.getById(levelId)
        val cur = state.value
        val prev = cur.starsFor(level.id)
        val updatedStars = if (clamped > prev) cur.starsByLevel + (level.id to clamped) else cur.starsByLevel
        state.value = cur.copy(
            starsByLevel = updatedStars,
            completedLevelSlugs = cur.completedLevelSlugs + level.slugId,
            inProgressPiecesByLevel = cur.inProgressPiecesByLevel - level.id,
            lastSelectedLevelId = level.id
        )
    }

    override suspend fun saveInProgressPieces(levelId: Int, placedPieceIds: Set<String>) {
        val cur = state.value
        val nextMap = if (placedPieceIds.isEmpty()) {
            cur.inProgressPiecesByLevel - levelId
        } else {
            cur.inProgressPiecesByLevel + (levelId to placedPieceIds)
        }
        state.value = cur.copy(inProgressPiecesByLevel = nextMap)
    }

    override suspend fun clearInProgressPieces(levelId: Int) {
        val cur = state.value
        state.value = cur.copy(inProgressPiecesByLevel = cur.inProgressPiecesByLevel - levelId)
    }

    override suspend fun unlockLevel(levelId: Int) {
        val cur = state.value
        state.value = cur.copy(unlockedLevelIds = cur.unlockedLevelIds + levelId)
    }

    override suspend fun setUnlockedAll(unlocked: Boolean) {
        state.value = state.value.copy(unlockedAll = unlocked)
    }

    override suspend fun setLastSelectedLevelId(levelId: Int) {
        val level = PuzzleLevels.getById(levelId)
        state.value = state.value.copy(lastSelectedLevelId = level.id, lastSelectedWheelIndex = level.index)
    }

    override suspend fun setLastSelectedWheelIndex(index: Int) {
        val level = PuzzleLevels.getByIndex(index)
        state.value = state.value.copy(lastSelectedWheelIndex = level.index, lastSelectedLevelId = level.id)
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
            completedLevelSlugs = emptySet(),
            unlockedLevelIds = PuzzleLevels.DEFAULT_FREE_IDS,
            inProgressPiecesByLevel = emptyMap(),
            lastSelectedLevelId = 1,
            lastSelectedWheelIndex = 0
        )
    }
}

package com.princesspuzzle.storybook.puzzle

import androidx.annotation.StringRes
import com.princesspuzzle.storybook.core.GameConfig

enum class PuzzleDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class SceneThemeId {
    PINK_CASTLE,
    BRUNETTE_PRINCESS,
    ROYAL_PONY,
    CHERRY_ICECREAM,
    BLONDE_PRINCESS,
    FLOWER_PRINCESS,
    ROYAL_KITTY,
    FRIENDLY_DRAGON,
    RAINBOW_PALACE,
    BUTTERFLY_TREE,
    PUMPKIN_CARRIAGE,
    MEADOW_UNICORN,
    ROYAL_TREASURE,
    MERMAID_PRINCESS,
    ROSE_PRINCESS,
    GRAND_BALL_PRINCESS
}

/**
 * Defines a single anatomical / component puzzle part matching the iOS normalized target space.
 */
data class PuzzlePieceDefinition(
    val id: String,
    val partIndex: Int,
    val name: String,
    val normalizedCenterX: Float,
    val normalizedCenterY: Float,
    val normalizedWidth: Float,
    val normalizedHeight: Float,
    val zIndex: Int
)

/**
 * Single source of truth for one of the 16 fairy-tale puzzles matching iOS `PuzzleLevel`.
 */
data class LevelDefinition(
    val id: Int,
    val slugId: String,
    val index: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val free: Boolean,
    val theme: SceneThemeId,
    val difficulty: PuzzleDifficulty,
    val pieces: List<PuzzlePieceDefinition>
) {
    val pieceCount: Int
        get() = pieces.size
}

object PuzzleLevels {
    val ALL: List<LevelDefinition> by lazy { PuzzleCatalogData.buildAllLevels() }
    val DEFAULT_FREE_IDS: Set<Int> by lazy { ALL.filter { it.free }.map { it.id }.toSet() }
    val TOTAL_COUNT: Int get() = ALL.size
    val MAX_TOTAL_STARS: Int get() = ALL.size * GameConfig.MAX_STARS

    fun getById(id: Int): LevelDefinition = ALL.firstOrNull { it.id == id } ?: ALL.first()

    fun getBySlug(slugId: String): LevelDefinition = ALL.firstOrNull { it.slugId == slugId } ?: ALL.first()

    fun getByIndex(index: Int): LevelDefinition {
        val count = ALL.size
        if (count == 0) return ALL.first()
        val wrapped = ((index % count) + count) % count
        return ALL[wrapped]
    }

    fun nextAfter(levelId: Int): LevelDefinition? {
        val index = ALL.indexOfFirst { it.id == levelId }
        return if (index >= 0) ALL.getOrNull(index + 1) else null
    }

    fun nextCircularAfter(index: Int): LevelDefinition = getByIndex(index + 1)

    fun isUnlocked(
        level: LevelDefinition,
        unlockedAll: Boolean,
        extraUnlockedIds: Set<Int> = emptySet(),
        debugUnlockAll: Boolean = false
    ): Boolean = level.free || unlockedAll || debugUnlockAll || level.id in extraUnlockedIds
}

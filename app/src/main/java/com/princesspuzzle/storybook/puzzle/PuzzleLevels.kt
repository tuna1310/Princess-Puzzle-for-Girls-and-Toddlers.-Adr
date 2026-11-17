package com.princesspuzzle.storybook.puzzle

import androidx.annotation.StringRes
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.core.GameConfig

enum class SceneThemeId {
    TIARA,
    BUNNY,
    UNICORN,
    CASTLE,
    DRAGON,
    CARRIAGE,
    WILLOW,
    MERMAID,
    TEAPARTY,
    TREEHOUSE,
    HARP,
    SWAN,
    PEGASUS,
    LANTERN,
    CAROUSEL,
    BALL
}

data class LevelDefinition(
    val id: Int,
    @StringRes val titleRes: Int,
    val pieceCount: Int,
    val free: Boolean,
    val theme: SceneThemeId,
    val accentHex: Long,
    val frameHex: Long
)

object PuzzleLevels {
    val ALL: List<LevelDefinition> =
        listOf(
            LevelDefinition(
                1,
                R.string.level_1_title,
                2,
                true,
                SceneThemeId.TIARA,
                0xFFFF9EC7,
                0xFFFFF3FA
            ),
            LevelDefinition(
                2,
                R.string.level_2_title,
                3,
                true,
                SceneThemeId.BUNNY,
                0xFFC59BFF,
                0xFFF7F2FF
            ),
            LevelDefinition(
                3,
                R.string.level_3_title,
                4,
                true,
                SceneThemeId.UNICORN,
                0xFF8FD8FF,
                0xFFF1FAFF
            ),
            LevelDefinition(
                4,
                R.string.level_4_title,
                4,
                true,
                SceneThemeId.CASTLE,
                0xFFFFB8D2,
                0xFFFFF5F8
            ),
            LevelDefinition(
                5,
                R.string.level_5_title,
                5,
                false,
                SceneThemeId.DRAGON,
                0xFFA8E6A3,
                0xFFF2FCF1
            ),
            LevelDefinition(
                6,
                R.string.level_6_title,
                6,
                false,
                SceneThemeId.CARRIAGE,
                0xFFF9C4E4,
                0xFFFDF3FA
            ),
            LevelDefinition(
                7,
                R.string.level_7_title,
                6,
                false,
                SceneThemeId.WILLOW,
                0xFF9EDBFF,
                0xFFF2FAFF
            ),
            LevelDefinition(
                8,
                R.string.level_8_title,
                8,
                false,
                SceneThemeId.MERMAID,
                0xFF98EAE0,
                0xFFF0FCFA
            ),
            LevelDefinition(
                9,
                R.string.level_9_title,
                8,
                false,
                SceneThemeId.TEAPARTY,
                0xFFFFC3D8,
                0xFFFFF4F8
            ),
            LevelDefinition(
                10,
                R.string.level_10_title,
                9,
                false,
                SceneThemeId.TREEHOUSE,
                0xFFBBE69F,
                0xFFF5FCEF
            ),
            LevelDefinition(
                11,
                R.string.level_11_title,
                9,
                false,
                SceneThemeId.HARP,
                0xFFD6B8FF,
                0xFFF8F4FF
            ),
            LevelDefinition(
                12,
                R.string.level_12_title,
                10,
                false,
                SceneThemeId.SWAN,
                0xFFAEE4FF,
                0xFFF3FBFF
            ),
            LevelDefinition(
                13,
                R.string.level_13_title,
                10,
                false,
                SceneThemeId.PEGASUS,
                0xFFFFD1B8,
                0xFFFFF7F2
            ),
            LevelDefinition(
                14,
                R.string.level_14_title,
                12,
                false,
                SceneThemeId.LANTERN,
                0xFFFFE194,
                0xFFFFFBEF
            ),
            LevelDefinition(
                15,
                R.string.level_15_title,
                12,
                false,
                SceneThemeId.CAROUSEL,
                0xFFFFAAD5,
                0xFFFFF3F9
            ),
            LevelDefinition(
                16,
                R.string.level_16_title,
                12,
                false,
                SceneThemeId.BALL,
                0xFFF3B6FF,
                0xFFFCF4FF
            )
        )

    val DEFAULT_FREE_IDS: Set<Int> by lazy { ALL.filter { it.free }.map { it.id }.toSet() }
    val TOTAL_COUNT: Int get() = ALL.size
    val MAX_TOTAL_STARS: Int get() = ALL.size * GameConfig.MAX_STARS

    fun getById(id: Int): LevelDefinition = ALL.firstOrNull { it.id == id } ?: ALL.first()

    fun nextAfter(levelId: Int): LevelDefinition? {
        val index = ALL.indexOfFirst { it.id == levelId }
        return if (index >= 0) ALL.getOrNull(index + 1) else null
    }

    fun isUnlocked(level: LevelDefinition, unlockedAll: Boolean, extraUnlockedIds: Set<Int> = emptySet()): Boolean =
        level.free || unlockedAll || level.id in extraUnlockedIds
}

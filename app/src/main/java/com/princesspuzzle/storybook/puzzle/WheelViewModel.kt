package com.princesspuzzle.storybook.puzzle

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.GeometryHelpers
import com.princesspuzzle.storybook.data.AppProgressState
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * State and rotation logic for the iOS-matched 16-item circular Planet Wheel screen.
 */
class WheelViewModel(
    val levels: List<LevelDefinition> = PuzzleLevels.ALL,
    initialWheelIndex: Int = 0
) {
    var rotationRad: Float = rotationForIndex(initialWheelIndex, levels.size)
        private set

    var activeLevelIndex: Int = normalizeIndex(initialWheelIndex)
        private set

    var isDragging: Boolean = false
        private set

    val stepAngleRad: Float
        get() = if (levels.isEmpty()) 0f else (2.0 * PI / levels.size.toDouble()).toFloat()

    val activeLevel: LevelDefinition
        get() = if (levels.isEmpty()) PuzzleLevels.ALL.first() else levels[activeLevelIndex]

    fun focusLevel(index: Int) {
        val wrapped = normalizeIndex(index)
        activeLevelIndex = wrapped
        rotationRad = rotationForIndex(wrapped, levels.size)
        isDragging = false
    }

    fun focusLevelById(levelId: Int) {
        val idx = levels.indexOfFirst { it.id == levelId }
        if (idx >= 0) {
            focusLevel(idx)
        }
    }

    /**
     * Rotates the wheel clockwise by one puzzle step (brings the previous radial item to 12 o'clock).
     * Returns true if the focused level changed.
     */
    fun rotateClockwise(): Boolean {
        if (levels.isEmpty()) return false
        val nextIdx = normalizeIndex(activeLevelIndex - 1)
        focusLevel(nextIdx)
        return true
    }

    /**
     * Rotates the wheel counter-clockwise by one puzzle step (brings the next radial item to 12 o'clock).
     */
    fun rotateCounterClockwise(): Boolean {
        if (levels.isEmpty()) return false
        val nextIdx = normalizeIndex(activeLevelIndex + 1)
        focusLevel(nextIdx)
        return true
    }

    fun beginDrag() {
        isDragging = true
    }

    /**
     * Applies angular drag delta in radians and returns `true` when crossing onto a new radial index.
     */
    fun applyDragDelta(deltaRadians: Float): Boolean {
        rotationRad += deltaRadians
        val nextIndex = GeometryHelpers.activeIndexOnWheel(rotationRad.toDouble(), levels.size)
        val changed = nextIndex != activeLevelIndex
        activeLevelIndex = nextIndex
        return changed
    }

    fun snapToNearestLevel(): Int {
        isDragging = false
        val nearest = GeometryHelpers.activeIndexOnWheel(rotationRad.toDouble(), levels.size)
        focusLevel(nearest)
        return nearest
    }

    fun targetSnapRotationRad(): Float = rotationForIndex(activeLevelIndex, levels.size)

    fun isLevelUnlocked(level: LevelDefinition, progress: AppProgressState, debugUnlockAll: Boolean = false): Boolean =
        PuzzleLevels.isUnlocked(
            level = level,
            unlockedAll = progress.unlockedAll,
            extraUnlockedIds = progress.unlockedLevelIds,
            debugUnlockAll = debugUnlockAll
        )

    fun isLevelCompleted(level: LevelDefinition, progress: AppProgressState): Boolean = progress.isLevelCompleted(level)

    fun completionSummaryCount(progress: AppProgressState): Int = levels.count { isLevelCompleted(it, progress) }

    fun starsEarnedForLevel(level: LevelDefinition, progress: AppProgressState): Int =
        (progress.starsByLevel[level.id] ?: if (isLevelCompleted(level, progress)) GameConfig.MAX_STARS else 0)
            .coerceIn(0, GameConfig.MAX_STARS)

    private fun normalizeIndex(index: Int): Int {
        val count = max(1, levels.size)
        return ((index % count) + count) % count
    }

    companion object {
        fun rotationForIndex(index: Int, count: Int): Float {
            if (count <= 0) return 0f
            val wrapped = ((index % count) + count) % count
            val step = (2.0 * PI / count.toDouble()).toFloat()
            return -wrapped * step
        }

        fun indexFromAngleDelta(dragRad: Float, stepRad: Float): Int =
            if (stepRad <= 0f) 0 else (dragRad / stepRad).roundToInt()
    }
}

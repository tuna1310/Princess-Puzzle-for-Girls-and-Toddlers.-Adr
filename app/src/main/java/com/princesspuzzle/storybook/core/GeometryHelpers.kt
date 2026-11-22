package com.princesspuzzle.storybook.core

import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Pure mathematical and geometric helper functions matching the iOS planet wheel
 * rotation physics and gameplay piece snapping.
 */
object GeometryHelpers {
    private const val TWO_PI = 2.0 * PI

    fun distance(ax: Float, ay: Float, bx: Float, by: Float): Float = hypot(ax - bx, ay - by)

    /**
     * Normalizes an angle in radians into `[0, 2π)`.
     */
    fun normalizeRadians(angle: Double): Double {
        var result = angle % TWO_PI
        if (result < 0.0) {
            result += TWO_PI
        }
        return result
    }

    /**
     * Computes the shortest signed angular difference in radians from [current] to [target].
     */
    fun shortestAngularDelta(current: Double, target: Double): Double {
        var diff = (target - current) % TWO_PI
        if (diff > PI) {
            diff -= TWO_PI
        } else if (diff < -PI) {
            diff += TWO_PI
        }
        return diff
    }

    /**
     * Determines which radial level index (`0 until totalCount`) sits closest to 12 o'clock.
     */
    fun activeIndexOnWheel(rotationRadians: Double, totalCount: Int): Int {
        if (totalCount <= 0) return 0
        val stepAngle = TWO_PI / totalCount.toDouble()
        val normalized = normalizeRadians(-rotationRadians)
        val rawIndex = (normalized / stepAngle).roundToInt() % totalCount
        return (rawIndex + totalCount) % totalCount
    }

    fun clampCoord(value: Float, minVal: Float, maxVal: Float): Float {
        val safeMax = max(minVal, maxVal)
        return value.coerceIn(minVal, safeMax)
    }
}

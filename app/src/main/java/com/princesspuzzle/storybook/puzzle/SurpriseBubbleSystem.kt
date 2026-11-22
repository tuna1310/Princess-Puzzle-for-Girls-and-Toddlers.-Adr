@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.puzzle

import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.GeometryHelpers
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SparkleParticle(
    var active: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radiusPx: Float = 6f,
    var alpha: Float = 1f,
    var elapsedSec: Float = 0f,
    var lifeSec: Float = 0.55f,
    var colorVariant: Int = 0
)

class SurpriseBubble(val index: Int) {
    var active: Boolean = false
    var popped: Boolean = false
    var baseX: Float = 0f
    var x: Float = 0f
    var y: Float = 0f
    var vy: Float = 0f
    var radiusPx: Float = 40f
    var swayAmplitudePx: Float = 16f
    var swayFreq: Float = 1.6f
    var phase: Float = 0f
    var ageSec: Float = 0f
    var tintIndex: Int = 0
}

/**
 * Zero-allocation pool manager for piece snap sparkles and the 24 floating celebratory
 * soap bubbles that kids tap to pop upon completing any puzzle.
 */
class SurpriseBubbleSystem {
    val sparkles: List<SparkleParticle> = List(GameConfig.MAX_SPARKLE_PARTICLES) { SparkleParticle() }
    val bubbles: List<SurpriseBubble> = List(GameConfig.VICTORY_BUBBLE_COUNT) { SurpriseBubble(index = it) }

    var celebrationActive: Boolean = false
        private set
    private var spawnedCount: Int = 0
    private var spawnTimerSec: Float = 0f
    private var viewportW: Float = 1080f
    private var viewportH: Float = 2000f
    private var density: Float = 2.5f
    private val rng = Random(2026)

    fun resetAll() {
        celebrationActive = false
        spawnedCount = 0
        spawnTimerSec = 0f
        sparkles.forEach { it.active = false }
        bubbles.forEach {
            it.active = false
            it.popped = false
            it.ageSec = 0f
        }
    }

    fun spawnSnapBurst(cx: Float, cy: Float, density: Float) {
        var remaining = 10
        for (i in sparkles.indices) {
            if (remaining <= 0) break
            val p = sparkles[i]
            if (!p.active) {
                val angle = (10 - remaining) * (2.0 * PI / 10.0) + rng.nextDouble(-0.2, 0.2)
                val speed = (85f + rng.nextFloat() * 95f) * density
                p.active = true
                p.x = cx
                p.y = cy
                p.vx = cos(angle).toFloat() * speed
                p.vy = sin(angle).toFloat() * speed - 25f * density
                p.radiusPx = (4f + rng.nextFloat() * 4.5f) * density
                p.alpha = 1f
                p.elapsedSec = 0f
                p.lifeSec = 0.48f + rng.nextFloat() * 0.22f
                p.colorVariant = remaining % 3
                remaining -= 1
            }
        }
    }

    fun startVictoryCelebration(viewportWidthPx: Float, viewportHeightPx: Float, density: Float) {
        resetAll()
        celebrationActive = true
        this.viewportW = viewportWidthPx.coerceAtLeast(200f)
        this.viewportH = viewportHeightPx.coerceAtLeast(300f)
        this.density = density.coerceAtLeast(1f)
        // Spawn initial wave immediately so the victory celebration feels instant
        repeat(4) { spawnNextBubble() }
    }

    fun step(dtSec: Float) {
        val dt = dtSec.coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
        for (i in sparkles.indices) {
            val s = sparkles[i]
            if (!s.active) continue
            s.elapsedSec += dt
            if (s.elapsedSec >= s.lifeSec) {
                s.active = false
            } else {
                s.x += s.vx * dt
                s.y += s.vy * dt
                s.vy += 120f * density * dt
                s.alpha = (1f - s.elapsedSec / s.lifeSec).coerceIn(0f, 1f)
            }
        }

        if (!celebrationActive) return

        if (spawnedCount < GameConfig.VICTORY_BUBBLE_COUNT) {
            spawnTimerSec += dt
            if (spawnTimerSec >= GameConfig.VICTORY_BUBBLE_SPAWN_INTERVAL_SEC) {
                spawnTimerSec = 0f
                spawnNextBubble()
            }
        }

        for (i in bubbles.indices) {
            val b = bubbles[i]
            if (!b.active) continue
            b.ageSec += dt
            b.y -= b.vy * dt
            b.x = b.baseX + sin(b.ageSec * b.swayFreq + b.phase) * b.swayAmplitudePx
            if (b.y < viewportH * 0.06f) {
                // Recycle unpopped bubbles cleanly below HUD pill until popped
                b.y = viewportH * 0.84f
                b.baseX = viewportW * (0.14f + rng.nextFloat() * 0.72f)
            }
        }
    }

    fun tryPopBubbleAt(px: Float, py: Float): SurpriseBubble? {
        val target = if (celebrationActive) findActiveBubbleAt(px, py) else null
        if (target != null) {
            target.popped = true
            target.active = false
            spawnPopDroplets(target.x, target.y, target.tintIndex)
        }
        return target
    }

    private fun findActiveBubbleAt(px: Float, py: Float): SurpriseBubble? {
        val hitPad = 14f * density
        return bubbles.asReversed().firstOrNull { b ->
            b.active && !b.popped && GeometryHelpers.distance(px, py, b.x, b.y) <= b.radiusPx + hitPad
        }
    }

    private fun spawnNextBubble() {
        if (spawnedCount >= bubbles.size) return
        val b = bubbles[spawnedCount]
        spawnedCount += 1
        val radiusDp = GameConfig.MIN_BUBBLE_RADIUS_DP +
            rng.nextFloat() * (GameConfig.MAX_BUBBLE_RADIUS_DP - GameConfig.MIN_BUBBLE_RADIUS_DP)
        b.active = true
        b.popped = false
        b.radiusPx = radiusDp * density
        b.baseX = viewportW * (0.12f + rng.nextFloat() * 0.76f)
        b.x = b.baseX
        b.y = viewportH * (0.76f + rng.nextFloat() * 0.14f)
        b.vy = (55f + rng.nextFloat() * 48f) * density
        b.swayAmplitudePx = (12f + rng.nextFloat() * 14f) * density
        b.swayFreq = 1.4f + rng.nextFloat() * 1.1f
        b.phase = rng.nextFloat() * (2f * PI.toFloat())
        b.ageSec = 0f
        b.tintIndex = (spawnedCount - 1) % 3
    }

    private fun spawnPopDroplets(cx: Float, cy: Float, variant: Int) {
        var remaining = 8
        for (i in sparkles.indices) {
            if (remaining <= 0) break
            val p = sparkles[i]
            if (!p.active) {
                val angle = (8 - remaining) * (2.0 * PI / 8.0)
                val speed = (90f + rng.nextFloat() * 70f) * density
                p.active = true
                p.x = cx
                p.y = cy
                p.vx = cos(angle).toFloat() * speed
                p.vy = sin(angle).toFloat() * speed
                p.radiusPx = (4.5f + rng.nextFloat() * 3f) * density
                p.alpha = 1f
                p.elapsedSec = 0f
                p.lifeSec = 0.38f
                p.colorVariant = variant
                remaining -= 1
            }
        }
    }
}

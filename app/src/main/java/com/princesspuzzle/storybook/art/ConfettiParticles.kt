package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.princesspuzzle.storybook.core.GameConfig
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleKind {
    CONFETTI,
    SPARKLE,
    STAR
}

class ConfettiParticle {
    var active = false
    var x = 0f
    var y = 0f
    var vx = 0f
    var vy = 0f
    var size = 0f
    var color = Color.White
    var rotationDeg = 0f
    var vrDeg = 0f
    var life = 0f
    var maxLife = 1f
    var kind = ParticleKind.CONFETTI
    var gravity = 180f
}

/**
 * Fixed-capacity pre-allocated particle ring pool for snap sparkles and win fireworks
 * so the `withFrameNanos` render loop never allocates particle objects.
 */
class ConfettiParticleSystem(
    capacity: Int = GameConfig.MAX_CONFETTI_PARTICLES
) {
    private val pool = Array(capacity) { ConfettiParticle() }
    private var nextCursor = 0
    private val scratchPath = Path()
    private val rng = Random(42)

    fun hasActiveParticles(): Boolean {
        for (p in pool) {
            if (p.active) return true
        }
        return false
    }

    fun clear() {
        for (p in pool) p.active = false
    }

    private fun acquire(): ConfettiParticle {
        for (offset in pool.indices) {
            val idx = (nextCursor + offset) % pool.size
            if (!pool[idx].active) {
                nextCursor = (idx + 1) % pool.size
                return pool[idx]
            }
        }
        val reused = pool[nextCursor]
        nextCursor = (nextCursor + 1) % pool.size
        return reused
    }

    fun emitSnapSparkles(cx: Float, cy: Float, density: Float, radius: Float = 42f) {
        val count = 16
        for (i in 0 until count) {
            val angle = (i.toFloat() / count) * (2f * PI.toFloat()) + (rng.nextFloat() - 0.5f) * 0.35f
            val speed = (70f + rng.nextFloat() * 130f) * density
            val p = acquire()
            p.active = true
            p.x = cx + cos(angle) * radius * 0.25f
            p.y = cy + sin(angle) * radius * 0.25f
            p.vx = cos(angle) * speed
            p.vy = sin(angle) * speed - 25f * density
            p.size = (6f + rng.nextFloat() * 6f) * density
            p.color = StorybookPalette.ConfettiColors[i % StorybookPalette.ConfettiColors.size]
            p.rotationDeg = rng.nextFloat() * 360f
            p.vrDeg = (rng.nextFloat() - 0.5f) * 340f
            p.life = 0f
            p.maxLife = 0.55f + rng.nextFloat() * 0.3f
            p.kind = if (i % 3 == 0) ParticleKind.STAR else ParticleKind.SPARKLE
            p.gravity = 120f * density
        }
    }

    fun emitCelebrationFireworks(width: Float, height: Float, density: Float) {
        val origins =
            arrayOf(
                Offset(width * 0.24f, height * 0.36f),
                Offset(width * 0.76f, height * 0.34f),
                Offset(width * 0.5f, height * 0.26f)
            )

        for (origin in origins) {
            for (i in 0 until 30) {
                val angle = -PI.toFloat() * 0.5f + (rng.nextFloat() - 0.5f) * PI.toFloat() * 1.45f
                val speed = (130f + rng.nextFloat() * 270f) * density
                val p = acquire()
                p.active = true
                p.x = origin.x + (rng.nextFloat() - 0.5f) * 24f * density
                p.y = origin.y + (rng.nextFloat() - 0.5f) * 20f * density
                p.vx = cos(angle) * speed
                p.vy = sin(angle) * speed - 60f * density
                p.kind = if (i % 5 == 0) ParticleKind.STAR else ParticleKind.CONFETTI
                p.size =
                    if (p.kind == ParticleKind.STAR) {
                        (9f + rng.nextFloat() * 6f) * density
                    } else {
                        (7f + rng.nextFloat() * 6f) * density
                    }
                p.color = StorybookPalette.ConfettiColors[i % StorybookPalette.ConfettiColors.size]
                p.rotationDeg = rng.nextFloat() * 360f
                p.vrDeg = (rng.nextFloat() - 0.5f) * 510f
                p.life = 0f
                p.maxLife = 1.7f + rng.nextFloat() * 1.1f
                p.gravity = 240f * density
            }
        }
    }

    fun update(dtSec: Float) {
        val dt = dtSec.coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
        val drag = 0.45f.pow(dt)
        for (p in pool) {
            if (p.active) {
                p.life += dt
                if (p.life >= p.maxLife) {
                    p.active = false
                } else {
                    p.vx *= drag
                    p.vy += p.gravity * dt
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.rotationDeg += p.vrDeg * dt
                }
            }
        }
    }

    fun draw(scope: DrawScope) {
        for (p in pool) {
            if (!p.active) continue
            val progress = p.life / p.maxLife
            val alpha = (if (progress < 0.7f) 1f else 1f - (progress - 0.7f) / 0.3f).coerceIn(
                0f,
                1f
            )

            scope.translate(p.x, p.y) {
                rotate(p.rotationDeg, pivot = Offset.Zero) {
                    when (p.kind) {
                        ParticleKind.CONFETTI -> {
                            val tilt = max(0.25f, abs(cos(p.life * 9f + p.rotationDeg * 0.017f)))
                            scale(1f, tilt, pivot = Offset.Zero) {
                                drawRoundRect(
                                    color = p.color,
                                    topLeft = Offset(-p.size * 0.65f, -p.size * 0.35f),
                                    size = Size(p.size * 1.3f, p.size * 0.7f),
                                    cornerRadius = CornerRadius(2f, 2f),
                                    alpha = alpha
                                )
                            }
                        }
                        ParticleKind.STAR -> {
                            buildStarPath(0f, 0f, p.size, p.size * 0.45f, targetPath = scratchPath)
                            drawPath(scratchPath, color = p.color, alpha = alpha)
                        }
                        ParticleKind.SPARKLE -> {
                            buildSparklePath(0f, 0f, p.size, scratchPath)
                            drawPath(scratchPath, color = p.color, alpha = alpha)
                        }
                    }
                }
            }
        }
    }
}

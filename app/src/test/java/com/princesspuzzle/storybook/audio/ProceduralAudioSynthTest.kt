package com.princesspuzzle.storybook.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProceduralAudioSynthTest {
    @Test
    fun snapFrequenciesAreStrictlyAscending() {
        assertEquals(12, ProceduralAudioSynth.SNAP_FREQUENCIES.size)
        for (i in 0 until ProceduralAudioSynth.SNAP_FREQUENCIES.lastIndex) {
            assertTrue(
                ProceduralAudioSynth.SNAP_FREQUENCIES[i] < ProceduralAudioSynth.SNAP_FREQUENCIES[i + 1],
                "Snap frequency $i must be strictly less than frequency ${i + 1}"
            )
        }
    }

    @Test
    fun wheelTickAndBubblePopBuffersGenerateAudibleEnergy() {
        val tick = ProceduralAudioSynth.synthesizeWheelTick()
        assertTrue(tick.size in 700..1500, "Wheel tick should be ~45ms at 22050Hz")
        assertTrue(tick.maxOf { kotlin.math.abs(it.toInt()) } > 2000)

        for (pitch in 0 until ProceduralAudioSynth.BUBBLE_PENTATONIC_FREQUENCIES.size) {
            val pop = ProceduralAudioSynth.synthesizeBubblePop(pitch)
            assertTrue(pop.size in 1800..3200, "Bubble pop should be ~110ms at 22050Hz")
            assertTrue(pop.maxOf { kotlin.math.abs(it.toInt()) } > 2000)
        }
    }

    @Test
    fun tapBufferHasValidNonZeroEnergy() {
        val tap = ProceduralAudioSynth.synthesizeTap()
        assertTrue(tap.isNotEmpty(), "Tap buffer should not be empty")
        assertTrue(tap.size in 500..2000, "Tap buffer size should match ~35ms at 22050Hz")
        val maxAmp = tap.maxOf { kotlin.math.abs(it.toInt()) }
        assertTrue(maxAmp > 2000, "Tap buffer must contain audible sound energy (amplitude > 2000)")
    }

    @Test
    fun pickupBufferHasValidNonZeroEnergy() {
        val pickup = ProceduralAudioSynth.synthesizePickup()
        assertTrue(pickup.isNotEmpty(), "Pickup buffer should not be empty")
        assertTrue(pickup.size in 1500..3000, "Pickup buffer size should match ~90ms at 22050Hz")
        val maxAmp = pickup.maxOf { kotlin.math.abs(it.toInt()) }
        assertTrue(maxAmp > 2000, "Pickup buffer must contain audible sound energy")
    }

    @Test
    fun bounceBufferHasValidNonZeroEnergy() {
        val bounce = ProceduralAudioSynth.synthesizeReturnBounce()
        assertTrue(bounce.isNotEmpty(), "Bounce buffer should not be empty")
        assertTrue(bounce.size in 4000..6000, "Bounce buffer size should match ~220ms at 22050Hz")
        val maxAmp = bounce.maxOf { kotlin.math.abs(it.toInt()) }
        assertTrue(maxAmp > 2000, "Bounce buffer must contain audible sound energy")
    }

    @Test
    fun snapBuffersForAll12PiecesGenerateAudibleWaveforms() {
        for (i in 0 until 12) {
            val snap = ProceduralAudioSynth.synthesizeSnap(i)
            assertTrue(snap.isNotEmpty(), "Snap buffer $i should not be empty")
            val maxAmp = snap.maxOf { kotlin.math.abs(it.toInt()) }
            assertTrue(maxAmp > 3000, "Snap buffer $i must have strong audible energy")
        }
    }

    @Test
    fun winFanfareGeneratesTriumphantSustainedChord() {
        val fanfare = ProceduralAudioSynth.synthesizeWinFanfare()
        assertTrue(fanfare.isNotEmpty(), "Fanfare buffer should not be empty")
        assertTrue(fanfare.size >= 25000, "Fanfare buffer must be at least ~1.2s at 22050Hz")
        val maxAmp = fanfare.maxOf { kotlin.math.abs(it.toInt()) }
        assertTrue(maxAmp > 5000, "Fanfare buffer must have strong celebratory energy")
    }

    @Test
    fun storybookMelodyGeneratesPeacefulLullabyLoop() {
        val melody = ProceduralAudioSynth.synthesizeStorybookMelody()
        assertTrue(melody.isNotEmpty(), "Melody buffer should not be empty")
        assertTrue(melody.size >= 200000, "Melody buffer should be ~12s loop")
        val maxAmp = melody.maxOf { kotlin.math.abs(it.toInt()) }
        assertTrue(maxAmp > 3000, "Melody buffer must contain gentle musical notes")
    }

    @Test
    fun scopedAudioEngineSafelyForwardsCalls() {
        var wheelCount = 0
        var tapCount = 0
        var pickupCount = 0
        var snapCount = 0
        var bubbleCount = 0
        var fanfareCount = 0
        var bounceCount = 0
        var released = false

        val mock = object : AudioEngine {
            override fun playWheelRotateTick() {
                wheelCount++
            }

            override fun playPickup() {
                pickupCount++
            }

            override fun playReturnBounce() {
                bounceCount++
            }

            override fun playSnap(placedCount: Int, totalCount: Int) {
                snapCount++
            }

            override fun playBubblePop(pitchIndex: Int) {
                bubbleCount++
            }

            override fun playWinFanfare() {
                fanfareCount++
            }

            override fun playTap() {
                tapCount++
            }

            override fun setAudioPreferences(
                soundEnabled: Boolean,
                musicEnabled: Boolean,
                soundVolume: Float,
                musicVolume: Float
            ) = Unit

            override fun pauseMusic() = Unit

            override fun resumeMusic() = Unit

            override fun release() {
                released = true
            }
        }

        val scoped = ScopedAudioEngine(mock)
        scoped.playWheelRotateTick()
        scoped.playTap()
        scoped.playPickup()
        scoped.playReturnBounce()
        scoped.playSnap(1, 4)
        scoped.playBubblePop(2)
        scoped.playWinFanfare()
        scoped.release()

        assertEquals(1, wheelCount)
        assertEquals(1, tapCount)
        assertEquals(1, pickupCount)
        assertEquals(1, bounceCount)
        assertEquals(1, snapCount)
        assertEquals(1, bubbleCount)
        assertEquals(1, fanfareCount)
        assertEquals(false, released)
    }
}

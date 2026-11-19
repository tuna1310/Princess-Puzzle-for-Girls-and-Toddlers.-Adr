package com.princesspuzzle.storybook.audio

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Pure-Kotlin audio synthesis algorithms generating PCM 16-bit audio buffers.
 * Zero external audio files required. All timbres are mathematically crafted
 * for toddler-safe, gentle, pleasant listening.
 */
object ProceduralAudioSynth {
    const val SAMPLE_RATE: Int = 22050
    private const val TWO_PI: Double = 2.0 * PI

    // Major Pentatonic scale frequencies in Hz (C5 to C7)
    val SNAP_FREQUENCIES: FloatArray = floatArrayOf(
        523.25f,
        587.33f,
        659.25f,
        783.99f,
        880.00f,
        1046.50f,
        1174.66f,
        1318.51f,
        1567.98f,
        1760.00f,
        1975.53f,
        2093.00f
    )

    private val FANFARE_FREQUENCIES = listOf(
        523.25,
        659.25,
        783.99,
        1046.50
    )

    private val MELODY_NOTES = doubleArrayOf(
        523.25,
        659.25,
        783.99,
        659.25,
        587.33,
        659.25,
        783.99,
        587.33,
        440.00,
        523.25,
        659.25,
        523.25,
        392.00,
        523.25,
        587.33,
        523.25
    )

    /**
     * Soft wooden pop for button taps (~35ms).
     */
    fun synthesizeTap(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.035f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val startFreq = 340.0
        val endFreq = 160.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec
            val freq = startFreq + (endFreq - startFreq) * progress
            val phase = TWO_PI * freq * t
            val envelope = exp(-t * 90.0)
            val sample = sin(phase) * envelope * 0.6
            val clamped = (sample * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            )
            buffer[i] = clamped.toShort()
        }
        return buffer
    }

    /**
     * Gentle ascending fairy-tale chirp for picking up a piece (~90ms).
     */
    fun synthesizePickup(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.090f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val startFreq = 420.0
        val endFreq = 640.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec
            val freq = startFreq + (endFreq - startFreq) * progress
            val phase = TWO_PI * freq * t
            val envelope = sin(PI * progress)
            val voice = 0.80 * sin(phase) + 0.20 * sin(2.0 * phase)
            val sample = voice * envelope * 0.55
            val clamped = (sample * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            )
            buffer[i] = clamped.toShort()
        }
        return buffer
    }

    /**
     * Playful spring "boing" / drop bounce when returned to tray (~220ms).
     */
    fun synthesizeReturnBounce(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.220f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val baseFreq = 220.0
        val modFreq = 14.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec
            val envelope = exp(-progress * 4.5)
            val vibrato = sin(TWO_PI * modFreq * t) * (1.0 - progress) * 45.0
            val instFreq = (baseFreq - 40.0 * progress + vibrato).coerceAtLeast(80.0)
            val phase = TWO_PI * instFreq * t
            val voice = 0.75 * sin(phase) + 0.25 * sin(2.0 * phase)
            val sample = voice * envelope * 0.65
            val clamped = (sample * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            )
            buffer[i] = clamped.toShort()
        }
        return buffer
    }

    /**
     * Sparkling music box / celesta chime for placing a puzzle piece (~300ms).
     * Fundamental frequency scales upward with [noteIndex].
     */
    fun synthesizeSnap(noteIndex: Int, sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 0.300f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val index = noteIndex.coerceIn(0, SNAP_FREQUENCIES.lastIndex)
        val freq = SNAP_FREQUENCIES[index].toDouble()

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = t / durationSec
            val envelope = exp(-progress * 6.0)
            val fundamental = sin(TWO_PI * freq * t)
            val second = 0.35 * sin(TWO_PI * freq * 2.0 * t) * exp(-progress * 8.0)
            val third = 0.15 * sin(TWO_PI * freq * 3.0 * t) * exp(-progress * 12.0)
            val sparkle = 0.10 * sin(TWO_PI * freq * 4.0 * t) * exp(-progress * 16.0)
            val sample = (fundamental + second + third + sparkle) * envelope * 0.70
            val clamped = (sample * Short.MAX_VALUE).toInt().coerceIn(
                Short.MIN_VALUE.toInt(),
                Short.MAX_VALUE.toInt()
            )
            buffer[i] = clamped.toShort()
        }
        return buffer
    }

    /**
     * Royal triumphant celebration fanfare arpeggio (~1.3s).
     */
    fun synthesizeWinFanfare(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 1.30f
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sample = computeFanfareSample(t)
            val clamped = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt()
            buffer[i] = clamped.toShort()
        }
        return buffer
    }

    private fun computeFanfareSample(t: Double): Double {
        return when {
            t < 0.14 -> computeNoteVoice(t, 0.00, 0.14, FANFARE_FREQUENCIES[0])
            t < 0.28 -> computeNoteVoice(t, 0.14, 0.28, FANFARE_FREQUENCIES[1])
            t < 0.42 -> computeNoteVoice(t, 0.28, 0.42, FANFARE_FREQUENCIES[2])
            t < 0.60 -> computeNoteVoice(t, 0.42, 0.60, FANFARE_FREQUENCIES[3])
            else -> {
                val progress = (t - 0.60) / 0.70
                val tremolo = 0.9 + 0.1 * sin(TWO_PI * 6.0 * t)
                val env = exp(-progress * 2.8) * tremolo
                val f = FANFARE_FREQUENCIES[3]
                val voice1 = sin(TWO_PI * f * t)
                val voice2 = 0.4 * sin(TWO_PI * (f * 1.5) * t)
                val voice3 = 0.3 * sin(TWO_PI * (f * 2.0) * t)
                (voice1 + voice2 + voice3) * env * 0.45
            }
        }
    }

    private fun computeNoteVoice(t: Double, start: Double, end: Double, freq: Double): Double {
        val noteT = t - start
        val noteDur = end - start
        val progress = noteT / noteDur
        val env = exp(-progress * 5.0)
        val voice1 = sin(TWO_PI * freq * t)
        val voice2 = 0.4 * sin(TWO_PI * (freq * 1.5) * t)
        val voice3 = 0.3 * sin(TWO_PI * (freq * 2.0) * t)
        return (voice1 + voice2 + voice3) * env * 0.45
    }

    /**
     * Soothing, bedtime-safe storybook lullaby loop (~12 seconds, loops seamlessly).
     */
    fun synthesizeStorybookMelody(sampleRate: Int = SAMPLE_RATE): ShortArray {
        val durationSec = 12.0
        val numSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(numSamples)
        val noteDuration = durationSec / MELODY_NOTES.size

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val noteIndex = (t / noteDuration).toInt().coerceIn(0, MELODY_NOTES.lastIndex)
            val noteT = t - (noteIndex * noteDuration)
            val noteProgress = noteT / noteDuration

            val f = MELODY_NOTES[noteIndex]
            val envelope = sin(PI * noteProgress.coerceIn(0.0, 1.0)) * exp(-noteProgress * 1.8)
            val voice1 = sin(TWO_PI * f * t)
            val voice2 = 0.25 * sin(TWO_PI * (f * 2.0) * t)
            val voice3 = 0.10 * sin(TWO_PI * (f * 3.0) * t)

            val bassFreq = if (noteIndex < 8) 130.81 else 110.0
            val bass = 0.15 * sin(TWO_PI * bassFreq * t)

            val sample = ((voice1 + voice2 + voice3) * envelope * 0.40 + bass * 0.15) * 0.50
            val clamped = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt()
            buffer[i] = clamped.toShort()
        }
        return buffer
    }
}

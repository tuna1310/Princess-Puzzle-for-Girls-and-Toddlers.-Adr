package com.princesspuzzle.storybook.audio

/**
 * Procedural audio playback contract including iOS wheel ticks and bubble pops.
 */
@Suppress("TooManyFunctions")
interface AudioEngine {
    fun playWheelRotateTick()

    fun playPickup()

    fun playReturnBounce()

    fun playSnap(placedCount: Int, totalCount: Int)

    fun playBubblePop(pitchIndex: Int)

    fun playWinFanfare()

    fun playTap()

    fun setAudioPreferences(
        soundEnabled: Boolean,
        musicEnabled: Boolean,
        soundVolume: Float = 1.0f,
        musicVolume: Float = 0.5f
    )

    fun pauseMusic()

    fun resumeMusic()

    fun release()
}

object NoOpAudioEngine : AudioEngine {
    override fun playWheelRotateTick() = Unit

    override fun playPickup() = Unit

    override fun playReturnBounce() = Unit

    override fun playSnap(placedCount: Int, totalCount: Int) = Unit

    override fun playBubblePop(pitchIndex: Int) = Unit

    override fun playWinFanfare() = Unit

    override fun playTap() = Unit

    override fun setAudioPreferences(
        soundEnabled: Boolean,
        musicEnabled: Boolean,
        soundVolume: Float,
        musicVolume: Float
    ) = Unit

    override fun pauseMusic() = Unit

    override fun resumeMusic() = Unit

    override fun release() = Unit
}

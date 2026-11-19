package com.princesspuzzle.storybook.audio

/**
 * Procedural audio playback contract.
 */
interface AudioEngine {
    fun playPickup()

    fun playReturnBounce()

    fun playSnap(placedCount: Int, totalCount: Int)

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
    override fun playPickup() = Unit

    override fun playReturnBounce() = Unit

    override fun playSnap(placedCount: Int, totalCount: Int) = Unit

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

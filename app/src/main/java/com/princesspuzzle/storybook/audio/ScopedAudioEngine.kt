package com.princesspuzzle.storybook.audio

/**
 * Scoped audio engine wrapper forwarding playback while preserving shared lifecycle.
 */
class ScopedAudioEngine(
    private val delegate: AudioEngine
) : AudioEngine {
    override fun playWheelRotateTick() = delegate.playWheelRotateTick()

    override fun playPickup() = delegate.playPickup()

    override fun playReturnBounce() = delegate.playReturnBounce()

    override fun playSnap(placedCount: Int, totalCount: Int) = delegate.playSnap(placedCount, totalCount)

    override fun playBubblePop(pitchIndex: Int) = delegate.playBubblePop(pitchIndex)

    override fun playWinFanfare() = delegate.playWinFanfare()

    override fun playTap() = delegate.playTap()

    override fun setAudioPreferences(
        soundEnabled: Boolean,
        musicEnabled: Boolean,
        soundVolume: Float,
        musicVolume: Float
    ) = delegate.setAudioPreferences(soundEnabled, musicEnabled, soundVolume, musicVolume)

    override fun pauseMusic() = delegate.pauseMusic()

    override fun resumeMusic() = delegate.resumeMusic()

    override fun release() {
        // Scoped release: never terminate the application-wide AudioEngine when a level ends.
    }
}

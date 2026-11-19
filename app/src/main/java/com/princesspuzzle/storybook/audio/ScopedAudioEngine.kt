package com.princesspuzzle.storybook.audio

/**
 * Scoped audio engine wrapper passed into [com.princesspuzzle.storybook.puzzle.PuzzleViewModel].
 * Forwards all playback and control methods to the underlying [delegate], but ensures
 * that ViewModel cleanup calls to [release] do not terminate the shared application-level audio engine.
 */
class ScopedAudioEngine(
    private val delegate: AudioEngine
) : AudioEngine {
    override fun playPickup() = delegate.playPickup()

    override fun playReturnBounce() = delegate.playReturnBounce()

    override fun playSnap(placedCount: Int, totalCount: Int) = delegate.playSnap(placedCount, totalCount)

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
        // Scoped release: do not destroy the shared parent audio engine when a single puzzle completes.
    }
}

package com.princesspuzzle.storybook.audio

/**
 * Procedural audio playback contract (implemented with AudioTrack synthesis in Step 6).
 */
interface AudioEngine {
    fun playPickup()

    fun playReturnBounce()

    fun playSnap(placedCount: Int, totalCount: Int)

    fun playWinFanfare()

    fun playTap()

    fun release()
}

object NoOpAudioEngine : AudioEngine {
    override fun playPickup() = Unit

    override fun playReturnBounce() = Unit

    override fun playSnap(placedCount: Int, totalCount: Int) = Unit

    override fun playWinFanfare() = Unit

    override fun playTap() = Unit

    override fun release() = Unit
}

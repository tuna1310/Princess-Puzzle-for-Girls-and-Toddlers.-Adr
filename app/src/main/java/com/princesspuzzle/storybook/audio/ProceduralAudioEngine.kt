package com.princesspuzzle.storybook.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log

/**
 * 100% Android Native procedural audio engine using [AudioTrack] in static mode.
 * Generates all sound effects and background music via [ProceduralAudioSynth] without
 * any external media files, providing zero latency and minimal memory overhead.
 */
class ProceduralAudioEngine : AudioEngine {
    private val lock = Any()

    @Volatile
    private var soundEnabled: Boolean = true

    @Volatile
    private var musicEnabled: Boolean = true

    @Volatile
    private var soundVolume: Float = 1.0f

    @Volatile
    private var musicVolume: Float = 0.50f

    @Volatile
    private var isReleased: Boolean = false

    private var tapTrack: AudioTrack? = null
    private var pickupTrack: AudioTrack? = null
    private var bounceTrack: AudioTrack? = null
    private val snapTracks = mutableListOf<AudioTrack>()
    private var fanfareTrack: AudioTrack? = null
    private var musicTrack: AudioTrack? = null

    init {
        initAudioTracks()
    }

    private fun initAudioTracks() {
        try {
            val tapData = ProceduralAudioSynth.synthesizeTap()
            tapTrack = buildStaticTrack(tapData, AudioAttributes.CONTENT_TYPE_SONIFICATION)

            val pickupData = ProceduralAudioSynth.synthesizePickup()
            pickupTrack = buildStaticTrack(pickupData, AudioAttributes.CONTENT_TYPE_SONIFICATION)

            val bounceData = ProceduralAudioSynth.synthesizeReturnBounce()
            bounceTrack = buildStaticTrack(bounceData, AudioAttributes.CONTENT_TYPE_SONIFICATION)

            for (i in 0 until ProceduralAudioSynth.SNAP_FREQUENCIES.size) {
                val snapData = ProceduralAudioSynth.synthesizeSnap(i)
                val track = buildStaticTrack(snapData, AudioAttributes.CONTENT_TYPE_SONIFICATION)
                if (track != null) {
                    snapTracks.add(track)
                }
            }

            val fanfareData = ProceduralAudioSynth.synthesizeWinFanfare()
            fanfareTrack = buildStaticTrack(fanfareData, AudioAttributes.CONTENT_TYPE_SONIFICATION)

            val musicData = ProceduralAudioSynth.synthesizeStorybookMelody()
            musicTrack = buildStaticTrack(musicData, AudioAttributes.CONTENT_TYPE_MUSIC)
            musicTrack?.let { track ->
                track.setLoopPoints(0, musicData.size, -1)
                track.setVolume(if (musicEnabled) musicVolume else 0f)
                if (musicEnabled) {
                    track.play()
                }
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "AudioTrack initialization failed due to state", e)
        } catch (e: UnsupportedOperationException) {
            Log.w(TAG, "AudioTrack unsupported on this environment", e)
        }
    }

    private fun buildStaticTrack(pcmData: ShortArray, contentType: Int): AudioTrack? {
        return try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(contentType)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(ProceduralAudioSynth.SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(pcmData.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmData, 0, pcmData.size)
            track
        } catch (e: UnsupportedOperationException) {
            Log.w(TAG, "Audio format or track mode unsupported", e)
            null
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Illegal audio buffer arguments", e)
            null
        }
    }

    private fun playStaticTrack(track: AudioTrack?, volume: Float) {
        if (!soundEnabled || isReleased || track == null) return
        try {
            synchronized(lock) {
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.pause()
                    track.reloadStaticData()
                    track.setVolume(volume.coerceIn(0f, 1f))
                    track.play()
                }
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Error playing sound track due to state", e)
        }
    }

    override fun playTap() {
        playStaticTrack(tapTrack, soundVolume * 0.7f)
    }

    override fun playPickup() {
        playStaticTrack(pickupTrack, soundVolume * 0.85f)
    }

    override fun playReturnBounce() {
        playStaticTrack(bounceTrack, soundVolume * 0.9f)
    }

    override fun playSnap(placedCount: Int, totalCount: Int) {
        if (snapTracks.isEmpty()) return
        val index = (placedCount - 1).coerceIn(0, snapTracks.lastIndex)
        playStaticTrack(snapTracks[index], soundVolume)
    }

    override fun playWinFanfare() {
        playStaticTrack(fanfareTrack, soundVolume)
    }

    override fun setAudioPreferences(
        soundEnabled: Boolean,
        musicEnabled: Boolean,
        soundVolume: Float,
        musicVolume: Float
    ) {
        this.soundEnabled = soundEnabled
        this.musicEnabled = musicEnabled
        this.soundVolume = soundVolume.coerceIn(0f, 1f)
        this.musicVolume = musicVolume.coerceIn(0f, 1f)
        updateMusicPlayback()
    }

    override fun pauseMusic() {
        try {
            synchronized(lock) {
                musicTrack?.takeIf { it.state == AudioTrack.STATE_INITIALIZED }?.pause()
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Error pausing music", e)
        }
    }

    override fun resumeMusic() {
        if (musicEnabled && !isReleased) {
            updateMusicPlayback()
        }
    }

    private fun updateMusicPlayback() {
        try {
            synchronized(lock) {
                val track = musicTrack ?: return
                if (track.state != AudioTrack.STATE_INITIALIZED) return
                if (musicEnabled) {
                    track.setVolume(musicVolume)
                    if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        track.play()
                    }
                } else {
                    track.setVolume(0f)
                    track.pause()
                }
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Error updating music playback", e)
        }
    }

    override fun release() {
        synchronized(lock) {
            if (isReleased) return
            isReleased = true
            try {
                tapTrack?.release()
                pickupTrack?.release()
                bounceTrack?.release()
                snapTracks.forEach { it.release() }
                snapTracks.clear()
                fanfareTrack?.release()
                musicTrack?.release()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Error releasing AudioTracks", e)
            } finally {
                tapTrack = null
                pickupTrack = null
                bounceTrack = null
                fanfareTrack = null
                musicTrack = null
            }
        }
    }

    companion object {
        private const val TAG = "ProceduralAudio"
    }
}

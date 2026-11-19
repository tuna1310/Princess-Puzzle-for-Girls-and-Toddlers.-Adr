package com.princesspuzzle.storybook.audio

import android.content.Context
import android.util.Log

/**
 * Provides a managed instance of [AudioEngine] across the app lifecycle.
 */
object AudioEngineProvider {
    private const val TAG = "AudioEngineProvider"

    @Volatile
    private var instance: AudioEngine? = null
    private val lock = Any()

    fun get(@Suppress("UnusedParameter") context: Context? = null): AudioEngine {
        return instance ?: synchronized(lock) {
            instance ?: try {
                ProceduralAudioEngine()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Audio hardware unavailable, falling back to NoOpAudioEngine", e)
                NoOpAudioEngine
            } catch (e: SecurityException) {
                Log.w(TAG, "Security exception initializing audio, falling back to NoOpAudioEngine", e)
                NoOpAudioEngine
            }.also { instance = it }
        }
    }

    /**
     * Creates a scoped engine wrapper safe for passing into ViewModels.
     */
    fun getScoped(context: Context? = null): AudioEngine {
        return ScopedAudioEngine(get(context))
    }

    fun release() {
        synchronized(lock) {
            instance?.release()
            instance = null
        }
    }
}

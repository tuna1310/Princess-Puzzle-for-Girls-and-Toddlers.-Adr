package com.princesspuzzle.storybook

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.princesspuzzle.storybook.audio.AudioEngineProvider
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.ui.PrincessPuzzleNavHost
import com.princesspuzzle.storybook.ui.theme.PrincessPuzzleTheme

private const val EXTRA_BENCHMARK_LEVEL_ID = "benchmark_level_id"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        applyImmersiveFullscreen()

        val benchmarkLevelId = if (BuildConfig.ALLOW_BENCHMARK_INTENT) {
            intent?.getIntExtra(EXTRA_BENCHMARK_LEVEL_ID, 0)?.takeIf { id ->
                id in 1..PuzzleLevels.TOTAL_COUNT
            }
        } else {
            null
        }

        setContent {
            PrincessPuzzleTheme {
                PrincessPuzzleNavHost(initialBenchmarkLevelId = benchmarkLevelId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AudioEngineProvider.get(this).resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        AudioEngineProvider.get(this).pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioEngineProvider.release()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveFullscreen()
        }
    }

    private fun applyImmersiveFullscreen() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

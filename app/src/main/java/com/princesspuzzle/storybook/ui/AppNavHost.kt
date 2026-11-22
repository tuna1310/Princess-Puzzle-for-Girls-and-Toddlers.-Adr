package com.princesspuzzle.storybook.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.princesspuzzle.storybook.BuildConfig
import com.princesspuzzle.storybook.art.IosIllustrationRenderer
import com.princesspuzzle.storybook.audio.AudioEngine
import com.princesspuzzle.storybook.audio.AudioEngineProvider
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.data.DataStorePurchaseService
import com.princesspuzzle.storybook.data.ProgressRepository
import com.princesspuzzle.storybook.data.ProgressRepositoryProvider
import com.princesspuzzle.storybook.puzzle.GameplayViewModel
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.puzzle.WheelViewModel
import com.princesspuzzle.storybook.ui.components.ParentalGateModal
import com.princesspuzzle.storybook.ui.screens.PlanetWheelScreen
import com.princesspuzzle.storybook.ui.screens.PuzzleGameplayScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private sealed interface RootScreenRoute {
    data object PlanetWheel : RootScreenRoute
    data class Gameplay(val levelId: Int) : RootScreenRoute
}

@Composable
fun PrincessPuzzleNavHost(initialBenchmarkLevelId: Int? = null) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val progressRepository = remember(context) { ProgressRepositoryProvider.get(context) }
    val purchaseService = remember(progressRepository) { DataStorePurchaseService(progressRepository) }
    val audioEngine = remember(context) { AudioEngineProvider.get(context) }
    val progressState by progressRepository.progressFlow.collectAsState(initial = AppProgressState())

    var debugUnlockAll by remember { mutableStateOf(BuildConfig.DEBUG && BuildConfig.UNLOCK_ALL_FOR_DEBUG) }
    var showParentalGateModal by remember { mutableStateOf(false) }
    var activeRoute by remember(initialBenchmarkLevelId) {
        mutableStateOf<RootScreenRoute>(
            if (initialBenchmarkLevelId != null) {
                RootScreenRoute.Gameplay(initialBenchmarkLevelId)
            } else {
                RootScreenRoute.PlanetWheel
            }
        )
    }

    val wheelViewModel = remember {
        WheelViewModel(levels = PuzzleLevels.ALL, initialWheelIndex = progressState.lastSelectedWheelIndex)
    }
    val gameplayViewModel = remember { GameplayViewModel() }

    LaunchedEffect(progressState.lastSelectedWheelIndex) {
        if (!wheelViewModel.isDragging && activeRoute == RootScreenRoute.PlanetWheel) {
            wheelViewModel.focusLevel(progressState.lastSelectedWheelIndex)
        }
    }
    LaunchedEffect(progressState.soundEnabled, progressState.musicEnabled) {
        audioEngine.setAudioPreferences(progressState.soundEnabled, progressState.musicEnabled)
    }

    val openLevelOrGate: (Int) -> Unit = { levelId ->
        val level = PuzzleLevels.getById(levelId)
        val unlocked = wheelViewModel.isLevelUnlocked(level, progressState, debugUnlockAll)
        audioEngine.playTap()
        if (!unlocked) {
            showParentalGateModal = true
        } else {
            IosIllustrationRenderer.clearAll()
            gameplayViewModel.loadLevel(level, progressState.inProgressPiecesByLevel[level.id].orEmpty())
            wheelViewModel.focusLevelById(level.id)
            scope.launch { progressRepository.setLastSelectedWheelIndex(level.index) }
            activeRoute = RootScreenRoute.Gameplay(level.id)
        }
    }

    when (val route = activeRoute) {
        RootScreenRoute.PlanetWheel -> WheelDestination(
            wheelViewModel = wheelViewModel,
            progressState = progressState,
            debugUnlockAll = debugUnlockAll,
            onToggleDebugUnlock = {
                if (BuildConfig.DEBUG) {
                    debugUnlockAll = !debugUnlockAll
                    audioEngine.playTap()
                }
            },
            onOpenParentalGate = {
                audioEngine.playTap()
                showParentalGateModal = true
            },
            onLaunchLevel = openLevelOrGate,
            audioEngine = audioEngine,
            progressRepository = progressRepository,
            scope = scope,
            view = view
        )
        is RootScreenRoute.Gameplay -> GameplayDestination(
            levelId = route.levelId,
            gameplayViewModel = gameplayViewModel,
            progressState = progressState,
            audioEngine = audioEngine,
            progressRepository = progressRepository,
            scope = scope,
            view = view,
            onBackToWheel = { activeRoute = RootScreenRoute.PlanetWheel },
            onNextPuzzle = openLevelOrGate
        )
    }

    if (showParentalGateModal) {
        ParentalGateModal(
            progressState = progressState,
            onUnlockAllPurchase = {
                audioEngine.playWinFanfare()
                scope.launch { purchaseService.purchaseUnlockAll() }
            },
            onRestorePurchase = {
                audioEngine.playTap()
                purchaseService.isUnlockedAll()
            },
            onSetSoundEnabled = { enabled -> scope.launch { progressRepository.setSoundEnabled(enabled) } },
            onSetMusicEnabled = { enabled -> scope.launch { progressRepository.setMusicEnabled(enabled) } },
            onRelockPack = {
                audioEngine.playTap()
                scope.launch { purchaseService.resetSimulatedPurchase() }
            },
            onResetProgress = {
                audioEngine.playTap()
                scope.launch { progressRepository.resetProgress() }
            },
            onDismiss = { showParentalGateModal = false }
        )
    }
}

@Composable
private fun WheelDestination(
    wheelViewModel: WheelViewModel,
    progressState: AppProgressState,
    debugUnlockAll: Boolean,
    onToggleDebugUnlock: () -> Unit,
    onOpenParentalGate: () -> Unit,
    onLaunchLevel: (Int) -> Unit,
    audioEngine: AudioEngine,
    progressRepository: ProgressRepository,
    scope: CoroutineScope,
    view: View
) {
    PlanetWheelScreen(
        wheelViewModel = wheelViewModel,
        progressState = progressState,
        debugUnlockAll = debugUnlockAll,
        onToggleDebugUnlock = onToggleDebugUnlock,
        onWheelTick = {
            audioEngine.playWheelRotateTick()
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        },
        onFocusIndexChanged = { idx -> scope.launch { progressRepository.setLastSelectedWheelIndex(idx) } },
        onToggleSound = {
            audioEngine.playTap()
            scope.launch { progressRepository.setSoundEnabled(!progressState.soundEnabled) }
        },
        onOpenParentalGate = onOpenParentalGate,
        onLaunchLevel = { selected -> onLaunchLevel(selected.id) }
    )
}

@Composable
private fun GameplayDestination(
    levelId: Int,
    gameplayViewModel: GameplayViewModel,
    progressState: AppProgressState,
    audioEngine: AudioEngine,
    progressRepository: ProgressRepository,
    scope: CoroutineScope,
    view: View,
    onBackToWheel: () -> Unit,
    onNextPuzzle: (Int) -> Unit
) {
    val currentLevel = PuzzleLevels.getById(levelId)
    LaunchedEffect(currentLevel.id) {
        if (gameplayViewModel.currentLevel.id != currentLevel.id) {
            val restored = progressState.inProgressPiecesByLevel[currentLevel.id].orEmpty()
            gameplayViewModel.loadLevel(currentLevel, restored)
        }
    }
    BackHandler {
        val savedIds = gameplayViewModel.placedPieceIds.toSet()
        scope.launch { progressRepository.saveInProgressPieces(levelId, savedIds) }
        audioEngine.playTap()
        onBackToWheel()
    }
    PuzzleGameplayScreen(
        level = currentLevel,
        gameplayViewModel = gameplayViewModel,
        onPiecePickup = {
            audioEngine.playPickup()
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        },
        onPieceReturned = { audioEngine.playReturnBounce() },
        onPieceSnapped = { placed, total, isWinNow, stars ->
            audioEngine.playSnap(placed, total)
            val snapHaptic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
            view.performHapticFeedback(snapHaptic)
            if (isWinNow) {
                audioEngine.playWinFanfare()
                scope.launch {
                    progressRepository.recordLevelCompletion(currentLevel.id, stars)
                    progressRepository.clearInProgressPieces(currentLevel.id)
                }
            }
        },
        onPiecesProgressSaved = { placedIds ->
            if (!gameplayViewModel.isCompleted) {
                scope.launch { progressRepository.saveInProgressPieces(currentLevel.id, placedIds) }
            }
        },
        onBubblePopped = { tintIdx ->
            audioEngine.playBubblePop(tintIdx)
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        },
        onBackToWheel = {
            audioEngine.playTap()
            onBackToWheel()
        },
        onNextPuzzle = { onNextPuzzle(PuzzleLevels.nextCircularAfter(currentLevel.index).id) }
    )
}

package com.princesspuzzle.storybook.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.princesspuzzle.storybook.BuildConfig
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.data.ProgressRepository
import com.princesspuzzle.storybook.data.ProgressRepositoryProvider
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.puzzle.PuzzleViewModel
import com.princesspuzzle.storybook.puzzle.PuzzleViewModelFactory
import com.princesspuzzle.storybook.ui.screens.HomeScreen
import com.princesspuzzle.storybook.ui.screens.LevelSelectScreen
import com.princesspuzzle.storybook.ui.screens.PuzzleScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch

internal sealed interface NextLevelDestination {
    data class OpenPuzzle(val levelId: Int) : NextLevelDestination
    data class BlockedLocked(val levelId: Int) : NextLevelDestination
    data object ReturnToLevelSelect : NextLevelDestination
}

internal fun resolveNextLevelDestination(
    currentLevelId: Int,
    progressState: AppProgressState,
    debugUnlockAll: Boolean
): NextLevelDestination {
    val next = PuzzleLevels.nextAfter(currentLevelId) ?: return NextLevelDestination.ReturnToLevelSelect
    val unlocked = PuzzleLevels.isUnlocked(
        level = next,
        unlockedAll = progressState.unlockedAll,
        extraUnlockedIds = progressState.unlockedLevelIds,
        debugUnlockAll = debugUnlockAll
    )
    return if (unlocked) {
        NextLevelDestination.OpenPuzzle(next.id)
    } else {
        NextLevelDestination.BlockedLocked(next.id)
    }
}

internal fun isRouteLevelAccessible(
    levelId: Int,
    progressState: AppProgressState,
    debugUnlockAll: Boolean,
    benchmarkLevelId: Int? = null
): Boolean {
    val benchmarkAllowed = benchmarkLevelId != null && benchmarkLevelId == levelId
    val level = PuzzleLevels.ALL.firstOrNull { it.id == levelId }
    val levelAllowed = level != null && PuzzleLevels.isUnlocked(
        level = level,
        unlockedAll = progressState.unlockedAll,
        extraUnlockedIds = progressState.unlockedLevelIds,
        debugUnlockAll = debugUnlockAll
    )
    return benchmarkAllowed || levelAllowed
}

private object Routes {
    const val HOME = "home"
    const val LEVEL_SELECT = "level_select"
    const val ARG_LEVEL_ID = "levelId"
    const val PUZZLE_PLAY_TEMPLATE = "puzzle_play/{$ARG_LEVEL_ID}"

    fun puzzlePlay(levelId: Int): String = "puzzle_play/$levelId"
}

@Composable
fun PrincessPuzzleNavHost(initialBenchmarkLevelId: Int? = null) {
    val context = LocalContext.current
    val progressRepository = remember(context) { ProgressRepositoryProvider.get(context) }
    val progressState by progressRepository.progressFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val debugUnlockAll = BuildConfig.DEBUG && BuildConfig.UNLOCK_ALL_FOR_DEBUG
    var lockedNoticeLevelId by remember { mutableStateOf<Int?>(null) }
    val startRoute = remember(initialBenchmarkLevelId) {
        if (initialBenchmarkLevelId != null) Routes.puzzlePlay(initialBenchmarkLevelId) else Routes.HOME
    }
    val navigateBackToLevels: () -> Unit = {
        if (!navController.popBackStack(Routes.LEVEL_SELECT, inclusive = false)) {
            navController.navigate(Routes.LEVEL_SELECT) {
                popUpTo(Routes.HOME) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                progressState = progressState,
                onPlayClick = {
                    navController.navigate(Routes.LEVEL_SELECT) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.LEVEL_SELECT) {
            LevelSelectScreen(
                progressState = progressState,
                onBackHome = { navController.popBackStack() },
                onSelectLevel = { levelId ->
                    val accessible = isRouteLevelAccessible(
                        levelId = levelId,
                        progressState = progressState,
                        debugUnlockAll = debugUnlockAll
                    )
                    if (accessible) {
                        lockedNoticeLevelId = null
                        scope.launch(NonCancellable) { progressRepository.setLastSelectedLevelId(levelId) }
                        navController.navigate(Routes.puzzlePlay(levelId)) {
                            launchSingleTop = true
                        }
                    } else {
                        lockedNoticeLevelId = levelId
                    }
                },
                debugUnlockAll = debugUnlockAll,
                lockedNoticeLevelId = lockedNoticeLevelId,
                onShowLockedNotice = { lockedNoticeLevelId = it },
                onDismissLockedNotice = { lockedNoticeLevelId = null }
            )
        }

        composable(
            route = Routes.PUZZLE_PLAY_TEMPLATE,
            arguments = listOf(navArgument(Routes.ARG_LEVEL_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt(Routes.ARG_LEVEL_ID) ?: 1
            val canAccess = isRouteLevelAccessible(
                levelId = levelId,
                progressState = progressState,
                debugUnlockAll = debugUnlockAll,
                benchmarkLevelId = initialBenchmarkLevelId
            )
            if (!canAccess) {
                LaunchedEffect(levelId) {
                    lockedNoticeLevelId = levelId
                    navigateBackToLevels()
                }
            } else {
                PuzzlePlayDestination(
                    levelId = levelId,
                    owner = backStackEntry,
                    progressRepository = progressRepository,
                    debugUnlockAll = debugUnlockAll,
                    scope = scope,
                    navController = navController,
                    onNavigateBackToLevels = navigateBackToLevels,
                    onLockedRedirect = { lockedId ->
                        lockedNoticeLevelId = lockedId
                        navigateBackToLevels()
                    }
                )
            }
        }
    }
}

@Composable
private fun PuzzlePlayDestination(
    levelId: Int,
    owner: ViewModelStoreOwner,
    progressRepository: ProgressRepository,
    debugUnlockAll: Boolean,
    scope: CoroutineScope,
    navController: NavHostController,
    onNavigateBackToLevels: () -> Unit,
    onLockedRedirect: (Int) -> Unit
) {
    val puzzleViewModel: PuzzleViewModel = viewModel(
        viewModelStoreOwner = owner,
        key = "puzzle_play_$levelId",
        factory = PuzzleViewModelFactory(levelId = levelId) { completedId, stars ->
            scope.launch(NonCancellable) { progressRepository.recordLevelCompletion(completedId, stars) }
        }
    )

    PuzzleScreen(
        viewModel = puzzleViewModel,
        onNavigateBackToLevels = onNavigateBackToLevels,
        onNextLevel = {
            val destination = resolveNextLevelDestination(
                currentLevelId = levelId,
                progressState = progressRepository.progressFlow.value,
                debugUnlockAll = debugUnlockAll
            )
            when (destination) {
                is NextLevelDestination.OpenPuzzle -> {
                    scope.launch(NonCancellable) {
                        progressRepository.setLastSelectedLevelId(destination.levelId)
                    }
                    navController.navigate(Routes.puzzlePlay(destination.levelId)) {
                        popUpTo(Routes.LEVEL_SELECT) { inclusive = false }
                        launchSingleTop = true
                    }
                }
                is NextLevelDestination.BlockedLocked -> onLockedRedirect(destination.levelId)
                NextLevelDestination.ReturnToLevelSelect -> onNavigateBackToLevels()
            }
        }
    )
}

package com.princesspuzzle.storybook.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.princesspuzzle.storybook.data.ProgressRepositoryProvider
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.puzzle.PuzzleViewModel
import com.princesspuzzle.storybook.puzzle.PuzzleViewModelFactory
import com.princesspuzzle.storybook.ui.screens.HomeScreen
import com.princesspuzzle.storybook.ui.screens.LevelSelectScreen
import com.princesspuzzle.storybook.ui.screens.PuzzleScreen
import kotlinx.coroutines.launch

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
    val startRoute = remember(initialBenchmarkLevelId) {
        if (initialBenchmarkLevelId != null) Routes.puzzlePlay(initialBenchmarkLevelId) else Routes.HOME
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
                    scope.launch { progressRepository.setLastSelectedLevelId(levelId) }
                    navController.navigate(Routes.puzzlePlay(levelId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.PUZZLE_PLAY_TEMPLATE,
            arguments = listOf(navArgument(Routes.ARG_LEVEL_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getInt(Routes.ARG_LEVEL_ID) ?: 1
            val puzzleViewModel: PuzzleViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = PuzzleViewModelFactory(levelId = levelId) { completedId, stars ->
                    scope.launch { progressRepository.recordLevelCompletion(completedId, stars) }
                }
            )

            PuzzleScreen(
                viewModel = puzzleViewModel,
                onNavigateBackToLevels = { navController.popBackStack() },
                onNextLevel = {
                    val next = PuzzleLevels.nextAfter(levelId)
                    val curProgress = progressRepository.progressFlow.value
                    val canOpenNext = next != null && PuzzleLevels.isUnlocked(
                        level = next,
                        unlockedAll = curProgress.unlockedAll,
                        extraUnlockedIds = curProgress.unlockedLevelIds
                    )
                    if (canOpenNext) {
                        scope.launch { progressRepository.setLastSelectedLevelId(next.id) }
                        navController.navigate(Routes.puzzlePlay(next.id)) {
                            popUpTo(Routes.LEVEL_SELECT) { inclusive = false }
                            launchSingleTop = true
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}

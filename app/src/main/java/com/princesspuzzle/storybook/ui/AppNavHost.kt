package com.princesspuzzle.storybook.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.princesspuzzle.storybook.puzzle.PuzzleViewModel
import com.princesspuzzle.storybook.ui.screens.PuzzleScreen

private object Routes {
    const val PUZZLE_PLAY = "puzzle_play"
}

@Composable
fun PrincessPuzzleNavHost(puzzleViewModel: PuzzleViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.PUZZLE_PLAY
    ) {
        composable(Routes.PUZZLE_PLAY) {
            PuzzleScreen(
                viewModel = puzzleViewModel,
                onNavigateBack = { puzzleViewModel.replay() }
            )
        }
    }
}

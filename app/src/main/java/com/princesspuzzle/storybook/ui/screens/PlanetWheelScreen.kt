@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.BuildConfig
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.IosMeadowBackdrop
import com.princesspuzzle.storybook.art.IosThemeColors
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.puzzle.LevelDefinition
import com.princesspuzzle.storybook.puzzle.WheelViewModel
import com.princesspuzzle.storybook.ui.components.CurvedBlueArrowButton
import com.princesspuzzle.storybook.ui.components.GlossyActionPill
import com.princesspuzzle.storybook.ui.components.RotatingPlanetWheel
import com.princesspuzzle.storybook.ui.components.SoundCircleButton
import kotlin.math.roundToInt

/**
 * Primary hub screen matching iOS `PlanetWheelScreenView`:
 * sunny fairy-tale meadow + interactive 16-level planet carousel +
 * top-left/top-right curved navigation arrows + bottom Play/Unlock/Sound bar.
 */
@Composable
fun PlanetWheelScreen(
    wheelViewModel: WheelViewModel,
    progressState: AppProgressState,
    debugUnlockAll: Boolean,
    onToggleDebugUnlock: () -> Unit,
    onWheelTick: () -> Unit,
    onFocusIndexChanged: (Int) -> Unit,
    onToggleSound: () -> Unit,
    onOpenParentalGate: () -> Unit,
    onLaunchLevel: (LevelDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    var wheelTickEpoch by remember { mutableIntStateOf(0) }
    var focusedIndex by remember { mutableIntStateOf(wheelViewModel.activeLevelIndex) }
    val activeLevel = wheelViewModel.levels.getOrElse(focusedIndex) { wheelViewModel.activeLevel }
    val activeUnlocked = wheelViewModel.isLevelUnlocked(activeLevel, progressState, debugUnlockAll)
    val showUnlockAllCta = !progressState.unlockedAll && !debugUnlockAll

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bg = IosMeadowBackdrop.getOrRender(
                size.width.roundToInt(),
                size.height.roundToInt()
            )
            drawImage(image = bg, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()))
        }

        RotatingPlanetWheel(
            wheelViewModel = wheelViewModel,
            progressState = progressState,
            debugUnlockAll = debugUnlockAll,
            wheelTickEpoch = wheelTickEpoch,
            onWheelTick = onWheelTick,
            onFocusIndexChanged = { newIdx ->
                focusedIndex = newIdx
                onFocusIndexChanged(newIdx)
            },
            onLaunchLevel = onLaunchLevel,
            modifier = Modifier.fillMaxSize()
        )

        TopWheelControls(
            debugUnlockAll = debugUnlockAll,
            onRotateClockwise = {
                wheelViewModel.rotateClockwise()
                focusedIndex = wheelViewModel.activeLevelIndex
                wheelTickEpoch += 1
                onWheelTick()
                onFocusIndexChanged(focusedIndex)
            },
            onRotateCounterClockwise = {
                wheelViewModel.rotateCounterClockwise()
                focusedIndex = wheelViewModel.activeLevelIndex
                wheelTickEpoch += 1
                onWheelTick()
                onFocusIndexChanged(focusedIndex)
            },
            onToggleDebugUnlock = onToggleDebugUnlock,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        BottomWheelHud(
            activeLevel = activeLevel,
            activeUnlocked = activeUnlocked,
            soundEnabled = progressState.soundEnabled,
            showUnlockAllCta = showUnlockAllCta,
            onToggleSound = onToggleSound,
            onPlayOrUnlockClick = { onLaunchLevel(activeLevel) },
            onOpenParentalGate = onOpenParentalGate,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun TopWheelControls(
    debugUnlockAll: Boolean,
    onRotateClockwise: () -> Unit,
    onRotateCounterClockwise: () -> Unit,
    onToggleDebugUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CurvedBlueArrowButton(
            clockwise = true,
            contentDescriptionText = stringResource(R.string.cd_rotate_clockwise),
            onClick = onRotateClockwise
        )

        if (BuildConfig.DEBUG) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xC8281D38))
                    .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onToggleDebugUnlock)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = stringResource(
                        if (debugUnlockAll) R.string.debug_bypass_on_chip else R.string.debug_bypass_off_chip
                    ),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }

        CurvedBlueArrowButton(
            clockwise = false,
            contentDescriptionText = stringResource(R.string.cd_rotate_counter_clockwise),
            onClick = onRotateCounterClockwise
        )
    }
}

@Composable
private fun BottomWheelHud(
    activeLevel: LevelDefinition,
    activeUnlocked: Boolean,
    soundEnabled: Boolean,
    showUnlockAllCta: Boolean,
    onToggleSound: () -> Unit,
    onPlayOrUnlockClick: () -> Unit,
    onOpenParentalGate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SoundCircleButton(
            soundEnabled = soundEnabled,
            onClick = onToggleSound,
            modifier = Modifier.size(50.dp)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFAFFFFFF), Color(0xF5FFF1F7))
                    )
                )
                .border(2.5.dp, Color.White, RoundedCornerShape(26.dp))
                .clickable(onClick = onPlayOrUnlockClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(activeLevel.titleRes),
                    color = IosThemeColors.inkPrimary,
                    fontSize = 13.5.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pluralStringResource(
                        if (activeUnlocked) R.plurals.wheel_pieces_tap_play else R.plurals.wheel_pieces_tap_unlock,
                        activeLevel.pieceCount,
                        activeLevel.pieceCount
                    ),
                    color = IosThemeColors.inkSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (activeUnlocked) {
                                listOf(IosThemeColors.pinkButtonTop, IosThemeColors.pinkButtonBottom)
                            } else {
                                listOf(IosThemeColors.goldButtonTop, IosThemeColors.goldButtonBottom)
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (activeUnlocked) Icons.Rounded.PlayArrow else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (activeUnlocked) Color.White else IosThemeColors.inkPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (showUnlockAllCta) {
            GlossyActionPill(
                label = stringResource(R.string.wheel_unlock_all),
                gradientColors = listOf(IosThemeColors.goldButtonTop, IosThemeColors.goldButtonBottom),
                textColor = IosThemeColors.inkPrimary,
                leadingIcon = Icons.Rounded.Lock,
                onClick = onOpenParentalGate
            )
        } else {
            GlossyActionPill(
                label = "",
                gradientColors = listOf(Color.White, Color(0xFFF3EBF8)),
                textColor = IosThemeColors.inkPrimary,
                leadingIcon = Icons.Rounded.Settings,
                onClick = onOpenParentalGate,
                modifier = Modifier.width(50.dp)
            )
        }
    }
}

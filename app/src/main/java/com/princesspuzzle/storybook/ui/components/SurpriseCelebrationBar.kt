@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.IosThemeColors
import com.princesspuzzle.storybook.core.GameConfig

/**
 * Victory overlay matching iOS `SurpriseCelebrationOverlayView`:
 * banner prompt + bubble pop counter + stars + Play Again / Next Puzzle controls.
 */
@Composable
fun SurpriseCelebrationBar(
    visible: Boolean,
    earnedStars: Int,
    poppedCount: Int,
    onPlayAgain: () -> Unit,
    onNextPuzzle: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xF5FFFFFF), Color(0xF2FFF0F8))
                    )
                )
                .border(2.5.dp, Color.White, RoundedCornerShape(26.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(GameConfig.MAX_STARS) { idx ->
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = if (idx < earnedStars) {
                            IosThemeColors.completedStarGold
                        } else {
                            Color(0xFFE0D4E0)
                        },
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = IosThemeColors.goldRich,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = " ${stringResource(R.string.win_bubbles_prompt)}",
                    color = IosThemeColors.inkPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (poppedCount > 0) {
                    Text(
                        text = " ${pluralStringResource(R.plurals.win_bubbles_popped, poppedCount, poppedCount)}",
                        color = IosThemeColors.pinkButtonBottom,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlossyActionPill(
                    label = stringResource(R.string.win_play_again),
                    gradientColors = listOf(IosThemeColors.greenBackTop, IosThemeColors.greenBackBottom),
                    leadingIcon = Icons.Rounded.Refresh,
                    onClick = onPlayAgain,
                    modifier = Modifier.weight(1f)
                )
                GlossyActionPill(
                    label = stringResource(R.string.win_next_puzzle),
                    gradientColors = listOf(IosThemeColors.pinkButtonTop, IosThemeColors.pinkButtonBottom),
                    leadingIcon = Icons.Rounded.PlayArrow,
                    onClick = onNextPuzzle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

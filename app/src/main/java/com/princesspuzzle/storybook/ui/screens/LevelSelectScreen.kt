package com.princesspuzzle.storybook.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.MeadowBackdropState
import com.princesspuzzle.storybook.art.SceneIllustrations
import com.princesspuzzle.storybook.art.StorybookPalette
import com.princesspuzzle.storybook.art.drawMeadowBackdrop
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.puzzle.LevelDefinition
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.ui.components.StorybookBadgePill
import com.princesspuzzle.storybook.ui.components.StorybookCircleBackButton
import com.princesspuzzle.storybook.ui.components.StorybookNoticeOverlay
import com.princesspuzzle.storybook.ui.components.StorybookPadlockBadge
import com.princesspuzzle.storybook.ui.components.StorybookStarRow
import com.princesspuzzle.storybook.ui.components.StorybookStarSummaryPill

private const val EDGE_PAD_DP = 14
private const val TOP_GAP_DP = 10
private const val GRID_MIN_CELL_DP = 152
private const val GRID_SPACING_DP = 12
private const val CARD_CORNER_DP = 20
private const val CARD_BORDER_DP = 2.5f
private const val CARD_SHADOW_DP = 4
private const val CARD_PAD_DP = 10
private const val CARD_GAP_DP = 6
private const val CHIP_CORNER_DP = 10
private const val CHIP_PAD_H_DP = 8
private const val CHIP_PAD_V_DP = 3
private const val CHIP_FONT_SP = 12
private const val CHIP_BORDER_DP = 1.5f
private const val CHIP_PCS_BG = 0xD9FFFFFF
private const val THUMB_CORNER_DP = 14
private const val LOCKED_VEIL_COLOR = 0x80F6ECFF
private const val PADLOCK_SIZE_DP = 48
private const val TITLE_FONT_SP = 14
private const val TITLE_LINE_HEIGHT_SP = 17
private const val CARD_STAR_DP = 20

@Composable
fun LevelSelectScreen(
    progressState: AppProgressState,
    onBackHome: () -> Unit,
    onSelectLevel: (Int) -> Unit,
    modifier: Modifier = Modifier,
    debugUnlockAll: Boolean = false,
    lockedNoticeLevelId: Int? = null,
    onShowLockedNotice: (Int) -> Unit = {},
    onDismissLockedNotice: () -> Unit = {}
) {
    val backdropState = remember { MeadowBackdropState() }
    val gridState = rememberLazyGridState()
    var localLockedLevelId by remember { mutableStateOf<Int?>(null) }
    val activeLockedLevelId = lockedNoticeLevelId ?: localLockedLevelId

    BackHandler { onBackHome() }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMeadowBackdrop(0f, backdropState)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = EDGE_PAD_DP.dp, vertical = EDGE_PAD_DP.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TOP_GAP_DP.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StorybookCircleBackButton(
                    contentDescriptionText = stringResource(R.string.cd_back_to_home),
                    onClick = onBackHome
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    StorybookBadgePill(label = stringResource(R.string.levels_title))
                }
                StorybookStarSummaryPill(
                    earnedStars = progressState.totalStars,
                    totalStars = PuzzleLevels.MAX_TOTAL_STARS
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = GRID_MIN_CELL_DP.dp),
                state = gridState,
                contentPadding = PaddingValues(top = GRID_SPACING_DP.dp, bottom = GRID_SPACING_DP.dp),
                horizontalArrangement = Arrangement.spacedBy(GRID_SPACING_DP.dp),
                verticalArrangement = Arrangement.spacedBy(GRID_SPACING_DP.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(PuzzleLevels.ALL, key = { it.id }) { level ->
                    val unlocked = PuzzleLevels.isUnlocked(
                        level = level,
                        unlockedAll = progressState.unlockedAll,
                        extraUnlockedIds = progressState.unlockedLevelIds,
                        debugUnlockAll = debugUnlockAll
                    )
                    val earnedStars = progressState.starsFor(level.id)
                    StorybookLevelCard(
                        level = level,
                        unlocked = unlocked,
                        earnedStars = earnedStars,
                        onClick = {
                            if (unlocked) {
                                onSelectLevel(level.id)
                            } else {
                                localLockedLevelId = level.id
                                onShowLockedNotice(level.id)
                            }
                        }
                    )
                }
            }
        }

        if (activeLockedLevelId != null) {
            val lockedLevel = PuzzleLevels.ALL.firstOrNull { it.id == activeLockedLevelId }
            val noticeBody = if (lockedLevel != null) {
                stringResource(R.string.levels_locked_named_body, stringResource(lockedLevel.titleRes))
            } else {
                stringResource(R.string.levels_locked_body)
            }
            StorybookNoticeOverlay(
                title = stringResource(R.string.levels_locked_title),
                body = noticeBody,
                okLabel = stringResource(R.string.dialog_ok),
                onDismiss = {
                    localLockedLevelId = null
                    onDismissLockedNotice()
                }
            )
        }
    }
}

@Composable
private fun StorybookLevelCard(level: LevelDefinition, unlocked: Boolean, earnedStars: Int, onClick: () -> Unit) {
    val density = LocalDensity.current.density
    val thumb = remember(level.theme, density) {
        SceneIllustrations.getOrRenderThumbnail(level.theme, density)
    }
    val cardShape = RoundedCornerShape(CARD_CORNER_DP.dp)
    val thumbShape = RoundedCornerShape(THUMB_CORNER_DP.dp)
    val chipShape = RoundedCornerShape(CHIP_CORNER_DP.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CARD_GAP_DP.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(CARD_SHADOW_DP.dp, cardShape)
            .clip(cardShape)
            .background(Color(level.frameHex))
            .border(CARD_BORDER_DP.dp, StorybookPalette.Ink, cardShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(CARD_PAD_DP.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(chipShape)
                    .background(Color(level.accentHex))
                    .border(CHIP_BORDER_DP.dp, StorybookPalette.Ink, chipShape)
                    .padding(horizontal = CHIP_PAD_H_DP.dp, vertical = CHIP_PAD_V_DP.dp)
            ) {
                Text(
                    text = level.id.toString(),
                    color = StorybookPalette.Ink,
                    fontSize = CHIP_FONT_SP.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Box(
                modifier = Modifier
                    .clip(chipShape)
                    .background(Color(CHIP_PCS_BG))
                    .border(CHIP_BORDER_DP.dp, StorybookPalette.Ink, chipShape)
                    .padding(horizontal = CHIP_PAD_H_DP.dp, vertical = CHIP_PAD_V_DP.dp)
            ) {
                Text(
                    text = pluralStringResource(R.plurals.hud_pieces_badge, level.pieceCount, level.pieceCount),
                    color = StorybookPalette.Ink,
                    fontSize = CHIP_FONT_SP.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(thumbShape)
                .border(CARD_BORDER_DP.dp, StorybookPalette.Ink, thumbShape)
        ) {
            Image(
                bitmap = thumb,
                contentDescription = stringResource(level.titleRes),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (!unlocked) {
                val lockedCd = stringResource(R.string.cd_locked_level)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(LOCKED_VEIL_COLOR))
                        .semantics { contentDescription = lockedCd }
                ) {
                    StorybookPadlockBadge(modifier = Modifier.size(PADLOCK_SIZE_DP.dp))
                }
            }
        }

        Text(
            text = stringResource(level.titleRes),
            color = StorybookPalette.Ink,
            fontSize = TITLE_FONT_SP.sp,
            lineHeight = TITLE_LINE_HEIGHT_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        StorybookStarRow(
            earnedStars = earnedStars,
            starSize = CARD_STAR_DP.dp
        )
    }
}

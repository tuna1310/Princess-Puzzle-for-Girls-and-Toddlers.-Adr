package com.princesspuzzle.storybook.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.StorybookPalette
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.data.AppProgressState

private const val SCRIM_COLOR = 0x73493055
private const val CARD_CORNER_DP = 28
private const val CARD_MAX_WIDTH_DP = 380
private const val CARD_SHADOW_DP = 8
private const val CARD_BORDER_DP = 3
private const val CARD_PAD_DP = 20
private const val GAP_DP = 10
private const val TITLE_SP = 22
private const val BANNER_CORNER_DP = 16
private const val BANNER_BORDER_DP = 2
private const val BANNER_PAD_H_DP = 14
private const val BANNER_PAD_V_DP = 10
private const val BANNER_UNLOCKED_BG = 0xFFE9FBE6
private const val BANNER_LOCKED_BG = 0xFFF3ECFF
private const val BANNER_STATUS_BG = 0xFFFFF4D8
private const val BANNER_SP = 14
private const val SETTING_LABEL_SP = 16
private const val TOGGLE_MIN_W_DP = 88

data class GrownUpsActions(
    val onUnlockAll: () -> Unit,
    val onRestorePurchase: () -> Unit,
    val onRelockPack: () -> Unit,
    val onToggleSound: () -> Unit,
    val onToggleMusic: () -> Unit,
    val onResetProgress: () -> Unit,
    val onExitApp: () -> Unit
)

@Composable
fun GrownUpsSettingsOverlay(
    progressState: AppProgressState,
    statusMessage: String?,
    actions: GrownUpsActions,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmReset by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(CARD_CORNER_DP.dp)
    BackHandler { onDismiss() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(SCRIM_COLOR))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .padding(CARD_PAD_DP.dp)
    ) {
        GrownUpsCardContent(
            cardShape = cardShape,
            progressState = progressState,
            statusMessage = statusMessage,
            confirmReset = confirmReset,
            actions = actions,
            onResetClick = {
                if (confirmReset) {
                    confirmReset = false
                    actions.onResetProgress()
                } else {
                    confirmReset = true
                }
            },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun GrownUpsCardContent(
    cardShape: RoundedCornerShape,
    progressState: AppProgressState,
    statusMessage: String?,
    confirmReset: Boolean,
    actions: GrownUpsActions,
    onResetClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GAP_DP.dp),
        modifier = Modifier
            .widthIn(max = CARD_MAX_WIDTH_DP.dp)
            .fillMaxWidth()
            .shadow(CARD_SHADOW_DP.dp, cardShape)
            .clip(cardShape)
            .background(StorybookPalette.SoftWhite)
            .border(CARD_BORDER_DP.dp, StorybookPalette.Ink, cardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .verticalScroll(rememberScrollState())
            .padding(CARD_PAD_DP.dp)
    ) {
        Text(
            text = stringResource(R.string.grownups_title),
            color = StorybookPalette.Ink,
            fontSize = TITLE_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        PackStatusBanner(unlockedAll = progressState.unlockedAll)

        if (!statusMessage.isNullOrBlank()) {
            SettingsBanner(text = statusMessage, bgColor = Color(BANNER_STATUS_BG))
        }

        if (!progressState.unlockedAll) {
            StorybookPillButton(
                label = stringResource(R.string.grownups_unlock_all),
                topColor = StorybookPalette.ButtonPinkTop,
                bottomColor = StorybookPalette.ButtonPinkBottom,
                onClick = actions.onUnlockAll,
                modifier = Modifier.fillMaxWidth()
            )
        }

        StorybookPillButton(
            label = stringResource(R.string.grownups_restore_purchase),
            topColor = StorybookPalette.ButtonBlueTop,
            bottomColor = StorybookPalette.ButtonBlueBottom,
            onClick = actions.onRestorePurchase,
            modifier = Modifier.fillMaxWidth()
        )

        SettingsToggleRow(
            label = stringResource(R.string.grownups_sound_label),
            enabled = progressState.soundEnabled,
            onToggle = actions.onToggleSound
        )

        SettingsToggleRow(
            label = stringResource(R.string.grownups_music_label),
            enabled = progressState.musicEnabled,
            onToggle = actions.onToggleMusic
        )

        GrownUpsFooterActions(
            unlockedAll = progressState.unlockedAll,
            confirmReset = confirmReset,
            onRelockPackClick = actions.onRelockPack,
            onResetClick = onResetClick,
            onExitApp = actions.onExitApp,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun GrownUpsFooterActions(
    unlockedAll: Boolean,
    confirmReset: Boolean,
    onRelockPackClick: () -> Unit,
    onResetClick: () -> Unit,
    onExitApp: () -> Unit,
    onDismiss: () -> Unit
) {
    if (unlockedAll) {
        StorybookPillButton(
            label = stringResource(R.string.grownups_relock_pack),
            topColor = StorybookPalette.SoftWhite,
            bottomColor = StorybookPalette.PetalPink,
            onClick = onRelockPackClick,
            modifier = Modifier.fillMaxWidth()
        )
    }

    StorybookPillButton(
        label = if (confirmReset) {
            stringResource(R.string.grownups_reset_confirm)
        } else {
            stringResource(R.string.grownups_reset_progress)
        },
        topColor = StorybookPalette.SoftWhite,
        bottomColor = if (confirmReset) StorybookPalette.ButtonPinkBottom else StorybookPalette.PetalPink,
        onClick = onResetClick,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        StorybookPillButton(
            label = stringResource(R.string.grownups_exit_app),
            topColor = StorybookPalette.SoftWhite,
            bottomColor = StorybookPalette.ButtonBlueBottom,
            onClick = onExitApp,
            modifier = Modifier.weight(1f)
        )
        StorybookPillButton(
            label = stringResource(R.string.grownups_close),
            topColor = StorybookPalette.ButtonPinkTop,
            bottomColor = StorybookPalette.ButtonPinkBottom,
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PackStatusBanner(unlockedAll: Boolean) {
    SettingsBanner(
        text = if (unlockedAll) {
            stringResource(R.string.grownups_pack_unlocked)
        } else {
            stringResource(R.string.grownups_pack_locked)
        },
        bgColor = Color(if (unlockedAll) BANNER_UNLOCKED_BG else BANNER_LOCKED_BG)
    )
}

@Composable
private fun SettingsBanner(text: String, bgColor: Color) {
    val shape = RoundedCornerShape(BANNER_CORNER_DP.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .border(BANNER_BORDER_DP.dp, StorybookPalette.Ink, shape)
            .padding(horizontal = BANNER_PAD_H_DP.dp, vertical = BANNER_PAD_V_DP.dp)
    ) {
        Text(
            text = text,
            color = StorybookPalette.Ink,
            fontSize = BANNER_SP.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettingsToggleRow(label: String, enabled: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = StorybookPalette.Ink,
            fontSize = SETTING_LABEL_SP.sp,
            fontWeight = FontWeight.ExtraBold
        )
        StorybookPillButton(
            label = if (enabled) {
                stringResource(R.string.grownups_toggle_on)
            } else {
                stringResource(R.string.grownups_toggle_off)
            },
            topColor = if (enabled) StorybookPalette.ButtonPinkTop else StorybookPalette.SoftWhite,
            bottomColor = if (enabled) StorybookPalette.ButtonPinkBottom else StorybookPalette.ButtonBlueBottom,
            minHeightDp = GameConfig.MIN_BUTTON_TOUCH_TARGET_DP.dp,
            onClick = onToggle,
            modifier = Modifier.defaultMinSize(minWidth = TOGGLE_MIN_W_DP.dp)
        )
    }
}

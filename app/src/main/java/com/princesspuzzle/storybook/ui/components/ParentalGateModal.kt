@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.ui.components

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.IosThemeColors
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.puzzle.ParentalChallenge
import com.princesspuzzle.storybook.puzzle.ParentalGateStep
import com.princesspuzzle.storybook.puzzle.StoreStatusNotice

/**
 * Two-step Parental Gate & simulated Unlock Store + Grown-Up Audio/Reset Sheet
 * matching iOS `ParentalGateModalView`.
 */
@Composable
fun ParentalGateModal(
    progressState: AppProgressState,
    onUnlockAllPurchase: () -> Unit,
    onRestorePurchase: () -> Boolean,
    onSetSoundEnabled: (Boolean) -> Unit,
    onSetMusicEnabled: (Boolean) -> Unit,
    onRelockPack: () -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(ParentalGateStep.CHALLENGE) }
    var challenge by remember { mutableStateOf(ParentalChallenge.generate()) }
    var wrongAnswerFlag by remember { mutableStateOf(false) }
    var storeNotice by remember { mutableStateOf(StoreStatusNotice.NONE) }
    var confirmResetPending by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x881A1026))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(0.90f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFFAFD), Color(0xFFFFF0F7))
                        )
                    )
                    .border(3.dp, Color.White, RoundedCornerShape(28.dp))
                    .clickable(enabled = false) {}
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            if (step == ParentalGateStep.CHALLENGE) {
                                R.string.parent_gate_title
                            } else {
                                R.string.store_unlock_title
                            }
                        ),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IosThemeColors.inkPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E4EE))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_close),
                            tint = IosThemeColors.inkPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (step == ParentalGateStep.CHALLENGE) {
                    ChallengeStepContent(
                        challenge = challenge,
                        showWrongHint = wrongAnswerFlag,
                        onSelectOption = { choice ->
                            if (challenge.isCorrect(choice)) {
                                wrongAnswerFlag = false
                                step = ParentalGateStep.STORE_AND_SETTINGS
                            } else {
                                wrongAnswerFlag = true
                                challenge = ParentalChallenge.generate()
                            }
                        }
                    )
                } else {
                    StoreAndSettingsContent(
                        progressState = progressState,
                        storeNotice = storeNotice,
                        confirmResetPending = confirmResetPending,
                        onUnlockAllClick = {
                            onUnlockAllPurchase()
                            storeNotice = StoreStatusNotice.UNLOCKED_ALL
                        },
                        onRestoreClick = {
                            val hadPurchase = onRestorePurchase()
                            storeNotice = if (hadPurchase) {
                                StoreStatusNotice.RESTORED_PURCHASE
                            } else {
                                StoreStatusNotice.NO_PRIOR_PURCHASE
                            }
                        },
                        onToggleSound = { onSetSoundEnabled(!progressState.soundEnabled) },
                        onToggleMusic = { onSetMusicEnabled(!progressState.musicEnabled) },
                        onRelockClick = {
                            onRelockPack()
                            storeNotice = StoreStatusNotice.RELOCKED
                        },
                        onResetClick = {
                            if (confirmResetPending) {
                                onResetProgress()
                                confirmResetPending = false
                                storeNotice = StoreStatusNotice.PROGRESS_RESET
                            } else {
                                confirmResetPending = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeStepContent(challenge: ParentalChallenge, showWrongHint: Boolean, onSelectOption: (Int) -> Unit) {
    Text(
        text = stringResource(R.string.parent_gate_subtitle),
        fontSize = 14.sp,
        color = IosThemeColors.inkSecondary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(14.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(2.dp, IosThemeColors.castlePinkLight, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.parent_gate_math_prompt, challenge.leftOperand, challenge.rightOperand),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = IosThemeColors.inkPrimary
        )
    }

    if (showWrongHint) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.parent_gate_math_wrong),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = IosThemeColors.crimson,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(14.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        challenge.options.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { option ->
                    GlossyActionPill(
                        label = option.toString(),
                        gradientColors = listOf(IosThemeColors.blueArrowTop, IosThemeColors.blueArrowBottom),
                        onClick = { onSelectOption(option) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 56.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreAndSettingsContent(
    progressState: AppProgressState,
    storeNotice: StoreStatusNotice,
    confirmResetPending: Boolean,
    onUnlockAllClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleMusic: () -> Unit,
    onRelockClick: () -> Unit,
    onResetClick: () -> Unit
) {
    val priceText = stringResource(R.string.store_price_display)
    Text(
        text = stringResource(R.string.store_unlock_body),
        fontSize = 14.sp,
        color = IosThemeColors.inkSecondary,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (!progressState.unlockedAll) {
        GlossyActionPill(
            label = stringResource(R.string.store_unlock_cta, priceText),
            gradientColors = listOf(IosThemeColors.pinkButtonTop, IosThemeColors.pinkButtonBottom),
            leadingIcon = Icons.Rounded.Star,
            onClick = onUnlockAllClick,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE7F9EB))
                .border(1.5.dp, IosThemeColors.emeraldTree, RoundedCornerShape(16.dp))
                .padding(vertical = 10.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.store_status_unlocked, priceText),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = IosThemeColors.shrubDark,
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    GlossyActionPill(
        label = stringResource(R.string.store_restore_cta),
        gradientColors = listOf(Color(0xFFF6F2FA), Color(0xFFE6DDF0)),
        textColor = IosThemeColors.inkPrimary,
        onClick = onRestoreClick,
        modifier = Modifier.fillMaxWidth()
    )

    val noticeText = when (storeNotice) {
        StoreStatusNotice.UNLOCKED_ALL -> stringResource(R.string.store_status_unlocked, priceText)
        StoreStatusNotice.RESTORED_PURCHASE -> stringResource(R.string.store_status_restored)
        StoreStatusNotice.NO_PRIOR_PURCHASE -> stringResource(R.string.store_status_no_purchase)
        StoreStatusNotice.RELOCKED -> stringResource(R.string.store_status_relocked)
        StoreStatusNotice.PROGRESS_RESET -> stringResource(R.string.store_status_reset_done)
        StoreStatusNotice.NONE -> null
    }
    if (noticeText != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = noticeText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = IosThemeColors.royalPurple,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = Color(0xFFECDCE6))
    Spacer(modifier = Modifier.height(10.dp))

    SettingToggleRow(
        label = stringResource(R.string.grownups_sound_label),
        enabled = progressState.soundEnabled,
        onClick = onToggleSound
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingToggleRow(
        label = stringResource(R.string.grownups_music_label),
        enabled = progressState.musicEnabled,
        onClick = onToggleMusic
    )

    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (progressState.unlockedAll) {
            GlossyActionPill(
                label = stringResource(R.string.grownups_relock_pack),
                gradientColors = listOf(Color(0xFFFFD8A8), Color(0xFFF59F00)),
                textColor = IosThemeColors.inkPrimary,
                onClick = onRelockClick,
                modifier = Modifier.weight(1f)
            )
        }
        GlossyActionPill(
            label = stringResource(
                if (confirmResetPending) R.string.grownups_reset_confirm else R.string.grownups_reset_progress
            ),
            gradientColors = if (confirmResetPending) {
                listOf(Color(0xFFFF6B8A), Color(0xFFD62851))
            } else {
                listOf(Color(0xFFFCE8EE), Color(0xFFF7CEDC))
            },
            textColor = if (confirmResetPending) Color.White else IosThemeColors.crimson,
            onClick = onResetClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingToggleRow(label: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = IosThemeColors.inkPrimary
        )
        GlossyActionPill(
            label = stringResource(if (enabled) R.string.grownups_toggle_on else R.string.grownups_toggle_off),
            gradientColors = if (enabled) {
                listOf(IosThemeColors.greenBackTop, IosThemeColors.greenBackBottom)
            } else {
                listOf(Color(0xFFEAE4EF), Color(0xFFD0C6D9))
            },
            textColor = if (enabled) Color.White else IosThemeColors.inkSecondary,
            onClick = onClick
        )
    }
}

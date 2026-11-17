package com.princesspuzzle.storybook.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.StorybookPalette
import com.princesspuzzle.storybook.core.GameConfig
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.isActive

private const val NANOS_PER_SEC = 1_000_000_000f
private const val MIN_FACTOR_LEFT = 3
private const val SPAN_FACTOR_LEFT = 7
private const val MIN_FACTOR_RIGHT = 4
private const val SPAN_FACTOR_RIGHT = 6
private const val MAX_INPUT_DIGITS = 3

private const val SCRIM_COLOR = 0x73493055
private const val CARD_CORNER_DP = 28
private const val CARD_MAX_WIDTH_DP = 360
private const val CARD_SHADOW_DP = 8
private const val CARD_BORDER_DP = 3
private const val CARD_PAD_DP = 20
private const val CONTENT_GAP_DP = 12
private const val TITLE_SP = 22
private const val SUBTITLE_SP = 14
private const val HOLD_CORNER_PERCENT = 50
private const val HOLD_MIN_HEIGHT_DP = 68
private const val HOLD_BORDER_DP = 2.5f
private const val HOLD_BG_COLOR = 0xFFFFF3F9
private const val HOLD_PROGRESS_COLOR = 0xFFFFB2D8
private const val HOLD_LABEL_SP = 17
private const val MATH_BOX_CORNER_DP = 16
private const val MATH_BOX_BG = 0xFFF3FBFF
private const val MATH_PROMPT_SP = 19
private const val MATH_ERROR_SP = 13
private const val MATH_ERROR_COLOR = 0xFFC24472
private const val KEY_ROW_GAP_DP = 8
private const val KEY_MIN_HEIGHT_DP = 46
private const val KEY_CORNER_DP = 14
private const val KEY_BORDER_DP = 2f
private const val KEY_FONT_SP = 16
private const val KEY_CLEAR = -1
private const val KEY_CHECK = -2
private const val DIGIT_SEVEN = 7
private const val DIGIT_EIGHT = 8
private const val DIGIT_NINE = 9
private const val DIGIT_FOUR = 4
private const val DIGIT_FIVE = 5
private const val DIGIT_SIX = 6
private const val DIGIT_ONE = 1
private const val DIGIT_TWO = 2
private const val DIGIT_THREE = 3

data class ParentalMathChallenge(val left: Int, val right: Int) {
    val expectedAnswer: Int
        get() = left * right
}

internal fun generateParentalMathChallenge(seed: Long = System.currentTimeMillis()): ParentalMathChallenge {
    val positive = abs(seed)
    val left = MIN_FACTOR_LEFT + (positive % SPAN_FACTOR_LEFT).toInt()
    val right = MIN_FACTOR_RIGHT + ((positive / SPAN_FACTOR_LEFT) % SPAN_FACTOR_RIGHT).toInt()
    return ParentalMathChallenge(left = left, right = right)
}

internal fun isParentalMathAnswerValid(challenge: ParentalMathChallenge, input: String): Boolean =
    input.trim().toIntOrNull() == challenge.expectedAnswer

internal fun computeParentalHoldProgress(heldSeconds: Float): Float =
    (heldSeconds / GameConfig.PARENTAL_GATE_HOLD_SECONDS).coerceIn(0f, 1f)

internal fun isParentalHoldSatisfied(heldSeconds: Float): Boolean = heldSeconds >= GameConfig.PARENTAL_GATE_HOLD_SECONDS

/**
 * Kid-safe Parental Gate allowing grown-ups in either by holding the star bar
 * continuously for [GameConfig.PARENTAL_GATE_HOLD_SECONDS] or solving a quick math prompt.
 */
@Composable
fun ParentalGateOverlay(onVerified: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler { onDismiss() }
    val challenge = remember { generateParentalMathChallenge() }
    var answerDigits by remember { mutableStateOf("") }
    var showWrongMath by remember { mutableStateOf(false) }
    var isHolding by remember { mutableStateOf(false) }
    var heldSeconds by remember { mutableFloatStateOf(0f) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(isHolding) {
        if (!isHolding) {
            heldSeconds = 0f
        } else {
            var lastNanos = 0L
            while (isActive && !completed) {
                withFrameNanos { nanos ->
                    if (lastNanos != 0L) {
                        val dt = ((nanos - lastNanos) / NANOS_PER_SEC)
                            .coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
                        heldSeconds += dt
                        if (isParentalHoldSatisfied(heldSeconds) && !completed) {
                            completed = true
                            onVerified()
                        }
                    }
                    lastNanos = nanos
                }
            }
        }
    }

    val cardShape = RoundedCornerShape(CARD_CORNER_DP.dp)
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
        ParentalGateCard(
            cardShape = cardShape,
            challenge = challenge,
            isHolding = isHolding,
            heldSeconds = heldSeconds,
            answerDigits = answerDigits,
            showWrongMath = showWrongMath,
            onHoldingChanged = { holding -> isHolding = holding },
            onDigit = { d ->
                showWrongMath = false
                if (answerDigits.length < MAX_INPUT_DIGITS) {
                    answerDigits += d.toString()
                }
            },
            onClear = {
                showWrongMath = false
                answerDigits = ""
            },
            onCheck = {
                if (isParentalMathAnswerValid(challenge, answerDigits) && !completed) {
                    completed = true
                    onVerified()
                } else {
                    showWrongMath = true
                    answerDigits = ""
                }
            },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ParentalGateCard(
    cardShape: RoundedCornerShape,
    challenge: ParentalMathChallenge,
    isHolding: Boolean,
    heldSeconds: Float,
    answerDigits: String,
    showWrongMath: Boolean,
    onHoldingChanged: (Boolean) -> Unit,
    onDigit: (Int) -> Unit,
    onClear: () -> Unit,
    onCheck: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CONTENT_GAP_DP.dp),
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
            text = stringResource(R.string.parent_gate_title),
            color = StorybookPalette.Ink,
            fontSize = TITLE_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.parent_gate_subtitle),
            color = StorybookPalette.Ink,
            fontSize = SUBTITLE_SP.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        ParentalHoldBar(
            isHolding = isHolding,
            heldSeconds = heldSeconds,
            onHoldingChanged = onHoldingChanged
        )

        Text(
            text = stringResource(R.string.parent_gate_or_math),
            color = StorybookPalette.Ink,
            fontSize = SUBTITLE_SP.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        ParentalMathSection(
            challenge = challenge,
            answerDigits = answerDigits,
            showWrongMath = showWrongMath,
            onDigit = onDigit,
            onClear = onClear,
            onCheck = onCheck
        )

        StorybookPillButton(
            label = stringResource(R.string.parent_gate_cancel),
            topColor = StorybookPalette.ButtonBlueTop,
            bottomColor = StorybookPalette.ButtonBlueBottom,
            onClick = onDismiss
        )
    }
}

@Composable
private fun ParentalHoldBar(isHolding: Boolean, heldSeconds: Float, onHoldingChanged: (Boolean) -> Unit) {
    val shape = RoundedCornerShape(HOLD_CORNER_PERCENT)
    val progress = computeParentalHoldProgress(heldSeconds)
    val remaining = max(0f, GameConfig.PARENTAL_GATE_HOLD_SECONDS - heldSeconds)
    val label = if (isHolding) {
        stringResource(R.string.parent_gate_hold_progress, remaining)
    } else {
        stringResource(R.string.parent_gate_hold_prompt)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HOLD_MIN_HEIGHT_DP.dp)
            .clip(shape)
            .background(Color(HOLD_BG_COLOR))
            .border(HOLD_BORDER_DP.dp, StorybookPalette.Ink, shape)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        onHoldingChanged(event.changes.any { it.pressed })
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (progress > 0f) {
                drawRect(
                    color = Color(HOLD_PROGRESS_COLOR),
                    size = Size(width = size.width * progress, height = size.height)
                )
            }
        }
        Text(
            text = label,
            color = StorybookPalette.Ink,
            fontSize = HOLD_LABEL_SP.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = CONTENT_GAP_DP.dp)
        )
    }
}

@Composable
private fun ParentalMathSection(
    challenge: ParentalMathChallenge,
    answerDigits: String,
    showWrongMath: Boolean,
    onDigit: (Int) -> Unit,
    onClear: () -> Unit,
    onCheck: () -> Unit
) {
    val boxShape = RoundedCornerShape(MATH_BOX_CORNER_DP.dp)
    val displayAnswer = answerDigits.ifEmpty { stringResource(R.string.parent_gate_math_blank) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KEY_ROW_GAP_DP.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(boxShape)
                .background(Color(MATH_BOX_BG))
                .border(KEY_BORDER_DP.dp, StorybookPalette.Ink, boxShape)
                .padding(vertical = CONTENT_GAP_DP.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.parent_gate_math_question,
                    challenge.left,
                    challenge.right,
                    displayAnswer
                ),
                color = StorybookPalette.Ink,
                fontSize = MATH_PROMPT_SP.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        if (showWrongMath) {
            Text(
                text = stringResource(R.string.parent_gate_math_wrong),
                color = Color(MATH_ERROR_COLOR),
                fontSize = MATH_ERROR_SP.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        KeypadRow(listOf(DIGIT_SEVEN, DIGIT_EIGHT, DIGIT_NINE), onDigit, onClear, onCheck)
        KeypadRow(listOf(DIGIT_FOUR, DIGIT_FIVE, DIGIT_SIX), onDigit, onClear, onCheck)
        KeypadRow(listOf(DIGIT_ONE, DIGIT_TWO, DIGIT_THREE), onDigit, onClear, onCheck)
        KeypadRow(listOf(KEY_CLEAR, 0, KEY_CHECK), onDigit, onClear, onCheck)
    }
}

@Composable
private fun KeypadRow(keys: List<Int>, onDigit: (Int) -> Unit, onClear: () -> Unit, onCheck: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(KEY_ROW_GAP_DP.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (code in keys) {
            val label = when (code) {
                KEY_CLEAR -> stringResource(R.string.parent_gate_clear)
                KEY_CHECK -> stringResource(R.string.parent_gate_check)
                else -> code.toString()
            }
            val topBg = when (code) {
                KEY_CHECK -> StorybookPalette.ButtonPinkTop
                KEY_CLEAR -> StorybookPalette.ButtonBlueTop
                else -> StorybookPalette.SoftWhite
            }
            val botBg = when (code) {
                KEY_CHECK -> StorybookPalette.ButtonPinkBottom
                KEY_CLEAR -> StorybookPalette.ButtonBlueBottom
                else -> StorybookPalette.PetalPink
            }
            val keyShape = RoundedCornerShape(KEY_CORNER_DP.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = KEY_MIN_HEIGHT_DP.dp)
                    .clip(keyShape)
                    .background(Brush.verticalGradient(listOf(topBg, botBg)))
                    .border(KEY_BORDER_DP.dp, StorybookPalette.Ink, keyShape)
                    .clickable(role = Role.Button) {
                        when (code) {
                            KEY_CLEAR -> onClear()
                            KEY_CHECK -> onCheck()
                            else -> onDigit(code)
                        }
                    }
            ) {
                Text(
                    text = label,
                    color = StorybookPalette.Ink,
                    fontSize = KEY_FONT_SP.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

package com.princesspuzzle.storybook.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.MeadowBackdropState
import com.princesspuzzle.storybook.art.SceneIllustrations
import com.princesspuzzle.storybook.art.StorybookPalette
import com.princesspuzzle.storybook.art.drawMeadowBackdrop
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.ui.components.StorybookCircleGrownUpsButton
import com.princesspuzzle.storybook.ui.components.StorybookLogoCrest
import com.princesspuzzle.storybook.ui.components.StorybookNoticeOverlay
import com.princesspuzzle.storybook.ui.components.StorybookPillButton
import com.princesspuzzle.storybook.ui.components.StorybookStarSummaryPill
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.isActive

private const val NANOS_PER_SECOND = 1_000_000_000f
private const val HOME_REDRAW_INTERVAL_NANOS = 50_000_000L
private const val HALF = 0.5f
private const val TWO_PI = (PI * 2.0).toFloat()
private const val EDGE_PAD_DP = 18
private const val SECTION_GAP_DP = 16
private const val LANDSCAPE_WHEEL_WEIGHT = 1f
private const val LANDSCAPE_RIGHT_WEIGHT = 1f
private const val WHEEL_MAX_PORTRAIT_DP = 290f
private const val WHEEL_MAX_LANDSCAPE_DP = 260f
private const val WHEEL_PORTRAIT_SHARE = 0.36f
private const val WHEEL_LANDSCAPE_SHARE = 0.68f
private const val WHEEL_DISC_ALPHA = 0.52f
private const val WHEEL_RING_ALPHA = 0.85f
private const val WHEEL_TRACK_ALPHA = 0.14f
private const val WHEEL_DISC_RATIO = 0.48f
private const val WHEEL_ORBIT_RATIO = 0.39f
private const val WHEEL_CENTER_RATIO = 0.52f
private const val WHEEL_GEM_RADIUS_RATIO = 0.055f
private const val WHEEL_SPIN_RAD_PER_SEC = 0.22f
private const val WHEEL_RING_STROKE_DP = 3f
private const val WHEEL_TRACK_STROKE_DP = 5f
private const val WHEEL_GEM_STROKE_DP = 2f
private const val CENTER_BORDER_DP = 4f
private const val CENTER_SHADOW_DP = 6
private const val PLAY_BTN_MIN_WIDTH_DP = 220
private const val HINT_SP = 14
private const val HINT_TEXT_COLOR = 0xCC6A496E

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(progressState: AppProgressState, onPlayClick: () -> Unit, modifier: Modifier = Modifier) {
    val density = LocalDensity.current.density
    val backdropState = remember { MeadowBackdropState() }
    var elapsedSec by remember { mutableLongStateOf(0L) }
    var timeAccumulator by remember { mutableFloatStateOf(0f) }
    var showGrownUpsNotice by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        var lastRedrawNanos = 0L
        while (isActive) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    val dt = ((nanos - lastNanos) / NANOS_PER_SECOND)
                        .coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
                    timeAccumulator += dt
                }
                lastNanos = nanos
                if (lastRedrawNanos == 0L || nanos - lastRedrawNanos >= HOME_REDRAW_INTERVAL_NANOS) {
                    lastRedrawNanos = nanos
                    elapsedSec = nanos
                }
            }
        }
    }

    val centerLevel = PuzzleLevels.getById(progressState.lastSelectedLevelId)
    val centerThumb = remember(centerLevel.theme, density) {
        SceneIllustrations.getOrRenderThumbnail(centerLevel.theme, density)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthDp = maxWidth.value
        val heightDp = maxHeight.value
        val isLandscape = widthDp > heightDp * GameConfig.LANDSCAPE_ASPECT_THRESHOLD

        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE")
            val tick = elapsedSec
            drawMeadowBackdrop(timeAccumulator, backdropState)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(EDGE_PAD_DP.dp)
        ) {
            HomeTopBar(
                earnedStars = progressState.totalStars,
                onGrownUpsClick = { showGrownUpsNotice = true }
            )

            if (isLandscape) {
                val wheelDp = min(WHEEL_MAX_LANDSCAPE_DP, heightDp * WHEEL_LANDSCAPE_SHARE).dp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SECTION_GAP_DP.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(LANDSCAPE_WHEEL_WEIGHT)
                    ) {
                        StorybookShowcaseWheel(
                            wheelSize = wheelDp,
                            angleOffsetRad = timeAccumulator * WHEEL_SPIN_RAD_PER_SEC,
                            centerThumb = centerThumb,
                            onCenterClick = onPlayClick
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SECTION_GAP_DP.dp, Alignment.CenterVertically),
                        modifier = Modifier.weight(LANDSCAPE_RIGHT_WEIGHT)
                    ) {
                        StorybookLogoCrest(
                            title = stringResource(R.string.app_title_short),
                            subtitle = stringResource(R.string.app_subtitle)
                        )
                        HomePlayAction(onPlayClick = onPlayClick)
                    }
                }
            } else {
                val maxSquareDp = min(widthDp * WHEEL_LANDSCAPE_SHARE, heightDp * WHEEL_PORTRAIT_SHARE)
                val wheelDp = min(WHEEL_MAX_PORTRAIT_DP, maxSquareDp).dp
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    StorybookLogoCrest(
                        title = stringResource(R.string.app_title_short),
                        subtitle = stringResource(R.string.app_subtitle)
                    )
                    StorybookShowcaseWheel(
                        wheelSize = wheelDp,
                        angleOffsetRad = timeAccumulator * WHEEL_SPIN_RAD_PER_SEC,
                        centerThumb = centerThumb,
                        onCenterClick = onPlayClick
                    )
                    HomePlayAction(onPlayClick = onPlayClick)
                }
            }
        }

        if (showGrownUpsNotice) {
            StorybookNoticeOverlay(
                title = stringResource(R.string.home_grownups_coming_title),
                body = stringResource(R.string.home_grownups_coming_body),
                okLabel = stringResource(R.string.dialog_ok),
                onDismiss = { showGrownUpsNotice = false }
            )
        }
    }
}

@Composable
private fun HomeTopBar(earnedStars: Int, onGrownUpsClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        StorybookStarSummaryPill(
            earnedStars = earnedStars,
            totalStars = PuzzleLevels.MAX_TOTAL_STARS
        )
        StorybookCircleGrownUpsButton(
            contentDescriptionText = stringResource(R.string.cd_grownups_button),
            onClick = onGrownUpsClick
        )
    }
}

@Composable
private fun HomePlayAction(onPlayClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SECTION_GAP_DP.dp)
    ) {
        StorybookPillButton(
            label = stringResource(R.string.home_play),
            topColor = StorybookPalette.ButtonPinkTop,
            bottomColor = StorybookPalette.ButtonPinkBottom,
            minHeightDp = GameConfig.PRIMARY_BUTTON_TOUCH_TARGET_DP.dp,
            onClick = onPlayClick,
            modifier = Modifier.widthIn(min = PLAY_BTN_MIN_WIDTH_DP.dp)
        )
        Text(
            text = stringResource(R.string.home_spin_hint),
            color = Color(HINT_TEXT_COLOR),
            fontSize = HINT_SP.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StorybookShowcaseWheel(
    wheelSize: Dp,
    angleOffsetRad: Float,
    centerThumb: ImageBitmap,
    onCenterClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(wheelSize)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * HALF
            val cy = size.height * HALF
            val dim = size.minDimension
            val orbitR = dim * WHEEL_ORBIT_RATIO
            val gemR = dim * WHEEL_GEM_RADIUS_RATIO

            drawCircle(
                color = Color.White,
                radius = dim * WHEEL_DISC_RATIO,
                center = Offset(cx, cy),
                alpha = WHEEL_DISC_ALPHA
            )
            drawCircle(
                color = Color.White,
                radius = dim * WHEEL_DISC_RATIO,
                center = Offset(cx, cy),
                alpha = WHEEL_RING_ALPHA,
                style = Stroke(width = WHEEL_RING_STROKE_DP * density)
            )
            drawCircle(
                color = StorybookPalette.Ink,
                radius = orbitR,
                center = Offset(cx, cy),
                alpha = WHEEL_TRACK_ALPHA,
                style = Stroke(width = WHEEL_TRACK_STROKE_DP * density)
            )

            val count = PuzzleLevels.ALL.size
            for (i in 0 until count) {
                val angle = angleOffsetRad + i * (TWO_PI / count)
                val gx = cx + cos(angle) * orbitR
                val gy = cy + sin(angle) * orbitR
                drawCircle(
                    color = Color(PuzzleLevels.ALL[i].accentHex),
                    radius = gemR,
                    center = Offset(gx, gy)
                )
                drawCircle(
                    color = StorybookPalette.Ink,
                    radius = gemR,
                    center = Offset(gx, gy),
                    style = Stroke(width = WHEEL_GEM_STROKE_DP * density)
                )
            }
        }

        val centerSize = wheelSize * WHEEL_CENTER_RATIO
        Box(
            modifier = Modifier
                .size(centerSize)
                .shadow(CENTER_SHADOW_DP.dp, CircleShape)
                .clip(CircleShape)
                .background(StorybookPalette.SoftWhite)
                .border(CENTER_BORDER_DP.dp, StorybookPalette.Ink, CircleShape)
                .clickable(role = Role.Button, onClick = onCenterClick)
        ) {
            Image(
                bitmap = centerThumb,
                contentDescription = stringResource(R.string.home_play),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

package com.princesspuzzle.storybook.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.princesspuzzle.storybook.R
import com.princesspuzzle.storybook.art.MeadowBackdropState
import com.princesspuzzle.storybook.art.SceneIllustrations
import com.princesspuzzle.storybook.art.SceneIllustrations.drawLivingCelebration
import com.princesspuzzle.storybook.art.StorybookPalette
import com.princesspuzzle.storybook.art.drawMeadowBackdrop
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.PuzzleLayout
import com.princesspuzzle.storybook.puzzle.PieceRuntime
import com.princesspuzzle.storybook.puzzle.PieceStatus
import com.princesspuzzle.storybook.puzzle.PuzzlePhysicsEngine
import com.princesspuzzle.storybook.puzzle.PuzzleUiState
import com.princesspuzzle.storybook.puzzle.PuzzleViewModel
import com.princesspuzzle.storybook.ui.components.StorybookBadgePill
import com.princesspuzzle.storybook.ui.components.StorybookCircleBackButton
import com.princesspuzzle.storybook.ui.components.StorybookPillButton
import com.princesspuzzle.storybook.ui.components.StorybookStarRow
import com.princesspuzzle.storybook.ui.components.StorybookTopProgressPill
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.isActive

private const val HALF = 0.5f
private const val NANOS_PER_SECOND = 1_000_000_000f
private const val IDLE_REDRAW_INTERVAL_NANOS = 66_000_000L
private const val COMPACT_TRAY_HEIGHT_DP = 210f
private const val HINT_VISIBLE_TRAY_HEIGHT_DP = 135f
private const val WIDE_CELEBRATION_TRAY_WIDTH_DP = 320f
private const val GLOW_PULSE_SPEED = 10f
private const val GLOW_PULSE_BASE = 0.75f
private const val GLOW_PULSE_AMP = 0.25f
private const val GLOW_THRESHOLD = 0.02f
private const val GLOW_PIECE_BORDER_THRESHOLD = 0.3f
private const val GLOW_STROKE_ALPHA = 0.95f
private const val HINT_PULSE_BASE = 0.55f
private const val HINT_PULSE_AMP = 0.45f
private const val HINT_SLOT_ALPHA = 0.55f
private const val HINT_STROKE_ALPHA = 0.8f
private const val GHOST_IMAGE_ALPHA = 0.22f
private const val SEAM_VISIBLE_THRESHOLD = 0.01f
private const val SEAM_ALPHA_MAX = 0.65f
private const val CELEBRATION_BREATH_SPEED = 3.2f
private const val CELEBRATION_BREATH_AMP = 0.01f
private const val RADIANS_TO_DEGREES = 180f / PI.toFloat()
private const val FRAME_PAD_DP = 10f
private const val FRAME_SHADOW_Y_DP = 5f
private const val OUTER_CORNER_DP = 22f
private const val INNER_CORNER_DP = 12f
private const val SHADOW_ELEVATED_Y_DP = 7f
private const val SHADOW_REST_Y_DP = 3f
private const val DASH_ON_DP = 8f
private const val DASH_OFF_DP = 6f
private const val HINT_LABEL_TOP_DP = 6
private const val HINT_LABEL_SP = 13
private const val CELEBRATION_PAD_H_DP = 12
private const val CELEBRATION_PAD_V_DP = 10
private const val CELEBRATION_STAR_COMPACT_DP = 30
private const val CELEBRATION_STAR_LARGE_DP = 38
private const val CELEBRATION_HEAD_COMPACT_SP = 21
private const val CELEBRATION_HEAD_LARGE_SP = 25
private const val CELEBRATION_SUB_SP = 14
private const val CELEBRATION_BTN_ROW_GAP_DP = 14
private const val CELEBRATION_BTN_COL_GAP_DP = 8
private const val TOP_HUD_GAP_DP = 8
private const val STROKE_GLOW_DP = 4f
private const val STROKE_HINT_DP = 4.5f
private const val STROKE_FRAME_DP = 3f
private const val STROKE_RIM_DP = 2f
private const val STROKE_TRAY_DP = 3f
private const val STROKE_BEVEL_DP = 4f
private const val STROKE_PIECE_DP = 2.4f
private const val STROKE_SHADOW_DP = 5f
private const val COLOR_BOARD_SHADOW = 0x336A496E
private const val COLOR_TRAY_BG = 0xA6FFFDF9
private const val COLOR_TRAY_BORDER = 0xE6FFFFFF
private const val COLOR_DASHED_SLOT = 0x476A496E
private const val COLOR_INNER_RIM = 0x666A496E
private const val COLOR_HINT_TEXT = 0xA86A496E
private const val COLOR_SUBHEADLINE = 0xCC6A496E
private const val COLOR_BEVEL = 0x8CFFFFFF
private const val COLOR_SHADOW_HIGH = 0x476A496E
private const val COLOR_SHADOW_LOW = 0x286A496E

private class RenderScratchCache(density: Float) {
    val backdropState = MeadowBackdropState()
    val clipBoardPath = Path()
    val livingScratchPath = Path()
    val dashedSlotStroke = Stroke(
        width = STROKE_RIM_DP * density,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_ON_DP * density, DASH_OFF_DP * density))
    )
    val glowStroke = Stroke(width = STROKE_GLOW_DP * density, join = StrokeJoin.Round)
    val hintStroke = Stroke(width = STROKE_HINT_DP * density, join = StrokeJoin.Round)
    val frameStroke = Stroke(width = STROKE_FRAME_DP * density)
    val innerRimStroke = Stroke(width = STROKE_RIM_DP * density)
    val trayStroke = Stroke(width = STROKE_TRAY_DP * density)
    val pieceBevelStroke = Stroke(width = STROKE_BEVEL_DP * density, join = StrokeJoin.Round)
    val pieceOutlineStroke = Stroke(width = STROKE_PIECE_DP * density, join = StrokeJoin.Round)
    val shadowOutlineStroke = Stroke(width = STROKE_SHADOW_DP * density, join = StrokeJoin.Round)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PuzzleScreen(
    viewModel: PuzzleViewModel,
    onNavigateBackToLevels: () -> Unit,
    onNextLevel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current.density
    val scratch = remember(density) { RenderScratchCache(density) }
    var frameTickNanos by remember { mutableLongStateOf(0L) }

    BackHandler { onNavigateBackToLevels() }

    LaunchedEffect(Unit) {
        var lastNanos = 0L
        var lastRedrawNanos = 0L
        while (isActive) {
            withFrameNanos { nanos ->
                if (lastNanos != 0L) {
                    viewModel.onFrame((nanos - lastNanos) / NANOS_PER_SECOND)
                }
                lastNanos = nanos
                val idleElapsed = nanos - lastRedrawNanos >= IDLE_REDRAW_INTERVAL_NANOS
                if (lastRedrawNanos == 0L || viewModel.shouldRenderFullRate() || idleElapsed) {
                    lastRedrawNanos = nanos
                    frameTickNanos = nanos
                }
            }
        }
    }

    val artwork = remember(uiState.level.theme, density) {
        SceneIllustrations.getOrRenderArtwork(uiState.level.theme, density)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val layout = viewModel.ensureViewportLayout(
            constraints.maxWidth.toFloat(),
            constraints.maxHeight.toFloat(),
            density,
            artwork
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(uiState.level.id) {
                    handlePuzzlePointerInput(viewModel)
                }
        ) {
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTickNanos
            val timeSec = viewModel.elapsedTimeSec
            drawMeadowBackdrop(timeSec, scratch.backdropState)

            if (viewModel.physicsEngine != null) {
                drawPuzzleScene(viewModel, layout, artwork, timeSec, uiState.completed, scratch)
                viewModel.particles.draw(this)
            }
        }

        PuzzleTopHud(
            layout = layout,
            uiState = uiState,
            onNavigateBackToLevels = onNavigateBackToLevels
        )

        if (!uiState.completed &&
            uiState.placedCount == 0 &&
            layout.tray.h > HINT_VISIBLE_TRAY_HEIGHT_DP * density
        ) {
            PuzzleTrayHintLabel(layout = layout)
        }

        if (uiState.showCelebrationActions) {
            PuzzleCelebrationPanel(
                layout = layout,
                uiState = uiState,
                winTimerSec = viewModel.winTimerSec,
                onLevels = onNavigateBackToLevels,
                onNextPuzzle = onNextLevel
            )
        }
    }
}

private suspend fun PointerInputScope.handlePuzzlePointerInput(viewModel: PuzzleViewModel) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val nowMs = System.currentTimeMillis()
            when (event.type) {
                PointerEventType.Press -> {
                    val pressed = event.changes.firstOrNull { it.pressed && !it.previousPressed }
                    if (pressed != null) {
                        val grabbed = viewModel.onPointerDown(
                            pointerId = pressed.id.value,
                            x = pressed.position.x,
                            y = pressed.position.y,
                            isTouch = pressed.type == PointerType.Touch,
                            nowMillis = nowMs
                        )
                        if (grabbed) pressed.consume()
                    }
                }
                PointerEventType.Move -> {
                    for (change in event.changes) {
                        if (change.pressed) {
                            viewModel.onPointerMove(change.id.value, change.position.x, change.position.y, nowMs)
                        }
                    }
                }
                PointerEventType.Release -> {
                    for (change in event.changes) {
                        if (!change.pressed) {
                            viewModel.onPointerUp(change.id.value, nowMs)
                        }
                    }
                }
                else -> {
                    for (change in event.changes) {
                        if (!change.pressed) {
                            viewModel.onPointerCancel(change.id.value)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PuzzleTopHud(layout: PuzzleLayout, uiState: PuzzleUiState, onNavigateBackToLevels: () -> Unit) {
    val topPadDp = with(LocalDensity.current) { layout.topBar.y.toDp() }
    val sidePadDp = with(LocalDensity.current) { layout.topBar.x.toDp() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TOP_HUD_GAP_DP.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = sidePadDp, end = sidePadDp, top = topPadDp)
    ) {
        StorybookCircleBackButton(
            contentDescriptionText = stringResource(R.string.cd_back_to_levels),
            onClick = onNavigateBackToLevels
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            StorybookTopProgressPill(
                title = stringResource(uiState.level.titleRes),
                progressText = stringResource(R.string.hud_progress, uiState.placedCount, uiState.totalCount)
            )
        }

        StorybookBadgePill(
            label = pluralStringResource(R.plurals.hud_pieces_badge, uiState.totalCount, uiState.totalCount),
            containerColor = StorybookPalette.ButtonBlueTop
        )
    }
}

@Composable
private fun PuzzleTrayHintLabel(layout: PuzzleLayout) {
    val trayX = with(LocalDensity.current) { layout.tray.x.toDp() }
    val trayY = with(LocalDensity.current) { layout.tray.y.toDp() }
    val trayW = with(LocalDensity.current) { layout.tray.w.toDp() }
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .offset(x = trayX, y = trayY + HINT_LABEL_TOP_DP.dp)
            .width(trayW)
    ) {
        Text(
            text = stringResource(R.string.puzzle_drag_hint),
            color = Color(COLOR_HINT_TEXT),
            fontSize = HINT_LABEL_SP.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PuzzleCelebrationPanel(
    layout: PuzzleLayout,
    uiState: PuzzleUiState,
    winTimerSec: Float,
    onLevels: () -> Unit,
    onNextPuzzle: () -> Unit
) {
    val trayX = with(LocalDensity.current) { layout.tray.x.toDp() }
    val trayY = with(LocalDensity.current) { layout.tray.y.toDp() }
    val trayW = with(LocalDensity.current) { layout.tray.w.toDp() }
    val trayH = with(LocalDensity.current) { layout.tray.h.toDp() }
    val earnedStars =
        (if (winTimerSec > GameConfig.STAR_ONE_DELAY_SEC) 1 else 0) +
            (if (winTimerSec > GameConfig.STAR_TWO_DELAY_SEC) 1 else 0) +
            (if (winTimerSec > GameConfig.STAR_THREE_DELAY_SEC) 1 else 0)
    val compact = layout.tray.h < COMPACT_TRAY_HEIGHT_DP * layout.density

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .offset(x = trayX, y = trayY)
            .size(width = trayW, height = trayH)
            .padding(horizontal = CELEBRATION_PAD_H_DP.dp, vertical = CELEBRATION_PAD_V_DP.dp)
    ) {
        StorybookStarRow(
            earnedStars = earnedStars,
            starSize = if (compact) CELEBRATION_STAR_COMPACT_DP.dp else CELEBRATION_STAR_LARGE_DP.dp
        )
        Text(
            text = stringResource(R.string.win_headline),
            color = StorybookPalette.Ink,
            fontSize = if (compact) CELEBRATION_HEAD_COMPACT_SP.sp else CELEBRATION_HEAD_LARGE_SP.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (!compact) {
            Text(
                text = stringResource(R.string.win_subheadline, stringResource(uiState.level.titleRes)),
                color = Color(COLOR_SUBHEADLINE),
                fontSize = CELEBRATION_SUB_SP.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        if (!layout.isLandscape && layout.tray.w > WIDE_CELEBRATION_TRAY_WIDTH_DP * layout.density) {
            Row(horizontalArrangement = Arrangement.spacedBy(CELEBRATION_BTN_ROW_GAP_DP.dp)) {
                StorybookPillButton(
                    label = stringResource(R.string.win_levels),
                    topColor = StorybookPalette.ButtonBlueTop,
                    bottomColor = StorybookPalette.ButtonBlueBottom,
                    onClick = onLevels
                )
                StorybookPillButton(
                    label = stringResource(R.string.win_next),
                    topColor = StorybookPalette.ButtonPinkTop,
                    bottomColor = StorybookPalette.ButtonPinkBottom,
                    onClick = onNextPuzzle
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(CELEBRATION_BTN_COL_GAP_DP.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StorybookPillButton(
                    label = stringResource(R.string.win_next),
                    topColor = StorybookPalette.ButtonPinkTop,
                    bottomColor = StorybookPalette.ButtonPinkBottom,
                    onClick = onNextPuzzle
                )
                StorybookPillButton(
                    label = stringResource(R.string.win_levels),
                    topColor = StorybookPalette.ButtonBlueTop,
                    bottomColor = StorybookPalette.ButtonBlueBottom,
                    onClick = onLevels
                )
            }
        }
    }
}

private fun DrawScope.drawPuzzleScene(
    viewModel: PuzzleViewModel,
    layout: PuzzleLayout,
    artwork: ImageBitmap,
    timeSec: Float,
    completed: Boolean,
    scratch: RenderScratchCache
) {
    val engine = viewModel.physicsEngine ?: return
    drawBoardFrameAndTray(layout, Color(viewModel.uiState.value.level.frameHex), scratch)

    val board = layout.board
    val innerCorner = INNER_CORNER_DP * layout.density
    scratch.clipBoardPath.reset()
    scratch.clipBoardPath.addRoundRect(
        RoundRect(board.x, board.y, board.x + board.w, board.y + board.h, CornerRadius(innerCorner, innerCorner))
    )

    val boardIntSize = IntSize(board.w.roundToInt(), board.h.roundToInt())
    val boardIntOffset = IntOffset(board.x.roundToInt(), board.y.roundToInt())
    val hintIdx = engine.hintPieceIndex
    val hintPulse = HINT_PULSE_BASE + HINT_PULSE_AMP * abs(sin(timeSec * GameConfig.HINT_PULSE_SPEED))

    clipPath(scratch.clipBoardPath) {
        drawRect(StorybookPalette.SoftWhite, topLeft = Offset(board.x, board.y), size = Size(board.w, board.h))
        drawImage(image = artwork, dstOffset = boardIntOffset, dstSize = boardIntSize, alpha = GHOST_IMAGE_ALPHA)
        drawBoardGhostSlots(viewModel, engine, board.x, board.y, hintIdx, hintPulse, timeSec, scratch)

        if (completed && viewModel.seamAlpha <= SEAM_VISIBLE_THRESHOLD) {
            drawCompletedLivingBoard(viewModel, layout, artwork, boardIntOffset, boardIntSize, timeSec, scratch)
        } else {
            drawPlacedPiecesOnBoard(viewModel, engine, board.x, board.y, artwork, boardIntSize, scratch)
        }
    }

    drawRoundRect(
        color = Color(COLOR_INNER_RIM),
        topLeft = Offset(board.x, board.y),
        size = Size(board.w, board.h),
        cornerRadius = CornerRadius(innerCorner, innerCorner),
        style = scratch.innerRimStroke
    )

    drawFloatingPieceLayers(viewModel, engine, artwork, boardIntSize, hintIdx, hintPulse, scratch)
}

private fun DrawScope.drawBoardFrameAndTray(layout: PuzzleLayout, frameColor: Color, scratch: RenderScratchCache) {
    val board = layout.board
    val tray = layout.tray
    val density = layout.density
    val framePad = FRAME_PAD_DP * density
    val outerCorner = OUTER_CORNER_DP * density

    drawRoundRect(
        color = Color(COLOR_BOARD_SHADOW),
        topLeft = Offset(board.x - framePad, board.y - framePad + FRAME_SHADOW_Y_DP * density),
        size = Size(board.w + framePad * 2f, board.h + framePad * 2f),
        cornerRadius = CornerRadius(outerCorner, outerCorner)
    )
    drawRoundRect(
        color = frameColor,
        topLeft = Offset(board.x - framePad, board.y - framePad),
        size = Size(board.w + framePad * 2f, board.h + framePad * 2f),
        cornerRadius = CornerRadius(outerCorner, outerCorner)
    )
    drawRoundRect(
        color = StorybookPalette.Ink,
        topLeft = Offset(board.x - framePad, board.y - framePad),
        size = Size(board.w + framePad * 2f, board.h + framePad * 2f),
        cornerRadius = CornerRadius(outerCorner, outerCorner),
        style = scratch.frameStroke
    )

    drawRoundRect(
        color = Color(COLOR_TRAY_BG),
        topLeft = Offset(tray.x, tray.y),
        size = Size(tray.w, tray.h),
        cornerRadius = CornerRadius(outerCorner, outerCorner)
    )
    drawRoundRect(
        color = Color(COLOR_TRAY_BORDER),
        topLeft = Offset(tray.x, tray.y),
        size = Size(tray.w, tray.h),
        cornerRadius = CornerRadius(outerCorner, outerCorner),
        style = scratch.trayStroke
    )
}

private fun DrawScope.drawBoardGhostSlots(
    viewModel: PuzzleViewModel,
    engine: PuzzlePhysicsEngine,
    boardX: Float,
    boardY: Float,
    hintIdx: Int,
    hintPulse: Float,
    timeSec: Float,
    scratch: RenderScratchCache
) {
    translate(boardX, boardY) {
        for (i in engine.runtimes.indices) {
            val piece = engine.runtimes[i]
            val geom = viewModel.geometryCache[piece.spec.id]
            if (piece.status != PieceStatus.PLACED && geom != null) {
                when {
                    piece.glowIntensity > GLOW_THRESHOLD -> {
                        val pulse = GLOW_PULSE_BASE + sin(timeSec * GLOW_PULSE_SPEED) * GLOW_PULSE_AMP
                        drawPath(geom.boardPath, StorybookPalette.SlotGlowFill, alpha = piece.glowIntensity * pulse)
                        drawPath(
                            geom.boardPath,
                            StorybookPalette.SlotGlowStroke,
                            alpha = piece.glowIntensity * GLOW_STROKE_ALPHA,
                            style = scratch.glowStroke
                        )
                    }
                    i == hintIdx -> {
                        drawPath(geom.boardPath, StorybookPalette.SlotGlowFill, alpha = HINT_SLOT_ALPHA * hintPulse)
                        drawPath(
                            geom.boardPath,
                            StorybookPalette.SlotGlowStroke,
                            alpha = HINT_STROKE_ALPHA * hintPulse,
                            style = scratch.hintStroke
                        )
                    }
                    else -> {
                        drawPath(geom.boardPath, Color(COLOR_DASHED_SLOT), style = scratch.dashedSlotStroke)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCompletedLivingBoard(
    viewModel: PuzzleViewModel,
    layout: PuzzleLayout,
    artwork: ImageBitmap,
    boardIntOffset: IntOffset,
    boardIntSize: IntSize,
    timeSec: Float,
    scratch: RenderScratchCache
) {
    val board = layout.board
    val breathScale = 1f + sin(viewModel.winTimerSec * CELEBRATION_BREATH_SPEED) * CELEBRATION_BREATH_AMP
    val pivot = Offset(board.x + board.w * HALF, board.y + board.h * HALF)
    scale(breathScale, breathScale, pivot = pivot) {
        drawImage(image = artwork, dstOffset = boardIntOffset, dstSize = boardIntSize)
        drawLivingCelebration(
            theme = viewModel.uiState.value.level.theme,
            boardX = board.x,
            boardY = board.y,
            boardSize = board.w,
            timeSec = timeSec,
            scratchPath = scratch.livingScratchPath
        )
    }
}

private fun DrawScope.drawPlacedPiecesOnBoard(
    viewModel: PuzzleViewModel,
    engine: PuzzlePhysicsEngine,
    boardX: Float,
    boardY: Float,
    artwork: ImageBitmap,
    boardIntSize: IntSize,
    scratch: RenderScratchCache
) {
    val boardW = boardIntSize.width.toFloat()
    val boardH = boardIntSize.height.toFloat()
    translate(boardX, boardY) {
        for (piece in engine.runtimes) {
            val geom = viewModel.geometryCache[piece.spec.id]
            if (piece.status == PieceStatus.PLACED && geom != null) {
                val sprite = geom.spriteBitmap
                if (sprite != null) {
                    translate(piece.spec.centerX * boardW, piece.spec.centerY * boardH) {
                        drawImage(sprite, topLeft = Offset(geom.spriteOffsetX, geom.spriteOffsetY))
                    }
                } else {
                    clipPath(geom.boardPath) {
                        drawImage(image = artwork, dstOffset = IntOffset.Zero, dstSize = boardIntSize)
                    }
                }
                if (viewModel.seamAlpha > SEAM_VISIBLE_THRESHOLD) {
                    drawPath(
                        geom.boardPath,
                        color = StorybookPalette.Ink,
                        alpha = viewModel.seamAlpha * SEAM_ALPHA_MAX,
                        style = scratch.innerRimStroke
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFloatingPieceLayers(
    viewModel: PuzzleViewModel,
    engine: PuzzlePhysicsEngine,
    artwork: ImageBitmap,
    boardIntSize: IntSize,
    hintIdx: Int,
    hintPulse: Float,
    scratch: RenderScratchCache
) {
    for (i in engine.runtimes.indices) {
        val piece = engine.runtimes[i]
        if (piece.status == PieceStatus.TRAY) {
            drawFloatingPiece(viewModel, piece, artwork, boardIntSize, i == hintIdx, hintPulse, scratch)
        }
    }
    for (i in engine.runtimes.indices) {
        val piece = engine.runtimes[i]
        if (piece.status == PieceStatus.RETURNING || piece.status == PieceStatus.SNAPPING) {
            drawFloatingPiece(viewModel, piece, artwork, boardIntSize, false, 0f, scratch)
        }
    }
    val activeIdx = engine.activePieceIndex
    if (activeIdx in engine.runtimes.indices) {
        drawFloatingPiece(viewModel, engine.runtimes[activeIdx], artwork, boardIntSize, false, 0f, scratch)
    }
}

private fun DrawScope.drawFloatingPiece(
    viewModel: PuzzleViewModel,
    piece: PieceRuntime,
    artwork: ImageBitmap,
    boardSize: IntSize,
    isHinted: Boolean,
    hintPulse: Float,
    scratch: RenderScratchCache
) {
    val geom = viewModel.geometryCache[piece.spec.id] ?: return
    val layout = viewModel.currentLayout ?: return
    val popScale = if (piece.status == PieceStatus.SNAPPING) {
        1f + sin(piece.snapAnimProgress * PI.toFloat()) * GameConfig.SNAP_POP_AMPLITUDE
    } else {
        1f
    }
    val totalScale = piece.scale * popScale
    val rotDeg = piece.rotation * RADIANS_TO_DEGREES
    val elevated = piece.status == PieceStatus.DRAGGING || piece.status == PieceStatus.RETURNING

    translate(piece.x, piece.y) {
        rotate(rotDeg, pivot = Offset.Zero) {
            scale(totalScale, totalScale, pivot = Offset.Zero) {
                translate(0f, (if (elevated) SHADOW_ELEVATED_Y_DP else SHADOW_REST_Y_DP) * layout.density) {
                    drawPath(
                        geom.localPath,
                        color = if (elevated) Color(COLOR_SHADOW_HIGH) else Color(COLOR_SHADOW_LOW),
                        style = scratch.shadowOutlineStroke
                    )
                }

                val sprite = geom.spriteBitmap
                if (sprite != null) {
                    drawImage(sprite, topLeft = Offset(geom.spriteOffsetX, geom.spriteOffsetY))
                } else {
                    clipPath(geom.localPath) {
                        drawImage(
                            image = artwork,
                            dstOffset = IntOffset(
                                (-piece.spec.centerX * layout.board.w).roundToInt(),
                                (-piece.spec.centerY * layout.board.h).roundToInt()
                            ),
                            dstSize = boardSize
                        )
                    }
                }

                drawPath(geom.localPath, color = Color(COLOR_BEVEL), style = scratch.pieceBevelStroke)
                val outlineColor = when {
                    piece.glowIntensity > GLOW_PIECE_BORDER_THRESHOLD -> StorybookPalette.SlotGlowStroke
                    isHinted && hintPulse > HALF -> StorybookPalette.SlotGlowStroke
                    else -> StorybookPalette.Ink
                }
                drawPath(geom.localPath, color = outlineColor, style = scratch.pieceOutlineStroke)
            }
        }
    }
}

@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.princesspuzzle.storybook.art.IosIllustrationRenderer
import com.princesspuzzle.storybook.art.IosMeadowBackdrop
import com.princesspuzzle.storybook.art.IosThemeColors
import com.princesspuzzle.storybook.art.PieceBitmapPair
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.puzzle.DragEndOutcome
import com.princesspuzzle.storybook.puzzle.DragMoveOutcome
import com.princesspuzzle.storybook.puzzle.GameplayViewModel
import com.princesspuzzle.storybook.puzzle.LevelDefinition
import com.princesspuzzle.storybook.puzzle.PhysicsPuzzleEngine
import com.princesspuzzle.storybook.puzzle.PiecePhysicsBody
import com.princesspuzzle.storybook.puzzle.SparkleParticle
import com.princesspuzzle.storybook.puzzle.SurpriseBubble
import com.princesspuzzle.storybook.ui.components.GlossyGreenBackButton
import com.princesspuzzle.storybook.ui.components.SurpriseCelebrationBar
import kotlin.math.roundToInt

/**
 * Interactive 60 FPS native physics puzzle screen matching iOS `PuzzleGameplayScreenView` + `PuzzlePhysicsScene`.
 */
@Composable
fun PuzzleGameplayScreen(
    level: LevelDefinition,
    gameplayViewModel: GameplayViewModel,
    onPiecePickup: () -> Unit,
    onPieceReturned: () -> Unit,
    onPieceSnapped: (placed: Int, total: Int, isWinNow: Boolean, stars: Int) -> Unit,
    onPiecesProgressSaved: (Set<String>) -> Unit,
    onBubblePopped: (Int) -> Unit,
    onBackToWheel: () -> Unit,
    onNextPuzzle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var frameTick by remember { mutableLongStateOf(0L) }
    var completedState by remember(level.id) { mutableStateOf(gameplayViewModel.isCompleted) }
    var earnedStarsState by remember(level.id) { mutableIntStateOf(gameplayViewModel.earnedStars) }
    var poppedBubblesState by remember(level.id) { mutableIntStateOf(gameplayViewModel.poppedBubblesCount) }

    LaunchedEffect(level.id, viewportSize) {
        if (viewportSize.width > 0 && viewportSize.height > 0) {
            gameplayViewModel.ensureViewportLayout(
                widthPx = viewportSize.width.toFloat(),
                heightPx = viewportSize.height.toFloat(),
                density = density
            )
            completedState = gameplayViewModel.isCompleted
            earnedStarsState = gameplayViewModel.earnedStars
            poppedBubblesState = gameplayViewModel.poppedBubblesCount
        }
    }

    LaunchedEffect(level.id) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                val dtSec = if (lastNanos == 0L) {
                    0.016f
                } else {
                    ((frameNanos - lastNanos) / 1_000_000_000.0).toFloat()
                }
                lastNanos = frameNanos
                gameplayViewModel.stepSimulation(dtSec)
                frameTick += 1L
            }
        }
    }

    val boardGeom = if (viewportSize.width > 0 && viewportSize.height > 0) {
        remember(level.id, viewportSize) {
            PhysicsPuzzleEngine.computeBoardGeometry(
                viewportSize.width.toFloat(),
                viewportSize.height.toFloat(),
                density
            )
        }
    } else {
        null
    }

    val pieceBitmaps = remember(level.id, boardGeom) {
        if (boardGeom == null) {
            emptyList()
        } else {
            val bw = boardGeom.boardWidthPx.roundToInt()
            val bh = boardGeom.boardHeightPx.roundToInt()
            level.pieces.map { piece ->
                IosIllustrationRenderer.getPieceBitmaps(level, piece, bw, bh)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewportSize = it }
    ) {
        GameplayPlayfieldCanvas(
            levelId = level.id,
            gameplayViewModel = gameplayViewModel,
            pieceBitmaps = pieceBitmaps,
            frameTick = frameTick,
            onPiecePickup = onPiecePickup,
            onPieceReturned = onPieceReturned,
            onPieceSnapped = { placed, total, wonNow, stars ->
                onPiecesProgressSaved(gameplayViewModel.placedPieceIds.toSet())
                if (wonNow) {
                    completedState = true
                    earnedStarsState = stars
                }
                onPieceSnapped(placed, total, wonNow, stars)
            },
            onBubblePopped = { tintIdx ->
                poppedBubblesState = gameplayViewModel.poppedBubblesCount
                onBubblePopped(tintIdx)
            }
        )

        GlossyGreenBackButton(
            onClick = {
                onPiecesProgressSaved(gameplayViewModel.placedPieceIds.toSet())
                onBackToWheel()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 12.dp)
        )

        SurpriseCelebrationBar(
            visible = completedState,
            earnedStars = earnedStarsState,
            poppedCount = poppedBubblesState,
            onPlayAgain = {
                gameplayViewModel.resetLevelForReplay(
                    viewportWidthPx = viewportSize.width.toFloat(),
                    viewportHeightPx = viewportSize.height.toFloat(),
                    density = density
                )
                completedState = false
                poppedBubblesState = 0
                onPiecesProgressSaved(emptySet())
            },
            onNextPuzzle = onNextPuzzle,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}

@Composable
private fun GameplayPlayfieldCanvas(
    levelId: Int,
    gameplayViewModel: GameplayViewModel,
    pieceBitmaps: List<PieceBitmapPair>,
    frameTick: Long,
    onPiecePickup: () -> Unit,
    onPieceReturned: () -> Unit,
    onPieceSnapped: (placed: Int, total: Int, isWinNow: Boolean, stars: Int) -> Unit,
    onBubblePopped: (Int) -> Unit
) {
    val activeDragRef = remember(levelId) { arrayOfNulls<String>(1) }
    val boardLayerCache = remember(levelId) { BoardStaticLayerCache() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(levelId) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downPos = down.position
                    val poppedBubble = gameplayViewModel.recordSurpriseBubblePop(downPos.x, downPos.y)
                    if (poppedBubble != null) {
                        down.consume()
                        onBubblePopped(poppedBubble.tintIndex)
                        return@awaitEachGesture
                    }

                    val hitBody = gameplayViewModel.physics.findTopHitPiece(downPos.x, downPos.y)
                        ?: return@awaitEachGesture

                    gameplayViewModel.registerInteractionActivity()
                    gameplayViewModel.physics.beginDrag(hitBody.definition.id)
                    activeDragRef[0] = hitBody.definition.id
                    down.consume()
                    onPiecePickup()

                    trackPiecePointer(
                        pointerId = down.id,
                        gameplayViewModel = gameplayViewModel,
                        onSnapped = {
                            activeDragRef[0] = null
                            val won = gameplayViewModel.isCompleted
                            onPieceSnapped(
                                gameplayViewModel.placedCount,
                                gameplayViewModel.totalPieces,
                                won,
                                gameplayViewModel.earnedStars
                            )
                        },
                        onReturned = {
                            activeDragRef[0] = null
                            gameplayViewModel.handleWrongPlacementDrop()
                            onPieceReturned()
                        }
                    )
                }
            }
    ) {
        // Read frame tick inside draw scope only -> 0 parent recompositions per frame!
        @Suppress("UNUSED_VARIABLE")
        val tick = frameTick

        val w = size.width
        val h = size.height
        val bodies = gameplayViewModel.physics.bodies
        if (bodies.isEmpty() || pieceBitmaps.isEmpty()) {
            val bg = IosMeadowBackdrop.getOrRender(w.roundToInt(), h.roundToInt())
            drawImage(image = bg, dstSize = IntSize(w.roundToInt(), h.roundToInt()), blendMode = BlendMode.Src)
            return@Canvas
        }

        val geom = gameplayViewModel.physics.geometry
        val bakedBackdrop = IosMeadowBackdrop.getOrRenderGameplayBackdrop(
            widthPx = w.roundToInt(),
            heightPx = h.roundToInt(),
            glowCenterX = geom.boardCenterX,
            glowCenterY = geom.boardCenterY,
            glowRadius = geom.boardWidthPx * 0.65f,
            dimAlpha = GameConfig.BOARD_DIM_OPACITY
        )
        drawImage(image = bakedBackdrop, dstSize = IntSize(w.roundToInt(), h.roundToInt()), blendMode = BlendMode.Src)

        if (boardLayerCache.shouldUseBakedBoard(bodies, gameplayViewModel.isCompleted)) {
            val layer = boardLayerCache.getOrBake(geom, bodies, pieceBitmaps)
            drawImage(
                image = layer,
                dstOffset = IntOffset(geom.boardLeftPx.roundToInt(), geom.boardTopPx.roundToInt())
            )
        } else {
            drawTargetSilhouettesAndPlaced(bodies, pieceBitmaps)
        }
        drawLooseAndDraggedBodies(bodies, pieceBitmaps)
        drawSnapSparkles(gameplayViewModel.effects.sparkles)
        drawSurpriseBubbles(gameplayViewModel.effects.bubbles)
    }
}

private class BoardStaticLayerCache {
    private var cachedBitmap: ImageBitmap? = null
    private var cachedW: Int = 0
    private var cachedH: Int = 0
    private var lastMask: Int = -1
    private val drawScope = CanvasDrawScope()

    fun shouldUseBakedBoard(bodies: List<PiecePhysicsBody>, victoryActive: Boolean): Boolean {
        var useStatic = !victoryActive
        for (i in bodies.indices) {
            val b = bodies[i]
            val animatingSnap = b.placed && (
                b.snapAnimElapsedSec < GameConfig.SNAP_ANIM_DURATION_SEC ||
                    b.grayFadeElapsedSec < GameConfig.GRAY_FADEOUT_DURATION_SEC
                )
            if (b.hintPulse > 0f || animatingSnap) {
                useStatic = false
            }
        }
        return useStatic
    }

    fun getOrBake(
        geom: com.princesspuzzle.storybook.puzzle.PuzzleBoardGeometry,
        bodies: List<PiecePhysicsBody>,
        bitmaps: List<PieceBitmapPair>
    ): ImageBitmap {
        val wPx = geom.boardWidthPx.roundToInt().coerceAtLeast(32)
        val hPx = geom.boardHeightPx.roundToInt().coerceAtLeast(32)
        var mask = 0
        for (i in bodies.indices) {
            if (bodies[i].placed) {
                mask = mask or (1 shl i)
            }
        }
        val existing = cachedBitmap
        val sizeMatches = cachedW == wPx && cachedH == hPx
        if (existing != null && sizeMatches && lastMask == mask) {
            return existing
        }

        val target = existing?.takeIf { sizeMatches } ?: ImageBitmap(wPx, hPx)
        cachedBitmap = target
        cachedW = wPx
        cachedH = hPx
        lastMask = mask

        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = Canvas(target),
            size = androidx.compose.ui.geometry.Size(wPx.toFloat(), hPx.toFloat())
        ) {
            drawRect(color = Color.Transparent, blendMode = BlendMode.Clear)
            translate(left = -geom.boardLeftPx, top = -geom.boardTopPx) {
                drawTargetSilhouettesAndPlaced(bodies, bitmaps)
            }
        }
        return target
    }
}

private suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.trackPiecePointer(
    pointerId: androidx.compose.ui.input.pointer.PointerId,
    gameplayViewModel: GameplayViewModel,
    onSnapped: () -> Unit,
    onReturned: () -> Unit
) {
    var lastMoveTimeNs = System.nanoTime()
    var lastVx = 0f
    var lastVy = 0f
    var tracking = true

    while (tracking) {
        val change = awaitPointerEvent().changes.firstOrNull { it.id == pointerId }
        when {
            change == null -> tracking = false
            !change.pressed -> {
                change.consume()
                finishTrackedPointerRelease(lastVx, lastVy, gameplayViewModel, onSnapped, onReturned)
                tracking = false
            }
            else -> {
                val nowNs = System.nanoTime()
                val dtSec = ((nowNs - lastMoveTimeNs) / 1_000_000_000f).coerceAtLeast(0.004f)
                lastMoveTimeNs = nowNs
                val delta = change.positionChange()
                if (delta != Offset.Zero) {
                    lastVx = delta.x / dtSec
                    lastVy = delta.y / dtSec
                }
                tracking = handleTrackedMove(change, delta, gameplayViewModel, onSnapped)
            }
        }
    }
}

private fun handleTrackedMove(
    change: androidx.compose.ui.input.pointer.PointerInputChange,
    delta: Offset,
    gameplayViewModel: GameplayViewModel,
    onSnapped: () -> Unit
): Boolean {
    var keepTracking = true
    if (delta != Offset.Zero) {
        change.consume()
        val move = gameplayViewModel.physics.moveDrag(delta.x, delta.y)
        if (move is DragMoveOutcome.Snapped) {
            gameplayViewModel.markPiecePlaced(move.piece)
            onSnapped()
            keepTracking = false
        }
    }
    return keepTracking
}

private fun finishTrackedPointerRelease(
    lastVx: Float,
    lastVy: Float,
    gameplayViewModel: GameplayViewModel,
    onSnapped: () -> Unit,
    onReturned: () -> Unit
) {
    when (val end = gameplayViewModel.physics.endDrag(lastVx, lastVy)) {
        is DragEndOutcome.Snapped -> {
            gameplayViewModel.markPiecePlaced(end.piece)
            onSnapped()
        }
        is DragEndOutcome.Returned -> onReturned()
        DragEndOutcome.None -> Unit
    }
}

private fun DrawScope.drawTargetSilhouettesAndPlaced(bodies: List<PiecePhysicsBody>, bitmaps: List<PieceBitmapPair>) {
    val maxZ = bodies.maxOfOrNull { it.definition.zIndex } ?: 10
    for (z in 1..maxZ) {
        drawTargetLayerForZ(z, bodies, bitmaps)
    }
}

private fun DrawScope.drawTargetLayerForZ(z: Int, bodies: List<PiecePhysicsBody>, bitmaps: List<PieceBitmapPair>) {
    for (i in bodies.indices) {
        val b = bodies[i]
        val pair = if (b.definition.zIndex == z) bitmaps.getOrNull(b.definition.partIndex) else null
        if (pair != null) {
            drawSlotAndPlacedPiece(b, pair)
        }
    }
}

private fun DrawScope.drawSlotAndPlacedPiece(body: PiecePhysicsBody, pair: PieceBitmapPair) {
    if (body.grayAlpha > 0.01f) {
        val sil = pair.silhouetteBitmap
        if (body.hintPulse > 0f) {
            drawCircle(
                color = IosThemeColors.goldLight.copy(alpha = 0.35f * body.hintPulse),
                radius = minOf(body.widthPx, body.heightPx) * 0.56f,
                center = Offset(body.targetX, body.targetY)
            )
        }
        drawImage(
            image = sil,
            srcSize = IntSize(sil.width, sil.height),
            dstOffset = IntOffset(
                (body.targetX - body.widthPx * 0.5f).roundToInt(),
                (body.targetY - body.heightPx * 0.5f).roundToInt()
            ),
            dstSize = IntSize(body.widthPx.roundToInt(), body.heightPx.roundToInt()),
            alpha = body.grayAlpha
        )
    }
    if (body.placed) {
        drawPieceBody(body, pair.colorBitmap, shadowAlpha = 0.08f)
    }
}

private fun DrawScope.drawLooseAndDraggedBodies(bodies: List<PiecePhysicsBody>, bitmaps: List<PieceBitmapPair>) {
    var draggedBody: PiecePhysicsBody? = null
    val maxOrder = bodies.maxOfOrNull { it.drawOrder } ?: 30
    for (order in 0..maxOrder) {
        draggedBody = drawLooseLayerForOrder(order, bodies, bitmaps) ?: draggedBody
    }
    draggedBody?.let { active ->
        bitmaps.getOrNull(active.definition.partIndex)?.let { pair ->
            drawPieceBody(active, pair.colorBitmap, shadowAlpha = 0.28f)
        }
    }
}

private fun DrawScope.drawLooseLayerForOrder(
    order: Int,
    bodies: List<PiecePhysicsBody>,
    bitmaps: List<PieceBitmapPair>
): PiecePhysicsBody? {
    var foundDragged: PiecePhysicsBody? = null
    for (i in bodies.indices) {
        val b = bodies[i]
        val matchingLoose = !b.placed && b.drawOrder == order
        if (matchingLoose && b.dragging) {
            foundDragged = b
        } else if (matchingLoose) {
            drawLoosePieceIfPresent(b, bitmaps)
        }
    }
    return foundDragged
}

private fun DrawScope.drawLoosePieceIfPresent(body: PiecePhysicsBody, bitmaps: List<PieceBitmapPair>) {
    bitmaps.getOrNull(body.definition.partIndex)?.let { pair ->
        drawPieceBody(body, pair.colorBitmap, shadowAlpha = 0.16f)
    }
}

private fun DrawScope.drawPieceBody(
    body: PiecePhysicsBody,
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    shadowAlpha: Float
) {
    val deg = Math.toDegrees(body.angleRad.toDouble()).toFloat()
    val pivot = Offset(body.x, body.y)
    if (body.hintPulse > 0f && !body.placed) {
        drawCircle(
            color = IosThemeColors.goldLight.copy(alpha = 0.34f * body.hintPulse),
            radius = minOf(body.widthPx, body.heightPx) * 0.55f,
            center = pivot
        )
    }
    rotate(degrees = deg, pivot = pivot) {
        scale(scale = body.scale, pivot = pivot) {
            val left = (body.x - body.widthPx * 0.5f).roundToInt()
            val top = (body.y - body.heightPx * 0.5f).roundToInt()
            if (shadowAlpha > 0.10f) {
                drawOval(
                    color = Color.Black.copy(alpha = shadowAlpha * 0.6f),
                    topLeft = Offset(body.x - body.widthPx * 0.35f, body.y + body.heightPx * 0.28f),
                    size = androidx.compose.ui.geometry.Size(body.widthPx * 0.70f, body.heightPx * 0.20f)
                )
            }
            drawImage(
                image = bitmap,
                srcSize = IntSize(bitmap.width, bitmap.height),
                dstOffset = IntOffset(left, top),
                dstSize = IntSize(body.widthPx.roundToInt(), body.heightPx.roundToInt())
            )
        }
    }
}

private fun DrawScope.drawSnapSparkles(sparkles: List<SparkleParticle>) {
    for (i in sparkles.indices) {
        val p = sparkles[i]
        if (!p.active) continue
        val color = when (p.colorVariant) {
            0 -> IosThemeColors.goldLight
            1 -> IosThemeColors.castlePinkLight
            else -> IosThemeColors.creamPearl
        }
        drawCircle(
            color = color.copy(alpha = p.alpha),
            radius = p.radiusPx,
            center = Offset(p.x, p.y)
        )
    }
}

private fun DrawScope.drawSurpriseBubbles(bubbles: List<SurpriseBubble>) {
    for (i in bubbles.indices) {
        val b = bubbles[i]
        if (!b.active) continue
        val fillColor = when (b.tintIndex) {
            0 -> IosThemeColors.bubbleTintCyan
            1 -> IosThemeColors.bubbleTintPink
            else -> IosThemeColors.bubbleTintGold
        }
        val center = Offset(b.x, b.y)
        drawCircle(color = fillColor, radius = b.radiusPx, center = center)
        drawCircle(
            color = IosThemeColors.bubbleRim,
            radius = b.radiusPx,
            center = center,
            style = Stroke(width = (b.radiusPx * 0.07f).coerceAtLeast(2f))
        )
        // Specular soap bubble highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.78f),
            radius = b.radiusPx * 0.22f,
            center = Offset(b.x - b.radiusPx * 0.36f, b.y - b.radiusPx * 0.36f)
        )
    }
}

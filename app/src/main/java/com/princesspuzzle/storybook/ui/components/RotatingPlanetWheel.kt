@file:Suppress("MagicNumber")

package com.princesspuzzle.storybook.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.princesspuzzle.storybook.art.IosIllustrationRenderer
import com.princesspuzzle.storybook.art.IosThemeColors
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.GeometryHelpers
import com.princesspuzzle.storybook.data.AppProgressState
import com.princesspuzzle.storybook.puzzle.LevelDefinition
import com.princesspuzzle.storybook.puzzle.PuzzleLevels
import com.princesspuzzle.storybook.puzzle.WheelViewModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private data class WheelGeometry(
    val cx: Float,
    val cy: Float,
    val outerRadius: Float,
    val innerRadius: Float,
    val orbitRadius: Float,
    val baseItemPx: Float,
    val itemCachePx: Int,
    val centerHeroCachePx: Int
)

/**
 * Interactive 16-level carousel matching iOS `RotatingPlanetWheelView`.
 */
@Composable
fun RotatingPlanetWheel(
    wheelViewModel: WheelViewModel,
    progressState: AppProgressState,
    debugUnlockAll: Boolean,
    wheelTickEpoch: Int,
    onWheelTick: () -> Unit,
    onFocusIndexChanged: (Int) -> Unit,
    onLaunchLevel: (LevelDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val animatedRotation = remember { Animatable(wheelViewModel.rotationRad) }

    LaunchedEffect(wheelTickEpoch, wheelViewModel.activeLevelIndex) {
        val currentTarget = wheelViewModel.rotationRad
        val delta = GeometryHelpers.shortestAngularDelta(
            animatedRotation.value.toDouble(),
            currentTarget.toDouble()
        ).toFloat()
        if (abs(delta) > 0.001f && !wheelViewModel.isDragging) {
            animatedRotation.animateTo(
                targetValue = animatedRotation.value + delta,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val geom = computeWheelGeometry(
            widthPx = constraints.maxWidth.toFloat().coerceAtLeast(240f),
            heightPx = constraints.maxHeight.toFloat().coerceAtLeast(240f)
        )
        WheelCanvas(
            geom = geom,
            density = density,
            scope = scope,
            animatedRotation = animatedRotation,
            wheelViewModel = wheelViewModel,
            progressState = progressState,
            debugUnlockAll = debugUnlockAll,
            onWheelTick = onWheelTick,
            onFocusIndexChanged = onFocusIndexChanged,
            onLaunchLevel = onLaunchLevel
        )
    }
}

@Composable
private fun WheelCanvas(
    geom: WheelGeometry,
    density: Float,
    scope: CoroutineScope,
    animatedRotation: Animatable<Float, AnimationVector1D>,
    wheelViewModel: WheelViewModel,
    progressState: AppProgressState,
    debugUnlockAll: Boolean,
    onWheelTick: () -> Unit,
    onFocusIndexChanged: (Int) -> Unit,
    onLaunchLevel: (LevelDefinition) -> Unit
) {
    val levels = wheelViewModel.levels
    val stepRad = wheelViewModel.stepAngleRad

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(levels.size, debugUnlockAll) {
                detectTapGestures { tapOffset ->
                    handleWheelTap(
                        tapOffset = tapOffset,
                        geom = geom,
                        animatedRotation = animatedRotation,
                        wheelViewModel = wheelViewModel,
                        stepRad = stepRad,
                        scope = scope,
                        onWheelTick = onWheelTick,
                        onFocusIndexChanged = onFocusIndexChanged,
                        onLaunchLevel = onLaunchLevel
                    )
                }
            }
            .pointerInput(levels.size) {
                var lastAngleRad = 0f
                detectDragGestures(
                    onDragStart = { startOffset ->
                        wheelViewModel.beginDrag()
                        lastAngleRad = atan2(startOffset.x - geom.cx, -(startOffset.y - geom.cy))
                    },
                    onDragEnd = {
                        val snappedIdx = wheelViewModel.snapToNearestLevel()
                        onFocusIndexChanged(snappedIdx)
                        animateWheelDelta(scope, animatedRotation, wheelViewModel.targetSnapRotationRad())
                    },
                    onDragCancel = {
                        val snappedIdx = wheelViewModel.snapToNearestLevel()
                        onFocusIndexChanged(snappedIdx)
                    },
                    onDrag = { change, _ ->
                        val pos = change.position
                        if (GeometryHelpers.distance(pos.x, pos.y, geom.cx, geom.cy) >
                            GameConfig.WHEEL_DRAG_MIN_PX * density
                        ) {
                            val nextAngle = atan2(pos.x - geom.cx, -(pos.y - geom.cy))
                            val delta = GeometryHelpers.shortestAngularDelta(
                                lastAngleRad.toDouble(),
                                nextAngle.toDouble()
                            ).toFloat()
                            lastAngleRad = nextAngle
                            val crossedNew = wheelViewModel.applyDragDelta(delta)
                            scope.launch { animatedRotation.snapTo(animatedRotation.value + delta) }
                            if (crossedNew) {
                                onWheelTick()
                                onFocusIndexChanged(wheelViewModel.activeLevelIndex)
                            }
                            change.consume()
                        }
                    }
                )
            }
    ) {
        drawWheelContents(
            geom = geom,
            currentRot = animatedRotation.value,
            levels = levels,
            stepRad = stepRad,
            progressState = progressState,
            debugUnlockAll = debugUnlockAll
        )
    }
}

private fun DrawScope.drawWheelContents(
    geom: WheelGeometry,
    currentRot: Float,
    levels: List<LevelDefinition>,
    stepRad: Float,
    progressState: AppProgressState,
    debugUnlockAll: Boolean
) {
    val focusedIndex = GeometryHelpers.activeIndexOnWheel(currentRot.toDouble(), levels.size)
    val center = Offset(geom.cx, geom.cy)

    drawCircle(color = IosThemeColors.wheelOuterDisk, radius = geom.outerRadius, center = center)
    drawCircle(
        color = IosThemeColors.wheelOuterRim,
        radius = geom.outerRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )
    drawLine(
        color = IosThemeColors.wheelIndicator,
        start = Offset(geom.cx, geom.cy - geom.innerRadius - 4.dp.toPx()),
        end = Offset(geom.cx, geom.cy - geom.outerRadius + 6.dp.toPx()),
        strokeWidth = 2.4.dp.toPx(),
        cap = StrokeCap.Round
    )

    levels.forEachIndexed { idx, level ->
        drawRadialWheelItem(
            idx = idx,
            level = level,
            geom = geom,
            angleRad = idx * stepRad + currentRot,
            isFocused = idx == focusedIndex,
            progressState = progressState,
            debugUnlockAll = debugUnlockAll
        )
    }

    drawInnerPlanetHub(
        geom = geom,
        activeLevel = levels[focusedIndex],
        progressState = progressState,
        debugUnlockAll = debugUnlockAll
    )
}

private fun DrawScope.drawRadialWheelItem(
    idx: Int,
    level: LevelDefinition,
    geom: WheelGeometry,
    angleRad: Float,
    isFocused: Boolean,
    progressState: AppProgressState,
    debugUnlockAll: Boolean
) {
    val ix = geom.cx + geom.orbitRadius * sin(angleRad)
    val iy = geom.cy - geom.orbitRadius * cos(angleRad)
    val itemSizePx = geom.baseItemPx * if (isFocused) 1.24f else 0.92f
    val unlocked = PuzzleLevels.isUnlocked(
        level = level,
        unlockedAll = progressState.unlockedAll,
        extraUnlockedIds = progressState.unlockedLevelIds,
        debugUnlockAll = debugUnlockAll
    )
    val completed = progressState.isLevelCompleted(level)
    val bitmap = IosIllustrationRenderer.getFullIllustration(level, geom.itemCachePx, geom.itemCachePx)

    if (isFocused) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(ix, iy),
                radius = itemSizePx * 0.78f
            ),
            radius = itemSizePx * 0.78f,
            center = Offset(ix, iy)
        )
    }

    rotate(degrees = Math.toDegrees(angleRad.toDouble()).toFloat(), pivot = Offset(ix, iy)) {
        drawImage(
            image = bitmap,
            srcSize = IntSize(bitmap.width, bitmap.height),
            dstOffset = IntOffset((ix - itemSizePx * 0.5f).roundToInt(), (iy - itemSizePx * 0.5f).roundToInt()),
            dstSize = IntSize(itemSizePx.roundToInt(), itemSizePx.roundToInt()),
            alpha = if (unlocked) 1f else 0.84f
        )
        if (!unlocked) {
            drawLockBadge(ix + itemSizePx * 0.31f, iy + itemSizePx * 0.31f, itemSizePx * 0.145f)
        } else if (completed) {
            drawStarBadge(ix + itemSizePx * 0.31f, iy - itemSizePx * 0.29f, itemSizePx * 0.145f)
        }
    }
    if (idx < 0) Unit
}

private fun DrawScope.drawInnerPlanetHub(
    geom: WheelGeometry,
    activeLevel: LevelDefinition,
    progressState: AppProgressState,
    debugUnlockAll: Boolean
) {
    val center = Offset(geom.cx, geom.cy)
    drawCircle(
        brush = Brush.verticalGradient(
            colors = listOf(IosThemeColors.wheelInnerTop, IosThemeColors.wheelInnerBottom),
            startY = geom.cy - geom.innerRadius,
            endY = geom.cy + geom.innerRadius
        ),
        radius = geom.innerRadius,
        center = center
    )
    drawOval(
        color = Color.White.copy(alpha = 0.36f),
        topLeft = Offset(geom.cx - geom.innerRadius * 0.62f, geom.cy - geom.innerRadius * 0.84f),
        size = Size(geom.innerRadius * 1.24f, geom.innerRadius * 0.48f)
    )
    drawCircle(
        color = IosThemeColors.wheelInnerStroke,
        radius = geom.innerRadius,
        center = center,
        style = Stroke(width = 5.dp.toPx())
    )

    val heroBitmap = IosIllustrationRenderer.getFullIllustration(
        activeLevel,
        geom.centerHeroCachePx,
        geom.centerHeroCachePx
    )
    val heroDrawSize = (geom.innerRadius * 1.46f).roundToInt()
    drawImage(
        image = heroBitmap,
        srcSize = IntSize(heroBitmap.width, heroBitmap.height),
        dstOffset = IntOffset(
            (geom.cx - heroDrawSize * 0.5f).roundToInt(),
            (geom.cy - heroDrawSize * 0.5f).roundToInt()
        ),
        dstSize = IntSize(heroDrawSize, heroDrawSize)
    )
    val unlocked = PuzzleLevels.isUnlocked(
        level = activeLevel,
        unlockedAll = progressState.unlockedAll,
        extraUnlockedIds = progressState.unlockedLevelIds,
        debugUnlockAll = debugUnlockAll
    )
    if (!unlocked) {
        drawLockBadge(
            cx = geom.cx + geom.innerRadius * 0.52f,
            cy = geom.cy + geom.innerRadius * 0.48f,
            radius = geom.innerRadius * 0.20f
        )
    }
}

private fun handleWheelTap(
    tapOffset: Offset,
    geom: WheelGeometry,
    animatedRotation: Animatable<Float, AnimationVector1D>,
    wheelViewModel: WheelViewModel,
    stepRad: Float,
    scope: CoroutineScope,
    onWheelTick: () -> Unit,
    onFocusIndexChanged: (Int) -> Unit,
    onLaunchLevel: (LevelDefinition) -> Unit
) {
    val distCenter = GeometryHelpers.distance(tapOffset.x, tapOffset.y, geom.cx, geom.cy)
    if (distCenter <= geom.innerRadius * 1.05f) {
        onLaunchLevel(wheelViewModel.activeLevel)
    } else if (distCenter <= geom.outerRadius * 1.08f) {
        val clickedIdx = findClosestRadialItem(
            tapOffset = tapOffset,
            geom = geom,
            rotationRad = animatedRotation.value,
            stepRad = stepRad,
            count = wheelViewModel.levels.size
        )
        if (clickedIdx != null) {
            if (clickedIdx == wheelViewModel.activeLevelIndex) {
                onLaunchLevel(wheelViewModel.levels[clickedIdx])
            } else {
                wheelViewModel.focusLevel(clickedIdx)
                onWheelTick()
                onFocusIndexChanged(clickedIdx)
                animateWheelDelta(scope, animatedRotation, wheelViewModel.rotationRad)
            }
        }
    }
}

private fun animateWheelDelta(
    scope: CoroutineScope,
    animatedRotation: Animatable<Float, AnimationVector1D>,
    targetRad: Float
) {
    scope.launch {
        val delta = GeometryHelpers.shortestAngularDelta(
            animatedRotation.value.toDouble(),
            targetRad.toDouble()
        ).toFloat()
        animatedRotation.animateTo(
            targetValue = animatedRotation.value + delta,
            animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
        )
    }
}

private fun computeWheelGeometry(widthPx: Float, heightPx: Float): WheelGeometry {
    val isLandscape = widthPx > heightPx * 1.12f
    val outerDiameterPx = if (isLandscape) {
        min(widthPx * 0.62f, heightPx * 0.76f)
    } else {
        min(widthPx * 0.90f, heightPx * 0.58f)
    }
    val innerRadius = outerDiameterPx * GameConfig.WHEEL_INNER_DIAMETER_RATIO * 0.50f
    val baseItemPx = outerDiameterPx * GameConfig.WHEEL_ITEM_SIZE_RATIO
    return WheelGeometry(
        cx = widthPx * 0.50f,
        cy = if (isLandscape) heightPx * 0.48f else heightPx * GameConfig.WHEEL_CENTER_Y_FRACTION,
        outerRadius = outerDiameterPx * 0.50f,
        innerRadius = innerRadius,
        orbitRadius = outerDiameterPx * GameConfig.WHEEL_ORBIT_RADIUS_RATIO,
        baseItemPx = baseItemPx,
        itemCachePx = (baseItemPx * 1.2f).roundToInt().coerceIn(72, 220),
        centerHeroCachePx = (innerRadius * 1.56f).roundToInt().coerceIn(120, 320)
    )
}

private fun findClosestRadialItem(
    tapOffset: Offset,
    geom: WheelGeometry,
    rotationRad: Float,
    stepRad: Float,
    count: Int
): Int? {
    var bestIdx: Int? = null
    var bestDist = geom.baseItemPx * 0.72f
    for (idx in 0 until count) {
        val angleRad = idx * stepRad + rotationRad
        val ix = geom.cx + geom.orbitRadius * sin(angleRad)
        val iy = geom.cy - geom.orbitRadius * cos(angleRad)
        val d = GeometryHelpers.distance(tapOffset.x, tapOffset.y, ix, iy)
        if (d < bestDist) {
            bestDist = d
            bestIdx = idx
        }
    }
    return bestIdx
}

private fun DrawScope.drawLockBadge(cx: Float, cy: Float, radius: Float) {
    drawCircle(color = IosThemeColors.lockBadgeGold, radius = radius, center = Offset(cx, cy))
    drawCircle(
        color = Color.White,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = (radius * 0.16f).coerceAtLeast(1.5f))
    )
    drawArc(
        color = IosThemeColors.lockShackle,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - radius * 0.34f, cy - radius * 0.56f),
        size = Size(radius * 0.68f, radius * 0.58f),
        style = Stroke(width = (radius * 0.16f).coerceAtLeast(1.5f), cap = StrokeCap.Round)
    )
    drawRoundRect(
        color = IosThemeColors.lockShackle,
        topLeft = Offset(cx - radius * 0.44f, cy - radius * 0.10f),
        size = Size(radius * 0.88f, radius * 0.64f),
        cornerRadius = CornerRadius(radius * 0.18f, radius * 0.18f)
    )
}

private fun DrawScope.drawStarBadge(cx: Float, cy: Float, radius: Float) {
    drawCircle(color = Color.White, radius = radius * 1.08f, center = Offset(cx, cy))
    val path = Path()
    for (i in 0 until 10) {
        val a = -PI / 2.0 + i * PI / 5.0
        val r = if (i % 2 == 0) radius * 0.88f else radius * 0.40f
        val px = cx + cos(a).toFloat() * r
        val py = cy + sin(a).toFloat() * r
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path = path, color = IosThemeColors.completedStarGold)
    drawPath(
        path = path,
        color = IosThemeColors.outlineInk,
        style = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

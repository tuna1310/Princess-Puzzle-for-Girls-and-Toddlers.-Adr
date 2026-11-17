package com.princesspuzzle.storybook.puzzle

import android.graphics.Region
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.princesspuzzle.storybook.art.ConfettiParticleSystem
import com.princesspuzzle.storybook.art.StorybookPalette
import com.princesspuzzle.storybook.audio.AudioEngine
import com.princesspuzzle.storybook.audio.NoOpAudioEngine
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.PuzzleLayout
import com.princesspuzzle.storybook.core.computePuzzleLayout
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PieceGeometryCache(
    val boardPath: Path,
    val localPath: Path,
    val hitRegion: Region,
    val spriteBitmap: ImageBitmap? = null,
    val spriteOffsetX: Float = 0f,
    val spriteOffsetY: Float = 0f,
    val traySpriteBitmap: ImageBitmap? = null,
    val traySpriteOffsetX: Float = 0f,
    val traySpriteOffsetY: Float = 0f
)

data class PuzzleUiState(
    val level: LevelDefinition = PuzzleLevels.getById(2),
    val placedCount: Int = 0,
    val totalCount: Int = 3,
    val completed: Boolean = false,
    val showCelebrationActions: Boolean = false
)

private const val SLICER_SEED_MULT = 17
private const val SHUFFLE_SEED_MULT = 31
private const val MIN_HIT_SCALE = 0.2f
private const val REGION_HIT_PAD_DP = 10f
private const val SNAP_SPARKLE_RADIUS_RATIO = 0.25f
private const val SPRITE_STROKE_PAD_DP = 5f
private const val SPRITE_SHADOW_ELEVATED_DP = 7f
private const val SPRITE_SHADOW_REST_DP = 3f
private const val SPRITE_BEVEL_STROKE_DP = 4f
private const val SPRITE_INK_STROKE_DP = 2.4f
private const val SPRITE_SHADOW_STROKE_DP = 5f
private const val COLOR_SPRITE_BEVEL = 0x8CFFFFFF
private const val COLOR_SPRITE_SHADOW_HIGH = 0x476A496E
private const val COLOR_SPRITE_SHADOW_LOW = 0x286A496E
private const val GLOW_ACTIVE_EPSILON = 0.01f
private const val ROTATION_ACTIVE_EPSILON = 0.002f
private const val DEFAULT_TRAY_SCALE = 0.5f

class PuzzleViewModel(
    initialLevelId: Int = 2,
    private val audio: AudioEngine = NoOpAudioEngine,
    private val onLevelCompleted: (Int, Int) -> Unit = { _, _ -> }
) : ViewModel() {

    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    var currentSpecs: List<JigsawPieceSpec> = emptyList()
        private set
    var currentLayout: PuzzleLayout? = null
        private set
    var physicsEngine: PuzzlePhysicsEngine? = null
        private set

    val particles = ConfettiParticleSystem()
    val geometryCache = HashMap<Int, PieceGeometryCache>()
    private var cachedBoardW = -1
    private var cachedBoardH = -1
    private var cachedArtwork: ImageBitmap? = null

    var elapsedTimeSec: Float = 0f
        private set
    var winTimerSec: Float = 0f
        private set
    var seamAlpha: Float = 1f
        private set
    var boardRevision: Int = 0
        private set
    private var secondBurstTriggered = false

    private var activeLevelId: Int = initialLevelId
    private var activePieceCount: Int = PuzzleLevels.getById(initialLevelId).pieceCount

    init {
        loadPuzzle(levelId = initialLevelId)
    }

    fun loadPuzzle(levelId: Int) {
        val level = PuzzleLevels.getById(levelId)
        val count = level.pieceCount
        activeLevelId = level.id
        activePieceCount = count

        currentSpecs = sliceJigsawPieces(count, seed = level.id * SLICER_SEED_MULT + count)
        physicsEngine = null
        currentLayout = null
        cachedBoardW = -1
        cachedBoardH = -1
        cachedArtwork = null
        geometryCache.clear()
        particles.clear()
        boardRevision++

        winTimerSec = 0f
        seamAlpha = 1f
        secondBurstTriggered = false

        _uiState.value = PuzzleUiState(
            level = level,
            placedCount = 0,
            totalCount = currentSpecs.size,
            completed = false,
            showCelebrationActions = false
        )
    }

    fun replay() {
        audio.playTap()
        loadPuzzle(activeLevelId)
    }

    fun advanceToNextLevel() {
        audio.playTap()
        val next = PuzzleLevels.nextAfter(activeLevelId)?.id ?: PuzzleLevels.ALL.first().id
        loadPuzzle(next)
    }

    fun nextLevelOrDemo() {
        advanceToNextLevel()
    }

    private fun isLayoutUpToDate(existing: PuzzleLayout?, widthPx: Float, heightPx: Float, density: Float): Boolean {
        return existing != null &&
            existing.width == widthPx &&
            existing.height == heightPx &&
            existing.density == density
    }

    fun ensureViewportLayout(
        widthPx: Float,
        heightPx: Float,
        density: Float,
        artwork: ImageBitmap? = null
    ): PuzzleLayout {
        val existing = currentLayout
        if (isLayoutUpToDate(existing, widthPx, heightPx, density) &&
            physicsEngine != null &&
            cachedArtwork === artwork
        ) {
            return existing!!
        }

        val layout = if (isLayoutUpToDate(existing, widthPx, heightPx, density)) {
            existing!!
        } else {
            computePuzzleLayout(widthPx, heightPx, density, currentSpecs)
        }
        currentLayout = layout

        val engine = physicsEngine
        if (engine == null) {
            physicsEngine = PuzzlePhysicsEngine(
                pieces = currentSpecs,
                initialLayout = layout,
                seed = activeLevelId * SHUFFLE_SEED_MULT + activePieceCount
            )
        } else {
            engine.syncToLayout(layout)
        }

        rebuildGeometryCacheIfNeeded(layout, artwork)
        boardRevision++
        return layout
    }

    private fun rebuildGeometryCacheIfNeeded(layout: PuzzleLayout, artwork: ImageBitmap?) {
        val bw = layout.board.w.roundToInt()
        val bh = layout.board.h.roundToInt()
        val sizeMatches = bw == cachedBoardW && bh == cachedBoardH
        if (sizeMatches && cachedArtwork === artwork && geometryCache.size == currentSpecs.size) {
            return
        }

        cachedBoardW = bw
        cachedBoardH = bh
        cachedArtwork = artwork
        geometryCache.clear()

        val clipRegion = Region(-bw, -bh, bw, bh)
        val boardSize = IntSize(bw.coerceAtLeast(1), bh.coerceAtLeast(1))
        val trayScale = layout.traySlots.firstOrNull()?.scale ?: DEFAULT_TRAY_SCALE
        for (spec in currentSpecs) {
            val boardPath = buildComposePath(spec, layout.board.w, layout.board.h, 0f, 0f)
            val localPath = buildComposePath(
                spec,
                layout.board.w,
                layout.board.h,
                -spec.centerX * layout.board.w,
                -spec.centerY * layout.board.h
            )
            val region = Region()
            region.setPath(localPath.asAndroidPath(), clipRegion)
            geometryCache[spec.id] = PieceSpriteBaker.buildCachedGeometry(
                spec = spec,
                boardPath = boardPath,
                localPath = localPath,
                region = region,
                layout = layout,
                boardSize = boardSize,
                trayScale = trayScale,
                artwork = artwork
            )
        }
    }

    fun isDragging(): Boolean = physicsEngine?.isActivelyDragging() == true

    fun shouldRenderFullRate(): Boolean {
        val engine = physicsEngine
        val uiActive = _uiState.value.completed || particles.hasActiveParticles()
        val engineActive = engine != null && (engine.hintPieceIndex >= 0 || hasAnimatingPiece(engine))
        return uiActive || engineActive
    }

    private fun hasAnimatingPiece(engine: PuzzlePhysicsEngine): Boolean {
        for (piece in engine.runtimes) {
            val scaleSettling = abs(piece.targetScale - piece.scale) > GameConfig.SCALE_SNAP_EPSILON
            val rotSettling = abs(piece.rotation) > ROTATION_ACTIVE_EPSILON
            val glowSettling = piece.glowIntensity > GLOW_ACTIVE_EPSILON &&
                piece.glowIntensity < 1f - GLOW_ACTIVE_EPSILON
            val nonDragMotion = piece.status == PieceStatus.RETURNING || piece.status == PieceStatus.SNAPPING
            val visualBusy = nonDragMotion || scaleSettling || rotSettling
            if (visualBusy || glowSettling) {
                return true
            }
        }
        return false
    }

    private fun isPointInsidePiece(piece: PieceRuntime, px: Float, py: Float, layout: PuzzleLayout): Boolean {
        val invScale = 1f / max(MIN_HIT_SCALE, piece.scale)
        val lx = (px - piece.x) * invScale
        val ly = (py - piece.y) * invScale
        val localPad = GameConfig.PIECE_HIT_PADDING_DP * layout.density * invScale
        val bw = layout.board.w
        val bh = layout.board.h

        val inBounds = lx >= (piece.spec.bounds.minX - piece.spec.centerX) * bw - localPad &&
            lx <= (piece.spec.bounds.maxX - piece.spec.centerX) * bw + localPad &&
            ly >= (piece.spec.bounds.minY - piece.spec.centerY) * bh - localPad &&
            ly <= (piece.spec.bounds.maxY - piece.spec.centerY) * bh + localPad

        val geom = geometryCache[piece.spec.id]
        if (!inBounds || geom == null) return false

        val ix = lx.roundToInt()
        val iy = ly.roundToInt()
        val tol = (REGION_HIT_PAD_DP * layout.density * invScale).roundToInt()
        return geom.hitRegion.contains(ix, iy) ||
            geom.hitRegion.contains(ix - tol, iy) ||
            geom.hitRegion.contains(ix + tol, iy) ||
            geom.hitRegion.contains(ix, iy - tol) ||
            geom.hitRegion.contains(ix, iy + tol)
    }

    fun hitTestPieceIndex(px: Float, py: Float): Int {
        val engine = physicsEngine
        val layout = currentLayout
        var bestIndex = -1

        if (engine != null && layout != null) {
            var bestZ = Int.MIN_VALUE
            for (i in engine.runtimes.indices) {
                val piece = engine.runtimes[i]
                val grabbable = piece.status == PieceStatus.TRAY || piece.status == PieceStatus.RETURNING
                if (grabbable && piece.zIndex > bestZ && isPointInsidePiece(piece, px, py, layout)) {
                    bestIndex = i
                    bestZ = piece.zIndex
                }
            }
        }
        return bestIndex
    }

    fun onPointerDown(pointerId: Long, x: Float, y: Float, isTouch: Boolean, nowMillis: Long): Boolean {
        val engine = physicsEngine
        val layout = currentLayout
        val ready = engine != null && layout != null && !_uiState.value.completed
        val idx = if (ready && !engine!!.hasActivePointer()) {
            hitTestPieceIndex(x, y)
        } else {
            -1
        }
        val grabbed = idx >= 0 && engine != null && layout != null
        if (grabbed) {
            engine!!.startDrag(idx, pointerId, x, y, layout!!, isTouch, nowMillis)
            audio.playPickup()
        }
        return grabbed
    }

    fun onPointerMove(pointerId: Long, x: Float, y: Float, nowMillis: Long): Boolean {
        val engine = physicsEngine
        val layout = currentLayout
        var moved = false
        if (engine != null && layout != null && engine.isTrackingPointer(pointerId)) {
            engine.moveDrag(pointerId, x, y, layout, nowMillis)
            moved = true
        }
        return moved
    }

    fun onPointerUp(pointerId: Long, nowMillis: Long): Boolean {
        val engine = physicsEngine
        val layout = currentLayout
        var handled = false
        if (engine != null && layout != null && engine.isTrackingPointer(pointerId)) {
            handleTrackedPointerUp(engine, layout, pointerId, nowMillis)
            handled = true
        }
        return handled
    }

    private fun handleTrackedPointerUp(
        engine: PuzzlePhysicsEngine,
        layout: PuzzleLayout,
        pointerId: Long,
        nowMillis: Long
    ) {
        val snapped = engine.endDrag(pointerId, layout, nowMillis)
        if (snapped) {
            var placed = 0
            var active: PieceRuntime? = null
            for (piece in engine.runtimes) {
                if (piece.status == PieceStatus.PLACED || piece.status == PieceStatus.SNAPPING) {
                    placed++
                }
                if (piece.status == PieceStatus.SNAPPING && active == null) {
                    active = piece
                }
            }
            audio.playSnap(placed, engine.runtimes.size)
            if (active != null) {
                particles.emitSnapSparkles(
                    active.snapTargetX,
                    active.snapTargetY,
                    layout.density,
                    min(layout.board.w, layout.board.h) * SNAP_SPARKLE_RADIUS_RATIO
                )
            }
        } else {
            audio.playReturnBounce()
        }
    }

    fun onPointerCancel(pointerId: Long): Boolean {
        val engine = physicsEngine
        val layout = currentLayout
        var handled = false
        if (engine != null && layout != null && engine.isTrackingPointer(pointerId)) {
            engine.cancelActiveDrag(layout)
            audio.playReturnBounce()
            handled = true
        }
        return handled
    }

    fun onFrame(dtSec: Float) {
        val clampedDt = dtSec.coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
        elapsedTimeSec += clampedDt
        if (!isDragging() && particles.hasActiveParticles()) {
            particles.update(clampedDt)
        }

        val engine = physicsEngine ?: return
        val layout = currentLayout ?: return
        val prevHint = engine.hintPieceIndex
        val res = engine.step(clampedDt, layout)

        val cur = _uiState.value
        if (res.placedCount != cur.placedCount) {
            _uiState.value = cur.copy(placedCount = res.placedCount)
            boardRevision++
        } else if (res.justSnappedPieceId >= 0 || engine.hintPieceIndex != prevHint) {
            boardRevision++
        }

        if (!cur.completed && res.allCompleted) {
            winTimerSec = 0f
            secondBurstTriggered = false
            audio.playWinFanfare()
            particles.emitCelebrationFireworks(layout.width, layout.height, layout.density)
            onLevelCompleted(cur.level.id, GameConfig.MAX_STARS)
            boardRevision++
            _uiState.value = _uiState.value.copy(
                placedCount = engine.runtimes.size,
                completed = true,
                showCelebrationActions = false
            )
        }

        if (_uiState.value.completed) {
            winTimerSec += clampedDt
            seamAlpha = max(0f, 1f - winTimerSec / GameConfig.SEAM_FADE_DURATION_SEC)
            if (!secondBurstTriggered && winTimerSec >= GameConfig.SECOND_CONFETTI_DELAY_SEC) {
                secondBurstTriggered = true
                particles.emitCelebrationFireworks(layout.width, layout.height, layout.density)
            }
            if (!_uiState.value.showCelebrationActions &&
                winTimerSec >= GameConfig.CELEBRATION_PANEL_DELAY_SEC
            ) {
                _uiState.value = _uiState.value.copy(showCelebrationActions = true)
            }
        }
    }

    override fun onCleared() {
        geometryCache.clear()
        cachedArtwork = null
        particles.clear()
        audio.release()
        super.onCleared()
    }
}

private object PieceSpriteBaker {
    fun buildCachedGeometry(
        spec: JigsawPieceSpec,
        boardPath: Path,
        localPath: Path,
        region: Region,
        layout: PuzzleLayout,
        boardSize: IntSize,
        trayScale: Float,
        artwork: ImageBitmap?
    ): PieceGeometryCache {
        if (artwork == null) {
            return PieceGeometryCache(boardPath, localPath, region)
        }
        val (dragBmp, dragOffX, dragOffY) = renderBakedPieceSprite(
            spec = spec,
            localPath = localPath,
            layout = layout,
            boardSize = boardSize,
            artwork = artwork,
            spriteScale = 1f,
            shadowColor = COLOR_SPRITE_SHADOW_HIGH,
            shadowYDp = SPRITE_SHADOW_ELEVATED_DP
        )
        val (trayBmp, trayOffX, trayOffY) = renderBakedPieceSprite(
            spec = spec,
            localPath = localPath,
            layout = layout,
            boardSize = boardSize,
            artwork = artwork,
            spriteScale = trayScale,
            shadowColor = COLOR_SPRITE_SHADOW_LOW,
            shadowYDp = SPRITE_SHADOW_REST_DP
        )
        return PieceGeometryCache(
            boardPath = boardPath,
            localPath = localPath,
            hitRegion = region,
            spriteBitmap = dragBmp,
            spriteOffsetX = dragOffX,
            spriteOffsetY = dragOffY,
            traySpriteBitmap = trayBmp,
            traySpriteOffsetX = trayOffX,
            traySpriteOffsetY = trayOffY
        )
    }

    private fun renderBakedPieceSprite(
        spec: JigsawPieceSpec,
        localPath: Path,
        layout: PuzzleLayout,
        boardSize: IntSize,
        artwork: ImageBitmap,
        spriteScale: Float,
        shadowColor: Int,
        shadowYDp: Float
    ): Triple<ImageBitmap, Float, Float> {
        val bounds = localPath.getBounds()
        val sidePad = SPRITE_STROKE_PAD_DP * layout.density
        val botPad = (SPRITE_STROKE_PAD_DP + shadowYDp) * layout.density
        val offsetX = floor((bounds.left - sidePad) * spriteScale)
        val offsetY = floor((bounds.top - sidePad) * spriteScale)
        val spriteW = ceil((bounds.right + sidePad) * spriteScale - offsetX).toInt().coerceAtLeast(1)
        val spriteH = ceil((bounds.bottom + botPad) * spriteScale - offsetY).toInt().coerceAtLeast(1)
        val sprite = ImageBitmap(spriteW, spriteH)
        val canvas = Canvas(sprite)
        val shadowStroke = Stroke(width = SPRITE_SHADOW_STROKE_DP * layout.density, join = StrokeJoin.Round)
        val bevelStroke = Stroke(width = SPRITE_BEVEL_STROKE_DP * layout.density, join = StrokeJoin.Round)
        val inkStroke = Stroke(width = SPRITE_INK_STROKE_DP * layout.density, join = StrokeJoin.Round)
        val artDstOffset = IntOffset(
            (-spec.centerX * layout.board.w).roundToInt(),
            (-spec.centerY * layout.board.h).roundToInt()
        )
        CanvasDrawScope().draw(
            density = Density(layout.density),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(spriteW.toFloat(), spriteH.toFloat())
        ) {
            translate(-offsetX, -offsetY) {
                scale(spriteScale, spriteScale, pivot = Offset.Zero) {
                    translate(0f, shadowYDp * layout.density) {
                        drawPath(localPath, color = Color(shadowColor), style = shadowStroke)
                    }
                    clipPath(localPath) {
                        drawImage(image = artwork, dstOffset = artDstOffset, dstSize = boardSize)
                    }
                    drawPath(localPath, color = Color(COLOR_SPRITE_BEVEL), style = bevelStroke)
                    drawPath(localPath, color = StorybookPalette.Ink, style = inkStroke)
                }
            }
        }
        return Triple(sprite, offsetX, offsetY)
    }
}

class PuzzleViewModelFactory(
    private val levelId: Int,
    private val audio: AudioEngine = NoOpAudioEngine,
    private val onLevelCompleted: (Int, Int) -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PuzzleViewModel(
            initialLevelId = levelId,
            audio = audio,
            onLevelCompleted = onLevelCompleted
        ) as T
    }
}

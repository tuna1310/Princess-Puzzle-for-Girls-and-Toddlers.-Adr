package com.princesspuzzle.storybook.puzzle

import android.graphics.Region
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import com.princesspuzzle.storybook.art.ConfettiParticleSystem
import com.princesspuzzle.storybook.audio.AudioEngine
import com.princesspuzzle.storybook.audio.NoOpAudioEngine
import com.princesspuzzle.storybook.core.GameConfig
import com.princesspuzzle.storybook.core.PuzzleLayout
import com.princesspuzzle.storybook.core.computePuzzleLayout
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
    val spriteOffsetY: Float = 0f
)

data class PuzzleUiState(
    val level: LevelDefinition = PuzzleLevels.getById(2),
    val pieceCountOverride: Int? = null,
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
private const val SPRITE_PAD_DP = 2f
private const val GLOW_ACTIVE_EPSILON = 0.01f

private val DEMO_SEQUENCE = listOf(
    // Level 2: Clover Bunny Princess (3 pieces - Step 1 & 2 default)
    2 to 3,
    // Level 1: Starlight Tiara (2 pieces)
    1 to 2,
    // Level 3: Cloudberry Unicorn (4 pieces)
    3 to 4,
    // Level 4: Cupcake Blossom Castle (4 pieces)
    4 to 4,
    // 6-piece grid demo on Unicorn
    3 to 6,
    // 9-piece grid demo on Castle
    4 to 9,
    // 12-piece grid demo on Bunny Princess
    2 to 12
)

class PuzzleViewModel(
    private val audio: AudioEngine = NoOpAudioEngine
) : ViewModel() {

    private var demoCursor = 0
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
    private var secondBurstTriggered = false

    private var activeLevelId: Int = 2
    private var activePieceCount: Int = 3

    init {
        loadPuzzle(levelId = 2, pieceCount = 3)
    }

    fun loadPuzzle(levelId: Int, pieceCount: Int? = null) {
        val level = PuzzleLevels.getById(levelId)
        val count = pieceCount ?: level.pieceCount
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

        winTimerSec = 0f
        seamAlpha = 1f
        secondBurstTriggered = false

        _uiState.value = PuzzleUiState(
            level = level,
            pieceCountOverride = if (count == level.pieceCount) null else count,
            placedCount = 0,
            totalCount = currentSpecs.size,
            completed = false,
            showCelebrationActions = false
        )
    }

    fun replay() {
        audio.playTap()
        loadPuzzle(activeLevelId, activePieceCount)
    }

    fun cycleDemoPuzzle() {
        audio.playTap()
        demoCursor = (demoCursor + 1) % DEMO_SEQUENCE.size
        val (lvlId, count) = DEMO_SEQUENCE[demoCursor]
        loadPuzzle(lvlId, count)
    }

    fun nextLevelOrDemo() {
        cycleDemoPuzzle()
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
            geometryCache[spec.id] = buildCachedGeometry(
                spec = spec,
                boardPath = boardPath,
                localPath = localPath,
                region = region,
                layout = layout,
                boardSize = boardSize,
                artwork = artwork
            )
        }
    }

    private fun buildCachedGeometry(
        spec: JigsawPieceSpec,
        boardPath: Path,
        localPath: Path,
        region: Region,
        layout: PuzzleLayout,
        boardSize: IntSize,
        artwork: ImageBitmap?
    ): PieceGeometryCache {
        if (artwork == null) {
            return PieceGeometryCache(boardPath, localPath, region)
        }
        val bounds = localPath.getBounds()
        val pad = SPRITE_PAD_DP * layout.density
        val offsetX = floor(bounds.left - pad)
        val offsetY = floor(bounds.top - pad)
        val spriteW = ceil(bounds.right + pad - offsetX).toInt().coerceAtLeast(1)
        val spriteH = ceil(bounds.bottom + pad - offsetY).toInt().coerceAtLeast(1)
        val sprite = ImageBitmap(spriteW, spriteH)
        val canvas = Canvas(sprite)
        CanvasDrawScope().draw(
            density = Density(layout.density),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(spriteW.toFloat(), spriteH.toFloat())
        ) {
            translate(-offsetX, -offsetY) {
                clipPath(localPath) {
                    drawImage(
                        image = artwork,
                        dstOffset = IntOffset(
                            (-spec.centerX * layout.board.w).roundToInt(),
                            (-spec.centerY * layout.board.h).roundToInt()
                        ),
                        dstSize = boardSize
                    )
                }
            }
        }
        return PieceGeometryCache(
            boardPath = boardPath,
            localPath = localPath,
            hitRegion = region,
            spriteBitmap = sprite,
            spriteOffsetX = offsetX,
            spriteOffsetY = offsetY
        )
    }

    fun shouldRenderFullRate(): Boolean {
        val engine = physicsEngine
        val uiActive = _uiState.value.completed || particles.hasActiveParticles()
        val engineActive = engine != null &&
            (engine.activePieceIndex >= 0 || engine.hintPieceIndex >= 0 || hasAnimatingPiece(engine))
        return uiActive || engineActive
    }

    private fun hasAnimatingPiece(engine: PuzzlePhysicsEngine): Boolean {
        for (piece in engine.runtimes) {
            val moving = piece.status == PieceStatus.DRAGGING ||
                piece.status == PieceStatus.RETURNING ||
                piece.status == PieceStatus.SNAPPING
            if (moving || piece.glowIntensity > GLOW_ACTIVE_EPSILON) {
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

    fun onPointerMove(pointerId: Long, x: Float, y: Float, nowMillis: Long) {
        val engine = physicsEngine ?: return
        val layout = currentLayout ?: return
        engine.moveDrag(pointerId, x, y, layout, nowMillis)
    }

    fun onPointerUp(pointerId: Long, nowMillis: Long) {
        val engine = physicsEngine ?: return
        val layout = currentLayout ?: return
        if (engine.isTrackingPointer(pointerId)) {
            val snapped = engine.endDrag(pointerId, layout, nowMillis)
            if (snapped) {
                val placed = engine.runtimes.count {
                    it.status == PieceStatus.PLACED || it.status == PieceStatus.SNAPPING
                }
                val active = engine.runtimes.firstOrNull { it.status == PieceStatus.SNAPPING }
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
    }

    fun onPointerCancel(pointerId: Long) {
        val engine = physicsEngine ?: return
        val layout = currentLayout ?: return
        if (engine.isTrackingPointer(pointerId)) {
            engine.cancelActiveDrag(layout)
            audio.playReturnBounce()
        }
    }

    fun onFrame(dtSec: Float) {
        val clampedDt = dtSec.coerceIn(GameConfig.MIN_FRAME_DELTA_SEC, GameConfig.MAX_FRAME_DELTA_SEC)
        elapsedTimeSec += clampedDt
        particles.update(clampedDt)

        val engine = physicsEngine ?: return
        val layout = currentLayout ?: return
        val res = engine.step(clampedDt, layout)

        val cur = _uiState.value
        if (res.placedCount != cur.placedCount) {
            _uiState.value = cur.copy(placedCount = res.placedCount)
        }

        if (!cur.completed && res.allCompleted) {
            winTimerSec = 0f
            secondBurstTriggered = false
            audio.playWinFanfare()
            particles.emitCelebrationFireworks(layout.width, layout.height, layout.density)
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
        audio.release()
        super.onCleared()
    }
}

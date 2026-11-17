import {
  drawLivingCelebrationOverlay,
  getCachedLevelArtwork
} from '../art/illustrations';
import { STORYBOOK_INK, traceRoundedRect } from '../art/drawUtils';
import type { SoundEngine } from '../audio/soundEngine';
import {
  computePuzzleLayout,
  pointInRect,
  type PuzzleLayout,
  type Rect
} from '../core/layout';
import { t } from '../i18n';
import type { ProgressStore } from '../storage/progressStore';
import type { PurchaseService } from '../storage/purchaseService';
import {
  drawBackArrowIcon,
  drawCircleButton,
  drawPillButton,
  drawSpeakerIcon,
  drawStarRow,
  ROUNDED_FONT_FAMILY
} from '../ui/canvasControls';
import {
  buildPiecePath2D,
  sliceJigsawPieces,
  type JigsawPieceSpec
} from './jigsawSlicer';
import { isLevelUnlocked, LEVELS, type LevelDefinition } from './levels';
import { ParticleSystem } from './particles';
import {
  computeTouchLiftOffset,
  createPieceRuntimes,
  releaseDraggedPiece,
  stepPiecesPhysics,
  syncPiecesToLayout,
  type PieceRuntime
} from './physics';

interface CachedPieceGeometry {
  boardPath: Path2D;
  localPath: Path2D;
}

export class PuzzleScreen {
  private level: LevelDefinition;
  private specs: JigsawPieceSpec[] = [];
  private runtimes: PieceRuntime[] = [];
  private layout: PuzzleLayout;
  private artworkCanvas: HTMLCanvasElement;
  private geometryCache = new Map<number, CachedPieceGeometry>();
  private cachedBoardW = -1;
  private cachedBoardH = -1;
  private particles = new ParticleSystem();

  private activePointerId: number | null = null;
  private draggedPiece: PieceRuntime | null = null;
  private grabOffsetX = 0;
  private grabOffsetY = 0;
  private lastMoveTime = 0;
  private topZ = 20;

  private completed = false;
  private winTimer = 0;
  private secondBurstDone = false;
  private seamAlpha = 1;
  private hintPulseTime = 0;
  private pressedAction: 'playAgain' | 'next' | 'home' | null = null;

  constructor(
    level: LevelDefinition,
    width: number,
    height: number,
    private readonly progress: ProgressStore,
    private readonly purchases: PurchaseService,
    private readonly sound: SoundEngine,
    private readonly onBackToHub: () => void,
    private readonly onLaunchLevel: (level: LevelDefinition) => void
  ) {
    this.level = level;
    this.artworkCanvas = getCachedLevelArtwork(level.theme, 800);
    this.specs = sliceJigsawPieces(level.pieceCount, level.id * 17 + 5);
    this.layout = computePuzzleLayout(width, height, this.specs);
    this.runtimes = createPieceRuntimes(this.specs, this.layout, level.id * 31 + 11);
    this.topZ = this.runtimes.length + 10;
    this.rebuildGeometryIfNeeded();
  }

  getLevel(): LevelDefinition {
    return this.level;
  }

  getSnapshot(): {
    levelId: number;
    completed: boolean;
    placedCount: number;
    totalCount: number;
    layout: PuzzleLayout;
    pieces: Array<{
      id: number;
      status: PieceRuntime['status'];
      x: number;
      y: number;
      snapTargetX: number;
      snapTargetY: number;
    }>;
  } {
    const placedCount = this.runtimes.filter(
      (p) => p.status === 'placed' || p.status === 'snapping'
    ).length;
    return {
      levelId: this.level.id,
      completed: this.completed,
      placedCount,
      totalCount: this.runtimes.length,
      layout: this.layout,
      pieces: this.runtimes.map((p) => ({
        id: p.spec.id,
        status: p.status,
        x: p.x,
        y: p.y,
        snapTargetX: p.snapTargetX,
        snapTargetY: p.snapTargetY
      }))
    };
  }

  resize(width: number, height: number): void {
    this.layout = computePuzzleLayout(width, height, this.specs);
    syncPiecesToLayout(this.runtimes, this.layout);
    this.rebuildGeometryIfNeeded();
  }

  private rebuildGeometryIfNeeded(): void {
    const bw = Math.round(this.layout.board.w);
    const bh = Math.round(this.layout.board.h);
    if (bw === this.cachedBoardW && bh === this.cachedBoardH) {
      return;
    }

    this.cachedBoardW = bw;
    this.cachedBoardH = bh;
    this.geometryCache.clear();

    for (const spec of this.specs) {
      const boardPath = buildPiecePath2D(spec, this.layout.board.w, this.layout.board.h, 0, 0);
      const localPath = buildPiecePath2D(
        spec,
        this.layout.board.w,
        this.layout.board.h,
        -spec.centerX * this.layout.board.w,
        -spec.centerY * this.layout.board.h
      );
      this.geometryCache.set(spec.id, { boardPath, localPath });
    }
  }

  private getTopButtons(layout: PuzzleLayout): {
    backBtn: { x: number; y: number; r: number };
    soundBtn: { x: number; y: number; r: number };
  } {
    const r = Math.max(20, Math.min(25, layout.topBar.h * 0.44));
    return {
      backBtn: {
        x: layout.topBar.x + r,
        y: layout.topBar.y + layout.topBar.h * 0.5,
        r
      },
      soundBtn: {
        x: layout.topBar.x + layout.topBar.w - r,
        y: layout.topBar.y + layout.topBar.h * 0.5,
        r
      }
    };
  }

  private getWinButtons(): {
    replayRect: Rect;
    nextRect: Rect;
    nextLevel: LevelDefinition | null;
  } {
    const tray = this.layout.tray;
    const btnW = Math.min(180, (tray.w - 36) * (tray.w > 330 && !this.layout.isLandscape ? 0.48 : 0.86));
    const btnH = 48;

    const nextCandidate = LEVELS.find((l) => l.id === this.level.id + 1) ?? null;
    const nextUnlocked =
      nextCandidate !== null &&
      isLevelUnlocked(nextCandidate, this.purchases.isUnlockedAll()) &&
      nextCandidate.implementedInStep2;

    if (!this.layout.isLandscape && tray.w > 320) {
      const totalW = btnW * 2 + 14;
      const startX = tray.x + (tray.w - totalW) * 0.5;
      const y = tray.y + tray.h - btnH - 14;
      return {
        replayRect: { x: startX, y, w: btnW, h: btnH },
        nextRect: { x: startX + btnW + 14, y, w: btnW, h: btnH },
        nextLevel: nextUnlocked ? nextCandidate : null
      };
    }

    const cx = tray.x + (tray.w - btnW) * 0.5;
    const bottomY = tray.y + tray.h - btnH - 12;
    return {
      replayRect: { x: cx, y: bottomY - btnH - 10, w: btnW, h: btnH },
      nextRect: { x: cx, y: bottomY, w: btnW, h: btnH },
      nextLevel: nextUnlocked ? nextCandidate : null
    };
  }

  private hitTestPiece(
    ctx: CanvasRenderingContext2D,
    px: number,
    py: number
  ): PieceRuntime | null {
    const candidates = [...this.runtimes]
      .filter((p) => p.status === 'tray' || p.status === 'returning')
      .sort((a, b) => b.zIndex - a.zIndex);

    const bw = this.layout.board.w;
    const bh = this.layout.board.h;

    for (const piece of candidates) {
      const geom = this.geometryCache.get(piece.spec.id);
      if (!geom) continue;

      const invScale = 1 / Math.max(0.2, piece.scale);
      const lx = (px - piece.x) * invScale;
      const ly = (py - piece.y) * invScale;

      // Fast local AABB pre-check with generous toddler touch padding
      const pad = 16 * invScale;
      const minX = (piece.spec.bounds.minX - piece.spec.centerX) * bw - pad;
      const maxX = (piece.spec.bounds.maxX - piece.spec.centerX) * bw + pad;
      const minY = (piece.spec.bounds.minY - piece.spec.centerY) * bh - pad;
      const maxY = (piece.spec.bounds.maxY - piece.spec.centerY) * bh + pad;

      if (lx < minX || lx > maxX || ly < minY || ly > maxY) {
        continue;
      }

      // Check center plus small cross offsets so tapping slightly outside thin edges still grabs
      const tol = 10 * invScale;
      if (
        ctx.isPointInPath(geom.localPath, lx, ly) ||
        ctx.isPointInPath(geom.localPath, lx - tol, ly) ||
        ctx.isPointInPath(geom.localPath, lx + tol, ly) ||
        ctx.isPointInPath(geom.localPath, lx, ly - tol) ||
        ctx.isPointInPath(geom.localPath, lx, ly + tol)
      ) {
        return piece;
      }
    }
    return null;
  }

  onPointerDown(
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    pointerId: number,
    pointerType: string
  ): void {
    this.sound.unlock();
    const { backBtn, soundBtn } = this.getTopButtons(this.layout);

    if (Math.hypot(x - backBtn.x, y - backBtn.y) <= backBtn.r + 10) {
      this.sound.playTap();
      this.onBackToHub();
      return;
    }

    if (Math.hypot(x - soundBtn.x, y - soundBtn.y) <= soundBtn.r + 10) {
      const next = !this.sound.isEnabled();
      this.sound.setEnabled(next);
      this.progress.setSoundEnabled(next);
      if (next) this.sound.playTap();
      return;
    }

    if (this.completed) {
      if (this.winTimer > 0.55) {
        const winBtns = this.getWinButtons();
        if (pointInRect(x, y, winBtns.replayRect, 6)) {
          this.pressedAction = 'playAgain';
          this.sound.playTap();
          this.onLaunchLevel(this.level);
          return;
        }
        if (pointInRect(x, y, winBtns.nextRect, 6)) {
          this.pressedAction = winBtns.nextLevel ? 'next' : 'home';
          this.sound.playTap();
          if (winBtns.nextLevel) {
            this.onLaunchLevel(winBtns.nextLevel);
          } else {
            this.onBackToHub();
          }
          return;
        }
      }
      return;
    }

    if (this.activePointerId !== null) {
      return;
    }

    const hit = this.hitTestPiece(ctx, x, y);
    if (!hit) {
      return;
    }

    this.activePointerId = pointerId;
    this.draggedPiece = hit;
    hit.status = 'dragging';
    hit.targetScale = 1;
    hit.vx = 0;
    hit.vy = 0;
    hit.zIndex = ++this.topZ;

    const liftOffset = computeTouchLiftOffset(this.layout.board, hit.spec, pointerType);
    this.grabOffsetX = hit.x - x;
    this.grabOffsetY = hit.y - y + liftOffset;
    if (pointerType === 'touch') {
      this.grabOffsetX *= 0.35;
      this.grabOffsetY = liftOffset;
    }

    this.lastMoveTime = performance.now();
    this.sound.playPickup();
  }

  onPointerMove(x: number, y: number, pointerId: number): void {
    if (this.activePointerId !== pointerId || !this.draggedPiece) {
      return;
    }

    const now = performance.now();
    const dt = Math.max(0.008, (now - this.lastMoveTime) / 1000);
    const nextX = Math.max(24, Math.min(this.layout.width - 24, x + this.grabOffsetX));
    const nextY = Math.max(24, Math.min(this.layout.height - 24, y + this.grabOffsetY));

    this.draggedPiece.vx = (nextX - this.draggedPiece.x) / dt;
    this.draggedPiece.vy = (nextY - this.draggedPiece.y) / dt;
    this.draggedPiece.x = nextX;
    this.draggedPiece.y = nextY;

    this.lastMoveTime = now;
  }

  onPointerUp(pointerId: number): void {
    this.pressedAction = null;
    if (this.activePointerId !== pointerId || !this.draggedPiece) {
      return;
    }

    const piece = this.draggedPiece;
    this.draggedPiece = null;
    this.activePointerId = null;

    // Clear stale velocity if finger paused before lifting
    if (performance.now() - this.lastMoveTime > 85) {
      piece.vx = 0;
      piece.vy = 0;
    }

    const result = releaseDraggedPiece(piece, this.layout.board);
    if (result === 'snapped') {
      const placedSoFar = this.runtimes.filter(
        (p) => p.status === 'placed' || p.status === 'snapping'
      ).length;
      this.sound.playSnap(placedSoFar, this.runtimes.length);
      this.particles.emitSnapSparkles(
        piece.snapTargetX,
        piece.snapTargetY,
        Math.min(this.layout.board.w, this.layout.board.h) * 0.25
      );
    } else {
      this.sound.playReturnBounce();
    }
  }

  update(dt: number): void {
    const clampedDt = Math.min(0.05, Math.max(0.001, dt));
    this.hintPulseTime += clampedDt;
    this.particles.update(clampedDt);

    const events = stepPiecesPhysics(this.runtimes, this.layout, clampedDt);

    if (!this.completed && events.allCompleted) {
      this.completed = true;
      this.winTimer = 0;
      this.secondBurstDone = false;
      this.progress.recordCompletion(this.level.id, 3);
      this.sound.playWinFanfare();
      this.particles.emitCelebrationFireworks(this.layout.width, this.layout.height);
    }

    if (this.completed) {
      this.winTimer += clampedDt;
      this.seamAlpha = Math.max(0, 1 - this.winTimer / 0.45);
      if (!this.secondBurstDone && this.winTimer >= 0.72) {
        this.secondBurstDone = true;
        this.particles.emitCelebrationFireworks(this.layout.width, this.layout.height);
      }
    }
  }

  draw(ctx: CanvasRenderingContext2D, _width: number, _height: number, time: number): void {
    this.rebuildGeometryIfNeeded();

    this.drawTopHud(ctx);
    this.drawBoard(ctx, time);
    this.drawTray(ctx);

    // Draw placed pieces cleanly inside board coordinates (or living whole scene once seamAlpha == 0)
    if (this.completed && this.seamAlpha <= 0.01) {
      this.drawCompletedLivingPicture(ctx, time);
    } else {
      this.drawPlacedPieces(ctx);
    }

    // Draw loose tray, returning, snapping, and dragging pieces ordered by zIndex
    const movingOrTray = [...this.runtimes]
      .filter((p) => p.status !== 'placed')
      .sort((a, b) => a.zIndex - b.zIndex);

    for (const piece of movingOrTray) {
      this.drawMovingPiece(ctx, piece);
    }

    if (this.completed && this.winTimer > 0.5) {
      this.drawCelebrationPanel(ctx);
    }

    this.particles.draw(ctx);
  }

  private drawTopHud(ctx: CanvasRenderingContext2D): void {
    const { topBar } = this.layout;
    const { backBtn, soundBtn } = this.getTopButtons(this.layout);

    drawCircleButton(
      ctx,
      backBtn.x,
      backBtn.y,
      backBtn.r,
      '#FFFDF9',
      '#D7F5CD',
      drawBackArrowIcon
    );

    drawCircleButton(
      ctx,
      soundBtn.x,
      soundBtn.y,
      soundBtn.r,
      '#FFFDF9',
      '#FFE9F5',
      (c, cx, cy, r) => drawSpeakerIcon(c, cx, cy, r, this.sound.isEnabled())
    );

    // Center title + progress pill
    const placedCount = this.runtimes.filter(
      (p) => p.status === 'placed' || p.status === 'snapping'
    ).length;
    const pillMaxW = Math.max(150, topBar.w - backBtn.r * 2 - soundBtn.r * 2 - 28);
    const pillW = Math.min(340, pillMaxW);
    const pillH = Math.min(44, topBar.h * 0.84);
    const pillX = topBar.x + (topBar.w - pillW) * 0.5;
    const pillY = topBar.y + (topBar.h - pillH) * 0.5;

    ctx.save();
    traceRoundedRect(ctx, pillX, pillY, pillW, pillH, pillH * 0.5);
    ctx.fillStyle = 'rgba(255, 253, 249, 0.9)';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = 2.2;
    ctx.stroke();

    ctx.fillStyle = STORYBOOK_INK;
    ctx.textBaseline = 'middle';

    const titleText = t(this.level.titleKey);
    const countText = t('hud.progress', {
      placed: placedCount,
      total: this.runtimes.length
    });

    if (pillW > 220) {
      ctx.font = `800 15px ${ROUNDED_FONT_FAMILY}`;
      ctx.textAlign = 'left';
      ctx.fillText(titleText, pillX + 16, pillY + pillH * 0.52);
      ctx.textAlign = 'right';
      ctx.fillStyle = '#D964A0';
      ctx.fillText(countText, pillX + pillW - 16, pillY + pillH * 0.52);
    } else {
      ctx.font = `800 15px ${ROUNDED_FONT_FAMILY}`;
      ctx.textAlign = 'center';
      ctx.fillText(countText, pillX + pillW * 0.5, pillY + pillH * 0.52);
    }
    ctx.restore();
  }

  private drawBoard(ctx: CanvasRenderingContext2D, _time: number): void {
    const { board } = this.layout;
    const framePad = 10;
    const cornerR = 18;

    ctx.save();
    // Outer wooden/storybook pastel frame shadow
    ctx.fillStyle = 'rgba(106, 73, 110, 0.2)';
    traceRoundedRect(
      ctx,
      board.x - framePad,
      board.y - framePad + 5,
      board.w + framePad * 2,
      board.h + framePad * 2,
      cornerR + 4
    );
    ctx.fill();

    // Outer frame body
    traceRoundedRect(
      ctx,
      board.x - framePad,
      board.y - framePad,
      board.w + framePad * 2,
      board.h + framePad * 2,
      cornerR + 4
    );
    ctx.fillStyle = this.level.frameColor;
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = 3;
    ctx.stroke();

    // Inner artwork area clipped
    ctx.save();
    traceRoundedRect(ctx, board.x, board.y, board.w, board.h, 12);
    ctx.clip();

    // Soft cream backing
    ctx.fillStyle = '#FFFDF9';
    ctx.fillRect(board.x, board.y, board.w, board.h);

    // Dimmed ghost reference picture so toddlers match both color & shape
    ctx.save();
    ctx.globalAlpha = 0.22;
    ctx.drawImage(this.artworkCanvas, board.x, board.y, board.w, board.h);
    ctx.restore();

    // Ghost silhouette slot outlines for every unplaced piece
    ctx.save();
    ctx.translate(board.x, board.y);
    for (const piece of this.runtimes) {
      if (piece.status === 'placed') continue;
      const geom = this.geometryCache.get(piece.spec.id);
      if (!geom) continue;

      if (piece.glowIntensity > 0.02) {
        const pulse = 0.75 + Math.sin(this.hintPulseTime * 10) * 0.25;
        ctx.fillStyle = `rgba(255, 224, 120, ${0.34 * piece.glowIntensity * pulse})`;
        ctx.fill(geom.boardPath);
        ctx.strokeStyle = `rgba(255, 132, 196, ${0.95 * piece.glowIntensity})`;
        ctx.lineWidth = 4;
        ctx.setLineDash([]);
        ctx.stroke(geom.boardPath);
      } else {
        ctx.strokeStyle = 'rgba(106, 73, 110, 0.28)';
        ctx.lineWidth = 2;
        ctx.setLineDash([6, 5]);
        ctx.stroke(geom.boardPath);
      }
    }
    ctx.restore();

    ctx.restore();

    // Crisp inner frame rim
    traceRoundedRect(ctx, board.x, board.y, board.w, board.h, 12);
    ctx.strokeStyle = 'rgba(106, 73, 110, 0.4)';
    ctx.lineWidth = 2;
    ctx.stroke();

    ctx.restore();
  }

  private drawTray(ctx: CanvasRenderingContext2D): void {
    const { tray } = this.layout;
    ctx.save();
    traceRoundedRect(ctx, tray.x, tray.y, tray.w, tray.h, 22);
    ctx.fillStyle = 'rgba(255, 253, 249, 0.65)';
    ctx.fill();
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.9)';
    ctx.lineWidth = 3;
    ctx.stroke();

    if (!this.completed) {
      const remaining = this.runtimes.filter((p) => p.status === 'tray').length;
      if (remaining === this.runtimes.length) {
        ctx.fillStyle = 'rgba(106, 73, 110, 0.65)';
        ctx.font = `700 13px ${ROUNDED_FONT_FAMILY}`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';
        ctx.fillText(t('puzzle.dragHint'), tray.x + tray.w * 0.5, tray.y + 8);
      }
    }
    ctx.restore();
  }

  private drawPlacedPieces(ctx: CanvasRenderingContext2D): void {
    const { board } = this.layout;
    ctx.save();
    traceRoundedRect(ctx, board.x, board.y, board.w, board.h, 12);
    ctx.clip();

    ctx.translate(board.x, board.y);
    for (const piece of this.runtimes) {
      if (piece.status !== 'placed') continue;
      const geom = this.geometryCache.get(piece.spec.id);
      if (!geom) continue;

      ctx.save();
      ctx.clip(geom.boardPath);
      ctx.drawImage(this.artworkCanvas, 0, 0, board.w, board.h);
      ctx.restore();

      if (this.seamAlpha > 0.01) {
        ctx.save();
        ctx.globalAlpha = this.seamAlpha * 0.65;
        ctx.strokeStyle = STORYBOOK_INK;
        ctx.lineWidth = 2;
        ctx.stroke(geom.boardPath);
        ctx.restore();
      }
    }
    ctx.restore();
  }

  private drawCompletedLivingPicture(ctx: CanvasRenderingContext2D, time: number): void {
    const { board } = this.layout;
    const breathScale = 1 + Math.sin(this.winTimer * 3.2) * 0.01;
    const cx = board.x + board.w * 0.5;
    const cy = board.y + board.h * 0.5;

    ctx.save();
    ctx.translate(cx, cy);
    ctx.scale(breathScale, breathScale);
    ctx.translate(-cx, -cy);

    traceRoundedRect(ctx, board.x, board.y, board.w, board.h, 12);
    ctx.clip();
    ctx.drawImage(this.artworkCanvas, board.x, board.y, board.w, board.h);
    drawLivingCelebrationOverlay(ctx, this.level.theme, board.x, board.y, board.w, time);
    ctx.restore();
  }

  private drawMovingPiece(ctx: CanvasRenderingContext2D, piece: PieceRuntime): void {
    const geom = this.geometryCache.get(piece.spec.id);
    if (!geom) return;

    const { board } = this.layout;
    const popScale =
      piece.status === 'snapping'
        ? 1 + Math.sin(piece.snapAnimProgress * Math.PI) * 0.08
        : 1;
    const totalScale = piece.scale * popScale;

    ctx.save();
    ctx.translate(piece.x, piece.y);
    ctx.rotate(piece.rotation);
    ctx.scale(totalScale, totalScale);

    // Elevation shadow for floating/dragged pieces
    ctx.save();
    const isElevated = piece.status === 'dragging' || piece.status === 'returning';
    ctx.shadowColor = isElevated ? 'rgba(106, 73, 110, 0.32)' : 'rgba(106, 73, 110, 0.18)';
    ctx.shadowBlur = isElevated ? 16 : 8;
    ctx.shadowOffsetY = isElevated ? 8 : 3;
    ctx.fillStyle = '#FFFDF9';
    ctx.fill(geom.localPath);
    ctx.restore();

    // Clipped illustration slice
    ctx.save();
    ctx.clip(geom.localPath);
    ctx.drawImage(
      this.artworkCanvas,
      -piece.spec.centerX * board.w,
      -piece.spec.centerY * board.h,
      board.w,
      board.h
    );
    ctx.restore();

    // Soft bright inner bevel & storybook outline
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.55)';
    ctx.lineWidth = 4;
    ctx.stroke(geom.localPath);

    ctx.strokeStyle = piece.glowIntensity > 0.3 ? '#FF84C4' : STORYBOOK_INK;
    ctx.lineWidth = 2.4;
    ctx.stroke(geom.localPath);

    ctx.restore();
  }

  private drawCelebrationPanel(ctx: CanvasRenderingContext2D): void {
    const tray = this.layout.tray;
    const appear = Math.min(1, (this.winTimer - 0.5) / 0.35);
    const { replayRect, nextRect, nextLevel } = this.getWinButtons();
    const cx = tray.x + tray.w * 0.5;

    ctx.save();
    ctx.globalAlpha = appear;

    // 3 popping celebratory stars
    const earnedStars =
      (this.winTimer > 0.55 ? 1 : 0) +
      (this.winTimer > 0.72 ? 1 : 0) +
      (this.winTimer > 0.9 ? 1 : 0);

    const compact = tray.h < 205;
    const starY = tray.y + (compact ? 26 : 34);
    drawStarRow(ctx, cx, starY, earnedStars, 3, compact ? 15 : 19, compact ? 38 : 48);

    // Cheerful headline
    ctx.fillStyle = STORYBOOK_INK;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.font = `900 ${compact ? 20 : 25}px ${ROUNDED_FONT_FAMILY}`;
    ctx.fillText(t('win.headline'), cx, starY + (compact ? 30 : 40));

    if (!compact) {
      ctx.font = `700 14px ${ROUNDED_FONT_FAMILY}`;
      ctx.fillStyle = 'rgba(106, 73, 110, 0.8)';
      ctx.fillText(
        t('win.subheadline', { title: t(this.level.titleKey) }),
        cx,
        starY + 68
      );
    }

    drawPillButton(ctx, replayRect, {
      label: t('win.playAgain'),
      fillTop: '#FFFDF9',
      fillBottom: '#DCEFFF',
      fontSize: 16,
      pressed: this.pressedAction === 'playAgain'
    });

    drawPillButton(ctx, nextRect, {
      label: nextLevel ? t('win.nextPuzzle') : t('win.backHome'),
      fillTop: '#FFB6DC',
      fillBottom: '#FF87C2',
      fontSize: 16,
      pressed: this.pressedAction === 'next' || this.pressedAction === 'home'
    });

    ctx.restore();
  }
}

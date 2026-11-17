import { drawLevelMedallionArtwork } from '../art/illustrations';
import { STORYBOOK_INK, traceRoundedRect, traceStar } from '../art/drawUtils';
import { pointInRect, type Rect } from '../core/layout';
import type { SoundEngine } from '../audio/soundEngine';
import { t } from '../i18n';
import type { ProgressStore } from '../storage/progressStore';
import type { PurchaseService } from '../storage/purchaseService';
import {
  drawChevronIcon,
  drawCircleButton,
  drawGearIcon,
  drawPadlockBadge,
  drawPillButton,
  drawSpeakerIcon,
  drawStarRow,
  ROUNDED_FONT_FAMILY
} from '../ui/canvasControls';
import { getLevelById, isLevelUnlocked, LEVELS, type LevelDefinition } from './levels';

export interface HubMetrics {
  width: number;
  height: number;
  isLandscape: boolean;
  wheelCx: number;
  wheelCy: number;
  wheelRadius: number;
  centerPreviewRadius: number;
  playButtonRect: Rect;
  prevBtnCenter: { x: number; y: number; r: number };
  nextBtnCenter: { x: number; y: number; r: number };
  soundBtnCenter: { x: number; y: number; r: number };
  grownupsBtnCenter: { x: number; y: number; r: number };
  cardRect: Rect;
}

interface NoticeBubble {
  title: string;
  body: string;
  timer: number;
}

export class HubScreen {
  private selectedIndex = 0;
  private wheelIndexFloat = 0;
  private targetWheelFloat = 0;
  private angularVelocity = 0;
  private draggingWheel = false;
  private dragStartAngle = 0;
  private dragStartX = 0;
  private dragStartY = 0;
  private dragMoved = false;
  private lastDragAngle = 0;
  private lastDragTime = 0;
  private lockedWobbleTimer = 0;
  private notice: NoticeBubble | null = null;
  private activePointerId: number | null = null;
  private pressedButton: 'play' | 'prev' | 'next' | null = null;

  constructor(
    private readonly progress: ProgressStore,
    private readonly purchases: PurchaseService,
    private readonly sound: SoundEngine,
    private readonly onStartLevel: (level: LevelDefinition) => void
  ) {
    const savedId = this.progress.getLastSelectedLevelId();
    const foundIdx = LEVELS.findIndex((l) => l.id === savedId);
    this.selectedIndex = foundIdx >= 0 ? foundIdx : 0;
    this.wheelIndexFloat = this.selectedIndex;
    this.targetWheelFloat = this.selectedIndex;
  }

  refreshFromStorage(): void {
    const savedId = this.progress.getLastSelectedLevelId();
    const foundIdx = LEVELS.findIndex((l) => l.id === savedId);
    if (foundIdx >= 0) {
      this.selectedIndex = foundIdx;
      this.targetWheelFloat = foundIdx;
    }
  }

  getSelectedLevel(): LevelDefinition {
    return LEVELS[this.selectedIndex] ?? LEVELS[0]!;
  }

  getMetrics(width: number, height: number): HubMetrics {
    const isLandscape = width > height * 1.05;
    const pad = Math.max(14, Math.min(24, Math.min(width, height) * 0.03));
    const btnR = Math.max(21, Math.min(26, Math.min(width, height) * 0.045));

    const soundBtnCenter = { x: width - pad - btnR * 3.35, y: pad + btnR, r: btnR };
    const grownupsBtnCenter = { x: width - pad - btnR, y: pad + btnR, r: btnR };

    if (isLandscape) {
      const topOffset = pad + btnR * 1.9;
      const availH = height - topOffset - pad;
      const wheelAreaW = width * 0.56;
      const wheelRadius = Math.max(88, Math.min(wheelAreaW * 0.32, availH * 0.34));
      const wheelCx = wheelAreaW * 0.52;
      const wheelCy = topOffset + availH * 0.52;
      const centerPreviewRadius = wheelRadius * 0.45;

      const cardW = Math.min(340, width * 0.36);
      const cardH = Math.min(320, availH * 0.88);
      const cardX = width * 0.58 + (width * 0.42 - pad - cardW) * 0.5;
      const cardY = topOffset + (availH - cardH) * 0.5;

      const playW = Math.min(220, cardW * 0.82);
      const playH = Math.max(46, Math.min(58, cardH * 0.2));
      const playButtonRect: Rect = {
        x: cardX + (cardW - playW) * 0.5,
        y: cardY + cardH - playH - 18,
        w: playW,
        h: playH
      };

      const navR = Math.max(17, Math.min(22, wheelRadius * 0.17));
      const medallionPad = Math.max(18, Math.min(28, wheelRadius * 0.18));
      const navOffset = Math.min(wheelCx - pad - navR, wheelRadius + medallionPad + navR * 1.25);
      return {
        width,
        height,
        isLandscape,
        wheelCx,
        wheelCy,
        wheelRadius,
        centerPreviewRadius,
        playButtonRect,
        prevBtnCenter: { x: wheelCx - navOffset, y: wheelCy, r: navR },
        nextBtnCenter: { x: wheelCx + navOffset, y: wheelCy, r: navR },
        soundBtnCenter,
        grownupsBtnCenter,
        cardRect: { x: cardX, y: cardY, w: cardW, h: cardH }
      };
    }

    // Portrait layout
    const headerBottom = pad + btnR * 2 + 44;
    const footerCardH = Math.max(146, Math.min(182, height * 0.23));
    const wheelAvailH = Math.max(180, height - headerBottom - footerCardH - pad * 1.2);
    const wheelRadius = Math.max(86, Math.min(width * 0.305, wheelAvailH * 0.36));
    const wheelCx = width * 0.5;
    const wheelCy = headerBottom + wheelAvailH * 0.5;
    const centerPreviewRadius = wheelRadius * 0.47;

    const cardW = Math.min(380, width - pad * 2);
    const cardX = (width - cardW) * 0.5;
    const cardY = height - footerCardH - pad;

    const playW = Math.min(230, cardW * 0.72);
    const playH = Math.max(46, Math.min(56, footerCardH * 0.34));
    const playButtonRect: Rect = {
      x: cardX + (cardW - playW) * 0.5,
      y: cardY + footerCardH - playH - 14,
      w: playW,
      h: playH
    };

    const navR = Math.max(17, Math.min(22, width * 0.05));
    const medallionPad = Math.max(18, Math.min(28, wheelRadius * 0.18));
    const navOffset = Math.min(width * 0.5 - pad * 0.7 - navR, wheelRadius + medallionPad + navR * 1.1);
    return {
      width,
      height,
      isLandscape,
      wheelCx,
      wheelCy,
      wheelRadius,
      centerPreviewRadius,
      playButtonRect,
      prevBtnCenter: { x: wheelCx - navOffset, y: wheelCy, r: navR },
      nextBtnCenter: { x: wheelCx + navOffset, y: wheelCy, r: navR },
      soundBtnCenter,
      grownupsBtnCenter,
      cardRect: { x: cardX, y: cardY, w: cardW, h: footerCardH }
    };
  }

  private wrapLevelIndex(floatIdx: number): number {
    const n = LEVELS.length;
    return ((Math.round(floatIdx) % n) + n) % n;
  }

  private getMedallionPosition(
    index: number,
    metrics: HubMetrics
  ): { x: number; y: number; r: number; depthFactor: number } {
    const n = LEVELS.length;
    const stepAngle = (Math.PI * 2) / n;
    const angle = -Math.PI / 2 + (index - this.wheelIndexFloat) * stepAngle;
    const x = metrics.wheelCx + Math.cos(angle) * metrics.wheelRadius;
    const y = metrics.wheelCy + Math.sin(angle) * metrics.wheelRadius;

    // Larger near top center (-PI/2 => sin == -1), slightly smaller near bottom
    const topness = (-Math.sin(angle) + 1) * 0.5; // 0 bottom .. 1 top
    const baseR = Math.max(16, Math.min(28, metrics.wheelRadius * 0.16));
    const r = baseR * (0.76 + topness * 0.42);
    return { x, y, r, depthFactor: topness };
  }

  private shortestFloatDelta(from: number, toIndex: number): number {
    const n = LEVELS.length;
    const curWrapped = ((from % n) + n) % n;
    let diff = toIndex - curWrapped;
    if (diff > n / 2) diff -= n;
    if (diff < -n / 2) diff += n;
    return from + diff;
  }

  private selectIndex(index: number): void {
    const wrapped = ((index % LEVELS.length) + LEVELS.length) % LEVELS.length;
    this.selectedIndex = wrapped;
    this.targetWheelFloat = this.shortestFloatDelta(this.wheelIndexFloat, wrapped);
    this.angularVelocity = 0;
    const lvl = LEVELS[wrapped];
    if (lvl) {
      this.progress.setLastSelectedLevelId(lvl.id);
    }
  }

  private tryLaunchSelected(): void {
    const level = this.getSelectedLevel();
    const unlocked = isLevelUnlocked(level, this.purchases.isUnlockedAll()) && level.implementedInStep2;
    if (unlocked) {
      this.sound.playTap();
      this.onStartLevel(level);
      return;
    }

    this.sound.playReturnBounce();
    this.lockedWobbleTimer = 0.45;
    this.notice = {
      title: t('hub.lockedNoticeTitle'),
      body: t('hub.lockedNoticeBody'),
      timer: 2.8
    };
  }

  onPointerDown(x: number, y: number, pointerId: number, width: number, height: number): void {
    this.sound.unlock();
    const m = this.getMetrics(width, height);

    if (this.notice && !pointInRect(x, y, m.cardRect)) {
      this.notice = null;
    }

    if (Math.hypot(x - m.soundBtnCenter.x, y - m.soundBtnCenter.y) <= m.soundBtnCenter.r + 8) {
      const next = !this.sound.isEnabled();
      this.sound.setEnabled(next);
      this.progress.setSoundEnabled(next);
      if (next) this.sound.playTap();
      return;
    }

    if (
      Math.hypot(x - m.grownupsBtnCenter.x, y - m.grownupsBtnCenter.y) <=
      m.grownupsBtnCenter.r + 8
    ) {
      this.sound.playTap();
      this.notice = {
        title: t('hub.grownupsNoticeTitle'),
        body: t('hub.grownupsNoticeBody'),
        timer: 3.0
      };
      return;
    }

    if (Math.hypot(x - m.prevBtnCenter.x, y - m.prevBtnCenter.y) <= m.prevBtnCenter.r + 10) {
      this.pressedButton = 'prev';
      this.sound.playTap();
      this.selectIndex(this.selectedIndex - 1);
      return;
    }

    if (Math.hypot(x - m.nextBtnCenter.x, y - m.nextBtnCenter.y) <= m.nextBtnCenter.r + 10) {
      this.pressedButton = 'next';
      this.sound.playTap();
      this.selectIndex(this.selectedIndex + 1);
      return;
    }

    if (pointInRect(x, y, m.playButtonRect, 6)) {
      this.pressedButton = 'play';
      this.tryLaunchSelected();
      return;
    }

    // Center preview tap launches selected puzzle
    if (Math.hypot(x - m.wheelCx, y - m.wheelCy) <= m.centerPreviewRadius + 8) {
      this.tryLaunchSelected();
      return;
    }

    // Start dragging carousel wheel
    const distFromWheel = Math.hypot(x - m.wheelCx, y - m.wheelCy);
    if (distFromWheel <= m.wheelRadius * 1.45) {
      this.activePointerId = pointerId;
      this.draggingWheel = true;
      this.dragMoved = false;
      this.dragStartX = x;
      this.dragStartY = y;
      this.dragStartAngle = Math.atan2(y - m.wheelCy, x - m.wheelCx);
      this.lastDragAngle = this.dragStartAngle;
      this.lastDragTime = performance.now();
      this.angularVelocity = 0;
    }
  }

  onPointerMove(x: number, y: number, pointerId: number, width: number, height: number): void {
    if (!this.draggingWheel || this.activePointerId !== pointerId) {
      return;
    }

    const m = this.getMetrics(width, height);
    if (Math.hypot(x - this.dragStartX, y - this.dragStartY) > 7) {
      this.dragMoved = true;
    }

    const angle = Math.atan2(y - m.wheelCy, x - m.wheelCx);
    let deltaAngle = angle - this.lastDragAngle;
    if (deltaAngle > Math.PI) deltaAngle -= Math.PI * 2;
    if (deltaAngle < -Math.PI) deltaAngle += Math.PI * 2;

    const stepAngle = (Math.PI * 2) / LEVELS.length;
    const indexDelta = -deltaAngle / stepAngle;
    this.wheelIndexFloat += indexDelta;
    this.targetWheelFloat = this.wheelIndexFloat;

    const now = performance.now();
    const dt = Math.max(0.008, (now - this.lastDragTime) / 1000);
    this.angularVelocity = indexDelta / dt;
    this.lastDragAngle = angle;
    this.lastDragTime = now;

    const newSelected = this.wrapLevelIndex(this.wheelIndexFloat);
    if (newSelected !== this.selectedIndex) {
      this.selectedIndex = newSelected;
      this.sound.playTap();
    }
  }

  onPointerUp(x: number, y: number, pointerId: number, width: number, height: number): void {
    this.pressedButton = null;
    if (!this.draggingWheel || this.activePointerId !== pointerId) {
      return;
    }

    this.draggingWheel = false;
    this.activePointerId = null;

    if (!this.dragMoved) {
      // Check if user tapped one of the 16 medallions directly
      const m = this.getMetrics(width, height);
      let tappedIdx = -1;
      let bestDist = Infinity;
      for (let i = 0; i < LEVELS.length; i++) {
        const pos = this.getMedallionPosition(i, m);
        const d = Math.hypot(x - pos.x, y - pos.y);
        if (d <= pos.r + 12 && d < bestDist) {
          bestDist = d;
          tappedIdx = i;
        }
      }
      if (tappedIdx >= 0) {
        if (tappedIdx === this.selectedIndex) {
          this.tryLaunchSelected();
        } else {
          this.sound.playTap();
          this.selectIndex(tappedIdx);
        }
        return;
      }
    }

    // Apply gentle coasting inertia then snap to nearest medallion
    const coastSteps = Math.max(-3, Math.min(3, this.angularVelocity * 0.14));
    const snapIdx = Math.round(this.wheelIndexFloat + coastSteps);
    this.selectedIndex = this.wrapLevelIndex(snapIdx);
    this.targetWheelFloat = snapIdx;
    const level = getLevelById(LEVELS[this.selectedIndex]!.id);
    this.progress.setLastSelectedLevelId(level.id);
  }

  update(dt: number): void {
    const clampedDt = Math.min(0.05, Math.max(0.001, dt));
    if (!this.draggingWheel) {
      const diff = this.targetWheelFloat - this.wheelIndexFloat;
      this.wheelIndexFloat += diff * Math.min(1, clampedDt * 10);
      if (Math.abs(diff) < 0.001) {
        this.wheelIndexFloat = this.targetWheelFloat;
      }
    }

    if (this.lockedWobbleTimer > 0) {
      this.lockedWobbleTimer = Math.max(0, this.lockedWobbleTimer - clampedDt);
    }

    if (this.notice) {
      this.notice.timer -= clampedDt;
      if (this.notice.timer <= 0) {
        this.notice = null;
      }
    }
  }

  draw(ctx: CanvasRenderingContext2D, width: number, height: number, time: number): void {
    const m = this.getMetrics(width, height);
    const pad = Math.max(14, Math.min(24, Math.min(width, height) * 0.03));

    // Top stars total badge
    const totalStars = this.progress.getTotalStars();
    const maxStars = LEVELS.length * 3;
    const badgeH = m.soundBtnCenter.r * 1.9;
    const badgeW = 118;
    ctx.save();
    traceRoundedRect(ctx, pad, pad + 2, badgeW, badgeH, badgeH * 0.5);
    ctx.fillStyle = 'rgba(255, 253, 248, 0.9)';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = 2.2;
    ctx.stroke();

    traceStar(ctx, pad + 24, pad + 2 + badgeH * 0.5, 11, 5.5);
    ctx.fillStyle = '#FFE066';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = 1.6;
    ctx.stroke();

    ctx.fillStyle = STORYBOOK_INK;
    ctx.font = `800 16px ${ROUNDED_FONT_FAMILY}`;
    ctx.textAlign = 'left';
    ctx.textBaseline = 'middle';
    ctx.fillText(
      t('hub.starsTotal', { earned: totalStars, total: maxStars }),
      pad + 42,
      pad + 3 + badgeH * 0.5
    );
    ctx.restore();

    // Top-right sound & grownups buttons
    drawCircleButton(
      ctx,
      m.soundBtnCenter.x,
      m.soundBtnCenter.y,
      m.soundBtnCenter.r,
      '#FFFDF9',
      '#FFE9F5',
      (c, cx, cy, r) => drawSpeakerIcon(c, cx, cy, r, this.sound.isEnabled())
    );
    drawCircleButton(
      ctx,
      m.grownupsBtnCenter.x,
      m.grownupsBtnCenter.y,
      m.grownupsBtnCenter.r,
      '#FFFDF9',
      '#E5F3FF',
      drawGearIcon
    );

    // Header Title in Portrait (or subtle top center in Landscape)
    ctx.save();
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    const titleY = m.isLandscape ? pad + m.soundBtnCenter.r : pad + m.soundBtnCenter.r * 2 + 24;
    const titleSize = m.isLandscape
      ? Math.max(18, Math.min(26, width * 0.028))
      : Math.max(20, Math.min(28, width * 0.056));
    ctx.font = `900 ${titleSize}px ${ROUNDED_FONT_FAMILY}`;
    ctx.lineWidth = 5;
    ctx.strokeStyle = 'rgba(255, 253, 249, 0.94)';
    ctx.strokeText(t('app.title'), m.isLandscape ? width * 0.45 : width * 0.5, titleY);
    ctx.fillStyle = STORYBOOK_INK;
    ctx.fillText(t('app.title'), m.isLandscape ? width * 0.45 : width * 0.5, titleY);
    ctx.restore();

    // Glowing carousel turntable disc
    ctx.save();
    ctx.beginPath();
    ctx.arc(m.wheelCx, m.wheelCy, m.wheelRadius + 24, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(255, 253, 249, 0.45)';
    ctx.fill();
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.85)';
    ctx.lineWidth = 3;
    ctx.stroke();

    // Inner ring connecting medallions
    ctx.beginPath();
    ctx.arc(m.wheelCx, m.wheelCy, m.wheelRadius, 0, Math.PI * 2);
    ctx.strokeStyle = 'rgba(106, 73, 110, 0.14)';
    ctx.lineWidth = 6;
    ctx.stroke();
    ctx.restore();

    // Draw all 16 medallions sorted from back (bottom) to front (top)
    const unlockedAll = this.purchases.isUnlockedAll();
    const sortedIndices = LEVELS.map((_, i) => ({
      i,
      pos: this.getMedallionPosition(i, m)
    })).sort((a, b) => a.pos.depthFactor - b.pos.depthFactor);

    for (const { i, pos } of sortedIndices) {
      const level = LEVELS[i]!;
      const isSelected = i === this.selectedIndex;
      const unlocked = isLevelUnlocked(level, unlockedAll) && level.implementedInStep2;
      const stars = this.progress.getStars(level.id);

      ctx.save();
      if (isSelected && this.lockedWobbleTimer > 0) {
        const wobble = Math.sin(this.lockedWobbleTimer * 36) * 5;
        ctx.translate(wobble, 0);
      }

      // Soft shadow
      ctx.beginPath();
      ctx.arc(pos.x, pos.y + 3, pos.r, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(106, 73, 110, 0.2)';
      ctx.fill();

      // Medallion background & artwork
      drawLevelMedallionArtwork(ctx, level, pos.x, pos.y, pos.r, time);

      // Ring border (bright gold/pink highlight when selected)
      ctx.beginPath();
      ctx.arc(pos.x, pos.y, pos.r, 0, Math.PI * 2);
      ctx.strokeStyle = isSelected ? '#FF87C2' : STORYBOOK_INK;
      ctx.lineWidth = isSelected ? 4 : 2.2;
      ctx.stroke();

      if (!unlocked) {
        ctx.beginPath();
        ctx.arc(pos.x, pos.y, pos.r, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(246, 232, 255, 0.48)';
        ctx.fill();
        drawPadlockBadge(ctx, pos.x + pos.r * 0.56, pos.y + pos.r * 0.52, pos.r * 0.45);
      } else if (stars > 0) {
        // Tiny golden badge for completed levels
        traceStar(ctx, pos.x + pos.r * 0.62, pos.y - pos.r * 0.6, pos.r * 0.32, pos.r * 0.15);
        ctx.fillStyle = '#FFE066';
        ctx.fill();
        ctx.strokeStyle = STORYBOOK_INK;
        ctx.lineWidth = 1.6;
        ctx.stroke();
      }

      ctx.restore();
    }

    // Center featured preview medallion inside the wheel
    const selected = this.getSelectedLevel();
    const selectedUnlocked = isLevelUnlocked(selected, unlockedAll) && selected.implementedInStep2;
    const pulse = 1 + Math.sin(time * 2.4) * 0.02;
    const previewR = m.centerPreviewRadius * pulse;

    ctx.save();
    ctx.beginPath();
    ctx.arc(m.wheelCx, m.wheelCy + 5, previewR + 5, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(106, 73, 110, 0.2)';
    ctx.fill();

    ctx.beginPath();
    ctx.arc(m.wheelCx, m.wheelCy, previewR + 5, 0, Math.PI * 2);
    ctx.fillStyle = '#FFFDF9';
    ctx.fill();
    ctx.strokeStyle = selectedUnlocked ? '#FFB5D8' : '#D4B8F0';
    ctx.lineWidth = 4;
    ctx.stroke();

    drawLevelMedallionArtwork(ctx, selected, m.wheelCx, m.wheelCy, previewR, time);
    if (!selectedUnlocked) {
      ctx.beginPath();
      ctx.arc(m.wheelCx, m.wheelCy, previewR, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(248, 238, 255, 0.45)';
      ctx.fill();
      drawPadlockBadge(ctx, m.wheelCx, m.wheelCy, previewR * 0.34);
    }
    ctx.restore();

    // Left / Right wheel arrows
    drawCircleButton(
      ctx,
      m.prevBtnCenter.x,
      m.prevBtnCenter.y,
      m.prevBtnCenter.r,
      '#FFFDF9',
      '#FFE5F3',
      (c, cx, cy, r) => drawChevronIcon(c, cx, cy, r, 'left')
    );
    drawCircleButton(
      ctx,
      m.nextBtnCenter.x,
      m.nextBtnCenter.y,
      m.nextBtnCenter.r,
      '#FFFDF9',
      '#FFE5F3',
      (c, cx, cy, r) => drawChevronIcon(c, cx, cy, r, 'right')
    );

    // Selected level info card + Play CTA
    this.drawSelectedCard(ctx, m, selected, selectedUnlocked);

    // Gentle notice toast when tapping locked level or Grown-ups in Step 2
    if (this.notice) {
      this.drawNoticeToast(ctx, width, height, this.notice);
    }
  }

  private drawSelectedCard(
    ctx: CanvasRenderingContext2D,
    m: HubMetrics,
    level: LevelDefinition,
    unlocked: boolean
  ): void {
    const { x, y, w, h } = m.cardRect;
    ctx.save();

    // Card background
    ctx.fillStyle = 'rgba(106, 73, 110, 0.16)';
    traceRoundedRect(ctx, x, y + 5, w, h, 24);
    ctx.fill();

    traceRoundedRect(ctx, x, y, w, h, 24);
    ctx.fillStyle = 'rgba(255, 253, 249, 0.94)';
    ctx.fill();
    ctx.strokeStyle = level.accentColor;
    ctx.lineWidth = 3;
    ctx.stroke();

    const cx = x + w * 0.5;
    const titleY = y + (m.isLandscape ? 36 : 26);

    // Level title
    ctx.fillStyle = STORYBOOK_INK;
    ctx.font = `900 ${Math.max(17, Math.min(22, w * 0.068))}px ${ROUNDED_FONT_FAMILY}`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(t(level.titleKey), cx, titleY);

    // Piece count + Free/Locked pill row
    const pillsY = titleY + (m.isLandscape ? 36 : 28);
    const pieceLabel = t('hub.piecesCount', { count: level.pieceCount });
    const statusLabel = unlocked ? t('hub.freeBadge') : t('hub.lockedBadge');
    ctx.font = `800 13px ${ROUNDED_FONT_FAMILY}`;

    const piecePillW = 94;
    const statusPillW = 78;
    const totalPillsW = piecePillW + statusPillW + 10;
    const pillStartX = cx - totalPillsW * 0.5;

    traceRoundedRect(ctx, pillStartX, pillsY - 13, piecePillW, 26, 13);
    ctx.fillStyle = '#E8F6FF';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = 1.6;
    ctx.stroke();
    ctx.fillStyle = STORYBOOK_INK;
    ctx.fillText(pieceLabel, pillStartX + piecePillW * 0.5, pillsY + 1);

    traceRoundedRect(ctx, pillStartX + piecePillW + 10, pillsY - 13, statusPillW, 26, 13);
    ctx.fillStyle = unlocked ? '#C9F5C2' : '#FFE8C7';
    ctx.fill();
    ctx.stroke();
    ctx.fillStyle = STORYBOOK_INK;
    ctx.fillText(statusLabel, pillStartX + piecePillW + 10 + statusPillW * 0.5, pillsY + 1);

    // Stars earned for this level
    const starsY = pillsY + (m.isLandscape ? 42 : 30);
    drawStarRow(ctx, cx, starsY, this.progress.getStars(level.id), 3, 12, 30);

    if (m.isLandscape && h > 220) {
      ctx.fillStyle = 'rgba(106, 73, 110, 0.72)';
      ctx.font = `700 13px ${ROUNDED_FONT_FAMILY}`;
      ctx.fillText(t('hub.spinHint'), cx, starsY + 36);
    }

    drawPillButton(ctx, m.playButtonRect, {
      label: unlocked ? t('hub.play') : t('hub.lockedBadge'),
      fillTop: unlocked ? '#FFB6DC' : '#EFE1FF',
      fillBottom: unlocked ? '#FF87C2' : '#D5BAFA',
      fontSize: 20,
      pressed: this.pressedButton === 'play'
    });

    ctx.restore();
  }

  private drawNoticeToast(
    ctx: CanvasRenderingContext2D,
    width: number,
    height: number,
    notice: NoticeBubble
  ): void {
    const boxW = Math.min(340, width - 32);
    const boxH = 82;
    const x = (width - boxW) * 0.5;
    const y = Math.max(76, height * 0.14);

    ctx.save();
    ctx.fillStyle = 'rgba(106, 73, 110, 0.22)';
    traceRoundedRect(ctx, x, y + 4, boxW, boxH, 20);
    ctx.fill();

    traceRoundedRect(ctx, x, y, boxW, boxH, 20);
    ctx.fillStyle = '#FFFDF9';
    ctx.fill();
    ctx.strokeStyle = '#FFB5D8';
    ctx.lineWidth = 3;
    ctx.stroke();

    ctx.fillStyle = STORYBOOK_INK;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.font = `900 16px ${ROUNDED_FONT_FAMILY}`;
    ctx.fillText(notice.title, x + boxW * 0.5, y + 28);

    ctx.font = `700 13px ${ROUNDED_FONT_FAMILY}`;
    ctx.fillStyle = 'rgba(106, 73, 110, 0.85)';
    ctx.fillText(notice.body, x + boxW * 0.5, y + 54);
    ctx.restore();
  }
}

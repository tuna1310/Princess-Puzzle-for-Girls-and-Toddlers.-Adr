import { STORYBOOK_INK, traceRoundedRect, traceStar } from '../art/drawUtils';
import type { Rect } from '../core/layout';

export const ROUNDED_FONT_FAMILY =
  "ui-rounded, 'SF Pro Rounded', 'Nunito', 'Quicksand', 'Arial Rounded MT Bold', 'Segoe UI', system-ui, sans-serif";

export interface PillButtonOptions {
  label: string;
  fillTop: string;
  fillBottom: string;
  textColor?: string;
  borderColor?: string;
  fontSize?: number;
  pressed?: boolean;
}

export function drawPillButton(
  ctx: CanvasRenderingContext2D,
  rect: Rect,
  opts: PillButtonOptions
): void {
  const { x, y, w, h } = rect;
  const radius = h * 0.5;
  const pressOffset = opts.pressed ? 2 : 0;

  ctx.save();
  // Soft drop shadow
  if (!opts.pressed) {
    ctx.fillStyle = 'rgba(106, 73, 110, 0.22)';
    traceRoundedRect(ctx, x, y + 4, w, h, radius);
    ctx.fill();
  }

  const grad = ctx.createLinearGradient(x, y + pressOffset, x, y + h + pressOffset);
  grad.addColorStop(0, opts.fillTop);
  grad.addColorStop(1, opts.fillBottom);

  traceRoundedRect(ctx, x, y + pressOffset, w, h, radius);
  ctx.fillStyle = grad;
  ctx.fill();

  ctx.strokeStyle = opts.borderColor ?? STORYBOOK_INK;
  ctx.lineWidth = 2.5;
  ctx.stroke();

  // Inner top sheen
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.55)';
  ctx.lineWidth = 2;
  traceRoundedRect(ctx, x + 4, y + 3 + pressOffset, w - 8, h * 0.45, radius * 0.7);
  ctx.stroke();

  ctx.fillStyle = opts.textColor ?? STORYBOOK_INK;
  const fontSize = opts.fontSize ?? Math.max(15, Math.min(22, h * 0.44));
  ctx.font = `800 ${fontSize}px ${ROUNDED_FONT_FAMILY}`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText(opts.label, x + w * 0.5, y + h * 0.52 + pressOffset);
  ctx.restore();
}

export function drawCircleButton(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  radius: number,
  fillTop: string,
  fillBottom: string,
  drawIcon: (ctx: CanvasRenderingContext2D, cx: number, cy: number, r: number) => void
): void {
  ctx.save();
  ctx.fillStyle = 'rgba(106, 73, 110, 0.2)';
  ctx.beginPath();
  ctx.arc(cx, cy + 3, radius, 0, Math.PI * 2);
  ctx.fill();

  const grad = ctx.createLinearGradient(cx, cy - radius, cx, cy + radius);
  grad.addColorStop(0, fillTop);
  grad.addColorStop(1, fillBottom);
  ctx.beginPath();
  ctx.arc(cx, cy, radius, 0, Math.PI * 2);
  ctx.fillStyle = grad;
  ctx.fill();

  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = 2.5;
  ctx.stroke();

  drawIcon(ctx, cx, cy, radius);
  ctx.restore();
}

export function drawBackArrowIcon(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  r: number
): void {
  ctx.save();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = Math.max(2.8, r * 0.16);
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';

  ctx.beginPath();
  ctx.moveTo(cx + r * 0.34, cy);
  ctx.lineTo(cx - r * 0.32, cy);
  ctx.moveTo(cx - r * 0.05, cy - r * 0.28);
  ctx.lineTo(cx - r * 0.35, cy);
  ctx.lineTo(cx - r * 0.05, cy + r * 0.28);
  ctx.stroke();
  ctx.restore();
}

export function drawChevronIcon(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  r: number,
  direction: 'left' | 'right'
): void {
  ctx.save();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = Math.max(2.8, r * 0.16);
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';
  const dir = direction === 'right' ? 1 : -1;

  ctx.beginPath();
  ctx.moveTo(cx - dir * r * 0.15, cy - r * 0.3);
  ctx.lineTo(cx + dir * r * 0.2, cy);
  ctx.lineTo(cx - dir * r * 0.15, cy + r * 0.3);
  ctx.stroke();
  ctx.restore();
}

export function drawSpeakerIcon(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  r: number,
  enabled: boolean
): void {
  ctx.save();
  ctx.fillStyle = STORYBOOK_INK;
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = Math.max(2.2, r * 0.12);
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';

  // Speaker cone
  ctx.beginPath();
  ctx.moveTo(cx - r * 0.36, cy - r * 0.15);
  ctx.lineTo(cx - r * 0.14, cy - r * 0.15);
  ctx.lineTo(cx + r * 0.1, cy - r * 0.34);
  ctx.lineTo(cx + r * 0.1, cy + r * 0.34);
  ctx.lineTo(cx - r * 0.14, cy + r * 0.15);
  ctx.lineTo(cx - r * 0.36, cy + r * 0.15);
  ctx.closePath();
  ctx.fill();

  if (enabled) {
    ctx.beginPath();
    ctx.arc(cx + r * 0.14, cy, r * 0.24, -0.7, 0.7);
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(cx + r * 0.14, cy, r * 0.4, -0.75, 0.75);
    ctx.stroke();
  } else {
    ctx.strokeStyle = '#D96B98';
    ctx.beginPath();
    ctx.moveTo(cx + r * 0.18, cy - r * 0.2);
    ctx.lineTo(cx + r * 0.44, cy + r * 0.2);
    ctx.moveTo(cx + r * 0.44, cy - r * 0.2);
    ctx.lineTo(cx + r * 0.18, cy + r * 0.2);
    ctx.stroke();
  }
  ctx.restore();
}

export function drawGearIcon(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  r: number
): void {
  ctx.save();
  ctx.translate(cx, cy);
  ctx.fillStyle = STORYBOOK_INK;
  const teeth = 8;
  ctx.beginPath();
  for (let i = 0; i < teeth * 2; i++) {
    const rad = i % 2 === 0 ? r * 0.46 : r * 0.34;
    const a = (i * Math.PI) / teeth;
    const px = Math.cos(a) * rad;
    const py = Math.sin(a) * rad;
    if (i === 0) ctx.moveTo(px, py);
    else ctx.lineTo(px, py);
  }
  ctx.closePath();
  ctx.fill();

  ctx.beginPath();
  ctx.arc(0, 0, r * 0.17, 0, Math.PI * 2);
  ctx.fillStyle = '#FFFDF9';
  ctx.fill();
  ctx.restore();
}

export function drawPadlockBadge(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  size: number
): void {
  ctx.save();
  ctx.translate(cx, cy);
  // Shackle
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = Math.max(2, size * 0.16);
  ctx.beginPath();
  ctx.arc(0, -size * 0.24, size * 0.3, Math.PI, 0);
  ctx.stroke();

  // Body
  traceRoundedRect(ctx, -size * 0.46, -size * 0.14, size * 0.92, size * 0.72, size * 0.22);
  ctx.fillStyle = '#FFE285';
  ctx.fill();
  ctx.lineWidth = Math.max(1.8, size * 0.12);
  ctx.stroke();

  // Keyhole
  ctx.beginPath();
  ctx.arc(0, size * 0.18, size * 0.11, 0, Math.PI * 2);
  ctx.fillStyle = STORYBOOK_INK;
  ctx.fill();
  ctx.restore();
}

export function drawStarRow(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  earnedStars: number,
  totalStars: number,
  starRadius: number,
  spacing: number
): void {
  const startX = cx - ((totalStars - 1) * spacing) * 0.5;
  for (let i = 0; i < totalStars; i++) {
    const sx = startX + i * spacing;
    const earned = i < earnedStars;
    traceStar(ctx, sx, cy, starRadius, starRadius * 0.48);
    ctx.fillStyle = earned ? '#FFE169' : 'rgba(255, 255, 255, 0.55)';
    ctx.fill();
    ctx.strokeStyle = earned ? STORYBOOK_INK : 'rgba(106, 73, 110, 0.45)';
    ctx.lineWidth = Math.max(1.4, starRadius * 0.18);
    ctx.stroke();
  }
}

export const STORYBOOK_INK = '#6A496E';
export const SOFT_WHITE = '#FFFDF9';

export function traceRoundedRect(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  w: number,
  h: number,
  r: number
): void {
  const radius = Math.max(0, Math.min(r, w * 0.5, h * 0.5));
  ctx.beginPath();
  ctx.moveTo(x + radius, y);
  ctx.lineTo(x + w - radius, y);
  ctx.quadraticCurveTo(x + w, y, x + w, y + radius);
  ctx.lineTo(x + w, y + h - radius);
  ctx.quadraticCurveTo(x + w, y + h, x + w - radius, y + h);
  ctx.lineTo(x + radius, y + h);
  ctx.quadraticCurveTo(x, y + h, x, y + h - radius);
  ctx.lineTo(x, y + radius);
  ctx.quadraticCurveTo(x, y, x + radius, y);
  ctx.closePath();
}

export function traceStar(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  outerR: number,
  innerR: number,
  points = 5,
  rotation = -Math.PI / 2
): void {
  ctx.beginPath();
  for (let i = 0; i < points * 2; i++) {
    const r = i % 2 === 0 ? outerR : innerR;
    const angle = rotation + (i * Math.PI) / points;
    const px = cx + Math.cos(angle) * r;
    const py = cy + Math.sin(angle) * r;
    if (i === 0) {
      ctx.moveTo(px, py);
    } else {
      ctx.lineTo(px, py);
    }
  }
  ctx.closePath();
}

export function drawSparkle(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  size: number,
  color = '#FFFDF2',
  alpha = 1
): void {
  ctx.save();
  ctx.globalAlpha *= alpha;
  ctx.fillStyle = color;
  ctx.beginPath();
  ctx.moveTo(cx, cy - size);
  ctx.quadraticCurveTo(cx + size * 0.16, cy - size * 0.16, cx + size, cy);
  ctx.quadraticCurveTo(cx + size * 0.16, cy + size * 0.16, cx, cy + size);
  ctx.quadraticCurveTo(cx - size * 0.16, cy + size * 0.16, cx - size, cy);
  ctx.quadraticCurveTo(cx - size * 0.16, cy - size * 0.16, cx, cy - size);
  ctx.closePath();
  ctx.fill();
  ctx.restore();
}

export function drawHeart(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  size: number,
  fill: string,
  stroke?: string,
  lineWidth = 2
): void {
  ctx.save();
  ctx.translate(cx, cy);
  ctx.beginPath();
  ctx.moveTo(0, size * 0.35);
  ctx.bezierCurveTo(-size * 0.9, -size * 0.2, -size * 0.5, -size * 0.9, 0, -size * 0.35);
  ctx.bezierCurveTo(size * 0.5, -size * 0.9, size * 0.9, -size * 0.2, 0, size * 0.35);
  ctx.closePath();
  ctx.fillStyle = fill;
  ctx.fill();
  if (stroke) {
    ctx.strokeStyle = stroke;
    ctx.lineWidth = lineWidth;
    ctx.lineJoin = 'round';
    ctx.stroke();
  }
  ctx.restore();
}

export function drawPuffyCloud(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  scale: number,
  fill = 'rgba(255, 253, 248, 0.92)'
): void {
  ctx.save();
  ctx.translate(cx, cy);
  ctx.scale(scale, scale);
  ctx.fillStyle = fill;
  ctx.beginPath();
  ctx.arc(-28, 6, 22, 0, Math.PI * 2);
  ctx.arc(0, -6, 28, 0, Math.PI * 2);
  ctx.arc(28, 6, 21, 0, Math.PI * 2);
  ctx.arc(0, 12, 24, 0, Math.PI * 2);
  ctx.fill();
  ctx.restore();
}

export function drawDaisyFlower(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  radius: number,
  petalColor = '#FFF9FC',
  centerColor = '#FFE17D'
): void {
  ctx.save();
  ctx.translate(cx, cy);
  ctx.fillStyle = petalColor;
  for (let i = 0; i < 6; i++) {
    const a = (i * Math.PI) / 3;
    ctx.beginPath();
    ctx.ellipse(
      Math.cos(a) * radius * 0.58,
      Math.sin(a) * radius * 0.58,
      radius * 0.42,
      radius * 0.28,
      a,
      0,
      Math.PI * 2
    );
    ctx.fill();
  }
  ctx.beginPath();
  ctx.arc(0, 0, radius * 0.36, 0, Math.PI * 2);
  ctx.fillStyle = centerColor;
  ctx.fill();
  ctx.restore();
}

export function drawGlossyEye(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  rx: number,
  ry: number,
  irisColor = '#5B7FD1',
  blinkProgress = 0
): void {
  ctx.save();
  if (blinkProgress > 0.55) {
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = Math.max(2, rx * 0.28);
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.arc(cx, cy + ry * 0.15, rx * 0.9, Math.PI * 0.12, Math.PI * 0.88);
    ctx.stroke();
    ctx.restore();
    return;
  }

  // Sclera
  ctx.beginPath();
  ctx.ellipse(cx, cy, rx, ry, 0, 0, Math.PI * 2);
  ctx.fillStyle = '#FFFFFF';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = Math.max(1.8, rx * 0.16);
  ctx.stroke();

  // Iris
  ctx.beginPath();
  ctx.ellipse(cx, cy + ry * 0.06, rx * 0.72, ry * 0.76, 0, 0, Math.PI * 2);
  ctx.fillStyle = irisColor;
  ctx.fill();

  // Pupil
  ctx.beginPath();
  ctx.ellipse(cx, cy + ry * 0.08, rx * 0.42, ry * 0.46, 0, 0, Math.PI * 2);
  ctx.fillStyle = '#3B2848';
  ctx.fill();

  // Dual specular highlights
  ctx.beginPath();
  ctx.arc(cx - rx * 0.24, cy - ry * 0.26, rx * 0.22, 0, Math.PI * 2);
  ctx.fillStyle = '#FFFFFF';
  ctx.fill();

  ctx.beginPath();
  ctx.arc(cx + rx * 0.22, cy + ry * 0.25, rx * 0.11, 0, Math.PI * 2);
  ctx.fill();
  ctx.restore();
}

export function drawBlushCheek(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  rx: number,
  ry: number
): void {
  ctx.save();
  ctx.fillStyle = 'rgba(255, 146, 178, 0.55)';
  ctx.beginPath();
  ctx.ellipse(cx, cy, rx, ry, 0, 0, Math.PI * 2);
  ctx.fill();
  ctx.restore();
}

export function drawPastelRainbow(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  outerR: number,
  bandThickness: number,
  alpha = 0.85
): void {
  const colors = ['#FFA6C1', '#FFD29D', '#FFF09E', '#B4F0B7', '#A5E3FF', '#D5B8FF'];
  ctx.save();
  ctx.globalAlpha *= alpha;
  ctx.lineCap = 'round';
  colors.forEach((col, i) => {
    const r = outerR - i * bandThickness;
    if (r <= 4) return;
    ctx.beginPath();
    ctx.arc(cx, cy, r, Math.PI * 1.04, Math.PI * 1.96);
    ctx.strokeStyle = col;
    ctx.lineWidth = bandThickness + 1;
    ctx.stroke();
  });
  ctx.restore();
}

export function drawButterfly(
  ctx: CanvasRenderingContext2D,
  cx: number,
  cy: number,
  size: number,
  wingColor: string,
  flapPhase: number
): void {
  ctx.save();
  ctx.translate(cx, cy);
  const flap = 0.55 + Math.abs(Math.sin(flapPhase)) * 0.55;
  ctx.scale(flap, 1);

  ctx.fillStyle = wingColor;
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = Math.max(1.5, size * 0.09);

  // Left wings
  ctx.beginPath();
  ctx.ellipse(-size * 0.48, -size * 0.24, size * 0.48, size * 0.34, -0.35, 0, Math.PI * 2);
  ctx.fill();
  ctx.stroke();
  ctx.beginPath();
  ctx.ellipse(-size * 0.36, size * 0.26, size * 0.34, size * 0.25, 0.3, 0, Math.PI * 2);
  ctx.fill();
  ctx.stroke();

  // Right wings
  ctx.beginPath();
  ctx.ellipse(size * 0.48, -size * 0.24, size * 0.48, size * 0.34, 0.35, 0, Math.PI * 2);
  ctx.fill();
  ctx.stroke();
  ctx.beginPath();
  ctx.ellipse(size * 0.36, size * 0.26, size * 0.34, size * 0.25, -0.3, 0, Math.PI * 2);
  ctx.fill();
  ctx.stroke();

  // Body
  ctx.beginPath();
  ctx.ellipse(0, 0, size * 0.12, size * 0.42, 0, 0, Math.PI * 2);
  ctx.fillStyle = STORYBOOK_INK;
  ctx.fill();
  ctx.restore();
}

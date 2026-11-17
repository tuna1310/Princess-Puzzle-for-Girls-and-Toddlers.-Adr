import type { LevelDefinition, SceneThemeId } from '../game/levels';
import {
  drawBlushCheek,
  drawButterfly,
  drawDaisyFlower,
  drawGlossyEye,
  drawHeart,
  drawPastelRainbow,
  drawPuffyCloud,
  drawSparkle,
  STORYBOOK_INK,
  traceRoundedRect,
  traceStar
} from './drawUtils';

const sceneCache = new Map<string, HTMLCanvasElement>();

/**
 * Render (or return cached) high-resolution square illustration canvas for a level.
 */
export function getCachedLevelArtwork(theme: SceneThemeId, size = 800): HTMLCanvasElement {
  const key = `${theme}:${size}`;
  const existing = sceneCache.get(key);
  if (existing) {
    return existing;
  }

  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext('2d');
  if (ctx) {
    drawLevelIllustration(ctx, theme, size, 0);
  }
  sceneCache.set(key, canvas);
  return canvas;
}

/**
 * Draw the complete original storybook illustration for a level inside `(0, 0, size, size)`.
 */
export function drawLevelIllustration(
  ctx: CanvasRenderingContext2D,
  theme: SceneThemeId,
  size: number,
  time: number
): void {
  ctx.save();
  switch (theme) {
    case 'tiara':
      drawTiaraScene(ctx, size, time);
      break;
    case 'bunny':
      drawBunnyPrincessScene(ctx, size, time);
      break;
    case 'unicorn':
      drawUnicornScene(ctx, size, time);
      break;
    case 'castle':
      drawCastleScene(ctx, size, time);
      break;
    default:
      drawLockedPlaceholderScene(ctx, theme, size, time);
      break;
  }
  ctx.restore();
}

/**
 * Draw the dynamic living-picture overlay during win celebration ("bức tranh sống dậy").
 */
export function drawLivingCelebrationOverlay(
  ctx: CanvasRenderingContext2D,
  theme: SceneThemeId,
  x: number,
  y: number,
  size: number,
  time: number
): void {
  ctx.save();
  ctx.translate(x, y);
  // Clip safely inside board bounds
  ctx.beginPath();
  ctx.rect(0, 0, size, size);
  ctx.clip();

  // Gentle twinkling starlight sparkles across every winning picture
  for (let i = 0; i < 8; i++) {
    const px = size * (0.14 + ((i * 0.23) % 0.74));
    const py = size * (0.12 + ((i * 0.19) % 0.68)) + Math.sin(time * 2.5 + i) * size * 0.015;
    const scale = 0.5 + 0.5 * Math.abs(Math.sin(time * 3.1 + i * 1.1));
    drawSparkle(ctx, px, py, size * 0.032 * scale, '#FFFDF2', 0.9);
  }

  if (theme === 'tiara') {
    // Floating golden hearts & shimmering crown aura
    for (let i = 0; i < 4; i++) {
      const hx = size * (0.25 + i * 0.16);
      const hy = size * (0.34 - ((time * 0.12 + i * 0.25) % 0.22));
      const alpha = Math.sin(((time * 0.5 + i * 0.25) % 1) * Math.PI);
      ctx.save();
      ctx.globalAlpha = Math.max(0, alpha * 0.85);
      drawHeart(ctx, hx, hy, size * 0.035, '#FF96C5', '#FFFDF8', 2);
      ctx.restore();
    }
  } else if (theme === 'bunny') {
    // Fluttering pastel butterflies orbiting Clover Bunny
    const b1x = size * 0.24 + Math.cos(time * 1.9) * size * 0.05;
    const b1y = size * 0.3 + Math.sin(time * 2.6) * size * 0.04;
    drawButterfly(ctx, b1x, b1y, size * 0.042, '#FFB3DA', time * 11);

    const b2x = size * 0.78 + Math.sin(time * 1.7) * size * 0.04;
    const b2y = size * 0.34 + Math.cos(time * 2.4) * size * 0.04;
    drawButterfly(ctx, b2x, b2y, size * 0.036, '#A6E4FF', time * 12 + 1);
  } else if (theme === 'unicorn') {
    // Magic stardust ribbon trailing the rainbow
    for (let i = 0; i < 6; i++) {
      const ang = Math.PI * 1.12 + i * 0.15 + Math.sin(time * 2 + i) * 0.04;
      const r = size * 0.44;
      const sx = size * 0.5 + Math.cos(ang) * r;
      const sy = size * 0.65 + Math.sin(ang) * r;
      drawSparkle(ctx, sx, sy, size * 0.025, '#FFE887', 0.85);
    }
  } else if (theme === 'castle') {
    // Fluttering twin butterflies & drifting clouds above towers
    drawButterfly(
      ctx,
      size * 0.22 + Math.sin(time * 1.6) * size * 0.05,
      size * 0.26 + Math.cos(time * 2.2) * size * 0.03,
      size * 0.035,
      '#FFD29D',
      time * 10
    );
    drawButterfly(
      ctx,
      size * 0.78 + Math.cos(time * 1.5) * size * 0.05,
      size * 0.22 + Math.sin(time * 2.1) * size * 0.03,
      size * 0.032,
      '#FFAFDA',
      time * 11 + 2
    );
  }

  ctx.restore();
}

/**
 * Draw a miniature level medallion emblem for the 16-level carousel wheel.
 */
export function drawLevelMedallionArtwork(
  ctx: CanvasRenderingContext2D,
  level: LevelDefinition,
  cx: number,
  cy: number,
  radius: number,
  time: number
): void {
  ctx.save();
  ctx.beginPath();
  ctx.arc(cx, cy, radius, 0, Math.PI * 2);
  ctx.clip();

  if (level.implementedInStep2) {
    const artwork = getCachedLevelArtwork(level.theme, 256);
    ctx.drawImage(artwork, cx - radius, cy - radius, radius * 2, radius * 2);
  } else {
    drawLockedPlaceholderScene(ctx, level.theme, radius * 2, time, cx - radius, cy - radius);
  }

  ctx.restore();
}

function drawSceneSkyAndMeadow(
  ctx: CanvasRenderingContext2D,
  s: number,
  skyTop: string,
  skyBottom: string,
  hillColor: string
): void {
  const sky = ctx.createLinearGradient(0, 0, 0, s);
  sky.addColorStop(0, skyTop);
  sky.addColorStop(0.7, skyBottom);
  sky.addColorStop(1, '#FFF8EE');
  ctx.fillStyle = sky;
  ctx.fillRect(0, 0, s, s);

  // Soft rolling hill at bottom
  ctx.beginPath();
  ctx.moveTo(0, s * 0.74);
  ctx.quadraticCurveTo(s * 0.3, s * 0.65, s * 0.65, s * 0.73);
  ctx.quadraticCurveTo(s * 0.86, s * 0.78, s, s * 0.7);
  ctx.lineTo(s, s);
  ctx.lineTo(0, s);
  ctx.closePath();
  ctx.fillStyle = hillColor;
  ctx.fill();
}

function drawTiaraScene(ctx: CanvasRenderingContext2D, s: number, _time: number): void {
  drawSceneSkyAndMeadow(ctx, s, '#D6C6FF', '#FFE6F4', '#FFD2EA');

  // Soft radiant halo behind tiara
  const halo = ctx.createRadialGradient(s * 0.5, s * 0.45, s * 0.05, s * 0.5, s * 0.45, s * 0.42);
  halo.addColorStop(0, 'rgba(255, 253, 240, 0.92)');
  halo.addColorStop(1, 'rgba(255, 253, 240, 0)');
  ctx.fillStyle = halo;
  ctx.fillRect(0, 0, s, s);

  // Decorative background sparkles & clouds
  drawPuffyCloud(ctx, s * 0.2, s * 0.2, s / 420, 'rgba(255,255,255,0.75)');
  drawPuffyCloud(ctx, s * 0.82, s * 0.22, s / 470, 'rgba(255,255,255,0.75)');

  // Plush royal lavender pillow cushion
  ctx.save();
  const cGrad = ctx.createLinearGradient(s * 0.2, s * 0.54, s * 0.8, s * 0.84);
  cGrad.addColorStop(0, '#E9C5FF');
  cGrad.addColorStop(0.5, '#D29EFF');
  cGrad.addColorStop(1, '#B883EE');

  ctx.beginPath();
  ctx.moveTo(s * 0.16, s * 0.66);
  ctx.quadraticCurveTo(s * 0.5, s * 0.52, s * 0.84, s * 0.66);
  ctx.quadraticCurveTo(s * 0.88, s * 0.76, s * 0.8, s * 0.84);
  ctx.quadraticCurveTo(s * 0.5, s * 0.91, s * 0.2, s * 0.84);
  ctx.quadraticCurveTo(s * 0.12, s * 0.76, s * 0.16, s * 0.66);
  ctx.closePath();
  ctx.fillStyle = cGrad;
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = s * 0.012;
  ctx.lineJoin = 'round';
  ctx.stroke();

  // Cushion gold cord trim & tassels
  ctx.strokeStyle = '#FFD875';
  ctx.lineWidth = s * 0.011;
  ctx.beginPath();
  ctx.moveTo(s * 0.19, s * 0.74);
  ctx.quadraticCurveTo(s * 0.5, s * 0.82, s * 0.81, s * 0.74);
  ctx.stroke();

  for (const tx of [s * 0.16, s * 0.84]) {
    ctx.beginPath();
    ctx.arc(tx, s * 0.73, s * 0.025, 0, Math.PI * 2);
    ctx.fillStyle = '#FFE28A';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = s * 0.008;
    ctx.stroke();
  }

  // Cute embroidered star on pillow front
  traceStar(ctx, s * 0.5, s * 0.73, s * 0.045, s * 0.021);
  ctx.fillStyle = '#FFF5BA';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = s * 0.007;
  ctx.stroke();
  ctx.restore();

  // Golden storybook tiara resting proudly on pillow
  ctx.save();
  const tGrad = ctx.createLinearGradient(s * 0.28, s * 0.28, s * 0.72, s * 0.62);
  tGrad.addColorStop(0, '#FFF09E');
  tGrad.addColorStop(0.5, '#FFD369');
  tGrad.addColorStop(1, '#F6B148');

  ctx.beginPath();
  ctx.moveTo(s * 0.24, s * 0.58);
  ctx.quadraticCurveTo(s * 0.23, s * 0.42, s * 0.3, s * 0.34);
  ctx.quadraticCurveTo(s * 0.37, s * 0.47, s * 0.4, s * 0.46);
  ctx.quadraticCurveTo(s * 0.45, s * 0.27, s * 0.5, s * 0.24);
  ctx.quadraticCurveTo(s * 0.55, s * 0.27, s * 0.6, s * 0.46);
  ctx.quadraticCurveTo(s * 0.63, s * 0.47, s * 0.7, s * 0.34);
  ctx.quadraticCurveTo(s * 0.77, s * 0.42, s * 0.76, s * 0.58);
  ctx.quadraticCurveTo(s * 0.5, s * 0.64, s * 0.24, s * 0.58);
  ctx.closePath();

  ctx.fillStyle = tGrad;
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = s * 0.013;
  ctx.lineJoin = 'round';
  ctx.stroke();

  // Tiara tip pearls
  const pearls = [
    [0.3, 0.33, 0.026, '#FFB3D9'],
    [0.5, 0.22, 0.034, '#9AE3FF'],
    [0.7, 0.33, 0.026, '#FFB3D9']
  ] as const;
  for (const [px, py, pr, col] of pearls) {
    ctx.beginPath();
    ctx.arc(s * px, s * py, s * pr, 0, Math.PI * 2);
    ctx.fillStyle = col;
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = s * 0.009;
    ctx.stroke();
    ctx.beginPath();
    ctx.arc(s * px - s * pr * 0.28, s * py - s * pr * 0.28, s * pr * 0.24, 0, Math.PI * 2);
    ctx.fillStyle = '#FFFFFF';
    ctx.fill();
  }

  // Center heart jewel & side gems on tiara band
  drawHeart(ctx, s * 0.5, s * 0.48, s * 0.068, '#FF7EB6', STORYBOOK_INK, s * 0.009);
  drawHeart(ctx, s * 0.37, s * 0.52, s * 0.036, '#A3E4FF', STORYBOOK_INK, s * 0.007);
  drawHeart(ctx, s * 0.63, s * 0.52, s * 0.036, '#A3E4FF', STORYBOOK_INK, s * 0.007);
  ctx.restore();

  drawSparkle(ctx, s * 0.19, s * 0.36, s * 0.03, '#FFFDF4');
  drawSparkle(ctx, s * 0.82, s * 0.38, s * 0.026, '#FFFDF4');
  drawSparkle(ctx, s * 0.5, s * 0.13, s * 0.024, '#FFE985');
}

function drawBunnyPrincessScene(ctx: CanvasRenderingContext2D, s: number, _time: number): void {
  drawSceneSkyAndMeadow(ctx, s, '#BCE8FF', '#EAF7FF', '#A7DE8E');

  drawPuffyCloud(ctx, s * 0.2, s * 0.17, s / 420);
  drawPuffyCloud(ctx, s * 0.8, s * 0.2, s / 460);

  // Foreground daisies
  drawDaisyFlower(ctx, s * 0.16, s * 0.85, s * 0.045, '#FFFDF8');
  drawDaisyFlower(ctx, s * 0.84, s * 0.83, s * 0.05, '#FFE3F1');
  drawDaisyFlower(ctx, s * 0.74, s * 0.9, s * 0.038, '#FFFDF8');

  const inkW = s * 0.011;

  // Star magic wand in bunny's right paw
  ctx.save();
  ctx.strokeStyle = '#EBB866';
  ctx.lineWidth = s * 0.016;
  ctx.lineCap = 'round';
  ctx.beginPath();
  ctx.moveTo(s * 0.67, s * 0.63);
  ctx.lineTo(s * 0.79, s * 0.41);
  ctx.stroke();
  traceStar(ctx, s * 0.8, s * 0.39, s * 0.06, s * 0.028);
  ctx.fillStyle = '#FFE478';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = s * 0.009;
  ctx.stroke();
  ctx.restore();

  // Little feet
  for (const fx of [s * 0.43, s * 0.57]) {
    ctx.beginPath();
    ctx.ellipse(fx, s * 0.83, s * 0.055, s * 0.032, 0, 0, Math.PI * 2);
    ctx.fillStyle = '#FFB5D5';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW;
    ctx.stroke();
  }

  // Fluffy pastel princess gown
  const dressGrad = ctx.createLinearGradient(s * 0.32, s * 0.52, s * 0.68, s * 0.82);
  dressGrad.addColorStop(0, '#E3C3FF');
  dressGrad.addColorStop(0.55, '#FFB8DE');
  dressGrad.addColorStop(1, '#FFA1D0');
  ctx.beginPath();
  ctx.moveTo(s * 0.41, s * 0.54);
  ctx.lineTo(s * 0.59, s * 0.54);
  ctx.quadraticCurveTo(s * 0.72, s * 0.71, s * 0.69, s * 0.81);
  ctx.quadraticCurveTo(s * 0.5, s * 0.85, s * 0.31, s * 0.81);
  ctx.quadraticCurveTo(s * 0.28, s * 0.71, s * 0.41, s * 0.54);
  ctx.closePath();
  ctx.fillStyle = dressGrad;
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW;
  ctx.lineJoin = 'round';
  ctx.stroke();

  // Dress scalloped sash & heart accent
  drawHeart(ctx, s * 0.5, s * 0.62, s * 0.035, '#FFF7CC', STORYBOOK_INK, s * 0.006);

  // Cute arms & puff sleeves
  for (const side of [-1, 1]) {
    const sx = s * (0.5 + side * 0.12);
    const sy = s * 0.58;
    ctx.beginPath();
    ctx.ellipse(sx + side * s * 0.04, sy + s * 0.04, s * 0.042, s * 0.028, side * 0.45, 0, Math.PI * 2);
    ctx.fillStyle = '#FFF7F2';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW;
    ctx.stroke();

    ctx.beginPath();
    ctx.arc(sx, sy, s * 0.034, 0, Math.PI * 2);
    ctx.fillStyle = '#EBCBFF';
    ctx.fill();
    ctx.stroke();
  }

  // Tall rounded bunny ears (behind head)
  for (const side of [-1, 1]) {
    ctx.save();
    ctx.translate(s * (0.5 + side * 0.095), s * 0.25);
    ctx.rotate(side * 0.18);
    ctx.beginPath();
    ctx.ellipse(0, 0, s * 0.056, s * 0.14, 0, 0, Math.PI * 2);
    ctx.fillStyle = '#FFF9F5';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW;
    ctx.stroke();

    ctx.beginPath();
    ctx.ellipse(0, s * 0.015, s * 0.028, s * 0.09, 0, 0, Math.PI * 2);
    ctx.fillStyle = '#FFC2D9';
    ctx.fill();
    ctx.restore();
  }

  // Round cream bunny head
  ctx.beginPath();
  ctx.ellipse(s * 0.5, s * 0.42, s * 0.175, s * 0.145, 0, 0, Math.PI * 2);
  ctx.fillStyle = '#FFF9F5';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW;
  ctx.stroke();

  // Flower crown on Clover Bunny
  drawDaisyFlower(ctx, s * 0.42, s * 0.295, s * 0.036, '#FFB5D6');
  drawDaisyFlower(ctx, s * 0.5, s * 0.28, s * 0.042, '#FFF9C7');
  drawDaisyFlower(ctx, s * 0.58, s * 0.295, s * 0.036, '#B5E4FF');

  // Glossy eyes, pink nose, smile, blush
  drawGlossyEye(ctx, s * 0.43, s * 0.415, s * 0.026, s * 0.031, '#7B6BC9');
  drawGlossyEye(ctx, s * 0.57, s * 0.415, s * 0.026, s * 0.031, '#7B6BC9');
  drawBlushCheek(ctx, s * 0.375, s * 0.455, s * 0.028, s * 0.017);
  drawBlushCheek(ctx, s * 0.625, s * 0.455, s * 0.028, s * 0.017);

  ctx.beginPath();
  ctx.ellipse(s * 0.5, s * 0.452, s * 0.016, s * 0.011, 0, 0, Math.PI * 2);
  ctx.fillStyle = '#FF90B8';
  ctx.fill();

  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = s * 0.008;
  ctx.lineCap = 'round';
  ctx.beginPath();
  ctx.arc(s * 0.484, s * 0.468, s * 0.016, 0.1, Math.PI * 0.85);
  ctx.stroke();
  ctx.beginPath();
  ctx.arc(s * 0.516, s * 0.468, s * 0.016, Math.PI * 0.15, Math.PI * 0.9);
  ctx.stroke();

  drawButterfly(ctx, s * 0.22, s * 0.36, s * 0.04, '#FFB8DC', 0.4);
  drawSparkle(ctx, s * 0.82, s * 0.28, s * 0.026, '#FFFDF0');
}

function drawUnicornScene(ctx: CanvasRenderingContext2D, s: number, _time: number): void {
  const sky = ctx.createLinearGradient(0, 0, 0, s);
  sky.addColorStop(0, '#B5E4FF');
  sky.addColorStop(0.65, '#FCE7F6');
  sky.addColorStop(1, '#FFF5E2');
  ctx.fillStyle = sky;
  ctx.fillRect(0, 0, s, s);

  // Big pastel rainbow behind unicorn
  drawPastelRainbow(ctx, s * 0.5, s * 0.66, s * 0.44, s * 0.024, 0.88);

  // Dreamy clouds at base
  drawPuffyCloud(ctx, s * 0.22, s * 0.72, s / 260, '#FFFFFF');
  drawPuffyCloud(ctx, s * 0.78, s * 0.71, s / 270, '#FFFFFF');
  drawPuffyCloud(ctx, s * 0.5, s * 0.8, s / 230, '#FFFDF9');

  const inkW = s * 0.011;

  // Fluffy rainbow tail
  const tailColors = ['#FF9FC6', '#FFE38E', '#9EE5FF', '#D2B3FF'];
  tailColors.forEach((col, i) => {
    ctx.beginPath();
    ctx.ellipse(
      s * (0.25 - i * 0.018),
      s * (0.56 + i * 0.028),
      s * 0.055,
      s * 0.032,
      -0.6 + i * 0.15,
      0,
      Math.PI * 2
    );
    ctx.fillStyle = col;
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW * 0.75;
    ctx.stroke();
  });

  // Back & front cute hooves/legs
  const legs = [
    [0.37, 0.67, 0.22],
    [0.44, 0.71, 0.08],
    [0.57, 0.7, -0.15],
    [0.64, 0.66, -0.35]
  ] as const;

  for (const [lx, ly, rot] of legs) {
    ctx.save();
    ctx.translate(s * lx, s * ly);
    ctx.rotate(rot);
    traceRoundedRect(ctx, -s * 0.028, -s * 0.05, s * 0.056, s * 0.11, s * 0.026);
    ctx.fillStyle = '#FFFDF9';
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW;
    ctx.stroke();
    // Pastel lavender hoof
    traceRoundedRect(ctx, -s * 0.026, s * 0.028, s * 0.052, s * 0.032, s * 0.015);
    ctx.fillStyle = '#E0B5FF';
    ctx.fill();
    ctx.stroke();
    ctx.restore();
  }

  // Plump ivory unicorn body
  ctx.beginPath();
  ctx.ellipse(s * 0.48, s * 0.58, s * 0.18, s * 0.125, -0.08, 0, Math.PI * 2);
  ctx.fillStyle = '#FFFDF9';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW;
  ctx.stroke();

  // Star cutie mark on hip
  traceStar(ctx, s * 0.39, s * 0.58, s * 0.032, s * 0.015);
  ctx.fillStyle = '#FFAFDA';
  ctx.fill();

  // Neck & big adorable head
  ctx.beginPath();
  ctx.ellipse(s * 0.62, s * 0.41, s * 0.145, s * 0.125, 0.1, 0, Math.PI * 2);
  ctx.fillStyle = '#FFFDF9';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW;
  ctx.stroke();

  // Soft pink muzzle
  ctx.beginPath();
  ctx.ellipse(s * 0.72, s * 0.44, s * 0.072, s * 0.058, 0.1, 0, Math.PI * 2);
  ctx.fillStyle = '#FFE4F0';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW;
  ctx.stroke();

  // Mane curls along crest
  const maneCurls = [
    [0.52, 0.32, 0.048, '#FF9EC7'],
    [0.49, 0.39, 0.045, '#FFE48E'],
    [0.47, 0.46, 0.042, '#9CE4FF'],
    [0.62, 0.29, 0.042, '#D3B2FF']
  ] as const;
  for (const [mx, my, mr, col] of maneCurls) {
    ctx.beginPath();
    ctx.arc(s * mx, s * my, s * mr, 0, Math.PI * 2);
    ctx.fillStyle = col;
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW * 0.8;
    ctx.stroke();
  }

  // Cute ear
  ctx.beginPath();
  ctx.ellipse(s * 0.56, s * 0.27, s * 0.028, s * 0.052, -0.3, 0, Math.PI * 2);
  ctx.fillStyle = '#FFFDF9';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW * 0.9;
  ctx.stroke();

  // Golden spiral unicorn horn
  ctx.save();
  ctx.translate(s * 0.67, s * 0.28);
  ctx.rotate(0.32);
  ctx.beginPath();
  ctx.moveTo(-s * 0.026, 0);
  ctx.lineTo(0, -s * 0.11);
  ctx.lineTo(s * 0.026, 0);
  ctx.closePath();
  ctx.fillStyle = '#FFE27D';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW * 0.85;
  ctx.lineJoin = 'round';
  ctx.stroke();
  ctx.restore();

  // Expressive eye, cheek, cute smile
  drawGlossyEye(ctx, s * 0.64, s * 0.39, s * 0.028, s * 0.032, '#866BE0');
  drawBlushCheek(ctx, s * 0.61, s * 0.455, s * 0.028, s * 0.017);

  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = s * 0.008;
  ctx.lineCap = 'round';
  ctx.beginPath();
  ctx.arc(s * 0.725, s * 0.455, s * 0.024, 0.2, Math.PI * 0.85);
  ctx.stroke();

  drawSparkle(ctx, s * 0.2, s * 0.24, s * 0.028, '#FFFDF2');
  drawSparkle(ctx, s * 0.83, s * 0.26, s * 0.03, '#FFE98F');
}

function drawCastleScene(ctx: CanvasRenderingContext2D, s: number, _time: number): void {
  drawSceneSkyAndMeadow(ctx, s, '#AEE2FF', '#E5F5FF', '#9DD985');

  // Smiling sun in upper left
  ctx.save();
  const sunCx = s * 0.16;
  const sunCy = s * 0.16;
  const sunRad = s * 0.068;
  ctx.strokeStyle = '#FFD768';
  ctx.lineWidth = s * 0.011;
  ctx.lineCap = 'round';
  for (let r = 0; r < 8; r++) {
    const a = (r * Math.PI) / 4;
    ctx.beginPath();
    ctx.moveTo(sunCx + Math.cos(a) * sunRad * 1.22, sunCy + Math.sin(a) * sunRad * 1.22);
    ctx.lineTo(sunCx + Math.cos(a) * sunRad * 1.5, sunCy + Math.sin(a) * sunRad * 1.5);
    ctx.stroke();
  }
  ctx.beginPath();
  ctx.arc(sunCx, sunCy, sunRad, 0, Math.PI * 2);
  ctx.fillStyle = '#FFE57E';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = s * 0.009;
  ctx.stroke();
  drawBlushCheek(ctx, sunCx - s * 0.032, sunCy + s * 0.014, s * 0.014, s * 0.009);
  drawBlushCheek(ctx, sunCx + s * 0.032, sunCy + s * 0.014, s * 0.014, s * 0.009);
  ctx.fillStyle = STORYBOOK_INK;
  ctx.beginPath();
  ctx.arc(sunCx - s * 0.022, sunCy - s * 0.008, s * 0.007, 0, Math.PI * 2);
  ctx.arc(sunCx + s * 0.022, sunCy - s * 0.008, s * 0.007, 0, Math.PI * 2);
  ctx.fill();
  ctx.beginPath();
  ctx.arc(sunCx, sunCy + s * 0.012, s * 0.018, 0.15, Math.PI - 0.15);
  ctx.stroke();
  ctx.restore();

  drawPastelRainbow(ctx, s * 0.56, s * 0.52, s * 0.36, s * 0.016, 0.65);
  drawPuffyCloud(ctx, s * 0.81, s * 0.17, s / 420);

  const inkW = s * 0.01;

  // Helper for castle towers + cupcake-cone turrets
  const drawTower = (
    tx: number,
    ty: number,
    tw: number,
    th: number,
    wallCol: string,
    roofCol: string,
    flagCol: string
  ): void => {
    // Tower body
    traceRoundedRect(ctx, tx, ty, tw, th, s * 0.018);
    ctx.fillStyle = wallCol;
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW;
    ctx.stroke();

    // Flagpole & pennant
    const cx = tx + tw * 0.5;
    const tipY = ty - tw * 1.15;
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW * 0.75;
    ctx.beginPath();
    ctx.moveTo(cx, ty - tw * 0.75);
    ctx.lineTo(cx, tipY);
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(cx, tipY);
    ctx.lineTo(cx + s * 0.075, tipY + s * 0.025);
    ctx.lineTo(cx, tipY + s * 0.05);
    ctx.closePath();
    ctx.fillStyle = flagCol;
    ctx.fill();
    ctx.stroke();

    // Cone roof
    ctx.beginPath();
    ctx.moveTo(tx - tw * 0.09, ty + s * 0.008);
    ctx.quadraticCurveTo(cx - tw * 0.15, ty - tw * 0.45, cx, ty - tw * 0.82);
    ctx.quadraticCurveTo(cx + tw * 0.15, ty - tw * 0.45, tx + tw * 1.09, ty + s * 0.008);
    ctx.closePath();
    ctx.fillStyle = roofCol;
    ctx.fill();
    ctx.strokeStyle = STORYBOOK_INK;
    ctx.lineWidth = inkW;
    ctx.lineJoin = 'round';
    ctx.stroke();

    // Little arched window
    ctx.beginPath();
    ctx.arc(cx, ty + th * 0.34, tw * 0.19, Math.PI, 0);
    ctx.lineTo(cx + tw * 0.19, ty + th * 0.52);
    ctx.lineTo(cx - tw * 0.19, ty + th * 0.52);
    ctx.closePath();
    ctx.fillStyle = '#FFF5BA';
    ctx.fill();
    ctx.stroke();
  };

  // Left & right side towers
  drawTower(s * 0.2, s * 0.43, s * 0.13, s * 0.34, '#FCE8F3', '#FFA6CF', '#9EE5FF');
  drawTower(s * 0.67, s * 0.43, s * 0.13, s * 0.34, '#FCE8F3', '#FFA6CF', '#9EE5FF');

  // Tall center royal keep
  drawTower(s * 0.42, s * 0.29, s * 0.16, s * 0.48, '#FFF5FB', '#C7A6FF', '#FFE17D');

  // Main castle hall wall
  traceRoundedRect(ctx, s * 0.29, s * 0.51, s * 0.42, s * 0.27, s * 0.02);
  ctx.fillStyle = '#FFEAF4';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW;
  ctx.stroke();

  // Battlements on main wall
  for (let i = 0; i < 5; i++) {
    traceRoundedRect(ctx, s * (0.305 + i * 0.083), s * 0.475, s * 0.056, s * 0.042, s * 0.01);
    ctx.fillStyle = '#FFD3E7';
    ctx.fill();
    ctx.stroke();
  }

  // Grand arched wooden door + heart crest
  ctx.beginPath();
  ctx.arc(s * 0.5, s * 0.65, s * 0.068, Math.PI, 0);
  ctx.lineTo(s * 0.568, s * 0.78);
  ctx.lineTo(s * 0.432, s * 0.78);
  ctx.closePath();
  ctx.fillStyle = '#DDA982';
  ctx.fill();
  ctx.strokeStyle = STORYBOOK_INK;
  ctx.lineWidth = inkW;
  ctx.stroke();

  drawHeart(ctx, s * 0.5, s * 0.56, s * 0.036, '#FF88BC', STORYBOOK_INK, s * 0.007);

  // Garden daisies by castle gate
  drawDaisyFlower(ctx, s * 0.26, s * 0.81, s * 0.036, '#FFFDF9');
  drawDaisyFlower(ctx, s * 0.74, s * 0.81, s * 0.036, '#FFE3F1');
}

function drawLockedPlaceholderScene(
  ctx: CanvasRenderingContext2D,
  theme: SceneThemeId,
  s: number,
  _time: number,
  ox = 0,
  oy = 0
): void {
  ctx.save();
  ctx.translate(ox, oy);
  const bg = ctx.createLinearGradient(0, 0, s, s);
  bg.addColorStop(0, '#F6E7FF');
  bg.addColorStop(1, '#DFF4FF');
  ctx.fillStyle = bg;
  ctx.fillRect(0, 0, s, s);

  // Soft storybook emblem silhouette per upcoming theme
  const cx = s * 0.5;
  const cy = s * 0.5;
  ctx.fillStyle = 'rgba(255, 255, 255, 0.78)';
  ctx.beginPath();
  ctx.arc(cx, cy, s * 0.31, 0, Math.PI * 2);
  ctx.fill();

  const pastelMap: Record<SceneThemeId, string> = {
    tiara: '#FFB3D9',
    bunny: '#D7B8FF',
    unicorn: '#9CE3FF',
    castle: '#FFC2DE',
    dragon: '#A8E6A3',
    carriage: '#F8BFE3',
    willow: '#9FD9FF',
    mermaid: '#92E5DB',
    teaparty: '#FFBED6',
    treehouse: '#BEE6A4',
    harp: '#D4B6FF',
    swan: '#AAE3FF',
    pegasus: '#FFD0B4',
    lantern: '#FFE08E',
    carousel: '#FFACD6',
    ball: '#F1B3FF'
  };

  drawHeart(ctx, cx, cy + s * 0.02, s * 0.18, pastelMap[theme] ?? '#E2C3FF', STORYBOOK_INK, s * 0.018);
  drawSparkle(ctx, cx - s * 0.18, cy - s * 0.16, s * 0.045, '#FFFDF2');
  drawSparkle(ctx, cx + s * 0.18, cy - s * 0.15, s * 0.04, '#FFE987');
  ctx.restore();
}

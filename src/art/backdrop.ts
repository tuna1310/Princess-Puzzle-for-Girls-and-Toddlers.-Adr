import { drawDaisyFlower, drawPuffyCloud, drawSparkle } from './drawUtils';

/**
 * Draw the animated pastel storybook meadow backdrop shared across screens.
 * Inspired by calm picture-book hills, a soft stream, wooden bridge, and drifting clouds.
 */
export function drawMeadowBackdrop(
  ctx: CanvasRenderingContext2D,
  width: number,
  height: number,
  time: number
): void {
  // Morning pastel sky gradient
  const skyGrad = ctx.createLinearGradient(0, 0, 0, height * 0.68);
  skyGrad.addColorStop(0, '#AEE2FF');
  skyGrad.addColorStop(0.55, '#E4F4FF');
  skyGrad.addColorStop(1, '#FFF4E8');
  ctx.fillStyle = skyGrad;
  ctx.fillRect(0, 0, width, height);

  // Soft warm glowing sun
  const sunX = width * 0.16;
  const sunY = height * 0.13;
  const sunR = Math.min(width, height) * 0.08;
  const sunGlow = ctx.createRadialGradient(sunX, sunY, sunR * 0.2, sunX, sunY, sunR * 2.2);
  sunGlow.addColorStop(0, 'rgba(255, 236, 150, 0.85)');
  sunGlow.addColorStop(1, 'rgba(255, 236, 150, 0)');
  ctx.fillStyle = sunGlow;
  ctx.beginPath();
  ctx.arc(sunX, sunY, sunR * 2.2, 0, Math.PI * 2);
  ctx.fill();

  ctx.beginPath();
  ctx.arc(sunX, sunY, sunR * 0.65, 0, Math.PI * 2);
  ctx.fillStyle = '#FFE98F';
  ctx.fill();

  // Gently drifting clouds
  const cloudCount = 4;
  for (let i = 0; i < cloudCount; i++) {
    const speed = 6 + i * 2.5;
    const span = width + 220;
    const rawX = ((i * span) / cloudCount + time * speed) % span;
    const cx = rawX - 110;
    const cy = height * (0.09 + (i % 3) * 0.075);
    const scale = Math.max(0.65, Math.min(1.25, width / 650)) * (0.82 + (i % 2) * 0.25);
    drawPuffyCloud(ctx, cx, cy, scale, 'rgba(255, 253, 249, 0.86)');
  }

  // Far pastel mint-sage hill
  ctx.beginPath();
  ctx.moveTo(0, height * 0.56);
  ctx.quadraticCurveTo(width * 0.28, height * 0.44, width * 0.58, height * 0.53);
  ctx.quadraticCurveTo(width * 0.82, height * 0.6, width, height * 0.49);
  ctx.lineTo(width, height);
  ctx.lineTo(0, height);
  ctx.closePath();
  const farHillGrad = ctx.createLinearGradient(0, height * 0.45, 0, height);
  farHillGrad.addColorStop(0, '#BDE8A3');
  farHillGrad.addColorStop(1, '#98D47C');
  ctx.fillStyle = farHillGrad;
  ctx.fill();

  // Mid rolling meadow hill
  ctx.beginPath();
  ctx.moveTo(0, height * 0.64);
  ctx.quadraticCurveTo(width * 0.35, height * 0.57, width * 0.68, height * 0.67);
  ctx.quadraticCurveTo(width * 0.88, height * 0.72, width, height * 0.63);
  ctx.lineTo(width, height);
  ctx.lineTo(0, height);
  ctx.closePath();
  const midHillGrad = ctx.createLinearGradient(0, height * 0.58, 0, height);
  midHillGrad.addColorStop(0, '#A8DE89');
  midHillGrad.addColorStop(1, '#82C767');
  ctx.fillStyle = midHillGrad;
  ctx.fill();

  // Sparkling sky-blue stream across the lower meadow
  ctx.save();
  ctx.beginPath();
  ctx.moveTo(0, height * 0.76);
  ctx.bezierCurveTo(width * 0.3, height * 0.72, width * 0.65, height * 0.85, width, height * 0.8);
  ctx.lineTo(width, height * 0.87);
  ctx.bezierCurveTo(width * 0.64, height * 0.92, width * 0.28, height * 0.79, 0, height * 0.83);
  ctx.closePath();
  const streamGrad = ctx.createLinearGradient(0, height * 0.74, width, height * 0.88);
  streamGrad.addColorStop(0, '#8ED7FA');
  streamGrad.addColorStop(0.5, '#AEE7FF');
  streamGrad.addColorStop(1, '#7CCDF5');
  ctx.fillStyle = streamGrad;
  ctx.fill();

  // Gentle animated water shimmer lines
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.55)';
  ctx.lineWidth = 2.2;
  ctx.lineCap = 'round';
  for (let i = 0; i < 5; i++) {
    const t = (i * 0.21 + time * 0.03) % 1;
    const rx = t * width;
    const ry = height * (0.785 + Math.sin(t * Math.PI * 1.4) * 0.04);
    ctx.beginPath();
    ctx.moveTo(rx - 14, ry);
    ctx.lineTo(rx + 14, ry + 1.5);
    ctx.stroke();
  }
  ctx.restore();

  // Soft golden footpath leading toward bridge
  ctx.save();
  ctx.beginPath();
  ctx.moveTo(width * 0.34, height);
  ctx.quadraticCurveTo(width * 0.42, height * 0.88, width * 0.6, height * 0.81);
  ctx.quadraticCurveTo(width * 0.64, height * 0.82, width * 0.65, height * 0.84);
  ctx.quadraticCurveTo(width * 0.49, height * 0.9, width * 0.44, height);
  ctx.closePath();
  ctx.fillStyle = '#F7E7B8';
  ctx.fill();
  ctx.restore();

  // Cute pastel wooden footbridge over stream
  ctx.save();
  const bx = width * 0.61;
  const by = height * 0.815;
  ctx.translate(bx, by);
  ctx.rotate(-0.22);
  ctx.fillStyle = '#D9A77A';
  ctx.strokeStyle = '#9E6E47';
  ctx.lineWidth = 2;
  for (let i = -2; i <= 2; i++) {
    ctx.beginPath();
    ctx.roundRect(i * 11 - 4.5, -14, 9, 28, 3);
    ctx.fill();
    ctx.stroke();
  }
  ctx.restore();

  // Little storybook meadow daisies
  const flowerPositions = [
    [0.08, 0.88, 9, '#FFFDF9'],
    [0.14, 0.93, 11, '#FFE2F0'],
    [0.24, 0.87, 8, '#FFFDF9'],
    [0.78, 0.91, 10, '#FFFDF9'],
    [0.88, 0.87, 12, '#FFE6F2'],
    [0.93, 0.94, 9, '#FFFDF9']
  ] as const;

  for (let i = 0; i < flowerPositions.length; i++) {
    const [fx, fy, fr, color] = flowerPositions[i]!;
    const sway = Math.sin(time * 1.6 + i) * 2;
    drawDaisyFlower(ctx, width * fx + sway, height * fy, fr, color);
  }

  // Faint floating fairy sparkles
  for (let i = 0; i < 6; i++) {
    const sx = width * (0.12 + ((i * 0.17 + Math.sin(time * 0.3 + i) * 0.03) % 0.8));
    const sy = height * (0.18 + ((i * 0.13 + Math.cos(time * 0.4 + i) * 0.02) % 0.52));
    const pulse = 0.25 + 0.4 * Math.abs(Math.sin(time * 1.4 + i * 1.3));
    drawSparkle(ctx, sx, sy, 5 + (i % 2) * 2, '#FFFDF4', pulse);
  }
}

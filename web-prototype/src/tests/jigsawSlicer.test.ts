import { describe, expect, it } from 'vitest';
import { sliceJigsawPieces, type CurveCommand } from '../game/jigsawSlicer';

function sampleCubicPoints(
  x0: number,
  y0: number,
  cmd: Extract<CurveCommand, { type: 'cubic' }>,
  steps = 12
): Array<{ x: number; y: number }> {
  const pts: Array<{ x: number; y: number }> = [];
  for (let i = 1; i <= steps; i++) {
    const t = i / steps;
    const mt = 1 - t;
    const x =
      mt * mt * mt * x0 +
      3 * mt * mt * t * cmd.cp1x +
      3 * mt * t * t * cmd.cp2x +
      t * t * t * cmd.x;
    const y =
      mt * mt * mt * y0 +
      3 * mt * mt * t * cmd.cp1y +
      3 * mt * t * t * cmd.cp2y +
      t * t * t * cmd.y;
    pts.push({ x, y });
  }
  return pts;
}

function approximateSignedArea(commands: CurveCommand[]): number {
  const poly: Array<{ x: number; y: number }> = [];
  let cx = 0;
  let cy = 0;
  for (const cmd of commands) {
    if (cmd.type === 'move') {
      cx = cmd.x;
      cy = cmd.y;
      poly.push({ x: cx, y: cy });
    } else if (cmd.type === 'line') {
      cx = cmd.x;
      cy = cmd.y;
      poly.push({ x: cx, y: cy });
    } else {
      poly.push(...sampleCubicPoints(cx, cy, cmd, 16));
      cx = cmd.x;
      cy = cmd.y;
    }
  }

  let area = 0;
  for (let i = 0; i < poly.length; i++) {
    const a = poly[i]!;
    const b = poly[(i + 1) % poly.length]!;
    area += a.x * b.y - b.x * a.y;
  }
  return Math.abs(area) * 0.5;
}

describe('jigsawSlicer', () => {
  const counts = [2, 3, 4, 5, 6, 8, 9, 10, 12];

  it.each(counts)('generates %i watertight jigsaw pieces covering unit board area', (count) => {
    const pieces = sliceJigsawPieces(count, count * 13);
    expect(pieces).toHaveLength(count);

    let totalArea = 0;
    for (const piece of pieces) {
      expect(piece.commands[0]?.type).toBe('move');
      expect(piece.bounds.minX).toBeGreaterThanOrEqual(-0.2);
      expect(piece.bounds.maxX).toBeLessThanOrEqual(1.2);
      expect(piece.bounds.minY).toBeGreaterThanOrEqual(-0.2);
      expect(piece.bounds.maxY).toBeLessThanOrEqual(1.2);
      totalArea += approximateSignedArea(piece.commands);
    }

    expect(totalArea).toBeCloseTo(1.0, 2);
  });
});

import { describe, expect, it } from 'vitest';
import { computePuzzleLayout } from '../core/layout';
import { sliceJigsawPieces } from '../game/jigsawSlicer';
import {
  createPieceRuntimes,
  releaseDraggedPiece,
  stepPiecesPhysics,
  syncPiecesToLayout
} from '../game/physics';

describe('puzzle layout and piece physics', () => {
  it('creates non-overlapping tray slots in both portrait and landscape', () => {
    const specs = sliceJigsawPieces(12, 7);

    for (const [w, h] of [
      [390, 844],
      [844, 390],
      [1024, 768]
    ] as const) {
      const layout = computePuzzleLayout(w, h, specs);
      expect(layout.traySlots).toHaveLength(12);
      expect(layout.board.w).toBeGreaterThan(140);

      for (let i = 0; i < layout.traySlots.length; i++) {
        for (let j = i + 1; j < layout.traySlots.length; j++) {
          const a = layout.traySlots[i]!;
          const b = layout.traySlots[j]!;
          const dist = Math.hypot(a.x - b.x, a.y - b.y);
          expect(dist).toBeGreaterThan(24);
        }
      }
    }
  });

  it('snaps piece when released near target and returns piece smoothly when dropped elsewhere', () => {
    const specs = sliceJigsawPieces(2, 1);
    const layout = computePuzzleLayout(390, 844, specs);
    const runtimes = createPieceRuntimes(specs, layout, 3);

    const p0 = runtimes[0]!;
    const p1 = runtimes[1]!;

    // Drop p0 far away from its target -> gentle return physics
    p0.status = 'dragging';
    p0.x = layout.tray.x + 20;
    p0.y = layout.tray.y + 40;
    p0.vx = 180;
    p0.vy = -120;

    expect(releaseDraggedPiece(p0, layout.board)).toBe('returning');
    expect(p0.status).toBe('returning');

    for (let step = 0; step < 180; step++) {
      const ev = stepPiecesPhysics(runtimes, layout, 1 / 60);
      if (ev.returnedPieces.includes(p0)) break;
    }
    expect(p0.status).toBe('tray');
    expect(Math.hypot(p0.x - p0.trayHomeX, p0.y - p0.trayHomeY)).toBeLessThan(2);

    // Drag p0 & p1 close to their snap targets -> snap & complete
    p0.status = 'dragging';
    p0.x = p0.snapTargetX + 12;
    p0.y = p0.snapTargetY - 10;
    expect(releaseDraggedPiece(p0, layout.board)).toBe('snapped');

    p1.status = 'dragging';
    p1.x = p1.snapTargetX - 8;
    p1.y = p1.snapTargetY + 6;
    expect(releaseDraggedPiece(p1, layout.board)).toBe('snapped');

    let completed = false;
    for (let step = 0; step < 60; step++) {
      const ev = stepPiecesPhysics(runtimes, layout, 1 / 60);
      if (ev.allCompleted) {
        completed = true;
        break;
      }
    }
    expect(completed).toBe(true);
    expect(p0.status).toBe('placed');
    expect(p1.status).toBe('placed');
  });

  it('repositions placed and tray pieces accurately on orientation change', () => {
    const specs = sliceJigsawPieces(4, 2);
    const portrait = computePuzzleLayout(390, 844, specs);
    const runtimes = createPieceRuntimes(specs, portrait, 5);

    runtimes[0]!.status = 'placed';
    const landscape = computePuzzleLayout(844, 390, specs);
    syncPiecesToLayout(runtimes, landscape);

    expect(runtimes[0]!.x).toBeCloseTo(runtimes[0]!.snapTargetX, 1);
    expect(runtimes[0]!.y).toBeCloseTo(runtimes[0]!.snapTargetY, 1);
    expect(runtimes[1]!.x).toBeCloseTo(runtimes[1]!.trayHomeX, 1);
    expect(runtimes[1]!.y).toBeCloseTo(runtimes[1]!.trayHomeY, 1);
  });
});

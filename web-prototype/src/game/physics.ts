import type { PuzzleLayout } from '../core/layout';
import type { JigsawPieceSpec } from './jigsawSlicer';

export type PieceStatus = 'tray' | 'dragging' | 'returning' | 'snapping' | 'placed';

export interface PieceRuntime {
  spec: JigsawPieceSpec;
  status: PieceStatus;
  /** Current world X of the piece cell center (in CSS pixels) */
  x: number;
  /** Current world Y of the piece cell center (in CSS pixels) */
  y: number;
  vx: number;
  vy: number;
  scale: number;
  targetScale: number;
  rotation: number;
  zIndex: number;
  snapTargetX: number;
  snapTargetY: number;
  trayHomeX: number;
  trayHomeY: number;
  trayScale: number;
  glowIntensity: number;
  snapAnimProgress: number;
  returnBounceTimer: number;
}

export interface PhysicsStepEvents {
  snappedPieces: PieceRuntime[];
  returnedPieces: PieceRuntime[];
  allCompleted: boolean;
}

export function seededShuffle<T>(items: T[], seed: number): T[] {
  const copy = [...items];
  let s = seed * 9301 + 49297;
  for (let i = copy.length - 1; i > 0; i--) {
    s = (s * 1664525 + 1013904223) >>> 0;
    const j = s % (i + 1);
    const tmp = copy[i]!;
    copy[i] = copy[j]!;
    copy[j] = tmp;
  }
  return copy;
}

export function computeSnapTarget(
  board: PuzzleLayout['board'],
  spec: JigsawPieceSpec
): { x: number; y: number } {
  return {
    x: board.x + spec.centerX * board.w,
    y: board.y + spec.centerY * board.h
  };
}

export function computeSnapRadius(board: PuzzleLayout['board'], spec: JigsawPieceSpec): number {
  const pieceW = spec.cellW * board.w;
  const pieceH = spec.cellH * board.h;
  return Math.max(34, Math.min(pieceW, pieceH) * 0.44);
}

/**
 * Create shuffled runtime states for all pieces in a puzzle level.
 */
export function createPieceRuntimes(
  pieces: JigsawPieceSpec[],
  layout: PuzzleLayout,
  seed: number
): PieceRuntime[] {
  const shuffledOrder = seededShuffle(pieces, seed);
  const slotByOrder = new Map<number, (typeof layout.traySlots)[number]>();

  shuffledOrder.forEach((piece, index) => {
    const slot = layout.traySlots[index];
    if (slot) {
      slotByOrder.set(piece.id, slot);
    }
  });

  return pieces.map((spec, idx) => {
    const snap = computeSnapTarget(layout.board, spec);
    const slot = slotByOrder.get(spec.id) ?? {
      pieceId: spec.id,
      x: layout.tray.x + layout.tray.w * 0.5,
      y: layout.tray.y + layout.tray.h * 0.5,
      scale: 0.6
    };

    return {
      spec,
      status: 'tray',
      x: slot.x,
      y: slot.y,
      vx: 0,
      vy: 0,
      scale: slot.scale,
      targetScale: slot.scale,
      rotation: 0,
      zIndex: idx + 1,
      snapTargetX: snap.x,
      snapTargetY: snap.y,
      trayHomeX: slot.x,
      trayHomeY: slot.y,
      trayScale: slot.scale,
      glowIntensity: 0,
      snapAnimProgress: 0,
      returnBounceTimer: 0
    };
  });
}

/**
 * Recalculate target & tray positions seamlessly when the screen resizes or rotates.
 */
export function syncPiecesToLayout(runtimes: PieceRuntime[], layout: PuzzleLayout): void {
  const oldHomeMap = new Map<number, number>();
  const sortedTray = [...runtimes].sort(
    (a, b) => a.trayHomeY - b.trayHomeY || a.trayHomeX - b.trayHomeX
  );
  sortedTray.forEach((p, i) => oldHomeMap.set(p.spec.id, i));

  for (const piece of runtimes) {
    const snap = computeSnapTarget(layout.board, piece.spec);
    piece.snapTargetX = snap.x;
    piece.snapTargetY = snap.y;

    const slotIdx = oldHomeMap.get(piece.spec.id) ?? 0;
    const slot = layout.traySlots[slotIdx] ?? layout.traySlots[0];
    if (slot) {
      piece.trayHomeX = slot.x;
      piece.trayHomeY = slot.y;
      piece.trayScale = slot.scale;
    }

    if (piece.status === 'placed' || piece.status === 'snapping') {
      piece.x = snap.x;
      piece.y = snap.y;
      piece.scale = 1;
      piece.targetScale = 1;
    } else if (piece.status === 'tray') {
      piece.x = piece.trayHomeX;
      piece.y = piece.trayHomeY;
      piece.scale = piece.trayScale;
      piece.targetScale = piece.trayScale;
    }
  }
}

export function computeTouchLiftOffset(
  board: PuzzleLayout['board'],
  spec: JigsawPieceSpec,
  pointerType: string
): number {
  if (pointerType !== 'touch') {
    return 0;
  }
  const pieceH = spec.cellH * board.h;
  return -Math.min(48, Math.max(24, pieceH * 0.36));
}

export function isNearMatchingSlot(piece: PieceRuntime, board: PuzzleLayout['board']): boolean {
  const dist = Math.hypot(piece.x - piece.snapTargetX, piece.y - piece.snapTargetY);
  return dist <= computeSnapRadius(board, piece.spec);
}

export function releaseDraggedPiece(
  piece: PieceRuntime,
  board: PuzzleLayout['board']
): 'snapped' | 'returning' {
  if (isNearMatchingSlot(piece, board)) {
    piece.status = 'snapping';
    piece.vx = 0;
    piece.vy = 0;
    piece.rotation = 0;
    piece.targetScale = 1;
    piece.glowIntensity = 1;
    piece.snapAnimProgress = 0;
    return 'snapped';
  }

  piece.status = 'returning';
  piece.targetScale = piece.trayScale;
  piece.glowIntensity = 0;
  // Add a playful gentle drop impulse before spring guides it smoothly home
  piece.vx = Math.max(-420, Math.min(420, piece.vx * 0.45));
  piece.vy = Math.max(-320, Math.min(420, piece.vy * 0.45 + 140));
  piece.returnBounceTimer = 0.14;
  return 'returning';
}

/**
 * Advance spring-damped return physics, scale easing, and snap celebration timers.
 */
export function stepPiecesPhysics(
  runtimes: PieceRuntime[],
  layout: PuzzleLayout,
  dt: number
): PhysicsStepEvents {
  const clampedDt = Math.min(0.05, Math.max(0.001, dt));
  const snappedPieces: PieceRuntime[] = [];
  const returnedPieces: PieceRuntime[] = [];

  for (const piece of runtimes) {
    // Smooth scale interpolation for grab/release zoom
    const scaleRate = Math.min(1, clampedDt * 16);
    piece.scale += (piece.targetScale - piece.scale) * scaleRate;

    if (piece.status === 'dragging') {
      const targetGlow = isNearMatchingSlot(piece, layout.board) ? 1 : 0;
      piece.glowIntensity += (targetGlow - piece.glowIntensity) * Math.min(1, clampedDt * 14);
      const targetRot = Math.max(-0.09, Math.min(0.09, piece.vx * 0.00018));
      piece.rotation += (targetRot - piece.rotation) * Math.min(1, clampedDt * 12);
      // Decay instantaneous drag velocity smoothly
      piece.vx *= Math.pow(0.04, clampedDt);
      piece.vy *= Math.pow(0.04, clampedDt);
      continue;
    }

    if (piece.status === 'snapping') {
      piece.x += (piece.snapTargetX - piece.x) * Math.min(1, clampedDt * 22);
      piece.y += (piece.snapTargetY - piece.y) * Math.min(1, clampedDt * 22);
      piece.rotation *= Math.pow(0.01, clampedDt);
      piece.snapAnimProgress = Math.min(1, piece.snapAnimProgress + clampedDt * 3.8);

      if (piece.snapAnimProgress >= 1) {
        piece.x = piece.snapTargetX;
        piece.y = piece.snapTargetY;
        piece.scale = 1;
        piece.rotation = 0;
        piece.glowIntensity = 0;
        piece.status = 'placed';
        snappedPieces.push(piece);
      }
      continue;
    }

    if (piece.status === 'returning') {
      if (piece.returnBounceTimer > 0) {
        piece.returnBounceTimer = Math.max(0, piece.returnBounceTimer - clampedDt);
        // Gentle initial gravity arc before spring takes over
        piece.vy += 680 * clampedDt;
      }

      const stiffness = 92;
      const damping = 12.5; // Slightly underdamped for a friendly soft bounce into tray
      const dx = piece.trayHomeX - piece.x;
      const dy = piece.trayHomeY - piece.y;

      const ax = dx * stiffness - piece.vx * damping;
      const ay = dy * stiffness - piece.vy * damping;

      piece.vx += ax * clampedDt;
      piece.vy += ay * clampedDt;
      piece.x += piece.vx * clampedDt;
      piece.y += piece.vy * clampedDt;

      // Soft viewport bounds keeping piece always visible
      const margin = 28;
      if (piece.x < margin) {
        piece.x = margin;
        piece.vx = Math.abs(piece.vx) * 0.5;
      } else if (piece.x > layout.width - margin) {
        piece.x = layout.width - margin;
        piece.vx = -Math.abs(piece.vx) * 0.5;
      }
      if (piece.y < margin) {
        piece.y = margin;
        piece.vy = Math.abs(piece.vy) * 0.5;
      } else if (piece.y > layout.height - margin) {
        piece.y = layout.height - margin;
        piece.vy = -Math.abs(piece.vy) * 0.5;
      }

      piece.rotation *= Math.pow(0.02, clampedDt);

      const dist = Math.hypot(piece.trayHomeX - piece.x, piece.trayHomeY - piece.y);
      const speed = Math.hypot(piece.vx, piece.vy);
      if (dist < 1.5 && speed < 14 && piece.returnBounceTimer <= 0) {
        piece.x = piece.trayHomeX;
        piece.y = piece.trayHomeY;
        piece.vx = 0;
        piece.vy = 0;
        piece.rotation = 0;
        piece.scale = piece.trayScale;
        piece.status = 'tray';
        returnedPieces.push(piece);
      }
    }
  }

  const allCompleted = runtimes.length > 0 && runtimes.every((p) => p.status === 'placed');

  return { snappedPieces, returnedPieces, allCompleted };
}

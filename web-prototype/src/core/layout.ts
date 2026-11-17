import type { JigsawPieceSpec } from '../game/jigsawSlicer';

export interface Rect {
  x: number;
  y: number;
  w: number;
  h: number;
}

export interface TraySlotLayout {
  pieceId: number;
  x: number;
  y: number;
  scale: number;
}

export interface PuzzleLayout {
  width: number;
  height: number;
  isLandscape: boolean;
  topBar: Rect;
  board: Rect;
  tray: Rect;
  traySlots: TraySlotLayout[];
}

export function pointInRect(px: number, py: number, rect: Rect, pad = 0): boolean {
  return (
    px >= rect.x - pad &&
    px <= rect.x + rect.w + pad &&
    py >= rect.y - pad &&
    py <= rect.y + rect.h + pad
  );
}

/**
 * Compute responsive board, tray, and non-overlapping piece slot coordinates
 * for any viewport size in portrait or landscape orientation.
 */
export function computePuzzleLayout(
  width: number,
  height: number,
  pieces: JigsawPieceSpec[]
): PuzzleLayout {
  const safeW = Math.max(280, width);
  const safeH = Math.max(280, height);
  const isLandscape = safeW > safeH * 1.05;
  const pad = Math.max(12, Math.min(24, Math.min(safeW, safeH) * 0.03));
  const topBarHeight = Math.max(50, Math.min(66, safeH * 0.085));

  const topBar: Rect = {
    x: pad,
    y: pad * 0.6,
    w: safeW - pad * 2,
    h: topBarHeight
  };

  const contentTop = topBar.y + topBar.h + pad * 0.65;
  const contentH = Math.max(180, safeH - contentTop - pad);
  const contentW = Math.max(220, safeW - pad * 2);

  let board: Rect;
  let tray: Rect;

  if (isLandscape) {
    const gap = pad;
    const maxBoardW = contentW * 0.58 - gap * 0.5;
    const boardSide = Math.max(160, Math.min(maxBoardW, contentH * 0.94));
    const boardAreaW = Math.max(boardSide + pad * 0.5, contentW * 0.58);

    board = {
      x: pad + (boardAreaW - boardSide) * 0.5,
      y: contentTop + (contentH - boardSide) * 0.5,
      w: boardSide,
      h: boardSide
    };

    const trayX = pad + boardAreaW + gap * 0.6;
    tray = {
      x: trayX,
      y: contentTop + pad * 0.15,
      w: Math.max(120, safeW - pad - trayX),
      h: contentH - pad * 0.3
    };
  } else {
    const gap = pad * 0.8;
    const trayRatio = pieces.length > 6 ? 0.36 : 0.32;
    const maxBoardH = contentH * (1 - trayRatio) - gap;
    const boardSide = Math.max(150, Math.min(contentW * 0.92, maxBoardH));

    board = {
      x: (safeW - boardSide) * 0.5,
      y: contentTop + Math.max(4, (maxBoardH - boardSide) * 0.5),
      w: boardSide,
      h: boardSide
    };

    const trayY = board.y + board.h + gap;
    tray = {
      x: pad,
      y: trayY,
      w: contentW,
      h: Math.max(96, safeH - pad - trayY)
    };
  }

  const traySlots = computeTraySlots(tray, board, pieces, isLandscape);

  return {
    width: safeW,
    height: safeH,
    isLandscape,
    topBar,
    board,
    tray,
    traySlots
  };
}

function computeTraySlots(
  tray: Rect,
  board: Rect,
  pieces: JigsawPieceSpec[],
  isLandscape: boolean
): TraySlotLayout[] {
  const count = pieces.length;
  if (count === 0) {
    return [];
  }

  const innerPad = Math.max(10, Math.min(tray.w, tray.h) * 0.06);
  const topReserve = tray.h > 135 ? 20 : 8;
  const availW = Math.max(40, tray.w - innerPad * 2);
  const availH = Math.max(40, tray.h - innerPad * 2 - topReserve);

  // Pick grid dimensions inside the tray that best match its aspect ratio
  let bestCols = 1;
  let bestRows = count;
  let bestScore = -Infinity;

  for (let cols = 1; cols <= count; cols++) {
    const rows = Math.ceil(count / cols);
    const cellW = availW / cols;
    const cellH = availH / rows;
    const minSide = Math.min(cellW, cellH);
    const aspectPenalty = isLandscape ? 0 : cols >= rows ? 4 : 0;
    const emptySlots = cols * rows - count;
    const score = minSide + aspectPenalty - emptySlots * 2;
    if (score > bestScore) {
      bestScore = score;
      bestCols = cols;
      bestRows = rows;
    }
  }

  const cellW = availW / bestCols;
  const cellH = availH / bestRows;

  // Compute representative piece visual extent including jigsaw tabs (~1.35x cell)
  const maxPieceNormW = Math.max(...pieces.map((p) => p.bounds.maxX - p.bounds.minX));
  const maxPieceNormH = Math.max(...pieces.map((p) => p.bounds.maxY - p.bounds.minY));
  const rawPieceW = maxPieceNormW * board.w;
  const rawPieceH = maxPieceNormH * board.h;

  const fitScaleX = (cellW * 0.84) / Math.max(1, rawPieceW);
  const fitScaleY = (cellH * 0.84) / Math.max(1, rawPieceH);
  const trayScale = Math.max(0.32, Math.min(0.86, fitScaleX, fitScaleY));

  const slots: TraySlotLayout[] = [];
  for (let idx = 0; idx < count; idx++) {
    const piece = pieces[idx]!;
    const row = Math.floor(idx / bestCols);
    const col = idx % bestCols;
    const itemsInRow = row === bestRows - 1 ? count - row * bestCols : bestCols;
    const rowWidth = itemsInRow * cellW;
    const rowStartX = tray.x + innerPad + (availW - rowWidth) * 0.5;

    const slotCenterX = rowStartX + (col + 0.5) * cellW;
    const slotCenterY = tray.y + innerPad + topReserve + (row + 0.5) * cellH;

    // Account for piece tab overhang asymmetry relative to cell center
    const boundsMidX = (piece.bounds.minX + piece.bounds.maxX) * 0.5;
    const boundsMidY = (piece.bounds.minY + piece.bounds.maxY) * 0.5;
    const offsetX = (piece.centerX - boundsMidX) * board.w * trayScale;
    const offsetY = (piece.centerY - boundsMidY) * board.h * trayScale;

    slots.push({
      pieceId: piece.id,
      x: slotCenterX + offsetX,
      y: slotCenterY + offsetY,
      scale: trayScale
    });
  }

  return slots;
}

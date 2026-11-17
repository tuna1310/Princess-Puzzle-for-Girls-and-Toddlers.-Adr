export type CurveCommand =
  | { type: 'move'; x: number; y: number }
  | { type: 'line'; x: number; y: number }
  | {
      type: 'cubic';
      cp1x: number;
      cp1y: number;
      cp2x: number;
      cp2y: number;
      x: number;
      y: number;
    };

export interface JigsawPieceSpec {
  id: number;
  row: number;
  col: number;
  /** Normalized board bounding box [0..1] (core cell without protruding tabs) */
  cellX: number;
  cellY: number;
  cellW: number;
  cellH: number;
  /** Normalized center of the piece cell */
  centerX: number;
  centerY: number;
  /** Normalized closed contour commands including interlocking tabs */
  commands: CurveCommand[];
  /** Normalized bounding box including protruding tabs */
  bounds: {
    minX: number;
    minY: number;
    maxX: number;
    maxY: number;
  };
}

interface DirectedEdge {
  startX: number;
  startY: number;
  segments: Exclude<CurveCommand, { type: 'move' }>[];
}

function seededUnit(seed: number): number {
  const x = Math.sin(seed * 12.9898 + 78.233) * 43758.5453;
  return x - Math.floor(x);
}

/**
 * Generate a rounded, toddler-friendly jigsaw tab edge from `(x0, y0)` to `(x1, y1)`.
 * When `tabDirection === 0` or edge is too short, falls back to a clean straight line.
 */
function buildEdgeSegments(
  x0: number,
  y0: number,
  x1: number,
  y1: number,
  tabDirection: -1 | 0 | 1,
  tabDepth: number
): DirectedEdge {
  const dx = x1 - x0;
  const dy = y1 - y0;
  const len = Math.hypot(dx, dy);

  if (tabDirection === 0 || len < 0.12 || tabDepth <= 0.001) {
    return {
      startX: x0,
      startY: y0,
      segments: [{ type: 'line', x: x1, y: y1 }]
    };
  }

  const tx = dx / len;
  const ty = dy / len;
  // Outward normal for clockwise traversal in Y-down coordinates
  const nx = ty;
  const ny = -tx;
  const h = Math.min(tabDepth, len * 0.27) * tabDirection;

  const pt = (u: number, v: number): { x: number; y: number } => ({
    x: x0 + tx * (u * len) + nx * (v * h),
    y: y0 + ty * (u * len) + ny * (v * h)
  });

  const a1 = pt(0.22, 0.0);
  const a2 = pt(0.38, 0.04);
  const aEnd = pt(0.38, 0.28);

  const b1 = pt(0.38, 0.44);
  const b2 = pt(0.3, 0.98);
  const bEnd = pt(0.5, 0.98);

  const c1 = pt(0.7, 0.98);
  const c2 = pt(0.62, 0.44);
  const cEnd = pt(0.62, 0.28);

  const d1 = pt(0.62, 0.04);
  const d2 = pt(0.78, 0.0);
  const dEnd = pt(1.0, 0.0);

  return {
    startX: x0,
    startY: y0,
    segments: [
      { type: 'cubic', cp1x: a1.x, cp1y: a1.y, cp2x: a2.x, cp2y: a2.y, x: aEnd.x, y: aEnd.y },
      { type: 'cubic', cp1x: b1.x, cp1y: b1.y, cp2x: b2.x, cp2y: b2.y, x: bEnd.x, y: bEnd.y },
      { type: 'cubic', cp1x: c1.x, cp1y: c1.y, cp2x: c2.x, cp2y: c2.y, x: cEnd.x, y: cEnd.y },
      { type: 'cubic', cp1x: d1.x, cp1y: d1.y, cp2x: d2.x, cp2y: d2.y, x: dEnd.x, y: dEnd.y }
    ]
  };
}

/**
 * Reverse a directed edge bitwise-exactly so two neighboring pieces share zero-gap seams.
 */
function reverseEdge(edge: DirectedEdge): DirectedEdge {
  const pts: Array<{ x: number; y: number }> = [{ x: edge.startX, y: edge.startY }];
  for (const seg of edge.segments) {
    pts.push({ x: seg.x, y: seg.y });
  }

  const reversedSegments: Exclude<CurveCommand, { type: 'move' }>[] = [];
  for (let i = edge.segments.length - 1; i >= 0; i--) {
    const seg = edge.segments[i]!;
    const prevPt = pts[i]!;
    if (seg.type === 'line') {
      reversedSegments.push({ type: 'line', x: prevPt.x, y: prevPt.y });
    } else {
      reversedSegments.push({
        type: 'cubic',
        cp1x: seg.cp2x,
        cp1y: seg.cp2y,
        cp2x: seg.cp1x,
        cp2y: seg.cp1y,
        x: prevPt.x,
        y: prevPt.y
      });
    }
  }

  const last = pts[pts.length - 1]!;
  return {
    startX: last.x,
    startY: last.y,
    segments: reversedSegments
  };
}

function uniqueSortedNumbers(values: number[], eps = 1e-6): number[] {
  const sorted = [...values].sort((a, b) => a - b);
  const result: number[] = [];
  for (const v of sorted) {
    if (result.length === 0 || Math.abs(v - result[result.length - 1]!) > eps) {
      result.push(v);
    }
  }
  return result;
}

function computeCommandsBounds(commands: CurveCommand[]): JigsawPieceSpec['bounds'] {
  let minX = Infinity;
  let minY = Infinity;
  let maxX = -Infinity;
  let maxY = -Infinity;

  const include = (x: number, y: number): void => {
    if (x < minX) minX = x;
    if (y < minY) minY = y;
    if (x > maxX) maxX = x;
    if (y > maxY) maxY = y;
  };

  for (const cmd of commands) {
    include(cmd.x, cmd.y);
    if (cmd.type === 'cubic') {
      include(cmd.cp1x, cmd.cp1y);
      include(cmd.cp2x, cmd.cp2y);
    }
  }

  return { minX, minY, maxX, maxY };
}

/**
 * Default friendly row distributions for piece counts 2..12.
 */
export function getDefaultRowLayout(pieceCount: number): number[] {
  switch (pieceCount) {
    case 2:
      return [2];
    case 3:
      return [3];
    case 4:
      return [2, 2];
    case 5:
      return [2, 3];
    case 6:
      return [3, 3];
    case 7:
      return [3, 4];
    case 8:
      return [4, 4];
    case 9:
      return [3, 3, 3];
    case 10:
      return [3, 4, 3];
    case 11:
      return [3, 4, 4];
    case 12:
      return [4, 4, 4];
    default: {
      const clamped = Math.max(2, Math.min(12, Math.round(pieceCount)));
      return getDefaultRowLayout(clamped);
    }
  }
}

/**
 * Slice the unit board `[0,1]x[0,1]` into `sum(rowCounts)` watertight rounded jigsaw pieces.
 */
export function sliceJigsawPieces(pieceCount: number, seed = 1): JigsawPieceSpec[] {
  const rowCounts = getDefaultRowLayout(pieceCount);
  const numRows = rowCounts.length;
  const rowHeight = 1 / numRows;
  const maxCols = Math.max(...rowCounts);
  const baseTabDepth = Math.min(rowHeight, 1 / maxCols) * 0.22;

  const rowCuts: number[][] = rowCounts.map((cols) => {
    const cuts: number[] = [];
    for (let c = 0; c <= cols; c++) {
      cuts.push(c / cols);
    }
    return cuts;
  });

  // Precompute shared horizontal seams between row r and row r+1 (directed left -> right)
  interface HorizontalSubEdge {
    x0: number;
    x1: number;
    edgeLR: DirectedEdge;
  }
  const horizontalSeams: HorizontalSubEdge[][] = [];
  for (let r = 0; r < numRows - 1; r++) {
    const y = (r + 1) * rowHeight;
    const unionX = uniqueSortedNumbers([...rowCuts[r]!, ...rowCuts[r + 1]!]);
    const seam: HorizontalSubEdge[] = [];
    for (let i = 0; i < unionX.length - 1; i++) {
      const x0 = unionX[i]!;
      const x1 = unionX[i + 1]!;
      const dir: -1 | 1 = seededUnit(seed * 97 + r * 31 + i * 7) > 0.5 ? 1 : -1;
      const edgeLR = buildEdgeSegments(x0, y, x1, y, dir, baseTabDepth);
      seam.push({ x0, x1, edgeLR });
    }
    horizontalSeams.push(seam);
  }

  // Precompute shared vertical seams within each row (directed top -> bottom)
  const verticalSeams: DirectedEdge[][] = [];
  for (let r = 0; r < numRows; r++) {
    const cols = rowCounts[r]!;
    const y0 = r * rowHeight;
    const y1 = (r + 1) * rowHeight;
    const rowVerts: DirectedEdge[] = [];
    for (let c = 1; c < cols; c++) {
      const x = rowCuts[r]![c]!;
      const dir: -1 | 1 = seededUnit(seed * 131 + r * 43 + c * 13) > 0.5 ? 1 : -1;
      rowVerts.push(buildEdgeSegments(x, y0, x, y1, dir, baseTabDepth));
    }
    verticalSeams.push(rowVerts);
  }

  const pieces: JigsawPieceSpec[] = [];
  let nextId = 0;

  for (let r = 0; r < numRows; r++) {
    const cols = rowCounts[r]!;
    const cuts = rowCuts[r]!;
    const y0 = r * rowHeight;
    const y1 = (r + 1) * rowHeight;

    for (let c = 0; c < cols; c++) {
      const x0 = cuts[c]!;
      const x1 = cuts[c + 1]!;
      const segments: Exclude<CurveCommand, { type: 'move' }>[] = [];

      // 1. Top edge (left -> right)
      if (r === 0) {
        segments.push({ type: 'line', x: x1, y: y0 });
      } else {
        const seam = horizontalSeams[r - 1]!;
        for (const sub of seam) {
          if (sub.x0 >= x0 - 1e-6 && sub.x1 <= x1 + 1e-6) {
            segments.push(...sub.edgeLR.segments);
          }
        }
      }

      // 2. Right edge (top -> bottom)
      if (c === cols - 1) {
        segments.push({ type: 'line', x: x1, y: y1 });
      } else {
        const rightEdge = verticalSeams[r]![c]!;
        segments.push(...rightEdge.segments);
      }

      // 3. Bottom edge (right -> left, reversed from horizontalSeams[r])
      if (r === numRows - 1) {
        segments.push({ type: 'line', x: x0, y: y1 });
      } else {
        const seam = horizontalSeams[r]!;
        for (let i = seam.length - 1; i >= 0; i--) {
          const sub = seam[i]!;
          if (sub.x0 >= x0 - 1e-6 && sub.x1 <= x1 + 1e-6) {
            segments.push(...reverseEdge(sub.edgeLR).segments);
          }
        }
      }

      // 4. Left edge (bottom -> top, reversed from verticalSeams[r][c-1])
      if (c === 0) {
        segments.push({ type: 'line', x: x0, y: y0 });
      } else {
        const leftEdge = reverseEdge(verticalSeams[r]![c - 1]!);
        segments.push(...leftEdge.segments);
      }

      const commands: CurveCommand[] = [{ type: 'move', x: x0, y: y0 }, ...segments];
      pieces.push({
        id: nextId++,
        row: r,
        col: c,
        cellX: x0,
        cellY: y0,
        cellW: x1 - x0,
        cellH: y1 - y0,
        centerX: (x0 + x1) * 0.5,
        centerY: (y0 + y1) * 0.5,
        commands,
        bounds: computeCommandsBounds(commands)
      });
    }
  }

  return pieces;
}

/**
 * Convert normalized piece curve commands into a Canvas Path2D scaled to `boardW x boardH`
 * centered around `(0, 0)` (relative to piece cell center) or board top-left.
 */
export function buildPiecePath2D(
  piece: JigsawPieceSpec,
  boardW: number,
  boardH: number,
  originX = 0,
  originY = 0
): Path2D {
  const path = new Path2D();
  for (const cmd of piece.commands) {
    if (cmd.type === 'move') {
      path.moveTo(originX + cmd.x * boardW, originY + cmd.y * boardH);
    } else if (cmd.type === 'line') {
      path.lineTo(originX + cmd.x * boardW, originY + cmd.y * boardH);
    } else {
      path.bezierCurveTo(
        originX + cmd.cp1x * boardW,
        originY + cmd.cp1y * boardH,
        originX + cmd.cp2x * boardW,
        originY + cmd.cp2y * boardH,
        originX + cmd.x * boardW,
        originY + cmd.y * boardH
      );
    }
  }
  path.closePath();
  return path;
}

/**
 * Trace normalized piece commands onto any CanvasRenderingContext2D-like object.
 */
export function tracePieceCommands(
  ctx: CanvasRenderingContext2D,
  piece: JigsawPieceSpec,
  boardW: number,
  boardH: number,
  originX = 0,
  originY = 0
): void {
  ctx.beginPath();
  for (const cmd of piece.commands) {
    if (cmd.type === 'move') {
      ctx.moveTo(originX + cmd.x * boardW, originY + cmd.y * boardH);
    } else if (cmd.type === 'line') {
      ctx.lineTo(originX + cmd.x * boardW, originY + cmd.y * boardH);
    } else {
      ctx.bezierCurveTo(
        originX + cmd.cp1x * boardW,
        originY + cmd.cp1y * boardH,
        originX + cmd.cp2x * boardW,
        originY + cmd.cp2y * boardH,
        originX + cmd.x * boardW,
        originY + cmd.y * boardH
      );
    }
  }
  ctx.closePath();
}

import { drawMeadowBackdrop } from '../art/backdrop';
import { SoundEngine } from '../audio/soundEngine';
import { HubScreen } from '../game/hubScreen';
import type { LevelDefinition } from '../game/levels';
import { PuzzleScreen } from '../game/puzzleScreen';
import { ProgressStore } from '../storage/progressStore';
import { LocalSimulatedPurchaseService, type PurchaseService } from '../storage/purchaseService';

export interface AppDebugSnapshot {
  screen: 'hub' | 'puzzle';
  width: number;
  height: number;
  selectedLevelId: number;
  hubMetrics?: ReturnType<HubScreen['getMetrics']>;
  puzzle?: ReturnType<PuzzleScreen['getSnapshot']>;
}

declare global {
  interface Window {
    __PRINCESS_PUZZLE_STATE__?: () => AppDebugSnapshot;
  }
}

export class PrincessPuzzleApp {
  private readonly ctx: CanvasRenderingContext2D;
  private readonly progress = new ProgressStore();
  private readonly purchases: PurchaseService = new LocalSimulatedPurchaseService();
  private readonly sound = new SoundEngine();

  private readonly hubScreen: HubScreen;
  private puzzleScreen: PuzzleScreen | null = null;
  private activeScreen: 'hub' | 'puzzle' = 'hub';

  private cssWidth = window.innerWidth;
  private cssHeight = window.innerHeight;
  private dpr = 1;
  private rafId = 0;
  private lastTimestamp = 0;
  private elapsedSeconds = 0;

  constructor(private readonly canvas: HTMLCanvasElement) {
    const context = canvas.getContext('2d');
    if (!context) {
      throw new Error('Failed to initialize 2D canvas context');
    }
    this.ctx = context;

    this.sound.setEnabled(this.progress.isSoundEnabled());
    this.hubScreen = new HubScreen(
      this.progress,
      this.purchases,
      this.sound,
      (level) => this.startLevel(level)
    );

    this.handleResize();
    this.bindEvents();

    window.__PRINCESS_PUZZLE_STATE__ = () => this.getDebugSnapshot();
  }

  getDebugSnapshot(): AppDebugSnapshot {
    return {
      screen: this.activeScreen,
      width: this.cssWidth,
      height: this.cssHeight,
      selectedLevelId: this.hubScreen.getSelectedLevel().id,
      hubMetrics:
        this.activeScreen === 'hub'
          ? this.hubScreen.getMetrics(this.cssWidth, this.cssHeight)
          : undefined,
      puzzle: this.puzzleScreen ? this.puzzleScreen.getSnapshot() : undefined
    };
  }

  start(): void {
    this.lastTimestamp = performance.now();
    const tick = (now: number): void => {
      const dt = Math.min(0.05, Math.max(0.001, (now - this.lastTimestamp) / 1000));
      this.lastTimestamp = now;
      this.elapsedSeconds += dt;

      this.update(dt);
      this.render();
      this.rafId = requestAnimationFrame(tick);
    };
    this.rafId = requestAnimationFrame(tick);
  }

  destroy(): void {
    cancelAnimationFrame(this.rafId);
  }

  private startLevel(level: LevelDefinition): void {
    this.progress.setLastSelectedLevelId(level.id);
    this.puzzleScreen = new PuzzleScreen(
      level,
      this.cssWidth,
      this.cssHeight,
      this.progress,
      this.purchases,
      this.sound,
      () => this.returnToHub(),
      (nextLevel) => this.startLevel(nextLevel)
    );
    this.activeScreen = 'puzzle';
  }

  private returnToHub(): void {
    this.puzzleScreen = null;
    this.activeScreen = 'hub';
    this.hubScreen.refreshFromStorage();
  }

  private handleResize = (): void => {
    const rect = this.canvas.getBoundingClientRect();
    this.cssWidth = Math.max(280, Math.round(rect.width || window.innerWidth));
    this.cssHeight = Math.max(280, Math.round(rect.height || window.innerHeight));
    this.dpr = Math.min(window.devicePixelRatio || 1, 2.5);

    this.canvas.width = Math.round(this.cssWidth * this.dpr);
    this.canvas.height = Math.round(this.cssHeight * this.dpr);

    if (this.puzzleScreen) {
      this.puzzleScreen.resize(this.cssWidth, this.cssHeight);
    }
  };

  private getCanvasCoords(ev: PointerEvent): { x: number; y: number } {
    const rect = this.canvas.getBoundingClientRect();
    return {
      x: ev.clientX - rect.left,
      y: ev.clientY - rect.top
    };
  }

  private bindEvents(): void {
    window.addEventListener('resize', this.handleResize);
    window.addEventListener('orientationchange', this.handleResize);

    if (typeof ResizeObserver !== 'undefined') {
      const observer = new ResizeObserver(() => this.handleResize());
      observer.observe(this.canvas);
    }

    this.canvas.addEventListener('pointerdown', (ev) => {
      ev.preventDefault();
      try {
        this.canvas.setPointerCapture(ev.pointerId);
      } catch {
        // Ignore capture failures on synthetic events
      }
      const { x, y } = this.getCanvasCoords(ev);
      if (this.activeScreen === 'hub') {
        this.hubScreen.onPointerDown(x, y, ev.pointerId, this.cssWidth, this.cssHeight);
      } else if (this.puzzleScreen) {
        this.puzzleScreen.onPointerDown(this.ctx, x, y, ev.pointerId, ev.pointerType);
      }
    });

    this.canvas.addEventListener('pointermove', (ev) => {
      const { x, y } = this.getCanvasCoords(ev);
      if (this.activeScreen === 'hub') {
        this.hubScreen.onPointerMove(x, y, ev.pointerId, this.cssWidth, this.cssHeight);
      } else if (this.puzzleScreen) {
        this.puzzleScreen.onPointerMove(x, y, ev.pointerId);
      }
    });

    const handleRelease = (ev: PointerEvent): void => {
      try {
        if (this.canvas.hasPointerCapture(ev.pointerId)) {
          this.canvas.releasePointerCapture(ev.pointerId);
        }
      } catch {
        // Ignore release errors
      }
      const { x, y } = this.getCanvasCoords(ev);
      if (this.activeScreen === 'hub') {
        this.hubScreen.onPointerUp(x, y, ev.pointerId, this.cssWidth, this.cssHeight);
      } else if (this.puzzleScreen) {
        this.puzzleScreen.onPointerUp(ev.pointerId);
      }
    };

    this.canvas.addEventListener('pointerup', handleRelease);
    this.canvas.addEventListener('pointercancel', handleRelease);
  }

  private update(dt: number): void {
    if (this.activeScreen === 'hub') {
      this.hubScreen.update(dt);
    } else if (this.puzzleScreen) {
      this.puzzleScreen.update(dt);
    }
  }

  private render(): void {
    this.ctx.save();
    this.ctx.setTransform(this.dpr, 0, 0, this.dpr, 0, 0);
    this.ctx.clearRect(0, 0, this.cssWidth, this.cssHeight);

    drawMeadowBackdrop(this.ctx, this.cssWidth, this.cssHeight, this.elapsedSeconds);

    if (this.activeScreen === 'hub') {
      this.hubScreen.draw(this.ctx, this.cssWidth, this.cssHeight, this.elapsedSeconds);
    } else if (this.puzzleScreen) {
      this.puzzleScreen.draw(this.ctx, this.cssWidth, this.cssHeight, this.elapsedSeconds);
    }

    this.ctx.restore();
  }
}

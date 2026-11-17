import { getSafeStorage, type KeyValueStorage } from './storageHelper';

export const PROGRESS_STORAGE_KEY = 'princess_puzzle_progress_v1';

export interface PlayerProgress {
  starsByLevel: Record<number, number>;
  soundEnabled: boolean;
  lastSelectedLevelId: number;
}

const DEFAULT_PROGRESS: PlayerProgress = {
  starsByLevel: {},
  soundEnabled: true,
  lastSelectedLevelId: 1
};

export class ProgressStore {
  private readonly storage: KeyValueStorage;
  private state: PlayerProgress;

  constructor(storage?: KeyValueStorage) {
    this.storage = storage ?? getSafeStorage();
    this.state = this.load();
  }

  private load(): PlayerProgress {
    try {
      const raw = this.storage.getItem(PROGRESS_STORAGE_KEY);
      if (!raw) {
        return { ...DEFAULT_PROGRESS, starsByLevel: {} };
      }
      const parsed = JSON.parse(raw) as Partial<PlayerProgress>;
      const starsByLevel: Record<number, number> = {};
      if (parsed.starsByLevel && typeof parsed.starsByLevel === 'object') {
        for (const [k, v] of Object.entries(parsed.starsByLevel)) {
          const levelId = Number(k);
          if (Number.isInteger(levelId) && typeof v === 'number' && v >= 0 && v <= 3) {
            starsByLevel[levelId] = v;
          }
        }
      }
      return {
        starsByLevel,
        soundEnabled: typeof parsed.soundEnabled === 'boolean' ? parsed.soundEnabled : true,
        lastSelectedLevelId:
          typeof parsed.lastSelectedLevelId === 'number' && parsed.lastSelectedLevelId >= 1
            ? parsed.lastSelectedLevelId
            : 1
      };
    } catch {
      return { ...DEFAULT_PROGRESS, starsByLevel: {} };
    }
  }

  private save(): void {
    try {
      this.storage.setItem(PROGRESS_STORAGE_KEY, JSON.stringify(this.state));
    } catch {
      // Ignore quota errors cleanly
    }
  }

  getStars(levelId: number): number {
    return this.state.starsByLevel[levelId] ?? 0;
  }

  getTotalStars(): number {
    return Object.values(this.state.starsByLevel).reduce((sum, s) => sum + s, 0);
  }

  recordCompletion(levelId: number, stars = 3): void {
    const clamped = Math.max(1, Math.min(3, Math.round(stars)));
    const prev = this.state.starsByLevel[levelId] ?? 0;
    if (clamped > prev) {
      this.state.starsByLevel[levelId] = clamped;
      this.save();
    }
  }

  isSoundEnabled(): boolean {
    return this.state.soundEnabled;
  }

  setSoundEnabled(enabled: boolean): void {
    this.state.soundEnabled = enabled;
    this.save();
  }

  getLastSelectedLevelId(): number {
    return this.state.lastSelectedLevelId;
  }

  setLastSelectedLevelId(levelId: number): void {
    this.state.lastSelectedLevelId = levelId;
    this.save();
  }

  resetAll(): void {
    this.state = { ...DEFAULT_PROGRESS, starsByLevel: {} };
    this.save();
  }
}

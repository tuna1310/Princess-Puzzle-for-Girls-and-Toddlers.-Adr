import { describe, expect, it } from 'vitest';
import { isLevelUnlocked, LEVELS } from '../game/levels';
import { ProgressStore } from '../storage/progressStore';
import {
  LocalSimulatedPurchaseService,
  UNLOCKED_ALL_STORAGE_KEY
} from '../storage/purchaseService';
import type { KeyValueStorage } from '../storage/storageHelper';

function createMemoryStorage(): KeyValueStorage {
  const map = new Map<string, string>();
  return {
    getItem: (k) => map.get(k) ?? null,
    setItem: (k, v) => {
      map.set(k, v);
    },
    removeItem: (k) => {
      map.delete(k);
    }
  };
}

describe('storage and purchase abstraction', () => {
  it('simulates unlockedAll via PurchaseService interface', async () => {
    const mem = createMemoryStorage();
    const service = new LocalSimulatedPurchaseService(mem);

    expect(service.isUnlockedAll()).toBe(false);
    expect(isLevelUnlocked(LEVELS[0]!, service.isUnlockedAll())).toBe(true);
    expect(isLevelUnlocked(LEVELS[4]!, service.isUnlockedAll())).toBe(false);

    const res = await service.purchaseUnlockAll();
    expect(res.success).toBe(true);
    expect(res.unlockedAll).toBe(true);
    expect(mem.getItem(UNLOCKED_ALL_STORAGE_KEY)).toBe('true');
    expect(isLevelUnlocked(LEVELS[4]!, service.isUnlockedAll())).toBe(true);

    service.resetSimulatedPurchase();
    expect(service.isUnlockedAll()).toBe(false);
  });

  it('persists stars, sound state, and last selected level', () => {
    const mem = createMemoryStorage();
    const store = new ProgressStore(mem);

    expect(store.getTotalStars()).toBe(0);
    store.recordCompletion(1, 3);
    store.recordCompletion(2, 2);
    // Higher star score stays preserved
    store.recordCompletion(1, 1);
    store.setSoundEnabled(false);
    store.setLastSelectedLevelId(3);

    const reloaded = new ProgressStore(mem);
    expect(reloaded.getStars(1)).toBe(3);
    expect(reloaded.getStars(2)).toBe(2);
    expect(reloaded.getTotalStars()).toBe(5);
    expect(reloaded.isSoundEnabled()).toBe(false);
    expect(reloaded.getLastSelectedLevelId()).toBe(3);

    reloaded.resetAll();
    expect(reloaded.getTotalStars()).toBe(0);
  });
});

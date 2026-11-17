import { getSafeStorage, type KeyValueStorage } from './storageHelper';

/**
 * Abstraction for entitlement and in-app purchase state.
 * Can be backed by localStorage simulation during web MVP and swapped
 * for Google Play / App Store billing via Capacitor later.
 */
export interface PurchaseResult {
  success: boolean;
  unlockedAll: boolean;
}

export interface PurchaseService {
  isUnlockedAll(): boolean;
  purchaseUnlockAll(): Promise<PurchaseResult>;
  restorePurchases(): Promise<PurchaseResult>;
  resetSimulatedPurchase(): void;
}

export const UNLOCKED_ALL_STORAGE_KEY = 'unlockedAll';

export class LocalSimulatedPurchaseService implements PurchaseService {
  private readonly storage: KeyValueStorage;

  constructor(storage?: KeyValueStorage) {
    this.storage = storage ?? getSafeStorage();
  }

  isUnlockedAll(): boolean {
    return this.storage.getItem(UNLOCKED_ALL_STORAGE_KEY) === 'true';
  }

  async purchaseUnlockAll(): Promise<PurchaseResult> {
    this.storage.setItem(UNLOCKED_ALL_STORAGE_KEY, 'true');
    return { success: true, unlockedAll: true };
  }

  async restorePurchases(): Promise<PurchaseResult> {
    return { success: true, unlockedAll: this.isUnlockedAll() };
  }

  resetSimulatedPurchase(): void {
    this.storage.removeItem(UNLOCKED_ALL_STORAGE_KEY);
  }
}

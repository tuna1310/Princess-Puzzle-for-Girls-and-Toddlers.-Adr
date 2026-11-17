package com.princesspuzzle.storybook.data

/**
 * Abstraction for entitlement and unlock state.
 * Backed by Jetpack DataStore simulation for MVP and designed to swap cleanly
 * with Google Play Billing later.
 */
interface PurchaseService {
    fun isUnlockedAll(): Boolean

    suspend fun purchaseUnlockAll(): Boolean

    suspend fun restorePurchases(): Boolean

    suspend fun resetSimulatedPurchase()
}

class InMemoryPurchaseService(initialUnlockedAll: Boolean = false) : PurchaseService {
    private var unlockedAll: Boolean = initialUnlockedAll

    override fun isUnlockedAll(): Boolean = unlockedAll

    override suspend fun purchaseUnlockAll(): Boolean {
        unlockedAll = true
        return true
    }

    override suspend fun restorePurchases(): Boolean = unlockedAll

    override suspend fun resetSimulatedPurchase() {
        unlockedAll = false
    }
}

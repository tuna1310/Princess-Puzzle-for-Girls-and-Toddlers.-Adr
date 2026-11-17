# Future Ideas & Deferred Enhancements (`IDEAS.md`)

## Post-MVP Play Store & Platform Enhancements
- **Google Play Billing Integration**:
  - Implement `PlayBillingPurchaseService` behind `data/PurchaseService.kt` using `com.android.billingclient:billing-ktx`.
  - Keep `unlockedAll` cached in Jetpack DataStore synchronized with `queryPurchasesAsync` on app resume/restore.
  - Ensure all purchase flows remain gated behind the Parental Gate.

## Gameplay & Kid-Friendly Polish
- **Configurable Piece Count for Toddlers**: Allow parents in Grown-Ups Settings to lock all 16 scenes to 2–4 pieces for 1–2 year olds.
- **Completed Sticker Gallery**: Allow children to revisit finished living scenes as an interactive picture book.
- **Additional Localization Packs**: Keep `values/strings.xml` as canonical English while optionally adding `values-es`, `values-fr`, etc. later if requested.
- **Haptic Soft Feedback**: Optional subtle `HapticFeedbackType.TextHandleMove` on piece snap (configurable in Grown-Ups Settings).

# Future Ideas & Deferred Enhancements (`IDEAS.md`)

## Planned After Web MVP Stabilization
- **Capacitor Android Wrapper**:
  - Wrap the production Vite `dist/` bundle using `@capacitor/core` and `@capacitor/android`.
  - Configure Android edge-to-edge fullscreen immersive mode, portrait/landscape orientation handling, adaptive launcher icons, and Google Play Billing plugin implementing the existing `PurchaseService` interface (`src/storage/purchaseService.ts`).
  - Keep `unlockedAll` local cache synchronized with Play Billing entitlement restoration.

## Gameplay & Polish Ideas
- **Gentle Ambient Lullaby Mode**: Optional soft procedural music-box background loop (muted/toggled via Parental Settings) using WebAudio triangle/sine chimes.
- **Toddler Assist Hints**: After 20 seconds of inactivity on a level, gently shimmer one unplaced tray piece and its matching ghost slot (no text, no nag).
- **Piece Count Override for Parents**: Allow grown-ups to toggle "Toddler Mode (2–4 pieces)" so a 2-year-old can enjoy later scenes like the Mermaid Kitten or Lantern Sky with fewer pieces.
- **Sticker Book Gallery**: Showcase completed living scenes in an interactive picture-book viewer where tapping characters triggers little reactions and sparkles.
- **Localization Packs**: Add additional `src/i18n/*.json` locale files once English MVP is finalized.

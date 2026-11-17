# Product Specification — Princess Storybook Puzzle (Android Native)

## 1. Overview
A gentle, kid-safe native **Android** jigsaw puzzle app built with **Kotlin**, **Jetpack Compose**, and **HTML/Canvas-ported procedural storybook art**, crafted for toddlers and young girls aged **1 to 6 years old**.

The earlier Vite + TypeScript + HTML5 Canvas prototype is preserved in [`web-prototype/`](web-prototype/) purely as a behavioral and visual reference.

## 2. Core Principles
- **Zero Punishment**: Never show red X marks, error buzzers, timers, or game-over states. Releasing a piece outside its matching ghost slot gently drops/bounces it back into the tray.
- **Toddler-First Ergonomics**: Minimum **64dp–80dp** touch targets, generous piece hitboxes larger than visual contours, upward touch-lift offset so fingers do not hide the piece, magnetic snap glow, and subtle inactivity hints.
- **Anti-Stuck Guarantee**: Viewport clamping, multi-touch single-pointer isolation, `PointerEventType.Cancel` recovery, and return-settle timeouts ensure pieces can never freeze or vanish off-screen.
- **100% Original Storybook Art & Audio**: Rounded pastel picture-book artwork drawn procedurally with Compose `Canvas` / `Path` (zero Disney IP, zero copied reference assets). Sound effects and soothing background music synthesized on-device via `AudioTrack`.
- **English-Only UI**: All user-facing strings live in `app/src/main/res/values/strings.xml` and are accessed via `stringResource(...)` (no hardcoded display strings, no `values-vi`).
- **Kid-Safe Privacy & Immersive Mode**: No `INTERNET` permission, no `ADVERTISING_ID`, no ads, no analytics/tracking. Fullscreen immersive sticky bars, `FLAG_KEEP_SCREEN_ON`, and Parental Gate protection for settings, simulated purchases, and app exit.
- **Responsive Portrait & Landscape**: Adapts cleanly to phones and tablets across rotations without losing in-progress puzzle state (`ViewModel` + `StateFlow`).

## 3. Technical Stack & Constraints
- **Language & UI**: Kotlin (JVM 17), Jetpack Compose + Material 3 (custom storybook styling), Compose Navigation, MVVM / unidirectional data flow (`ViewModel` + `StateFlow`).
- **SDK Targets**: `minSdk = 24`, `compileSdk = 37`, `targetSdk = 37` (locally installed `platforms/android-37.0`).
- **Hot-Loop Performance**: `withFrameNanos` physics and confetti updates mutate pre-allocated structures in place with zero per-frame object allocations; level artwork renders once to a cached offscreen `ImageBitmap`.
- **Persistence & Purchases**: Jetpack DataStore (Preferences) with schema versioning; `PurchaseService` interface backed by `SimulatedPurchaseService` (`unlockedAll` flag in DataStore).
- **Calibrated Parameters**: All tolerances, physics constants, touch sizes, and timings reside in `core/GameConfig.kt` (no magic numbers).

## 4. Development Roadmap (Android Native)
1. **Step 1 — Prototype Relocation, Android Scaffold & 3-Piece Interactive Demo**: Move web prototype to `web-prototype/` via `git mv`; initialize Gradle KTS project with version catalog, ktlint, detekt, Android Lint, JUnit tests, adaptive launcher icon, SplashScreen, and working 3-piece drag-and-snap demo.
2. **Step 2 — Jigsaw Slicer Port, Drop/Bounce Physics, Anti-Stuck, Hints & Rotation**: Port watertight 2–12 piece Bezier slicer (`JigsawSlicer`), spring/bounce return physics (`PuzzlePhysicsEngine`), anti-stuck recovery, inactivity shimmer hints, and rotation state preservation.
3. **Step 3 — 16 Levels & Full Original Illustrations**: Port all 16 level definitions (2 to 12 pieces; Levels 1–4 free, 5–16 locked) and craft 16 unique original illustrations with distinct non-repeating living animations.
4. **Step 4 — Home Carousel Hub, Level Select & DataStore Persistence**: Port rotating Storybook Carousel Hub, star progression, and versioned DataStore persistence.
5. **Step 5 — Parental Gate, Simulated IAP & Grown-Ups Settings**: 3-second hold + math fallback Parental Gate protecting Settings, simulated Unlock/Restore (`unlockedAll`), progress reset, and app exit.
6. **Step 6 — `AudioTrack` Synthesizer & Full `strings.xml` Audit**: Real-time procedural SFX + soothing looping music with lifecycle cleanup.
7. **Step 7 — Performance Optimization & Release Packaging**: Frame timing / GPU profiling targeting 60 FPS, R8/ProGuard shrinking, env-driven release signing, `bundleRelease` AAB verification.

## 5. Definition of Done (Per Step)
- `./gradlew test` passes all unit tests.
- `./gradlew lint ktlintCheck detekt` passes cleanly with zero errors.
- `./gradlew assembleDebug` succeeds and installs/runs on an Android emulator or physical device (verified with `adb exec-out screencap` when on emulator).
- `SPEC.md`, `DECISIONS.md`, `README.md`, and `CHANGELOG.md` are up to date.
- Committed using Conventional Commits.

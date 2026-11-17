# Princess Storybook Puzzle (Android)

A kid-safe native **Android** jigsaw puzzle app built with **Kotlin**, **Jetpack Compose**, **Canvas/Path**, and **Gradle Kotlin DSL**, designed for toddlers and young girls aged 1 to 6.

> **Reference Prototype**: The earlier Vite + TypeScript + HTML5 Canvas web prototype is archived in [`web-prototype/`](web-prototype/) with full git history for reference.

## Key Features (Android Steps 1 & 2)
- **100% Compose & Canvas UI**: Single `MainActivity` with `SplashScreen` API, immersive fullscreen system bars, `FLAG_KEEP_SCREEN_ON`, and zero XML layouts.
- **Kid-Safe Privacy**: No `INTERNET` permission, no `ADVERTISING_ID`, no ads, no analytics/tracking, English-only `strings.xml`.
- **Watertight Jigsaw Slicer (2–12 Pieces)**: Smooth Bezier interlocking puzzle cuts ported cleanly to Kotlin (`JigsawSlicer`).
- **Zero-Allocation 2D Drag & Bounce Physics**: Frame-driven (`withFrameNanos`) spring/drop/bounce return physics, upward touch-lift offset, magnetic slot glow, anti-stuck timeout & multi-touch isolation, and automatic inactivity hint shimmer.
- **Responsive Portrait & Landscape**: Preserves active puzzle progress seamlessly across screen rotations via `ViewModel` + `StateFlow`.

## Commands

```bash
# Run unit tests
./gradlew test

# Run Android Lint, ktlint, and detekt
./gradlew lint ktlintCheck detekt

# Build debug APK
./gradlew assembleDebug

# Install directly to connected Android phone or running emulator
./gradlew installDebug

# Build release AAB (requires signing env vars for release keystore)
./gradlew bundleRelease
```

## Installing on a Physical Android Phone via ADB
1. On your Android phone, open **Settings > About phone** and tap **Build number** 7 times to enable **Developer options**.
2. Open **Settings > System > Developer options** and enable **USB debugging** (or **Wireless debugging** over Wi-Fi on Android 11+).
3. Connect via USB (or run `adb pair <ip>:<pair-port>` then `adb connect <ip>:<port>` for Wireless debugging).
4. Verify your device appears:
   ```bash
   adb devices
   ```
5. Install and launch:
   ```bash
   ./gradlew installDebug
   ```

## Project Structure
- `app/src/main/java/com/princesspuzzle/storybook/`
  - `core/` — `GameConfig` constants and responsive `PuzzleLayout` solver.
  - `puzzle/` — `JigsawSlicer`, `PuzzlePhysicsEngine`, `PuzzleLevels`, `PuzzleViewModel`.
  - `art/` — `MeadowBackdrop`, `StarterIllustrations`, `DrawUtils`, `ConfettiParticles`.
  - `audio/` — Audio engine interface (synthesizer arriving in Step 6).
  - `data/` — `PurchaseService` interface & storage contracts.
  - `ui/` — Compose screens, chunky toddler controls, theme, navigation.
- `web-prototype/` — Reference Vite + TypeScript web prototype.

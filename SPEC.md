# Product Specification — Princess Storybook Puzzle (Android Native)

## 1. Overview
A gentle, kid-safe native **Android** puzzle app built with **Kotlin**, **Jetpack Compose**, and procedural storybook art + `AudioTrack` sound synthesis, remade 1:1 in structure, feel, and gameplay after the native iOS storybook app (`PrincessPuzzle`).

The earlier Vite + TypeScript + HTML5 Canvas web prototype remains preserved in [`web-prototype/`](web-prototype/) purely as an archive reference.

## 2. Screen Flow & Core Gameplay
- **Screen 1 — Planet Wheel Hub (`PlanetWheelScreen`)**:
  - Sunny fairy-tale meadow backdrop with rolling hills, clouds, blossoms, and curved stream (`IosMeadowBackdrop`).
  - Interactive circular Planet Wheel (`RotatingPlanetWheel`): translucent outer ring (`WHEEL_DIM_ALPHA`), 12-o'clock focus line indicator, **16 radial storybook illustrations** rotated outward along the orbit (`WHEEL_ORBIT_RADIUS_RATIO = 0.36f`), gold lock / completion star badges, and a pastel pink-to-cream inner planet hub previewing the focused puzzle.
  - Angular drag (`atan2(dx, -dy)`) with rotational tick sound + spring snap, plus glossy curved blue clockwise / counter-clockwise buttons (`CurvedBlueArrowButton`), bottom sound mute toggle (`SoundCircleButton`), active puzzle pill (`Play` / `Unlock`), `Unlock All` / `Settings` button, and a `BuildConfig.DEBUG`-only lock-preview chip.
- **Screen 2 — 2D Physics Puzzle Gameplay (`PuzzleGameplayScreen`)**:
  - Dimmed meadow (`0.18f`) with baked board spotlight halo and top-left `GlossyGreenBackButton`.
  - **Anatomical / Component Puzzle Parts (`5..9` parts per scene)**: Normalized part silhouettes rendered in clean opaque matte grayscale layered by `zIndex` inside the upper target zone.
  - **2D Gravity & Snap Engine (`PhysicsPuzzleEngine`)**: Unplaced parts settle naturally with gravity (`760dp/s²`), gentle tilt, staggered meadow shelves, river-bank floor collisions, and horizontal separation (`applyLoosePieceSeparation`) so no piece hides underneath another.
  - **Zero-Latency Drag & Magnetic Snap**: Synchronous pointer tracking with magnetic auto-snap (`28dp` phone / `38dp` tablet), release-snap fallback (`60dp` phone / `80dp` tablet), 10 radial sparkles, ascending pentatonic chimes, wrong-drop spin return without punishment, inactivity glow hints (`7s`), and mid-puzzle DataStore persistence (`inProgressPiecesByLevel`).
  - **Surprise Celebration (`SurpriseBubbleSystem` & `SurpriseCelebrationBar`)**: Completing a puzzle animates a gentle victory wave across placed parts, spawns **24 floating pop-able soap bubbles** playing 5-note pentatonic pops, displays earned stars (1–3 based on drops), and provides `Play Again` and circular `Next Puzzle`.
- **Modal — Adult Parental Gate & Simulated Store (`ParentalGateModal`)**:
  - Step 1 (`Parents Only`): Randomized addition challenge (`12..28 + 11..24`) with 4 choice pills (wrong tap regenerates question).
  - Step 2 (`Unlock all puzzles — $2.99`): Unlock All CTA (`unlockedAll = true`), Restore Purchase check, Sound/Music toggles, Re-Lock Magic Pack, and two-tap Reset Stars & Progress.

## 3. Technical Stack & Constraints
- **Language & UI**: Kotlin (JVM 17), Jetpack Compose + Material 3, single `MainActivity` edge-to-edge fullscreen, `FLAG_KEEP_SCREEN_ON`.
- **SDK Targets**: `minSdk = 24`, `compileSdk = 37`, `targetSdk = 37`.
- **Performance**: Baked `ImageBitmap` dimmed backdrop drawn via `BlendMode.Src`, `BoardStaticLayerCache`, pre-rendered `PieceBitmapPair` sprites (`colorBitmap` + `silhouetteBitmap`), zero per-frame allocations inside Canvas `draw`.
- **Persistence**: Jetpack DataStore Preferences (`CURRENT_SCHEMA_VERSION = 2`) with v0/v1 -> v2 schema migration (`ProgressSchemaMigration`).
- **Release Readiness**: `release` & `releaseTestable` strictly enforce `UNLOCK_ALL_FOR_DEBUG = false` (Levels 1–4 free, Levels 5–16 locked until Parental Gate + Unlock All).

## 4. Verification Standard
- `./gradlew testDebugUnitTest detekt ktlintCheck lintDebug` must pass cleanly with zero failures or errors.
- `installDebug` and `installReleaseTestable` verified on connected physical Android hardware.
- `SPEC.md`, `DECISIONS.md`, `README.md`, and `CHANGELOG.md` updated cleanly before Conventional Commit.

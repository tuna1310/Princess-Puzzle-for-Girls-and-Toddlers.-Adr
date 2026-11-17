# Changelog

All notable changes to **Princess Storybook Puzzle** are documented here.

## Android Step 2 — Jigsaw Slicer Port, Drop/Bounce Physics, Anti-Stuck, Hints & Rotation
- Ported watertight multi-row Bezier jigsaw slicer (`JigsawSlicer.kt`) supporting 2 to 12 pieces with pure-Kotlin curve geometry and Compose `Path` builders.
- Implemented zero-allocation `withFrameNanos` drag, snap, and spring-bounce return physics (`PuzzlePhysicsEngine.kt`) with upward touch-lift offset, multi-touch pointer isolation, viewport clamping, and anti-stuck timeout recovery.
- Added subtle inactivity shimmer hint highlighting an unplaced tray piece and its target ghost slot after idle delay (`GameConfig.HINT_INACTIVITY_DELAY_SEC`).
- Implemented portrait/landscape responsive `PuzzleLayout` (`PuzzleLayout.kt`) and `PuzzleViewModel` (`StateFlow`) preserving piece positions and progress seamlessly on screen rotation.
- Ported animated storybook meadow backdrop, 4 starter pastel illustrations (*Starlight Tiara*, *Clover Bunny Princess*, *Cloudberry Unicorn*, *Cupcake Blossom Castle*), and pre-allocated confetti celebration particles.
- Added JUnit tests covering slicer area watertightness (2–12 pieces), portrait/landscape tray non-overlap, snap/return/anti-stuck physics, hint trigger, rotation sync, and level metadata.
- Optimized Compose canvas rendering: cached static meadow terrain (`MeadowBackdropState`), pre-baked per-piece sprite bitmaps (`PieceGeometryCache`), paced idle redraws (15 Hz idle / 60 Hz active), fixed compact HUD piece badge layout (`%1$d pc`/`%1$d pcs`), and configured low-RAM Gradle daemon settings (`gradle.properties`).

## Android Step 1 — Prototype Relocation, Android Scaffold & 3-Piece Interactive Demo
- Moved web prototype into `web-prototype/` via `git mv` preserving complete git history.
- Initialized native Android project (Kotlin 2.0, Jetpack Compose, Gradle KTS, `gradle/libs.versions.toml`, Gradle 8.13 wrapper) with `minSdk 24`, `compileSdk 37`, `targetSdk 37`.
- Configured Android Lint, `ktlint`, and `detekt`, plus centralized `GameConfig` constants, `PurchaseService` contract, adaptive vector icon, `SplashScreen`, edge-to-edge immersive mode, `FLAG_KEEP_SCREEN_ON`, R8 release configuration, and English-only `strings.xml`.
- Delivered playable 3-piece storybook puzzle demo with drag-and-drop snapping and celebration completion.

## Web Prototype Archive (Steps 1–2)
- Prior Vite + TypeScript + HTML5 Canvas 2D PWA vertical slice archived in `web-prototype/`.

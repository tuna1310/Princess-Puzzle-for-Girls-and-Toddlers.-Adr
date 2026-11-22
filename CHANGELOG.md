# Changelog

All notable changes to **Princess Storybook Puzzle** are documented here.

## Android Native 1:1 iOS Architectural Remake
- **Planet Wheel Carousel Hub**: Replaced the temporary Home + 2-column grid with `PlanetWheelScreen` and `RotatingPlanetWheel` featuring the translucent outer ring, 12-o'clock indicator line, 16 radially oriented scenes (`WHEEL_ORBIT_RADIUS_RATIO = 0.36f`), gold lock / completion star badges, pastel pink-to-cream inner sphere, clockwise/counter-clockwise glossy curved blue arrows, bottom sound toggle, active puzzle pill, `Unlock All` CTA, and `BuildConfig.DEBUG` lock-preview chip.
- **Anatomical Part Catalog & Storybook Vector Art**: Replaced square jigsaw cuts with exact iOS `slugId` levels (`castle_pink` .. `grand_ball_princess`), 5–9 anatomical/object parts layered by `zIndex`, flared scalloped princess gowns (`drawPrincessGown`), wavy hair/bangs (`drawWavyHairBack`, `drawHairBangs`), bat wings (`drawDragonWing`), and opaque matte soft-gray target silhouettes (`IosIllustrationRenderer`).
- **2D Gravity Physics, Magnetic Snap & 24 Surprise Bubbles**: Built `PhysicsPuzzleEngine` (`760dp/s²` gravity, staggered meadow shelves `shelfFloorLiftPx`, riverbank ledge floor, horizontal piece separation, synchronous `awaitEachGesture` dragging, `28/38dp` magnetic snap) and `SurpriseBubbleSystem` (10 radial sparkles + 24 floating pop-able celebration bubbles + `SurpriseCelebrationBar`).
- **Parental Gate & Store Unlock Modal**: Implemented 2-step `ParentalGateModal` (`Parents Only` `12..28 + 11..24` 4-choice gate -> `$2.99` `Unlock All 16 Puzzles`, `Restore Previous Purchase`, `Sound`/`Music` toggles, `Re-Lock Magic Pack`, two-tap `Reset`).
- **Native Audio & DataStore v2 Persistence**: Added wheel rotate tick (`playWheelRotateTick`) and 5-note pentatonic bubble pops (`playBubblePop`) to `ProceduralAudioSynth` / `ProceduralAudioEngine`, and upgraded `ProgressRepository` to `CURRENT_SCHEMA_VERSION = 2` saving `completedLevelSlugs`, `lastSelectedWheelIndex`, and `inProgressPiecesByLevel` with v0/v1 -> v2 migration.
- **120Hz `SM_A236E` GPU Optimization & Verification**: Baked dimmed spotlight backdrop (`BlendMode.Src`) + `BoardStaticLayerCache`, improving R8-minified `releaseTestable` 9-piece Level 16 performance on `SM_A236E` (`120Hz`) from `30.80%` legacy jank / `19ms` p95 down to `1.37%` legacy jank (`0.05%` janky frames, `11ms` p50, `12ms` p90, `13ms` p95, `0` missed vsync); passed all 21 JUnit tests (`9+3+2+3+4`), `detekt`, `ktlintCheck`, and `lintDebug`.

## Android Step 6 — Reference App Research & 100% Native Procedural Audio Engine
- Completed extensive reverse engineering of iOS reference app *Princess Puzzle for Girls and Toddlers* (`id682756571` by Tommy Hass) extracting official screenshots, palette, mechanics, screen flows, and specifications into 6 comprehensive documents (`APP_ANALYSIS.md`, `SCREEN_FLOW.md`, `FEATURE_SPECIFICATION.md`, `UI_COMPONENTS.md`, `GAMEPLAY_MECHANICS.md`, `ANDROID_ARCHITECTURE.md`).
- Implemented 100% Android Native procedural audio engine using `AudioTrack` (`ProceduralAudioEngine.kt` and `ProceduralAudioSynth.kt`) with zero external audio assets:
  - Button tap wooden pop (`playTap()`).
  - Piece pickup ascending chirp (`playPickup()`).
  - Drop return spring bounce (`playReturnBounce()`).
  - Major pentatonic ascending celesta chimes across 12 piece placements (`playSnap(placedCount, totalCount)`).
  - Royal celebration fanfare arpeggio with shimmering tremolo chord (`playWinFanfare()`).
  - Seamlessly looping, bedtime-safe 12-second storybook melody in C Major (`synthesizeStorybookMelody()`).
- Added scoped audio engine wrapper (`ScopedAudioEngine.kt`) ensuring level ViewModel teardown never terminates shared application audio or background music.
- Integrated audio lifecycle callbacks in `MainActivity` (`onResume`, `onPause`, `onDestroy`) and preferences sync in `AppNavHost` (`setAudioPreferences`).
- Added thorough JUnit test suite covering all waveform synthesis parameters (`ProceduralAudioSynthTest.kt`), passing with 100% success rate across all 8 test classes.
- Verified Android SDK Lint, `ktlintCheck`, `detekt`, and generated valid APK (`app-debug.apk`, 10.59 MB) and App Bundle (`app-debug.aab`, 8.98 MB).

## Android Step 5 (Early) & Level Unlock / Transition Fixes
- Added debug-only unlock override (`BuildConfig.DEBUG && BuildConfig.UNLOCK_ALL_FOR_DEBUG`, strictly `false` in `release` and `releaseTestable`) with an in-grid Debug Lock Preview toggle so testers can both play all 16 levels immediately and preview release lock behavior.
- Hardened `Next` transitions (`1 -> 2 -> 3 -> 4`), explicit `PuzzleViewModel` keying, `NonCancellable` DataStore star saves, complete runtime/particle/cache resets, `hasNextLevel` celebration button guard on Level 16, and safe `LevelSelect` fallback navigation (`PuzzleTransitionTest.kt`).
- Guarded locked level clicks, `Next` into locked Level 5, and direct locked routes in `AppNavHost` so they show a friendly English `Magic Pack Puzzle` notice from `strings.xml` with a `Grown-Ups` shortcut and never crash or stall.
- Implemented `ParentalGateOverlay` (3-second hold OR single-digit multiplication check) and `GrownUpsSettingsOverlay` (`Unlock All 16 Puzzles`, simulated `Restore Purchase`, `Sound`/`Music` DataStore toggles, `Re-Lock Magic Pack`, two-tap `Reset Stars & Progress`, and `Exit App`) backed by `DataStorePurchaseService` (`ParentalGateAndSettingsTest.kt`).

## Fixes — Drag-and-Drop 60 FPS Performance Optimization
- Added `releaseTestable` profileable R8 build type (`ALLOW_BENCHMARK_INTENT`, debug-signed `.profile` ID) for real-device `dumpsys gfxinfo framestats` measurement on `CPH2471` (Android 12, 12-piece Level 16).
- Eliminated per-frame path tessellation and touch latency: cached Layer 1 static board (`staticBoardBitmap` rebuilt only on resize/snap), baked dual-resolution native piece quads (`spriteBitmap` + `traySpriteBitmap`), paused idle background/confetti updates during active drags, bounded `SceneIllustrations` LRU (`ARTWORK_LRU_CAPACITY = 2`), and updated piece coordinates synchronously inside pointer events.
- Verified on-device 30.6s continuous 12-piece drag (`CPH2471` `releaseTestable`): `Janky frames (legacy)` dropped from `16.54%` to `0.00%` (`0 / 1839` frames), `50th/95th/99th` percentiles improved from `19/20/21 ms` to `8/10/15 ms`, and `0` frames exceeded `32 ms`.

## Android Step 4 — Home Screen, 16-Level Grid, Rotation & DataStore Persistence
- Added custom storybook `HomeScreen` featuring the self-designed crown & ribbon logo crest, slowly revolving storybook preview medallion wheel, star summary pill, large `>= 80dp` `Play` button, and placeholder `Grown-Ups` corner button.
- Added adaptive `LevelSelectScreen` grid displaying all 16 levels with `200px` cached procedural thumbnails, star rows, free/locked indicators (Levels 1–4 free, 5–16 locked behind gentle parental pack notice), and rotation-safe scroll state.
- Implemented Jetpack DataStore persistence (`DataStoreProgressRepository`) saving per-level stars, unlocked levels, `unlockedAll`, last selected puzzle, sound/music preferences, and `schema_version = 1` with `ProgressSchemaMigration` and `DataStorePurchaseService`.
- Updated puzzle completion overlay (`Next` and `Levels` hero buttons) and system/top-bar Back navigation so leaving an active puzzle always returns cleanly to Level Select.
- Added DataStore persistence, v0 -> v1 migration, and reset unit tests in `ProgressDataStoreTest.kt`.

## Android Step 3 — Full 16 Levels, Original Scenes & Unique Living Rewards
- Completed original pastel illustrations for Levels 5–16 (`EnchantedScenesA.kt`, `EnchantedScenesB.kt`) covering 5 to 12 pieces progressively, with English titles stored in `strings.xml`.
- Implemented 16 unique non-repeating living reward animations (`LivingCelebrations.kt`) tailored to each theme (bubbles, rotating carriage wheels, drifting willow leaves, teapot steam, fireflies, harp sound waves, pond ripples, comet streaks, sky lanterns, marquee lights, chandelier prisms).
- Consolidated level metadata in `PuzzleLevels.kt` as a single source of truth, bounded artwork memory with an LRU cache + `200px` thumbnail generator in `SceneIllustrations.kt`, and removed interactive piece-count switching from normal release play (`BuildConfig.DEBUG` gated).

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

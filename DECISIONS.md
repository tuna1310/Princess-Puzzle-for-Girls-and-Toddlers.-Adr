# Architectural & Product Decisions (`DECISIONS.md`)

## 2026-11-17 — Technical Pivot: Native Android (Kotlin + Jetpack Compose)
1. **Pivot from Web PWA to Native Android App**
   - **Decision**: Relocated the Vite + TypeScript + HTML5 Canvas prototype into `web-prototype/` via `git mv` and initialized a pure Kotlin + Jetpack Compose Android project at the repository root.
   - **Rationale**: The product target is a native Android toddler app without browser chrome, network dependencies, or WebView overhead, offering direct `AudioTrack` synthesis, `SplashScreen` API, immersive sticky system bars, `DataStore` persistence, adaptive launcher icons, and Play Store `.aab` (`bundleRelease`) readiness.
   - **Preservation**: Keeping `web-prototype/` with full git history preserves exact mathematical reference formulas (Bezier jigsaw seams, layout math, spring physics constants, and original character designs) during porting.

2. **Target & Compile SDK Selection (`minSdk 24`, `compileSdk 37`, `targetSdk 37`)**
   - **Decision**: Configured `minSdk = 24`, `compileSdk = 37`, and `targetSdk = 37` (against locally installed `platforms/android-37.0`, API level 37.0 / Android 17 preview platform metadata) paired with `build-tools/36.0.0`, Gradle `8.13`, AGP `8.13.2`, Kotlin `2.0.21`, and Java 17 toolchain.
   - **Rationale**: Using the newest installed Android SDK (`android-37.0`) satisfies strict `OldTargetApi` Android Lint checks cleanly with zero lint errors (AGP `8.13.2` logs an informational tested-up-to-36.1 warning, which builds cleanly without suppressing lint). `minSdk = 24` covers >98% of active Android tablets and phones with full `Canvas` and `DataStore` support.

3. **Quality Toolchain: Android Lint + ktlint + detekt**
   - **Decision**: Enforced official Kotlin style via `org.jlleitschuh.gradle.ktlint`, static complexity/code-smell analysis via `detekt`, and strict Android Lint checks (`abortOnError = true`).
   - **Rationale**: Catches Compose/Android anti-patterns, formatting drift, and magic-number creep automatically before every commit.

4. **Hot-Loop Zero-Allocation Architecture & Cached Artwork Bitmap**
   - **Decision**: Pre-render each puzzle's procedural illustration once to an offscreen `ImageBitmap` (`800x800`), pre-compile jigsaw `Path` contours per board resize, and mutate pre-allocated `PieceRuntime` and `ConfettiParticle` objects in place inside `withFrameNanos`.
   - **Rationale**: Prevents GC pauses during rapid finger dragging on mid-range Android tablets and phones, keeping frame times well under the 16.6 ms (60 FPS) budget.

5. **Centralized Calibration (`GameConfig`)**
   - **Decision**: Consolidated all touch target sizes (`>= 64dp`, primary `80dp`), snap tolerance, spring stiffness/damping, touch-lift offset, anti-stuck timeout, hint delay, and parental gate durations in `core/GameConfig.kt`.
   - **Rationale**: Eliminates scattered magic numbers and simplifies tuning on real touchscreens.

6. **Step 1 & 2 Verification Launch Experience**
   - **Decision**: Until Step 4 introduces the full 16-level Carousel Hub, the app boots directly into the playable **3-Piece Storybook Puzzle** (*Clover Bunny Princess*) with a large `>= 64dp` piece-count cycle pill (`3 pcs` -> `2`, `4`, `6`, `9`, `12` pcs) in the top bar.
   - **Rationale**: Allows immediate hands-on testing of Step 1's 3-piece demo AND Step 2's full 2–12 piece slicer, bounce/return physics, anti-stuck recovery, inactivity hints, and portrait/landscape rotation right on a physical device.

7. **Render Caching & Low-RAM Build Tuning**
   - **Decision**: Cached static sky/hills/stream terrain into a viewport `ImageBitmap` in `MeadowBackdropState`, pre-baked clipped jigsaw piece sprites into `PieceGeometryCache`, and paced Compose `withFrameNanos` redraws at 15 Hz when idle while snapping immediately to 60 Hz during drag, bounce, snap glow, hint shimmer, or celebration. Tuned `gradle.properties` (`-Xmx1536m`, `kotlin.daemon.jvmargs=-Xmx768m`, `idletimeout=120000`, `workers.max=1`) for single-daemon low-RAM builds.
   - **Rationale**: Eliminates per-frame `Brush`/shader/path-clipping overhead on mid-range devices and software renderers while keeping build memory bounded on 8 GB workstations.

8. **Single-Source Level Catalog & LRU Artwork Cache (Step 3)**
   - **Decision**: Consolidated all 16 level definitions, piece counts (fixed 2..12 per level; removed release piece-count switcher via `BuildConfig.DEBUG`), and unlock queries in `PuzzleLevels.kt`, backed by an LRU `ImageBitmap` cache (`ARTWORK_LRU_CAPACITY = 3`) plus `200px` thumbnail cache in `SceneIllustrations.kt` and 16 unique per-theme celebration animations in `LivingCelebrations.kt`.
   - **Rationale**: Keeps adding new levels trivial while preventing heap bloat across all 16 scenes (`~3x` full-resolution bitmaps active at once instead of 16).

9. **Home, 16-Level Grid & Singleton DataStore Persistence (Step 4)**
   - **Decision**: Implemented `Home` -> `LevelSelect` -> `PuzzlePlay/{levelId}` navigation in `AppNavHost.kt` with a process-wide singleton `ProgressRepositoryProvider` wrapping Jetpack DataStore (`schema_version = 1`, `ProgressSchemaMigration`), `PuzzleViewModelFactory` scoped per navigation back-stack entry for rotation safety, and fixed `Next` / `Levels` completion actions (read-only piece badge in puzzle HUD).
   - **Rationale**: Prevents duplicate DataStore file conflicts across configuration changes, ensures earned stars and unlocks survive cold restarts cleanly, and prevents toddlers from exiting the app via system Back during active play.

10. **Drag-and-Drop 60 FPS Performance Measurement & 3-Layer Render Pipeline (`releaseTestable`)**
    - **Benchmark Setup**: Physical device `CPH2471` (`720x1612`, Android 12) running R8-minified, debug-signed, `<profileable android:shell="true"/>` build (`com.princesspuzzle.storybook.profile`, `releaseTestable`), continuously dragging pieces on 12-piece Level 16 for 30.5–30.6 seconds (`dumpsys gfxinfo ... framestats`).
    - **Root Cause**: Per-frame `RenderThread+GPU` time averaged `15.70ms` (`p95 = 16.33ms`) from tessellating ~49 cubic-Bezier `drawPath` / `clipPath` strokes every frame (board `RoundRect` clip, scaled `800x800` ghost, 12 dashed slots, and 12 unplaced pieces each stroking shadow + bevel + ink). In addition, `startDrag` delayed initial lift until the next `withFrameNanos` tick instead of updating immediately on touch-down/move.
    - **Measured Results (Before -> After)**:
      - `Janky frames (legacy)`: **`288 / 1741 (16.54%)` -> `0 / 1839 (0.00%)`** (`Janky frames`: `0.23% -> 0.00%`)
      - `50th / 90th / 95th / 99th percentile`: **`19 / 20 / 20 / 21 ms` -> `8 / 9 / 10 / 15 ms`**
      - `Frames > 32 ms`: **`0 -> 0`** (`1284` frames `>= 17 ms` reduced to `2` frames)
      - `GPU 50th / 95th percentile`: **`7 / 7 ms` -> `4 / 5 ms`**; `PROFILEDATA` (`n=120`) `UI avg`: `1.92ms -> 1.01ms`, `RT+GPU p50`: `15.96ms -> 6.24ms`.
    - **Decision**: Separated rendering into (1) a cached full-screen `staticBoardBitmap` layer (backdrop, frame, ghost, dashed slots, tray, placed pieces — redrawn only on resize/snap), (2) native pre-baked `spriteBitmap` (`1.0x`) and `traySpriteBitmap` (`trayScale`) quads blitted 1:1 with zero per-frame path clipping during drag, and (3) synchronous pointer position updates + immediate canvas redraw invalidation on `Press`/`Move`/`Release`, pausing idle animations and confetti updates while actively dragging.

11. **Level 5–16 Access After Level 4, Debug Unlock Flag & Early Step 5 (Parental Gate + Simulated IAP)**
    - **Root Cause Analysis**: Being unable to advance beyond Level 4 was caused by **intentional freemium pack design** paired with an unimplemented Step 5 unlock UI rather than a `1 -> 2 -> 3 -> 4` transition bug: `PuzzleLevels` marks Levels 1–4 `free = true` and Levels 5–16 `free = false`, while `HomeScreen`'s Grown-Ups button and locked-level dialogs pointed to placeholder Step 5 notices and `AppNavHost.onNextLevel` popped silently back to Level Select when hitting locked Level 5.
    - **Decision**:
      - Added `BuildConfig.UNLOCK_ALL_FOR_DEBUG` (`true` ONLY on `debug`, `false` on `release` and `releaseTestable`) gated by `BuildConfig.DEBUG` with a debug-only Lock Preview toggle banner in `LevelSelectScreen` so debug builds unlock all 16 levels immediately without the Parental Gate while `release`/`releaseTestable` strictly enforce the starter/paid lock split.
      - Hardened `AppNavHost` with explicit `viewModel(key = ...)`, `NonCancellable` progress saves, safe `navigateBackToLevels()` fallback, `hasNextLevel` guard on Level 16, route-level `isRouteLevelAccessible` guard returning locked triggers cleanly to `LevelSelect` with a gentle English notice (no crashes or silent failures).
      - Implemented Step 5 early: dual-mode `ParentalGateOverlay` (continuous 3-second hold OR multiplication challenge with keypad), `GrownUpsSettingsOverlay` (`Unlock All 16 Puzzles`, `Restore Purchase`, `Sound`/`Music` toggles, two-tap `Reset Stars & Progress`, `Re-Lock Magic Pack`, and `Exit App`), backed by `DataStorePurchaseService` and `unlockedAll` persistence.

## 2026-11-22 — 1:1 Native iOS Architectural Remake (`PlanetWheel` + `PhysicsPuzzle` + `SurpriseBubbles`)
12. **Carousel Hub & 2D Anatomical Rigid-Body Physics Remake**
    - **Decision**: Replaced the temporary Home + Grid + Square-Jigsaw pipeline with the 1:1 iOS structure (`AppNavHost` routing solely between `PlanetWheelScreen` and `PuzzleGameplayScreen` + `ParentalGateModal`):
      - `PlanetWheelScreen` & `RotatingPlanetWheel`: 16 radial puzzles orbiting at `WHEEL_ORBIT_RADIUS_RATIO = 0.36f` (`WHEEL_ITEM_SIZE_RATIO = 0.17f`, `1.24x` focused scale) around a pastel pink-to-cream inner sphere, clockwise/counter-clockwise curved blue arrow buttons, wheel tick synthesis, and `Unlock All` / compact bottom HUD.
      - `PuzzleCatalogData` & `PuzzleLevels`: Converted all 16 levels to exact iOS `slugId`s (`castle_pink` .. `grand_ball_princess`) with 5–9 normalized anatomical/object parts layered by `zIndex`.
      - `IosIllustrationRenderer` & `IosVectorPrimitives`: Pre-rendered native color bitmaps and opaque matte soft-gray silhouettes (`(lum * 0.58 + 92).coerceIn(120, 236)` preserving full source alpha `a` to eliminate overlapping X-ray seams) alongside storybook ballgowns, wavy hair, bangs, and bat wings.
      - `PhysicsPuzzleEngine` & `SurpriseBubbleSystem`: Implemented 2D gravity (`760dp/s²`), staggered meadow shelves (`shelfFloorLiftPx`), horizontal separation (`nudgeLoosePair`) so loose pieces stay cleanly spread, zero-latency `awaitEachGesture` magnetic snap (`28dp`/`38dp`), and 24 floating pop-able surprise bubbles with 5-note pentatonic pops (`playBubblePop`).
      - `DataStoreProgressRepository`: Upgraded to `CURRENT_SCHEMA_VERSION = 2` persisting `completedLevelSlugs`, `lastSelectedWheelIndex`, and `inProgressPiecesByLevel`, with automatic migration from v0/v1 preferences (`ProgressSchemaMigration`).

13. **120Hz `SM_A236E` Hardware Verification & Static Layer GPU Optimization (`releaseTestable`)**
    - **Device Setup**: Physical device `SM_A236E` (`R5CWA26LE6N`, `1080x2408`, Android 14, `120.00001 Hz` modeId 1) running R8-minified, profileable `com.princesspuzzle.storybook.profile` (`releaseTestable`) on 9-piece Level 16 (`Grand Royal Ball Princess`).
    - **Root Cause Before Polish**: Drawing the full-screen `1080x2408` backdrop + full-screen `0.18f` black overlay + per-frame `Brush.radialGradient` + 9–18 separate sprite draws every tick produced `Janky frames (legacy): 629 / 2042 (30.80%)` (`50th: 12ms`, `90th/95th: 19ms`, `GPU 50th/95th: 8ms/11ms`).
    - **Fix**: Pre-baked the dimmed meadow + spotlight halo into `IosMeadowBackdrop.getOrRenderGameplayBackdrop(...)` blitted with `BlendMode.Src` (zero full-screen dim overdraw and zero per-frame `Brush` shader allocation) plus `BoardStaticLayerCache` (`boardWidth x boardHeight` offscreen bitmap rebuilt only on placement state change when snap/fade finishes).
    - **Measured Results (Before -> After on `SM_A236E` 120Hz)**:
      - `Janky frames`: **`2 / 2042 (0.10%)` -> `1 / 2051 (0.05%)`** (`Number Missed Vsync: 0`, `Number Frame deadline missed: 1`)
      - `Janky frames (legacy)`: **`30.80% (629/2042) -> 1.37% (28/2051)`**
      - `50th / 90th / 95th / 99th percentile`: **`12 / 19 / 19 / 20 ms` -> `11 / 12 / 13 / 17 ms`**
      - `GPU 50th / 95th percentile`: **`8 / 11 ms` -> `8 / 10 ms`**

---

## Archive: Earlier Web Prototype Decisions (2026-11-17)
- Original web decisions (4-free / 12-locked split, watertight multi-row jigsaw slicer `[2]..[4,4,4]`, zero-punishment 3-star completion, original pastel storybook characters) carry forward directly into the Android port; see `web-prototype/` git history for details.

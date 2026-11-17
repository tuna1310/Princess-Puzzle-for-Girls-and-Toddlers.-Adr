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

---

## Archive: Earlier Web Prototype Decisions (2026-11-17)
- Original web decisions (4-free / 12-locked split, watertight multi-row jigsaw slicer `[2]..[4,4,4]`, zero-punishment 3-star completion, original pastel storybook characters) carry forward directly into the Android port; see `web-prototype/` git history for details.

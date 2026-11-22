# Princess Storybook Puzzle (Android Native)

A kid-safe native **Android** puzzle app built with **Kotlin**, **Jetpack Compose**, **Canvas/Path**, and native `AudioTrack` procedural synthesis, remade 1:1 after the native iOS storybook architecture for toddlers and young girls aged 1 to 6.

> **Reference Archive**: The earlier Vite + TypeScript + HTML5 Canvas web prototype is archived in [`web-prototype/`](web-prototype/) with full git history.

## Key Features
- **Interactive 16-Puzzle Planet Wheel Hub**: Translucent rotating carousel displaying all 16 original fairy-tale illustrations radially with 12-o'clock focus snapping, glossy blue clockwise / counter-clockwise navigation arrows, sound toggle, active puzzle pill, and `Unlock All` control.
- **5–9 Anatomical Part Physics Gameplay**: Opaque matte grayscale silhouettes in the upper board and gravity-simulated, tilted puzzle parts distributed across the meadow hills and riverbank below (`PhysicsPuzzleEngine`).
- **Magnetic Snap & 24 Pop-able Surprise Bubbles**: Zero-latency pointer dragging with magnetic snap tolerances, sparkle bursts, victory bounce wave, **24 floating tap-to-pop soap bubbles**, and `Play Again` / `Next Puzzle`.
- **Kid-Safe Freemium & Adult Parental Gate**: Levels 1–4 free, Levels 5–16 locked behind an arithmetic `Parents Only` gate (`12..28 + 11..24`) and `$2.99` simulated Unlock / Restore Store (`UNLOCK_ALL_FOR_DEBUG` is restricted strictly to `debug` builds).
- **100% Native `AudioTrack` Audio Engine**: Wheel ticks, wooden button pops, pickup chirps, bounce springs, ascending celesta snap chimes, 5-note pentatonic bubble pops, royal fanfare, and looping soothing lullaby melody.
- **Versioned DataStore Persistence (`schema_version = 2`)**: Saves completed slugs, per-level stars, mid-puzzle placed piece IDs, active wheel index, `unlockedAll`, and audio settings across restarts and schema migrations.

## Commands

```bash
# Run formatting and full verification suite
./gradlew ktlintFormat
./gradlew testDebugUnitTest detekt ktlintCheck lintDebug

# Build & install onto connected Android device
./gradlew installDebug

# Build & install profileable R8 releaseTestable build (Levels 5-16 strictly locked by default)
./gradlew installReleaseTestable
```

## Project Structure
- `app/src/main/java/com/princesspuzzle/storybook/`
  - `core/` — `GameConfig` and `GeometryHelpers` (radian wrapping, shortest angular delta, wheel focus index).
  - `art/` — `IosThemeColors`, `IosMeadowBackdrop` (cached offscreen + pre-baked `BlendMode.Src` dimmed backdrop), `IosVectorPrimitives`, `IosScenesGroupA`, `IosScenesGroupB`, `IosIllustrationRenderer`.
  - `puzzle/` — `PuzzleLevels`, `PuzzleCatalogData`, `PhysicsPuzzleEngine`, `SurpriseBubbleSystem`, `ParentalGateModels`, `WheelViewModel`, `GameplayViewModel`.
  - `audio/` — `AudioEngine`, `ScopedAudioEngine`, `ProceduralAudioSynth`, `ProceduralAudioEngine`, `AudioEngineProvider`.
  - `data/` — `ProgressRepository` (`DataStoreProgressRepository`, `ProgressSchemaMigration`), `PurchaseService`.
  - `ui/` — `AppNavHost`, `PlanetWheelScreen`, `PuzzleGameplayScreen`, `RotatingPlanetWheel`, `IosButtons`, `ParentalGateModal`, `SurpriseCelebrationBar`.
- `web-prototype/` — Reference Vite + TypeScript web prototype.

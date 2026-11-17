# Changelog

All notable milestone changes to **Princess Storybook Puzzle** are documented here.

## Step 2 — Core Jigsaw Physics, Storybook Art & Playable Vertical Slice

- Implemented watertight multi-row Bezier jigsaw slicer (`src/game/jigsawSlicer.ts`) supporting 2 to 12 interlocking pieces.
- Built 2D toddler-ergonomic drag-and-drop physics (`src/game/physics.ts`) with touch-lift offset, magnetic slot glow, spring snap, and gentle non-punitive return physics.
- Created procedural pastel storybook backdrop and original illustrations (`src/art/`) for the 4 free starter levels (_Starlight Tiara_, _Clover Bunny Princess_, _Cloudberry Unicorn_, _Cupcake Blossom Castle_).
- Built WebAudio sound synthesizer (`src/audio/soundEngine.ts`) for pickup plucks, soft return bounces, pentatonic snap chimes, and celebration fanfare.
- Built rotating 16-level Storybook Carousel Hub (`src/game/hubScreen.ts`), Gameplay Screen (`src/game/puzzleScreen.ts`), and celebration confetti/star particle rewards (`src/game/particles.ts`).
- Verified TypeScript, 17 Vitest unit tests, production PWA build, and Playwright Chrome E2E across portrait and landscape viewports.

## Step 1 — Foundation, PWA & Responsive Canvas Engine

- Initialized Vite + TypeScript strict project with `host: true` LAN access and `vite-plugin-pwa` offline manifest/service worker (`vite.config.ts`).
- Created English-only `src/i18n/en.json` dictionary and `t()` helper (`src/i18n/index.ts`).
- Implemented `PurchaseService` interface (`LocalSimulatedPurchaseService` backed by `unlockedAll` in `localStorage`) and `ProgressStore` (`src/storage/`).
- Built responsive portrait/landscape layout solver (`src/core/layout.ts`) and HiDPI Canvas 2D runtime (`src/core/app.ts`).
- Documented specification and initial design decisions (`SPEC.md`, `DECISIONS.md`, `IDEAS.md`, `README.md`).

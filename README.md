# Princess Storybook Puzzle

An original pastel storybook jigsaw puzzle web game (PWA) built with **Vite**, **TypeScript**, and **HTML5 Canvas 2D**, crafted for toddlers and young children aged 1 to 6.

## Features (Steps 1 & 2)
- **Zero-Punishment Jigsaw Gameplay**: Smooth Bezier interlocking pieces, dimmed ghost silhouette board, magnetic snap glow, upward touch-lift offset, and playful spring-return physics when pieces are released outside their slot.
- **Responsive Portrait & Landscape**: Adapts cleanly to phones and tablets in both orientations with HiDPI (`devicePixelRatio`) rendering and full touch isolation (`touch-action: none`).
- **Original Storybook Art & WebAudio**: Hand-crafted procedural pastel scenes and gentle marimba/chime sound effects with zero copyrighted assets, zero ads, and zero tracking.
- **Living Win Rewards**: Completed puzzles dissolve seam lines, trigger living scene animations, burst pastel confetti fireworks, and award celebratory stars.
- **PWA & LAN Ready**: Configured with `vite-plugin-pwa` for offline play and `host: true` for instant mobile testing over Wi-Fi.

## Getting Started

```bash
npm install
npm run dev
```

Vite exposes the dev server on your local network (`--host` enabled in `vite.config.ts`). Open the printed `Network: http://<your-lan-ip>:5173` URL on your phone or tablet on the same Wi-Fi network.

## Scripts
- `npm run dev` — Start development server with LAN access (`host: true`).
- `npm run build` — Type-check and build production PWA bundle into `dist/`.
- `npm run preview` — Preview production build with LAN access.
- `npm run test` — Run Vitest unit test suite.
- `npm run typecheck` — Run strict TypeScript check (`tsc --noEmit`).

## Project Structure
- `src/i18n/` — `en.json` strings and `t(key)` translator.
- `src/storage/` — Progress persistence and `PurchaseService` interface (`unlockedAll`).
- `src/audio/` — Procedural WebAudio chime, bounce, snap, and fanfare synthesizer.
- `src/art/` — Original storybook backdrops, puzzle illustrations, and living win animations.
- `src/game/` — Jigsaw slicer, 2D physics engine, particle system, level catalog, and screens.
- `src/core/` — HiDPI Canvas manager, responsive layout solver, pointer input controller.
- `src/tests/` — Vitest unit tests for i18n, jigsaw slicing, physics, and storage.

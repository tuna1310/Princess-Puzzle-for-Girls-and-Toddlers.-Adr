# Product Specification — Princess Storybook Puzzle

## 1. Overview

A gentle, tactile HTML5 Canvas 2D jigsaw puzzle web game (packaged as an offline-capable PWA) designed for toddlers and young girls aged **1 to 6 years old**.

## 2. Core Principles

- **Zero Punishment**: Never show red X marks, error buzzers, timers, or game-over states. Dropping a piece outside its target slot softly bounces/slides it back to the tray.
- **Toddler-First Ergonomics**: Generous hit targets, upward touch-lift offset so small fingers never obscure the dragged piece, clear ghost silhouette slots, and magnetic snap cues.
- **100% Original Storybook Art & Audio**: Rounded, soft-pastel picture-book illustrations rendered procedurally via Canvas 2D / SVG with original princess & fairy-tale characters (zero copyrighted/Disney characters or copied sample assets). Sound effects synthesized locally via WebAudio.
- **English-Only In-Game Text**: All displayed UI strings reside in `src/i18n/en.json` and are accessed exclusively via `t(key)`.
- **Privacy & Safety**: Zero ads, zero analytics/tracking, zero external network dependencies during gameplay.
- **Responsive Orientation**: Seamless adaptation to both **portrait** and **landscape** phone/tablet screens, with LAN access (`host: true`) for mobile testing.

## 3. Gameplay & Progression

- **Single Focused Mechanic**: Drag-and-drop jigsaw puzzle with 2D spring/gravity physics.
- **16 Levels Total**: Progressive difficulty scaling from **2 pieces** up to **12 pieces**:
  - Levels 1–4: Free starter puzzles (2, 3, 4, 4 pieces).
  - Levels 5–16: Locked puzzles (5, 6, 6, 8, 8, 9, 9, 10, 10, 12, 12, 12 pieces), unlocked via a simulated IAP `PurchaseService` interface backed by `unlockedAll` in `localStorage`.
- **Win Celebration**: Upon placing the final piece, seam lines dissolve, the illustration comes alive with gentle character/environment animation, paper confetti fireworks burst across the screen, and 3 stars are awarded.
- **Parental Gate**: Protects grown-up settings and simulated purchase unlock behind a child-safe gate (3-second press-and-hold or adult math check).

## 4. Development Roadmap

- **Step 1 — Foundation, PWA & Canvas Engine**: Project tooling, Vite + TypeScript + PWA (`host: true`), `src/i18n/en.json` + `t()`, `PurchaseService` & progress storage abstractions, HiDPI responsive Canvas 2D engine, unit test setup, core docs (`SPEC.md`, `DECISIONS.md`, `IDEAS.md`, `README.md`).
- **Step 2 — Core Jigsaw Physics, Audio & Playable Vertical Slice**: Multi-row jigsaw geometry slicer (2–12 pieces), drag-and-drop physics (touch offset, magnetic glow, spring snap, soft tray return), WebAudio synthesizer, Storybook Meadow Hub with rotating carousel wheel, original art + living win celebration for free starter levels (1–4) plus locked slots (5–16).
- **Step 3 — Full 16 Original Illustrations & Living Animations**: Complete hand-crafted procedural artwork and unique post-win living animations for Levels 5–16 (5 to 12 pieces).
- **Step 4 — Parental Gate, Settings, Simulated IAP & Cross-Device QA**: Interactive Parental Gate (3s hold / math challenge), Grown-ups Settings & Unlock All modal, offline PWA audit, portrait/landscape device testing.

## 5. Definition of Done

Every milestone commit must satisfy all of the following checks cleanly:

- `npm run lint` passes with zero errors and zero warnings (`ESLint` + `Prettier` clean via `npm run format:check`).
- `npm run test` passes all unit tests (`Vitest`) with zero skipped or broken assertions.
- `npm run build` succeeds (`tsc --noEmit && vite build`) and generates a valid offline PWA bundle (`dist/manifest.webmanifest` + `dist/sw.js`).
- `CHANGELOG.md` is updated with the milestone summary.
- Visual/interactive behavior is verified in a real browser across portrait and landscape mobile/tablet viewports.

# Architectural & Product Decisions (`DECISIONS.md`)

## 2026-11-17 — Initial Roadmap & Baseline Assumptions
Because no pre-existing `SPEC.md` or `/reference` directory existed in the initialized workspace, the Lead Developer derived the concrete roadmap and resolved open product details from the user brief and attached visual references:

1. **Four-Step Roadmap Alignment**
   - **Decision**: Organized implementation into Steps 1–4 (`SPEC.md` Section 4) and delivered Steps 1 and 2 together as the first playable vertical slice.
   - **Rationale**: Ensures the project compiles, installs as a PWA over LAN (`host: true`), runs automated tests, and offers playable puzzles immediately before investing in all 16 illustrations (Step 3) and the full Parental Gate/Settings suite (Step 4).

2. **Free vs. Locked Level Split (4 Free / 12 Locked)**
   - **Decision**: Levels 1–4 (2, 3, 4, 4 pieces) are unlocked by default; Levels 5–16 (5 to 12 pieces) start locked and are governed by `PurchaseService.isUnlockedAll()` (`unlockedAll` in `localStorage`).
   - **Rationale**: Satisfies "first few levels free, remaining locked" while giving toddlers immediate variety across 2-, 3-, and 4-piece puzzles in Step 2.

3. **Watertight Multi-Row Jigsaw Slicing (2 to 12 Pieces)**
   - **Decision**: Slice each full storybook scene using a flexible row-count jigsaw generator (`[2]`, `[3]`, `[2,2]`, `[2,3]`, `[3,3]`, `[4,2]`, `[3,4,3]`, `[4,3]`) with shared sub-interval Bezier tab/blank seams rather than arbitrary non-interlocking cuts.
   - **Rationale**: Guarantees seamless zero-gap piece locking, intuitive silhouette + color cues on the dimmed ghost frame, and consistent scaling from 2 pieces (age 1–2) up to 12 pieces (age 5–6).

4. **Tray Ergonomics & Touch Offset**
   - **Decision**: Unplaced pieces rest in a neatly spaced, non-overlapping tray at reduced scale and smoothly scale up to `1.0x` board size when grabbed; touch drags apply an upward finger-clearance offset so toddlers' thumbs do not hide the piece.
   - **Rationale**: Eliminates overlapping-piece frustration on small phone screens while preserving 1:1 desktop mouse accuracy.

5. **Always-Positive Star Reward (3 Stars on Completion)**
   - **Decision**: Completing any puzzle awards 3 celebratory stars unconditionally.
   - **Rationale**: Enforces the zero-punishment toddler design philosophy (no accuracy or time penalties for ages 1–6).

6. **Offline-Safe Typography & Procedural Art/Audio**
   - **Decision**: Use local rounded system font stacks (`ui-rounded`, `"Nunito"`, `"Arial Rounded MT Bold"`, etc.) and 100% procedural Canvas 2D/SVG art + WebAudio synthesis, with zero external CDN fonts or tracking calls.
   - **Rationale**: Guarantees instant offline PWA startup and strict privacy compliance.

7. **Step 2 Locked-Level Feedback**
   - **Decision**: Tapping a locked level (5–16) or the Grown-ups button during Step 2 displays a friendly, non-punitive storybook notice pointing to the upcoming Grown-ups Parental Gate (Step 4) while keeping the `PurchaseService` interface wired end-to-end.

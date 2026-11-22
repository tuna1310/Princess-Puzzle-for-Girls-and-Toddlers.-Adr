package com.princesspuzzle.storybook.core

/**
 * Centralized calibrated constants for the iOS-matched planet carousel,
 * 2D physics gameplay, magnetic piece snapping, bubble surprise, and kid-safe controls.
 */
object GameConfig {
    // Catalog & entitlement
    const val FREE_LEVEL_COUNT = 4
    const val TOTAL_LEVEL_COUNT = 16
    const val MAX_STARS = 3
    const val TABLET_WIDTH_THRESHOLD_DP = 600f

    // Touch & snap tolerances
    const val MIN_BUTTON_TOUCH_TARGET_DP = 64f
    const val PIECE_HIT_PADDING_DP = 16f
    const val VIEWPORT_EDGE_MARGIN_DP = 24f
    const val MAGNETIC_SNAP_PHONE_DP = 28f
    const val MAGNETIC_SNAP_TABLET_DP = 38f
    const val RELEASE_SNAP_PHONE_DP = 60f
    const val RELEASE_SNAP_TABLET_DP = 80f

    // Planet wheel proportions
    const val WHEEL_CENTER_Y_FRACTION = 0.44f
    const val WHEEL_INNER_DIAMETER_RATIO = 0.44f
    const val WHEEL_ORBIT_RADIUS_RATIO = 0.36f
    const val WHEEL_ITEM_SIZE_RATIO = 0.17f
    const val WHEEL_DRAG_MIN_PX = 6f
    const val WHEEL_DIM_ALPHA = 0.36f

    // 2D physics simulation (SpriteKit-matched parameters ported to dp/s)
    const val MIN_FRAME_DELTA_SEC = 0.001f
    const val MAX_FRAME_DELTA_SEC = 0.045f
    const val BOARD_DIM_OPACITY = 0.18f
    const val GRAVITY_DP_PER_SEC_SQ = 760f
    const val PIECE_RESTITUTION = 0.24f
    const val PIECE_BOUNDARY_FRICTION = 0.55f
    const val PIECE_LINEAR_DAMPING = 0.35f
    const val PIECE_ANGULAR_DAMPING = 0.45f
    const val PIECE_DRAG_SCALE = 1.07f
    const val PIECE_SNAP_PEAK_SCALE = 1.12f
    const val SNAP_ANIM_DURATION_SEC = 0.18f
    const val GRAY_FADEOUT_DURATION_SEC = 0.14f
    const val STALE_MOVE_THRESHOLD_MS = 85L

    // Hints & victory bubble celebration
    const val HINT_INACTIVITY_DELAY_SEC = 7.0f
    const val VICTORY_BUBBLE_COUNT = 24
    const val VICTORY_BUBBLE_SPAWN_INTERVAL_SEC = 0.26f
    const val MIN_BUBBLE_RADIUS_DP = 26f
    const val MAX_BUBBLE_RADIUS_DP = 42f
    const val MAX_SPARKLE_PARTICLES = 64

    // Parental gate & cache
    const val PARENTAL_GATE_HOLD_SECONDS = 3.0f
    const val ILLUSTRATION_RENDER_SCALE = 2.0f
    const val ILLUSTRATION_CACHE_LIMIT = 24
}

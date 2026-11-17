package com.princesspuzzle.storybook.core

/**
 * Centralized calibrated constants for jigsaw ergonomics, physics,
 * anti-stuck recovery, inactivity hints, and kid-safe controls.
 */
object GameConfig {
    // Touch ergonomics (dp)
    const val MIN_BUTTON_TOUCH_TARGET_DP = 64f
    const val PRIMARY_BUTTON_TOUCH_TARGET_DP = 80f
    const val PIECE_HIT_PADDING_DP = 18f
    const val TOUCH_LIFT_OFFSET_MIN_DP = 28f
    const val TOUCH_LIFT_OFFSET_MAX_DP = 54f
    const val TOUCH_LIFT_PIECE_FRACTION = 0.36f
    const val MIN_SNAP_RADIUS_DP = 36f
    const val SNAP_RADIUS_PIECE_FRACTION = 0.44f
    const val VIEWPORT_EDGE_MARGIN_DP = 28f

    // Layout proportions
    const val LANDSCAPE_ASPECT_THRESHOLD = 1.05f
    const val MIN_LAYOUT_DIMENSION_PX = 280f
    const val TOP_BAR_MIN_HEIGHT_DP = 64f
    const val TOP_BAR_MAX_HEIGHT_DP = 76f
    const val LANDSCAPE_BOARD_SHARE = 0.58f
    const val PORTRAIT_TRAY_SHARE_SMALL = 0.33f
    const val PORTRAIT_TRAY_SHARE_LARGE = 0.37f
    const val TRAY_LARGE_PIECE_THRESHOLD = 6
    const val TRAY_SCALE_MIN = 0.32f
    const val TRAY_SCALE_MAX = 0.86f
    const val TRAY_CELL_FILL_RATIO = 0.84f

    // Physics simulation (seconds / spring units)
    const val MAX_FRAME_DELTA_SEC = 0.05f
    const val MIN_FRAME_DELTA_SEC = 0.001f
    const val SCALE_EASE_RATE = 26f
    const val SCALE_SNAP_EPSILON = 0.005f
    const val GLOW_EASE_RATE = 14f
    const val SNAP_POSITION_EASE_RATE = 22f
    const val SNAP_ANIM_SPEED = 3.8f
    const val SNAP_POP_AMPLITUDE = 0.08f
    const val DRAG_TILT_MAX_RAD = 0.09f
    const val DRAG_TILT_VELOCITY_FACTOR = 0.00018f
    const val DRAG_VELOCITY_DECAY_BASE = 0.04f

    const val RETURN_SPRING_STIFFNESS = 92f
    const val RETURN_SPRING_DAMPING = 12.5f
    const val RETURN_DROP_IMPULSE_Y_DP = 140f
    const val RETURN_BOUNCE_GRAVITY_DP = 680f
    const val RETURN_BOUNCE_DURATION_SEC = 0.14f
    const val RETURN_VELOCITY_CLAMP_DP = 420f
    const val RETURN_BOUNCE_RESTITUTION = 0.5f
    const val RETURN_SETTLE_DIST_DP = 2f
    const val RETURN_SETTLE_SPEED_DP = 16f
    const val RETURN_SETTLE_TIMEOUT_SEC = 2.0f
    const val STALE_MOVE_THRESHOLD_MS = 85L

    // Celebration & hint timings
    const val SEAM_FADE_DURATION_SEC = 0.45f
    const val SECOND_CONFETTI_DELAY_SEC = 0.72f
    const val CELEBRATION_PANEL_DELAY_SEC = 0.5f
    const val HINT_INACTIVITY_DELAY_SEC = 7.0f
    const val HINT_PULSE_SPEED = 3.2f
    const val STAR_ONE_DELAY_SEC = 0.55f
    const val STAR_TWO_DELAY_SEC = 0.72f
    const val STAR_THREE_DELAY_SEC = 0.9f

    // Parental gate & storage constants
    const val PARENTAL_GATE_HOLD_SECONDS = 3.0f
    const val ARTWORK_CACHE_SIZE_PX = 800
    const val THUMBNAIL_CACHE_SIZE_PX = 200
    const val ARTWORK_LRU_CAPACITY = 2
    const val MAX_CONFETTI_PARTICLES = 64
    const val MAX_STARS = 3
    const val FREE_LEVEL_COUNT = 4
    const val TOTAL_LEVEL_COUNT = 16
}

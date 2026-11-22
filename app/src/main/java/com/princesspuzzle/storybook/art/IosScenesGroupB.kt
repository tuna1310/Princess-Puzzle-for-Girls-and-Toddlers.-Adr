package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawButterfly
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawCrown
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawDaisy
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawFivePointStar
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawHairBangs
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawHeart
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawPrincessArms
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawPrincessFace
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawPrincessGown
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawPrincessShoes
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawSpireAndFlag
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawWavyHairBack
import com.princesspuzzle.storybook.art.IosVectorPrimitives.outlinedOval
import com.princesspuzzle.storybook.art.IosVectorPrimitives.outlinedPath
import com.princesspuzzle.storybook.art.IosVectorPrimitives.outlinedRoundRect
import com.princesspuzzle.storybook.art.IosVectorPrimitives.strokeWidth
import com.princesspuzzle.storybook.puzzle.SceneThemeId

/**
 * Vector part renderers for Levels 9-16 (Fantasy & Royal Finale tiers).
 */
internal object IosScenesGroupB {
    fun DrawScope.drawGroupBPart(theme: SceneThemeId, partIndex: Int, w: Float, h: Float) {
        when (theme) {
            SceneThemeId.RAINBOW_PALACE -> drawRainbowPalacePart(partIndex, w, h)
            SceneThemeId.BUTTERFLY_TREE -> drawButterflyTreePart(partIndex, w, h)
            SceneThemeId.PUMPKIN_CARRIAGE -> drawPumpkinCarriagePart(partIndex, w, h)
            SceneThemeId.MEADOW_UNICORN -> drawMeadowUnicornPart(partIndex, w, h)
            SceneThemeId.ROYAL_TREASURE -> drawRoyalTreasurePart(partIndex, w, h)
            SceneThemeId.MERMAID_PRINCESS -> drawMermaidPrincessPart(partIndex, w, h)
            SceneThemeId.ROSE_PRINCESS -> drawRosePrincessPart(partIndex, w, h)
            SceneThemeId.GRAND_BALL_PRINCESS -> drawGrandBallPrincessPart(partIndex, w, h)
            else -> Unit
        }
    }

    private fun DrawScope.drawRainbowPalacePart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> {
                drawButterfly(0.50f, 0.11f, 0.18f, 0.13f, IosThemeColors.goldLight, IosThemeColors.crimson, w, h)
                outlinedRoundRect(0.50f, 0.19f, 0.34f, 0.04f, 0.4f, IosThemeColors.turquoise, w, h)
            }
            1 -> drawSpireAndFlag(0.24f, 0.30f, 0.17f, 0.21f, IosThemeColors.crimson, IosThemeColors.goldLight, w, h)
            2 -> drawSpireAndFlag(0.50f, 0.27f, 0.19f, 0.22f, IosThemeColors.goldRich, IosThemeColors.crimson, w, h)
            3 -> drawSpireAndFlag(0.76f, 0.30f, 0.17f, 0.21f, IosThemeColors.crimson, IosThemeColors.goldLight, w, h)
            4 -> {
                outlinedRoundRect(0.50f, 0.56f, 0.25f, 0.34f, 0.10f, IosThemeColors.turquoise, w, h)
                outlinedRoundRect(0.50f, 0.51f, 0.10f, 0.12f, 0.4f, IosThemeColors.goldLight, w, h)
            }
            5, 6 -> {
                val wx = if (partIndex == 5) 0.23f else 0.77f
                outlinedRoundRect(wx, 0.62f, 0.22f, 0.30f, 0.10f, IosThemeColors.goldLight, w, h)
                drawHeart(wx, 0.60f, 0.08f, 0.07f, IosThemeColors.crimson, w, h)
            }
            7 -> {
                outlinedRoundRect(0.50f, 0.76f, 0.26f, 0.22f, 0.40f, IosThemeColors.crimson, w, h)
                drawFivePointStar(0.50f, 0.75f, 0.04f, 0.018f, IosThemeColors.goldLight, w, h)
            }
        }
    }

    private fun DrawScope.drawButterflyTreePart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> drawButterfly(0.66f, 0.16f, 0.22f, 0.17f, IosThemeColors.castlePink, IosThemeColors.goldLight, w, h)
            1 -> {
                outlinedOval(0.50f, 0.33f, 0.28f, 0.25f, IosThemeColors.emeraldTree, w, h)
                drawDaisy(0.50f, 0.32f, 0.036f, IosThemeColors.castlePinkLight, IosThemeColors.goldRich, w, h)
            }
            2 -> outlinedOval(0.24f, 0.42f, 0.24f, 0.22f, IosThemeColors.mintLeaf, w, h)
            3 -> outlinedOval(0.76f, 0.42f, 0.24f, 0.22f, IosThemeColors.mintLeaf, w, h)
            4 -> {
                outlinedRoundRect(0.50f, 0.64f, 0.22f, 0.32f, 0.20f, IosThemeColors.woodWarm, w, h)
                outlinedRoundRect(0.50f, 0.66f, 0.11f, 0.14f, 0.42f, IosThemeColors.goldLight, w, h)
            }
            5 -> {
                outlinedOval(0.50f, 0.84f, 0.48f, 0.14f, IosThemeColors.hillFarTop, w, h)
                drawDaisy(0.34f, 0.84f, 0.03f, Color.White, IosThemeColors.goldRich, w, h)
                drawDaisy(0.66f, 0.84f, 0.03f, IosThemeColors.castlePink, IosThemeColors.goldRich, w, h)
            }
        }
    }

    private fun DrawScope.drawPumpkinCarriagePart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> drawCrown(0.50f, 0.19f, 0.28f, 0.15f, IosThemeColors.goldRich, IosThemeColors.castlePink, w, h)
            1 -> {
                outlinedOval(0.50f, 0.46f, 0.50f, 0.32f, IosThemeColors.castlePink, w, h)
                outlinedOval(0.50f, 0.46f, 0.30f, 0.31f, IosThemeColors.castlePinkLight, w, h)
            }
            2 -> {
                outlinedRoundRect(0.50f, 0.44f, 0.22f, 0.17f, 0.32f, IosThemeColors.skyTop, w, h)
                outlinedOval(0.43f, 0.42f, 0.08f, 0.14f, IosThemeColors.crimson, w, h)
                outlinedOval(0.57f, 0.42f, 0.08f, 0.14f, IosThemeColors.crimson, w, h)
            }
            3 -> {
                outlinedRoundRect(0.80f, 0.36f, 0.12f, 0.15f, 0.28f, IosThemeColors.goldLight, w, h)
                drawHeart(0.80f, 0.36f, 0.05f, 0.05f, IosThemeColors.dragonOrange, w, h)
            }
            4 -> outlinedRoundRect(0.50f, 0.70f, 0.19f, 0.12f, 0.25f, IosThemeColors.royalPurple, w, h)
            5, 6 -> {
                val cx = if (partIndex == 5) 0.26f else 0.74f
                outlinedOval(cx, 0.76f, 0.23f, 0.23f, IosThemeColors.goldLight, w, h)
                outlinedOval(cx, 0.76f, 0.12f, 0.12f, IosThemeColors.goldRich, w, h)
            }
        }
    }

    private fun DrawScope.drawMeadowUnicornPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> {
                val horn = Path().apply {
                    moveTo(0.71f * w, 0.22f * h)
                    lineTo(0.81f * w, 0.06f * h)
                    lineTo(0.76f * w, 0.23f * h)
                    close()
                }
                outlinedPath(horn, IosThemeColors.goldLight, w, h)
                drawCrown(0.68f, 0.18f, 0.14f, 0.08f, IosThemeColors.goldRich, IosThemeColors.castlePink, w, h)
            }
            1 -> {
                outlinedOval(0.60f, 0.24f, 0.17f, 0.18f, IosThemeColors.hairLavender, w, h)
                outlinedOval(0.69f, 0.31f, 0.25f, 0.18f, IosThemeColors.kittyWhite, w, h)
                outlinedOval(0.79f, 0.33f, 0.11f, 0.09f, IosThemeColors.castlePinkLight, w, h)
                drawCircle(
                    IosThemeColors.outlineInk,
                    radius = strokeWidth(w, h) * 1.6f,
                    center = Offset(0.70f * w, 0.29f * h)
                )
            }
            2 -> {
                drawDaisy(0.57f, 0.44f, 0.032f, IosThemeColors.castlePink, IosThemeColors.goldRich, w, h)
                drawDaisy(0.64f, 0.45f, 0.032f, Color.White, IosThemeColors.goldRich, w, h)
            }
            3 -> {
                outlinedOval(0.44f, 0.48f, 0.33f, 0.20f, IosThemeColors.kittyWhite, w, h)
                drawFivePointStar(0.38f, 0.48f, 0.035f, 0.015f, IosThemeColors.goldLight, w, h)
            }
            4 -> {
                outlinedRoundRect(0.60f, 0.70f, 0.08f, 0.28f, 0.4f, IosThemeColors.kittyWhite, w, h)
                outlinedRoundRect(0.70f, 0.71f, 0.08f, 0.28f, 0.4f, IosThemeColors.kittyWhite, w, h)
                outlinedRoundRect(0.60f, 0.83f, 0.09f, 0.05f, 0.3f, IosThemeColors.goldRich, w, h)
                outlinedRoundRect(0.70f, 0.84f, 0.09f, 0.05f, 0.3f, IosThemeColors.goldRich, w, h)
            }
            5 -> {
                outlinedRoundRect(0.31f, 0.70f, 0.08f, 0.28f, 0.4f, IosThemeColors.kittyWhite, w, h)
                outlinedRoundRect(0.41f, 0.71f, 0.08f, 0.28f, 0.4f, IosThemeColors.kittyWhite, w, h)
                outlinedRoundRect(0.31f, 0.83f, 0.09f, 0.05f, 0.3f, IosThemeColors.goldRich, w, h)
                outlinedRoundRect(0.41f, 0.84f, 0.09f, 0.05f, 0.3f, IosThemeColors.goldRich, w, h)
            }
            6 -> {
                outlinedOval(0.17f, 0.50f, 0.15f, 0.22f, IosThemeColors.castlePink, w, h)
                outlinedOval(0.13f, 0.59f, 0.13f, 0.17f, IosThemeColors.turquoise, w, h)
                outlinedOval(0.15f, 0.65f, 0.10f, 0.11f, IosThemeColors.goldLight, w, h)
            }
        }
    }

    private fun DrawScope.drawRoyalTreasurePart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> {
                val gem = Path().apply {
                    moveTo(0.50f * w, 0.09f * h)
                    lineTo(0.60f * w, 0.18f * h)
                    lineTo(0.50f * w, 0.27f * h)
                    lineTo(0.40f * w, 0.18f * h)
                    close()
                }
                outlinedPath(gem, IosThemeColors.magenta, w, h)
            }
            1 -> drawCrown(0.50f, 0.31f, 0.46f, 0.18f, IosThemeColors.goldLight, IosThemeColors.crystalBlue, w, h)
            2 -> {
                drawLine(
                    IosThemeColors.goldRich,
                    Offset(0.31f * w, 0.50f * h),
                    Offset(0.18f * w, 0.70f * h),
                    strokeWidth(w, h) * 1.4f
                )
                drawFivePointStar(0.31f, 0.49f, 0.085f, 0.038f, IosThemeColors.goldLight, w, h)
            }
            3 -> {
                outlinedOval(0.72f, 0.54f, 0.22f, 0.21f, IosThemeColors.goldRich, w, h)
                drawHeart(0.72f, 0.55f, 0.12f, 0.11f, IosThemeColors.crimson, w, h)
            }
            4, 5 -> {
                val sx = if (partIndex == 4) 0.35f else 0.66f
                outlinedOval(sx, 0.80f, 0.24f, 0.15f, IosThemeColors.crystalBlue, w, h)
                drawHeart(sx, 0.79f, 0.06f, 0.05f, IosThemeColors.creamPearl, w, h)
            }
        }
    }

    private fun DrawScope.drawMermaidPrincessPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> drawCrown(0.50f, 0.11f, 0.22f, 0.10f, IosThemeColors.goldLight, IosThemeColors.turquoise, w, h)
            1 -> drawWavyHairBack(0.50f, 0.26f, 0.44f, 0.26f, IosThemeColors.hairCoral, w, h)
            2 -> {
                drawPrincessFace(
                    0.50f,
                    0.25f,
                    0.22f,
                    0.19f,
                    IosThemeColors.skinFair,
                    IosThemeColors.turquoiseDeep,
                    w,
                    h
                )
                drawHairBangs(0.50f, 0.25f, 0.22f, 0.19f, IosThemeColors.hairCoral, w, h)
            }
            3 -> {
                outlinedOval(0.44f, 0.43f, 0.12f, 0.11f, IosThemeColors.castleLavender, w, h)
                outlinedOval(0.56f, 0.43f, 0.12f, 0.11f, IosThemeColors.castleLavender, w, h)
            }
            4 -> drawPrincessArms(0.50f, 0.43f, 0.60f, 0.15f, IosThemeColors.skinFair, IosThemeColors.turquoise, w, h)
            5 -> outlinedOval(0.48f, 0.62f, 0.26f, 0.22f, IosThemeColors.turquoise, w, h)
            6 -> {
                outlinedOval(0.52f, 0.81f, 0.18f, 0.15f, IosThemeColors.turquoiseDeep, w, h)
                outlinedOval(0.65f, 0.84f, 0.17f, 0.13f, IosThemeColors.mintLeaf, w, h)
            }
            7 -> drawFivePointStar(0.24f, 0.74f, 0.065f, 0.028f, IosThemeColors.castlePink, w, h)
        }
    }

    private fun DrawScope.drawRosePrincessPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> drawCrown(0.50f, 0.11f, 0.22f, 0.10f, IosThemeColors.goldRich, IosThemeColors.roseRed, w, h)
            1 -> drawWavyHairBack(0.50f, 0.26f, 0.40f, 0.25f, IosThemeColors.hairAuburn, w, h)
            2 -> {
                drawPrincessFace(0.50f, 0.25f, 0.22f, 0.19f, IosThemeColors.skinFair, IosThemeColors.roseRed, w, h)
                drawHairBangs(0.50f, 0.25f, 0.22f, 0.19f, IosThemeColors.hairAuburn, w, h)
            }
            3 -> {
                outlinedRoundRect(0.50f, 0.43f, 0.22f, 0.15f, 0.25f, IosThemeColors.roseRed, w, h)
                drawHeart(0.50f, 0.43f, 0.06f, 0.05f, IosThemeColors.goldLight, w, h)
            }
            4 -> drawPrincessGown(
                0.50f,
                0.52f,
                0.30f,
                0.58f,
                0.32f,
                IosThemeColors.royalPurple,
                IosThemeColors.castlePinkLight,
                w,
                h
            )
            5 -> drawPrincessGown(
                0.50f,
                0.66f,
                0.26f,
                0.46f,
                0.24f,
                IosThemeColors.castlePink,
                IosThemeColors.roseRed,
                w,
                h
            )
            6 -> {
                drawDaisy(0.75f, 0.45f, 0.045f, IosThemeColors.roseRed, IosThemeColors.goldLight, w, h)
                drawCircle(
                    IosThemeColors.emeraldTree,
                    radius = strokeWidth(w, h) * 3f,
                    center = Offset(0.76f * w, 0.53f * h)
                )
            }
            7 -> drawPrincessShoes(0.50f, 0.85f, 0.30f, 0.14f, IosThemeColors.skinFair, IosThemeColors.goldRich, w, h)
        }
    }

    private fun DrawScope.drawGrandBallPrincessPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> drawCrown(0.50f, 0.10f, 0.23f, 0.10f, IosThemeColors.crystalBlue, IosThemeColors.goldLight, w, h)
            1 -> drawWavyHairBack(0.50f, 0.23f, 0.36f, 0.20f, IosThemeColors.hairBlonde, w, h)
            2 -> {
                drawPrincessFace(0.50f, 0.25f, 0.22f, 0.18f, IosThemeColors.skinFair, IosThemeColors.royalPurple, w, h)
                drawHairBangs(0.50f, 0.25f, 0.22f, 0.18f, IosThemeColors.hairBlonde, w, h)
                drawHeart(0.50f, 0.34f, 0.05f, 0.04f, IosThemeColors.crimson, w, h)
            }
            3 -> outlinedRoundRect(0.50f, 0.43f, 0.22f, 0.14f, 0.25f, IosThemeColors.goldRich, w, h)
            4 -> {
                outlinedOval(0.28f, 0.44f, 0.16f, 0.07f, IosThemeColors.skinFair, w, h)
                outlinedOval(0.20f, 0.42f, 0.14f, 0.14f, IosThemeColors.castlePinkLight, w, h)
            }
            5 -> {
                outlinedOval(0.72f, 0.44f, 0.16f, 0.07f, IosThemeColors.skinFair, w, h)
                drawFivePointStar(0.81f, 0.38f, 0.04f, 0.018f, IosThemeColors.goldLight, w, h)
            }
            6 -> drawPrincessGown(
                0.50f,
                0.57f,
                0.22f,
                0.46f,
                0.17f,
                IosThemeColors.magenta,
                IosThemeColors.castlePinkLight,
                w,
                h
            )
            7 -> drawPrincessGown(
                0.50f,
                0.72f,
                0.30f,
                0.58f,
                0.23f,
                IosThemeColors.goldLight,
                IosThemeColors.goldRich,
                w,
                h
            )
            8 -> drawPrincessShoes(
                0.50f,
                0.88f,
                0.30f,
                0.13f,
                IosThemeColors.skinFair,
                IosThemeColors.crystalBlue,
                w,
                h
            )
        }
    }
}

package com.princesspuzzle.storybook.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawButterfly
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawCrown
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawDaisy
import com.princesspuzzle.storybook.art.IosVectorPrimitives.drawDragonWing
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
 * Vector part renderers for Levels 1-8 (Starter & Princess tiers).
 */
internal object IosScenesGroupA {
    fun DrawScope.drawGroupAPart(theme: SceneThemeId, partIndex: Int, w: Float, h: Float) {
        when (theme) {
            SceneThemeId.PINK_CASTLE -> drawPinkCastlePart(partIndex, w, h)
            SceneThemeId.BRUNETTE_PRINCESS -> drawBrunettePrincessPart(partIndex, w, h)
            SceneThemeId.ROYAL_PONY -> drawRoyalPonyPart(partIndex, w, h)
            SceneThemeId.CHERRY_ICECREAM -> drawCherryIceCreamPart(partIndex, w, h)
            SceneThemeId.BLONDE_PRINCESS -> drawBlondePrincessPart(partIndex, w, h)
            SceneThemeId.FLOWER_PRINCESS -> drawFlowerPrincessPart(partIndex, w, h)
            SceneThemeId.ROYAL_KITTY -> drawRoyalKittyPart(partIndex, w, h)
            SceneThemeId.FRIENDLY_DRAGON -> drawFriendlyDragonPart(partIndex, w, h)
            else -> Unit
        }
    }

    private fun DrawScope.drawPinkCastlePart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> {
                drawFivePointStar(0.28f, 0.14f, 0.068f, 0.036f, IosThemeColors.goldLight, w, h)
                drawButterfly(0.68f, 0.14f, 0.15f, 0.13f, IosThemeColors.castlePink, IosThemeColors.goldLight, w, h)
            }
            1 -> {
                drawSpireAndFlag(0.50f, 0.30f, 0.24f, 0.22f, IosThemeColors.magenta, IosThemeColors.goldLight, w, h)
                outlinedRoundRect(0.50f, 0.44f, 0.22f, 0.11f, 0.12f, IosThemeColors.castlePinkLight, w, h)
                outlinedOval(0.50f, 0.44f, 0.07f, 0.07f, IosThemeColors.skyTop, w, h)
            }
            2, 3 -> {
                val tx = if (partIndex == 2) 0.25f else 0.75f
                drawSpireAndFlag(tx, 0.40f, 0.18f, 0.17f, IosThemeColors.crimson, IosThemeColors.goldLight, w, h)
                outlinedRoundRect(tx, 0.63f, 0.18f, 0.34f, 0.12f, IosThemeColors.castlePink, w, h)
                outlinedRoundRect(tx, 0.55f, 0.07f, 0.10f, 0.4f, IosThemeColors.skyTop, w, h)
                outlinedRoundRect(tx, 0.78f, 0.21f, 0.05f, 0.25f, IosThemeColors.castlePinkLight, w, h)
            }
            4 -> {
                outlinedRoundRect(0.50f, 0.68f, 0.38f, 0.34f, 0.08f, IosThemeColors.castlePinkLight, w, h)
                listOf(0.36f, 0.50f, 0.64f).forEach { bx ->
                    outlinedRoundRect(bx, 0.51f, 0.08f, 0.05f, 0.15f, IosThemeColors.castlePink, w, h)
                }
                outlinedRoundRect(0.50f, 0.77f, 0.19f, 0.22f, 0.42f, IosThemeColors.woodWarm, w, h)
                drawHeart(0.50f, 0.75f, 0.09f, 0.08f, IosThemeColors.goldLight, w, h)
            }
        }
    }

    private fun DrawScope.drawBrunettePrincessPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> {
                drawCrown(0.50f, 0.12f, 0.22f, 0.12f, IosThemeColors.goldLight, IosThemeColors.crimson, w, h)
                drawDaisy(0.61f, 0.15f, 0.032f, IosThemeColors.castlePink, IosThemeColors.goldRich, w, h)
            }
            1 -> {
                outlinedOval(0.48f, 0.28f, 0.38f, 0.27f, IosThemeColors.hairChestnut, w, h)
                drawPrincessFace(0.48f, 0.28f, 0.24f, 0.21f, IosThemeColors.skinFair, IosThemeColors.hairChestnut, w, h)
                outlinedOval(0.42f, 0.19f, 0.15f, 0.06f, IosThemeColors.hairChestnut, w, h)
                outlinedOval(0.54f, 0.19f, 0.15f, 0.06f, IosThemeColors.hairChestnut, w, h)
            }
            2 -> drawPrincessArms(0.48f, 0.46f, 0.64f, 0.16f, IosThemeColors.skinFair, IosThemeColors.castlePink, w, h)
            3 -> {
                outlinedRoundRect(0.46f, 0.44f, 0.22f, 0.14f, 0.28f, IosThemeColors.castlePink, w, h)
                val skirt = Path().apply {
                    moveTo(0.36f * w, 0.49f * h)
                    lineTo(0.56f * w, 0.49f * h)
                    lineTo(0.65f * w, 0.70f * h)
                    quadraticTo(0.46f * w, 0.73f * h, 0.27f * w, 0.70f * h)
                    close()
                }
                outlinedPath(skirt, IosThemeColors.castlePinkLight, w, h)
                drawCrown(0.46f, 0.60f, 0.11f, 0.06f, IosThemeColors.goldLight, IosThemeColors.magenta, w, h)
            }
            4 -> {
                drawLine(
                    IosThemeColors.goldRich,
                    Offset(0.76f * w, 0.43f * h),
                    Offset(0.80f * w, 0.50f * h),
                    strokeWidth(w, h) * 1.3f,
                    StrokeCap.Round
                )
                drawLine(
                    IosThemeColors.royalPurple,
                    Offset(0.83f * w, 0.44f * h),
                    Offset(0.80f * w, 0.50f * h),
                    strokeWidth(w, h) * 1.3f,
                    StrokeCap.Round
                )
                outlinedRoundRect(0.79f, 0.56f, 0.17f, 0.15f, 0.22f, IosThemeColors.magenta, w, h)
                drawHeart(0.79f, 0.56f, 0.06f, 0.05f, IosThemeColors.goldLight, w, h)
            }
            5 -> drawPrincessShoes(0.48f, 0.83f, 0.34f, 0.24f, IosThemeColors.skinFair, IosThemeColors.crimson, w, h)
        }
    }

    private fun DrawScope.drawRoyalPonyPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> {
                outlinedOval(0.64f, 0.16f, 0.18f, 0.14f, IosThemeColors.magenta, w, h)
                drawDaisy(0.62f, 0.15f, 0.032f, Color.White, IosThemeColors.goldRich, w, h)
                outlinedOval(0.71f, 0.26f, 0.28f, 0.21f, IosThemeColors.creamPearl, w, h)
                outlinedOval(0.82f, 0.28f, 0.13f, 0.11f, IosThemeColors.castlePinkLight, w, h)
                drawCircle(
                    IosThemeColors.outlineInk,
                    radius = strokeWidth(w, h) * 1.8f,
                    center = Offset(0.72f * w, 0.24f * h)
                )
                drawCircle(Color.White, radius = strokeWidth(w, h) * 0.7f, center = Offset(0.715f * w, 0.232f * h))
            }
            1 -> {
                outlinedRoundRect(0.60f, 0.63f, 0.09f, 0.30f, 0.4f, IosThemeColors.creamPearl, w, h)
                outlinedRoundRect(0.71f, 0.64f, 0.09f, 0.30f, 0.4f, IosThemeColors.creamPearl, w, h)
                outlinedRoundRect(0.60f, 0.76f, 0.10f, 0.06f, 0.3f, IosThemeColors.goldRich, w, h)
                outlinedRoundRect(0.71f, 0.77f, 0.10f, 0.06f, 0.3f, IosThemeColors.goldRich, w, h)
                outlinedOval(0.64f, 0.44f, 0.21f, 0.17f, IosThemeColors.creamPearl, w, h)
            }
            2 -> {
                outlinedOval(0.49f, 0.42f, 0.20f, 0.13f, IosThemeColors.turquoise, w, h)
                outlinedRoundRect(0.49f, 0.51f, 0.06f, 0.16f, 0.3f, IosThemeColors.goldRich, w, h)
                outlinedOval(0.49f, 0.58f, 0.09f, 0.07f, IosThemeColors.turquoiseDeep, w, h)
            }
            3 -> {
                outlinedOval(0.38f, 0.44f, 0.27f, 0.21f, IosThemeColors.creamPearl, w, h)
                drawFivePointStar(0.33f, 0.45f, 0.032f, 0.014f, IosThemeColors.castlePink, w, h)
            }
            4 -> {
                outlinedRoundRect(0.30f, 0.68f, 0.09f, 0.30f, 0.4f, IosThemeColors.castlePinkLight, w, h)
                outlinedRoundRect(0.41f, 0.69f, 0.09f, 0.30f, 0.4f, IosThemeColors.castlePinkLight, w, h)
                outlinedRoundRect(0.30f, 0.82f, 0.10f, 0.06f, 0.3f, IosThemeColors.goldRich, w, h)
                outlinedRoundRect(0.41f, 0.83f, 0.10f, 0.06f, 0.3f, IosThemeColors.goldRich, w, h)
            }
            5 -> {
                outlinedOval(0.16f, 0.50f, 0.16f, 0.24f, IosThemeColors.magenta, w, h)
                outlinedOval(0.12f, 0.61f, 0.14f, 0.16f, IosThemeColors.castlePink, w, h)
            }
        }
    }

    private fun DrawScope.drawCherryIceCreamPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> {
                drawLine(
                    IosThemeColors.emeraldTree,
                    Offset(0.49f * w, 0.20f * h),
                    Offset(0.57f * w, 0.08f * h),
                    strokeWidth(w, h),
                    StrokeCap.Round
                )
                drawLine(
                    IosThemeColors.emeraldTree,
                    Offset(0.64f * w, 0.20f * h),
                    Offset(0.57f * w, 0.08f * h),
                    strokeWidth(w, h),
                    StrokeCap.Round
                )
                outlinedOval(0.49f, 0.22f, 0.11f, 0.10f, IosThemeColors.crimson, w, h)
                outlinedOval(0.64f, 0.22f, 0.11f, 0.10f, IosThemeColors.crimson, w, h)
            }
            1 -> {
                outlinedOval(0.50f, 0.36f, 0.34f, 0.22f, IosThemeColors.castlePink, w, h)
                outlinedOval(0.50f, 0.26f, 0.26f, 0.17f, IosThemeColors.creamPearl, w, h)
                drawHeart(0.50f, 0.34f, 0.07f, 0.06f, IosThemeColors.crimson, w, h)
            }
            2 -> outlinedOval(0.27f, 0.34f, 0.19f, 0.13f, IosThemeColors.mintLeaf, w, h)
            3 -> outlinedOval(0.73f, 0.36f, 0.19f, 0.13f, IosThemeColors.mintLeaf, w, h)
            4 -> {
                val upper = Path().apply {
                    moveTo(0.36f * w, 0.47f * h)
                    lineTo(0.60f * w, 0.47f * h)
                    lineTo(0.54f * w, 0.69f * h)
                    lineTo(0.41f * w, 0.69f * h)
                    close()
                }
                outlinedPath(upper, IosThemeColors.waffleGold, w, h)
            }
            5 -> {
                val lower = Path().apply {
                    moveTo(0.41f * w, 0.68f * h)
                    lineTo(0.53f * w, 0.68f * h)
                    lineTo(0.47f * w, 0.91f * h)
                    close()
                }
                outlinedPath(lower, IosThemeColors.waffleGold, w, h)
            }
        }
    }

    private fun DrawScope.drawBlondePrincessPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> drawCrown(0.52f, 0.11f, 0.22f, 0.11f, IosThemeColors.castlePink, IosThemeColors.crystalBlue, w, h)
            1 -> {
                drawWavyHairBack(0.50f, 0.26f, 0.36f, 0.23f, IosThemeColors.hairBlonde, w, h)
                drawPrincessFace(0.50f, 0.26f, 0.23f, 0.19f, IosThemeColors.skinFair, IosThemeColors.crystalBlue, w, h)
                drawHairBangs(0.50f, 0.26f, 0.23f, 0.19f, IosThemeColors.hairBlonde, w, h)
            }
            2 -> {
                drawPrincessArms(
                    0.50f,
                    0.43f,
                    0.64f,
                    0.16f,
                    IosThemeColors.skinFair,
                    IosThemeColors.castlePinkLight,
                    w,
                    h
                )
                outlinedRoundRect(0.50f, 0.43f, 0.22f, 0.14f, 0.25f, IosThemeColors.magenta, w, h)
            }
            3 -> drawDaisy(0.52f, 0.49f, 0.048f, IosThemeColors.turquoise, IosThemeColors.goldLight, w, h)
            4 -> drawPrincessGown(
                0.49f,
                0.60f,
                0.24f,
                0.50f,
                0.20f,
                IosThemeColors.magenta,
                IosThemeColors.castlePinkLight,
                w,
                h
            )
            5 -> drawPrincessGown(
                0.49f,
                0.69f,
                0.34f,
                0.56f,
                0.16f,
                IosThemeColors.castlePinkLight,
                Color.White,
                w,
                h
            )
            6 -> drawPrincessShoes(0.51f, 0.84f, 0.34f, 0.21f, IosThemeColors.skinFair, IosThemeColors.turquoise, w, h)
        }
    }

    private fun DrawScope.drawFlowerPrincessPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> drawCrown(0.47f, 0.11f, 0.22f, 0.10f, IosThemeColors.creamPearl, IosThemeColors.castlePink, w, h)
            1 -> drawWavyHairBack(0.47f, 0.26f, 0.42f, 0.27f, IosThemeColors.hairBlonde, w, h)
            2 -> {
                drawPrincessFace(0.47f, 0.25f, 0.22f, 0.19f, IosThemeColors.skinFair, IosThemeColors.emeraldTree, w, h)
                drawHairBangs(0.47f, 0.25f, 0.22f, 0.19f, IosThemeColors.hairBlonde, w, h)
            }
            3 -> {
                outlinedRoundRect(0.47f, 0.43f, 0.20f, 0.14f, 0.25f, IosThemeColors.castlePink, w, h)
                drawDaisy(0.47f, 0.47f, 0.035f, Color.White, IosThemeColors.goldRich, w, h)
            }
            4 -> outlinedOval(0.38f, 0.62f, 0.23f, 0.24f, IosThemeColors.magenta, w, h)
            5 -> outlinedOval(0.56f, 0.62f, 0.23f, 0.24f, IosThemeColors.castlePink, w, h)
            6 -> {
                outlinedOval(0.66f, 0.44f, 0.18f, 0.07f, IosThemeColors.skinFair, w, h)
                outlinedRoundRect(0.76f, 0.51f, 0.20f, 0.14f, 0.25f, IosThemeColors.waffleGold, w, h)
                drawDaisy(0.73f, 0.43f, 0.035f, Color.White, IosThemeColors.goldRich, w, h)
                drawDaisy(0.80f, 0.43f, 0.035f, IosThemeColors.castlePinkLight, IosThemeColors.goldRich, w, h)
            }
            7 -> {
                outlinedOval(0.28f, 0.68f, 0.12f, 0.18f, IosThemeColors.skinFair, w, h)
                drawPrincessShoes(0.48f, 0.82f, 0.34f, 0.20f, IosThemeColors.skinFair, IosThemeColors.magenta, w, h)
            }
        }
    }

    private fun DrawScope.drawRoyalKittyPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> drawCrown(0.50f, 0.15f, 0.23f, 0.12f, IosThemeColors.goldLight, IosThemeColors.magenta, w, h)
            1 -> {
                val ears = Path().apply {
                    moveTo(0.34f * w, 0.29f * h)
                    lineTo(0.36f * w, 0.19f * h)
                    lineTo(0.45f * w, 0.25f * h)
                    close()
                }
                outlinedPath(ears, IosThemeColors.kittyWhite, w, h)
                val earR = Path().apply {
                    moveTo(0.66f * w, 0.29f * h)
                    lineTo(0.64f * w, 0.19f * h)
                    lineTo(0.55f * w, 0.25f * h)
                    close()
                }
                outlinedPath(earR, IosThemeColors.kittyWhite, w, h)
                drawPrincessFace(0.50f, 0.34f, 0.34f, 0.22f, IosThemeColors.kittyWhite, IosThemeColors.outlineInk, w, h)
                outlinedOval(0.50f, 0.35f, 0.05f, 0.035f, IosThemeColors.goldRich, w, h)
                listOf(-1f, 1f).forEach { dir ->
                    drawLine(
                        IosThemeColors.outlineInk,
                        Offset((0.50f + dir * 0.13f) * w, 0.34f * h),
                        Offset((0.50f + dir * 0.21f) * w, 0.32f * h),
                        strokeWidth(w, h) * 0.8f
                    )
                    drawLine(
                        IosThemeColors.outlineInk,
                        Offset((0.50f + dir * 0.13f) * w, 0.36f * h),
                        Offset((0.50f + dir * 0.21f) * w, 0.37f * h),
                        strokeWidth(w, h) * 0.8f
                    )
                }
            }
            2 -> {
                outlinedRoundRect(0.50f, 0.59f, 0.34f, 0.28f, 0.34f, IosThemeColors.castlePinkLight, w, h)
                drawDaisy(0.50f, 0.56f, 0.045f, Color.White, IosThemeColors.goldRich, w, h)
            }
            3 -> {
                outlinedRoundRect(0.77f, 0.57f, 0.16f, 0.15f, 0.28f, IosThemeColors.magenta, w, h)
                drawHeart(0.77f, 0.57f, 0.06f, 0.05f, IosThemeColors.creamPearl, w, h)
            }
            4 -> {
                outlinedOval(0.28f, 0.78f, 0.14f, 0.12f, IosThemeColors.kittyWhite, w, h)
                outlinedOval(0.44f, 0.84f, 0.11f, 0.09f, IosThemeColors.kittyWhite, w, h)
                outlinedOval(0.58f, 0.84f, 0.11f, 0.09f, IosThemeColors.kittyWhite, w, h)
            }
        }
    }

    private fun DrawScope.drawFriendlyDragonPart(partIndex: Int, w: Float, h: Float) {
        when (partIndex) {
            0 -> {
                outlinedRoundRect(0.32f, 0.17f, 0.06f, 0.07f, 0.3f, IosThemeColors.creamPearl, w, h)
                drawPrincessFace(
                    0.27f,
                    0.27f,
                    0.28f,
                    0.20f,
                    IosThemeColors.dragonAmber,
                    IosThemeColors.emeraldTree,
                    w,
                    h
                )
            }
            1 -> drawDragonWing(0.36f, 0.40f, 0.28f, 0.21f, pointRight = false, IosThemeColors.dragonOrange, w, h)
            2 -> drawDragonWing(0.68f, 0.34f, 0.30f, 0.23f, pointRight = true, IosThemeColors.dragonOrange, w, h)
            3 -> {
                outlinedOval(0.48f, 0.52f, 0.32f, 0.28f, IosThemeColors.dragonAmber, w, h)
                outlinedOval(0.48f, 0.55f, 0.18f, 0.19f, IosThemeColors.goldLight, w, h)
            }
            4 -> {
                val tail = Path().apply {
                    moveTo(0.62f * w, 0.58f * h)
                    cubicTo(0.84f * w, 0.50f * h, 0.90f * w, 0.70f * h, 0.76f * w, 0.72f * h)
                    lineTo(0.86f * w, 0.64f * h)
                    close()
                }
                outlinedPath(tail, IosThemeColors.dragonOrange, w, h)
            }
            5 -> {
                outlinedOval(0.30f, 0.74f, 0.12f, 0.14f, IosThemeColors.dragonAmber, w, h)
                outlinedOval(0.40f, 0.76f, 0.10f, 0.12f, IosThemeColors.goldRich, w, h)
            }
            6 -> {
                outlinedOval(0.54f, 0.78f, 0.13f, 0.16f, IosThemeColors.dragonAmber, w, h)
                outlinedOval(0.64f, 0.80f, 0.11f, 0.14f, IosThemeColors.goldRich, w, h)
            }
        }
    }
}

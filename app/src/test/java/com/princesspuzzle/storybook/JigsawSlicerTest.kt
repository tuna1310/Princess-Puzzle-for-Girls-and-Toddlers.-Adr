package com.princesspuzzle.storybook

import com.princesspuzzle.storybook.puzzle.CurveCommand
import com.princesspuzzle.storybook.puzzle.sliceJigsawPieces
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class JigsawSlicerTest {
    private fun sampleCubic(x0: Float, y0: Float, cmd: CurveCommand.Cubic, steps: Int = 14): List<Pair<Float, Float>> {
        val pts = ArrayList<Pair<Float, Float>>(steps)
        for (i in 1..steps) {
            val t = i.toFloat() / steps
            val mt = 1f - t
            val x =
                mt * mt * mt * x0 +
                    3f * mt * mt * t * cmd.cp1x +
                    3f * mt * t * t * cmd.cp2x +
                    t * t * t * cmd.x
            val y =
                mt * mt * mt * y0 +
                    3f * mt * mt * t * cmd.cp1y +
                    3f * mt * t * t * cmd.cp2y +
                    t * t * t * cmd.y
            pts.add(x to y)
        }
        return pts
    }

    private fun approximateArea(commands: List<CurveCommand>): Float {
        val poly = ArrayList<Pair<Float, Float>>()
        var cx = 0f
        var cy = 0f
        for (cmd in commands) {
            when (cmd) {
                is CurveCommand.Move -> {
                    cx = cmd.x
                    cy = cmd.y
                    poly.add(cx to cy)
                }
                is CurveCommand.Line -> {
                    cx = cmd.x
                    cy = cmd.y
                    poly.add(cx to cy)
                }
                is CurveCommand.Cubic -> {
                    poly.addAll(sampleCubic(cx, cy, cmd))
                    cx = cmd.x
                    cy = cmd.y
                }
            }
        }
        var sum = 0f
        for (i in poly.indices) {
            val a = poly[i]
            val b = poly[(i + 1) % poly.size]
            sum += a.first * b.second - b.first * a.second
        }
        return abs(sum) * 0.5f
    }

    @Test
    fun `slices watertight pieces covering unit board area for counts 2 through 12`() {
        for (count in 2..12) {
            val pieces = sliceJigsawPieces(count, seed = count * 13)
            assertEquals(count, pieces.size, "Expected $count pieces")

            var totalArea = 0f
            for (piece in pieces) {
                assertTrue(piece.commands.first() is CurveCommand.Move)
                assertTrue(piece.bounds.minX >= -0.2f)
                assertTrue(piece.bounds.maxX <= 1.2f)
                assertTrue(piece.bounds.minY >= -0.2f)
                assertTrue(piece.bounds.maxY <= 1.2f)
                totalArea += approximateArea(piece.commands)
            }
            assertEquals(1.0f, totalArea, 0.015f, "Area sum mismatch for $count pieces")
        }
    }
}
